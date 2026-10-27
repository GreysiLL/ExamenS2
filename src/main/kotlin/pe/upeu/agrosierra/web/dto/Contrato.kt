package pe.upeu.agrosierra.web.dto
import jakarta.validation.constraints.*
data class LoginPeticion(@field:NotBlank val usuario: String = "", @field:NotBlank val contrasena: String = "")
data class LaborPeticion(val loteId: Long = 0, val tipo: String = "", val fecha: String = "", val cantidad: Double = 0.0, val observacion: String = "",val cantidadJornales: Double = 0.0)
data class Pagina<T>(val contenido: List<T>, val pagina: Int, val tamanio: Int, val totalElementos: Long, val totalPaginas: Int, val ultima: Boolean)
data class ErrorRespuesta(val timestamp: String = java.time.LocalDateTime.now().toString(), val status: Int, val error: String, val message: String, val path: String, val validationErrors: Map<String,String> = emptyMap())
class Fallo(val codigo: Int, override val message: String, val campos: Map<String,String> = emptyMap()): RuntimeException(message)
