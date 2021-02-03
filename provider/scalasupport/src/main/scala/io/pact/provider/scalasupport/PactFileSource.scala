package io.pact.provider.scalasupport

import java.io.File
import io.pact.core.model.{DefaultPactReader, RequestResponsePact}
import org.apache.commons.io.FileUtils

object PactFileSource {
  def loadFiles(baseDir: File): Seq[RequestResponsePact] = {
    import scala.collection.JavaConverters._
    FileUtils.listFiles(baseDir, Array("json"), true).asScala
      .map(DefaultPactReader.INSTANCE.loadPact(_).asInstanceOf[RequestResponsePact])
      .toSeq
  }
}
