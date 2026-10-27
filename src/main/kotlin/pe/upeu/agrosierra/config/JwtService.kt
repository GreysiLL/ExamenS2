package pe.upeu.agrosierra.config
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.context.annotation.DependsOn
import pe.upeu.agrosierra.seguridad.Usuario
import java.util.*
@Service
@DependsOn("dataSourceScriptDatabaseInitializer")
class JwtService(db: JdbcTemplate) {
 private val key = run {
  val guardada=db.queryForList("SELECT valor FROM configuracion WHERE clave='jwt'",String::class.java).firstOrNull()
  val bytes=guardada?.let { Base64.getDecoder().decode(it) } ?: ByteArray(64).also { java.security.SecureRandom().nextBytes(it); db.update("INSERT INTO configuracion VALUES('jwt',?)",Base64.getEncoder().encodeToString(it)) }
  Keys.hmacShaKeyFor(bytes)
 }
 fun firmar(u: Usuario): String = Jwts.builder().subject(u.id.toString()).claim("usuario",u.usuario).claim("privilegios",u.privilegios).issuedAt(Date()).expiration(Date(System.currentTimeMillis()+3_600_000)).signWith(key).compact()
 fun usuarioId(token: String): Long = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject.toLong()
}
