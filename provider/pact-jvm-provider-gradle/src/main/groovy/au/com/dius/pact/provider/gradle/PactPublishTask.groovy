package au.com.dius.pact.provider.gradle

import au.com.dius.pact.com.github.michaelbull.result.Ok
import au.com.dius.pact.core.pactbroker.PactBrokerClient
import groovy.io.FileType
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.fusesource.jansi.AnsiConsole
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

/**
 * Task to push pact files to a pact broker
 */
@SuppressWarnings('Println')
class PactPublishTask extends DefaultTask {

    @TaskAction
    void publishPacts() {
        AnsiConsole.systemInstall()
        if (!project.pact.publish) {
            throw new GradleScriptException('You must add a pact publish configuration to your build before you can ' +
                'use the pactPublish task', null)
        }

        PactPublish pactPublish = project.pact.publish
        if (pactPublish.pactDirectory == null) {
            pactPublish.pactDirectory = project.file("${project.buildDir}/pacts")
        }
        if (pactPublish.version == null) {
            pactPublish.version = project.version
        }

        def options = [:]
        if (StringUtils.isNotEmpty(pactPublish.pactBrokerToken)) {
            options.authentication = [pactPublish.pactBrokerAuthenticationScheme ?: 'bearer',
                                      pactPublish.pactBrokerToken]
        }
        else if (StringUtils.isNotEmpty(pactPublish.pactBrokerUsername)) {
          options.authentication = [pactPublish.pactBrokerAuthenticationScheme ?: 'basic',
                                    pactPublish.pactBrokerUsername, pactPublish.pactBrokerPassword]
        }
        def brokerClient = new PactBrokerClient(pactPublish.pactBrokerUrl, options)
        File pactDirectory = pactPublish.pactDirectory as File
        boolean anyFailed = false
        pactDirectory.eachFileMatch(FileType.FILES, Pattern.compile(pactPublish.include)) { pactFile ->
          if (pactFileIsExcluded(pactPublish, pactFile)) {
            println("Not publishing '${pactFile.name}' as it matches an item in the excluded list")
          } else {
            def result
            if (pactPublish.tags) {
              print "Publishing '${pactFile.name}' with tags ${pactPublish.tags.join(', ')} ... "
            } else {
              print "Publishing '${pactFile.name}' ... "
            }
            result = brokerClient.uploadContract(pactFile, pactPublish.version, pactPublish.tags)
            if (result instanceof Ok) {
              println result.value
              if (!anyFailed && result.value.startsWith('FAILED!')) {
                anyFailed = true
              }
            } else {
              println result.error
              anyFailed = true
            }
          }
        }

        AnsiConsole.systemUninstall()

        if (anyFailed) {
          throw new GradleScriptException('One or more of the pact files were rejected by the pact broker', null)
        }
    }

  static boolean pactFileIsExcluded(PactPublish pactPublish, File pactFile) {
    pactPublish.excludes.any {
      FilenameUtils.getBaseName(pactFile.name) ==~ it
    }
  }
}
