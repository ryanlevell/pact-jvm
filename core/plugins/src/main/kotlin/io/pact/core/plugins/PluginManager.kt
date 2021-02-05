package io.pact.core.plugins

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.vdurmont.semver4j.Semver
import io.pact.core.support.Json
import io.pact.core.support.json.JsonParser
import io.pact.core.support.json.JsonValue
import mu.KLogging
import org.apache.commons.lang3.SystemUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

interface PactPluginManifest {
  val pluginDir: File
  val pluginInterfaceVersion: Int
  val name: String
  val version: String
  val executableType: String
  val minimumRequiredVersion: String?
  val entryPoint: String
  val dependencies: List<String>
}

data class DefaultPactPluginManifest(
  override val pluginDir: File,
  override val pluginInterfaceVersion: Int,
  override val name: String,
  override val version: String,
  override val executableType: String,
  override val minimumRequiredVersion: String?,
  override val entryPoint: String,
  override val dependencies: List<String>
): PactPluginManifest {
  companion object {
    fun fromJson(pluginDir: File, pluginJson: JsonValue.Object): PactPluginManifest {
      return DefaultPactPluginManifest(
        pluginDir,
        Json.toInteger(pluginJson["pluginInterfaceVersion"]) ?: 1,
        Json.toString(pluginJson["name"]),
        Json.toString(pluginJson["version"]),
        Json.toString(pluginJson["executableType"]),
        Json.toString(pluginJson["minimumRequiredVersion"]),
        Json.toString(pluginJson["entryPoint"]),
        listOf()
      )
    }
  }
}

interface PactPlugin {
  val port: Int?
  val serverKey: String?
  val processPid: Long?
}

data class DefaultPactPlugin(
  val cp: ChildProcess,
  override val port: Int?,
  override val serverKey: String
) : PactPlugin {
  override val processPid: Long
    get() = cp.pid
}

interface PluginManager {
  /**
   * Loads the plugin by name
   */
  fun loadPlugin(name: String): Result<PactPlugin, String>
}

object DefaultPluginManager: KLogging(), PluginManager {
  private val PLUGIN_MANIFEST_REGISTER: MutableMap<String, PactPluginManifest> = mutableMapOf()
  private val PLUGIN_REGISTER: MutableMap<String, PactPlugin> = mutableMapOf()

  override fun loadPlugin(name: String): Result<PactPlugin, String> {
    return if (PLUGIN_REGISTER.containsKey(name)) {
      Ok(PLUGIN_REGISTER[name]!!)
    } else {
      when (val manifest = loadPluginManifest(name)) {
        is Ok -> initialisePlugin(manifest.value)
        is Err -> Err(manifest.error)
      }
    }
  }

  private fun initialisePlugin(manifest: PactPluginManifest): Result<PactPlugin, String> {
    val result = when (manifest.executableType) {
      "exec" -> startPluginProcess(manifest)
      "ruby" -> loadRubyPlugin(manifest)
      else -> Err("Plugin executable type of ${manifest.executableType} is not supported")
    }
    return when (result) {
      is Ok -> {
        PLUGIN_REGISTER[manifest.name] = result.value
        logger.debug { "Plugin process started OK (port = ${result.value.port}), sending init message" }
        TODO()
      }
      is Err -> Err(result.error)
    }
  }

  private fun loadRubyPlugin(manifest: PactPluginManifest): Result<PactPlugin, String> {
    val rvm = lookForProgramInPath("rvm")
    return if (rvm is Ok && manifest.minimumRequiredVersion != null) {
      logger.debug { "Found RVM at ${rvm.value}" }
      startPluginProcess(manifest, mapOf(), rvm.value.toString(), manifest.minimumRequiredVersion.toString(), "do")
    } else {
      when (val ruby = lookForProgramInPath("ruby")) {
        is Ok -> {
          logger.debug { "Found Ruby interpreter at ${ruby.value}" }
          val versionCheck = checkRubyVersion(manifest, ruby)
          if (versionCheck is Err) {
            Err(versionCheck.error)
          } else {
            val parent = ruby.value.parent
            //          when (val bundler = lookForProgramInPath("bundle")) {
            //            is Ok -> startPluginProcess(manifest,
            //              mapOf("BUNDLE_GEMFILE" to manifest.pluginDir.resolve("Gemfile").toString()),
            //              bundler.value.toString(), "exec", ruby.value.toString(), "-C${manifest.pluginDir}")
            //            is Err -> {
            //              logger.debug { "Bundler not found in path - ${bundler.error}" }
            //              val bundlePath = parent.resolve("bundle")
            //              if (bundlePath.toFile().exists()) {
            //                startPluginProcess(manifest,
            //                  mapOf("BUNDLE_GEMFILE" to manifest.pluginDir.resolve("Gemfile").toString()),
            //                  bundlePath.toString(), "exec", ruby.value.toString(), "-C${manifest.pluginDir}")
            //              } else {
            //                startPluginProcess(manifest, mapOf(), ruby.value.toString(), "-C${manifest.pluginDir}")
            //              }
            //            }
            //          }
            startPluginProcess(manifest,
              mapOf("BUNDLE_GEMFILE" to manifest.pluginDir.resolve("Gemfile").toString()),
              ruby.value.toString(), "-C${manifest.pluginDir}")
          }
        }
        is Err -> Err(ruby.error)
      }
    }
  }

