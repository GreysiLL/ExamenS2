package pe.upeu.agrosierra.config
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.support.TransactionTemplate
import java.time.*
@Component
@Order(0)
class DatosSemilla(private val db: JdbcTemplate,private val encoder: PasswordEncoder,private val tx: TransactionTemplate): CommandLineRunner {
 override fun run(vararg args: String) { tx.executeWithoutResult {
  if(db.queryForObject("SELECT COUNT(*) FROM usuario",Long::class.java)!!>0)return@executeWithoutResult
  val lectura=setOf("LOTE_LEER","LABOR_LEER")
  val permisos=mapOf("TECNICO" to lectura+setOf("LABOR_CREAR","LABOR_EDITAR"),"JEFE_CAMPO" to lectura+setOf("LABOR_CREAR","LABOR_EDITAR","LABOR_ELIMINAR","CAMPANIA_CERRAR"),"ADMINISTRADOR" to lectura+"USUARIO_GESTIONAR")
  permisos.values.flatten().toSet().forEach { db.update("INSERT INTO privilegio VALUES(?)",it) }
  permisos.forEach { (rol,lista) -> db.update("INSERT INTO rol VALUES(?,?)",rol,rol=="JEFE_CAMPO");lista.forEach { db.update("INSERT INTO rol_privilegio VALUES(?,?)",rol,it) } }
  listOf(Triple("tecnico1","Técnico de campo","TECNICO"),Triple("jefe1","Jefe de campo","JEFE_CAMPO"),Triple("admin","Administrador","ADMINISTRADOR")).forEach { (u,n,r) -> db.update("INSERT INTO usuario(usuario,nombre,password_hash,rol) VALUES(?,?,?,?)",u,n,encoder.encode("Agro2026*"),r) }
  val valles=listOf("Mantaro","Sagrado","Tambo");val cultivos=listOf("Papa","Maíz","Quinua","Haba")
  val nombres=listOf("La Esperanza","Los Sauces","El Mirador","Pampa Verde","Las Lomas","Santa Rosa","El Molino","La Ribera")
  for(i in 1..8)db.update("INSERT INTO lote(codigo,nombre,hectareas,cultivo,valle,campania_cerrada,dosis_maxima) VALUES(?,?,?,?,?,?,?)","AS-${i.toString().padStart(3,'0')}",nombres[i-1],2.0+i/2.0,cultivos[(i-1)%4],valles[(i-1)%3],i>=7,2.0)
  for(i in 0 until 20) {
   val lote=i%8+1;val tipo=if(lote==1 && i==8)"COSECHA" else if(i%2==0)"RIEGO" else "FUMIGACION"
   db.update("INSERT INTO labor(lote_id,tipo,fecha,responsable_id,cantidad,observacion,creado_en) VALUES(?,?,?,?,?,?,?)",lote,tipo,LocalDate.now().minusDays((i/8).toLong()),if(i==16)2 else 1,1.0,"Registro inicial de campaña",LocalDateTime.now().minusHours(if(i>=8)48 else 1))
  }
 } }
}
