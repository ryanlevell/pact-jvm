package io.pact.provider.spring

import io.pact.provider.junit.RestPactRunner
import io.pact.provider.junitsupport.Provider
import io.pact.provider.junitsupport.loader.PactFolder
import io.pact.provider.junitsupport.target.TestTarget
import io.pact.provider.spring.target.MockMvcTarget
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@RunWith(RestPactRunner)
@Provider('Multipart-Service')
@PactFolder('pacts')
@SuppressWarnings(['JUnitPublicField', 'PublicInstanceField'])
class MultiPartSpec {

  @Controller
  static class FormController {
    @RequestMapping(value = '/api/form', method = RequestMethod.POST)
    ResponseEntity create(@RequestParam MultipartFile page001,
                          @RequestParam String entityId,
                          @RequestParam String entityType) throws Exception {
      if (entityId == '99199292' && entityType == 'TYPE' && page001.contentType == 'image/png') {
        new ResponseEntity(HttpStatus.CREATED)
      } else {
        new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
      }
    }
  }

  @TestTarget
  public final MockMvcTarget target = new MockMvcTarget()

  @Before
  void setup() {
    target.setControllers(new FormController())
  }
}