  private fun startPluginProcess(
    manifest: PactPluginManifest,
    env: Map<String, String> = mapOf(),
    vararg command: String
  ): Result<PactPlugin, String> {
    logger.debug { "Starting plugin with manifest $manifest" }
    val pb = if (command.isNotEmpty()) {
      ProcessBuilder(command.asList() + manifest.pluginDir.resolve(manifest.entryPoint).toString())
    } else {
      ProcessBuilder(manifest.pluginDir.resolve(manifest.entryPoint).toString())
    }
      .directory(manifest.pluginDir)

    env.forEach { (k, v) -> pb.environment()[k] = v }

    val cp = ChildProcess(pb, manifest)
    return try {
      logger.debug { "Starting plugin ${manifest.name} process ${pb.command()}" }
      cp.start()
      logger.debug { "Plugin ${manifest.name} started with PID ${cp.pid}" }
      val startupInfo = cp.channel.poll(1000, TimeUnit.MILLISECONDS)
      if (startupInfo is JsonValue.Object) {
        Ok(DefaultPactPlugin(cp, Json.toInteger(startupInfo["port"]), Json.toString(startupInfo["serverKey"])))
      } else {
        cp.destroy()
        Err("Plugin process did not output the correct startup message - got $startupInfo")
      }
    } catch (e: Exception) {
      logger.error(e) { "Plugin process did not start correctly" }
      cp.destroy()
      Err("Plugin process did not start correctly - ${e.message}")
    }
  }

  private fun checkRubyVersion(manifest: PactPluginManifest, ruby: Ok<Path>) =
    if (manifest.minimumRequiredVersion != null) {
      logger.debug { "Checking if Ruby version meets minimum version of ${manifest.minimumRequiredVersion}" }
      when (val rubyOut = SystemExec.execute(ruby.value.toString(), "--version")) {
        is Ok -> {
          logger.debug { "Got Ruby version: ${rubyOut.value}" }
          val rubyVersionStr = rubyOut.value.split(Regex("\\s+"))
          if (rubyVersionStr.size > 1) {
            val rubyVersion = Semver(rubyVersionStr[1].replace(Regex("(p\\d+)"), "+$1"), Semver.SemverType.NPM)
            if (rubyVersion.isLowerThan(manifest.minimumRequiredVersion)) {
              Err("Ruby version $rubyVersion does not meet the minimum version of ${manifest.minimumRequiredVersion}")
            } else {
              Ok("")
            }
          } else {
            Err("Unrecognised ruby version format: ${rubyOut.value}")
          }
        }
        is Err -> Err("Could not execute Ruby interpreter - ${rubyOut.error}")
      }
    } else {
      Ok("")
    }

  private fun loadPluginManifest(name: String): Result<PactPluginManifest, String> {
    return if (PLUGIN_MANIFEST_REGISTER.containsKey(name)) {
      Ok(PLUGIN_MANIFEST_REGISTER[name]!!)
    } else {
      val pluginDir = System.getenv("PACT_PLUGIN_DIR") ?: System.getenv("HOME") + "/.pact/plugins"
      for (file in File(pluginDir).walk()) {
        if (file.isFile && file.name == "pact-plugin.json") {
          logger.debug { "Found plugin manifest: $file" }
          val pluginJson = file.bufferedReader().use { JsonParser.parseReader(it) }
          if (pluginJson.isObject) {
            val plugin = DefaultPactPluginManifest.fromJson(file.parentFile, pluginJson.asObject()!!)
            if (plugin.name == name) {
              PLUGIN_MANIFEST_REGISTER[name] = plugin
              return Ok(plugin)
            }
          }
        }
      }
      Err("No plugin with name '$name' was found in the Pact plugin directory '$pluginDir'")
    }
  }

  private fun lookForProgramInPath(desiredProgram: String): Result<Path, String> {
    val pb = ProcessBuilder(if (SystemUtils.IS_OS_WINDOWS) "where" else "which", desiredProgram)
    return try {
      val proc = pb.start()
      val errCode = proc.waitFor()
      if (errCode == 0) {
        BufferedReader(InputStreamReader(proc.inputStream)).use { reader ->
          Ok(Paths.get(reader.readLine()))
        }
      } else {
        Err("$desiredProgram not found in in PATH")
      }
    } catch (ex: IOException) {
      logger.error(ex) { "Something went wrong while searching for $desiredProgram - ${ex.message}" }
      Err("Something went wrong while searching for $desiredProgram - ${ex.message}")
    } catch (ex: InterruptedException) {
      logger.error(ex) { "Something went wrong while searching for $desiredProgram - ${ex.message}" }
      Err("Something went wrong while searching for $desiredProgram - ${ex.message}")
    }
  }
}
