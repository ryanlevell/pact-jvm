package au.com.dius.pact.core.support

object ContentTypeUtils {

  val XMLREGEXP = """^\s*<\?xml\s*version.*""".toRegex()
  val HTMLREGEXP = """^\s*(<!DOCTYPE)|(<HTML>).*""".toRegex()
  val JSONREGEXP = """^\s*(true|false|null|[0-9]+|"\w*|\{\s*(}|"\w+)|\[\s*).*""".toRegex()
  val XMLREGEXP2 = """^\s*<\w+\s*(:\w+=[\"”][^\"”]+[\"”])?.*""".toRegex()

  fun detectContentType(s: String): String {
    return when {
      XMLREGEXP.find(s) != null -> "application/xml"
      HTMLREGEXP.find(s.toUpperCase()) != null -> "text/html"
      JSONREGEXP.find(s) != null -> "application/json"
      XMLREGEXP2.find(s) != null -> "application/xml"
      else -> "text/plain"
    }
  }

}
