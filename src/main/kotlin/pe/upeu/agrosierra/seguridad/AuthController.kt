package pe.upeu.agrosierra.seguridad
import pe.upeu.agrosierra.config.JwtService
import pe.upeu.agrosierra.web.dto.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
@RestController
@RequestMapping("/api/v1")
class AuthController(private val usuarios: UsuarioRepository,private val encoder: PasswordEncoder,private val jwt: JwtService) {
 @PostMapping("/auth/login") fun login(@Valid @RequestBody p: LoginPeticion): Map<String,Any> {
  val u=usuarios.porNombre(p.usuario)
  if(u==null || !encoder.matches(p.contrasena,u.passwordHash)) throw Fallo(401,"Usuario o contraseÃ±a incorrectos")
  return datos(u)+mapOf("token" to jwt.firmar(u),"expiraEn" to 3600)
 }
 @GetMapping("/usuarios/me") fun actual(a: Authentication) = datos(a.principal as Usuario)
 private fun datos(u: Usuario): Map<String,Any> = mapOf("id" to u.id,"usuario" to u.usuario,"nombre" to u.nombre,"rol" to u.rol,"privilegios" to u.privilegios,"edicionGlobal" to u.edicionGlobal)
}
