package pe.upeu.agrosierra.config
import pe.upeu.agrosierra.web.dto.*
import jakarta.servlet.http.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.slf4j.LoggerFactory
import tools.jackson.databind.ObjectMapper
@RestControllerAdvice
class ManejadorDeErrores {
 @ExceptionHandler(Fallo::class) fun fallo(e: Fallo, r: HttpServletRequest) = respuesta(e.codigo,e.message,r,e.campos)
 @ExceptionHandler(MethodArgumentNotValidException::class) fun validacion(e: MethodArgumentNotValidException,r: HttpServletRequest) = respuesta(400,"Datos invÃ¡lidos",r,e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Valor invÃ¡lido") })
 @ExceptionHandler(HttpMessageNotReadableException::class,MethodArgumentTypeMismatchException::class) fun formato(e: Exception,r: HttpServletRequest) = respuesta(400,"Formato de solicitud invÃ¡lido",r)
 @ExceptionHandler(Exception::class) fun inesperado(e: Exception,r: HttpServletRequest): ResponseEntity<ErrorRespuesta> { LoggerFactory.getLogger(javaClass).error("Fallo en {}",r.requestURI,e);return respuesta(500,"No se pudo completar la operaciÃ³n",r) }
 private fun respuesta(c: Int,m: String,r: HttpServletRequest,v: Map<String,String> = emptyMap()) = ResponseEntity.status(c).body(ErrorRespuesta(status=c,error=HttpStatus.valueOf(c).reasonPhrase,message=m,path=r.requestURI,validationErrors=v))
}
fun escribirError(mapper: ObjectMapper,r: HttpServletRequest,s: HttpServletResponse,c: Int,m: String) { s.status=c;s.contentType="application/json";s.characterEncoding="UTF-8";mapper.writeValue(s.writer,ErrorRespuesta(status=c,error=HttpStatus.valueOf(c).reasonPhrase,message=m,path=r.requestURI)) }
