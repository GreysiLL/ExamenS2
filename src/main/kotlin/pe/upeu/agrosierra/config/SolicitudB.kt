package pe.upeu.agrosierra.config
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.support.TransactionTemplate
@Component
@Order(20)
class SolicitudB(private val db: JdbcTemplate,private val encoder: PasswordEncoder,private val tx: TransactionTemplate): CommandLineRunner {
 override fun run(vararg args: String) { tx.executeWithoutResult {
  val rol="SUPERVISOR_CALIDAD"
  if(db.queryForObject("SELECT COUNT(*) FROM rol WHERE nombre=?",Long::class.java,rol)==0L) {
   db.update("INSERT INTO rol VALUES(?,TRUE)",rol)
   setOf("LOTE_LEER","LABOR_LEER","LABOR_EDITAR").forEach { db.update("INSERT INTO rol_privilegio VALUES(?,?)",rol,it) }
  }
  if(db.queryForObject("SELECT COUNT(*) FROM usuario WHERE usuario='calidad1'",Long::class.java)==0L)db.update("INSERT INTO usuario(usuario,nombre,password_hash,rol) VALUES(?,?,?,?)","calidad1","Supervisor de calidad",encoder.encode("Agro2026*"),rol)
 } }
}
