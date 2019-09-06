package au.com.dius.pact.core.support

import spock.lang.Specification
import spock.lang.Unroll

class ContentTypeUtilsTest extends Specification {

  @SuppressWarnings('LineLength')
  @Unroll
  def 'detect content type'() {
    expect:
    ContentTypeUtils.INSTANCE.detectContentType(data) == type

    where:

    data                                                         | type
    ''                                                           | 'text/plain'
    '{"json": true}'                                             | 'application/json'
    '{}'                                                         | 'application/json'
    '[]'                                                         | 'application/json'
    '[1,2,3]'                                                    | 'application/json'
    '"string"'                                                   | 'application/json'
    '<?xml version="1.0" encoding="UTF-8"?>\n<json>false</json>' | 'application/xml'
    '<json>false</json>'                                         | 'application/xml'
    'this is not json'                                           | 'text/plain'
    '<html><body>this is also not json</body></html>'            | 'text/html'
    'openapi: 3.0.1\n'                                           | 'application/yaml'
  }

}
