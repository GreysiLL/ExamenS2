package pe.upeu.agrosierra.repositorio
import pe.upeu.agrosierra.dominio.*
import pe.upeu.agrosierra.web.dto.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
@Component
class AgroRepository(val db: JdbcTemplate) {
 private fun lote(r: ResultSet) = Lote(r.getLong("id"),r.getString("codigo"),r.getString("nombre"),r.getDouble("hectareas"),r.getString("cultivo"),r.getString("valle"),r.getBoolean("campania_cerrada"),r.getDouble("dosis_maxima"))
 private fun labor(r: ResultSet) = Labor(r.getLong("id"),r.getLong("lote_id"),TipoLabor.valueOf(r.getString("tipo")),r.getDate("fecha").toLocalDate(),r.getString("responsable"),r.getDouble("cantidad"),r.getString("observacion"),r.getLong("responsable_id"),r.getTimestamp("creado_en").toLocalDateTime(),r.getDouble("cantidad_jornales"))
 fun lote(id: Long) = db.query("SELECT * FROM lote WHERE id=?",{r,_->lote(r)},id).firstOrNull() ?: throw Fallo(404,"El lote no existe")
 fun lotes(p: Int,t: Int,v: String?,e: Boolean?): Pagina<Lote> {
  val where=mutableListOf("1=1");val args=mutableListOf<Any>()
  if(!v.isNullOrBlank()){where+="valle=?";args+=v};if(e!=null){where+="campania_cerrada=?";args+=e}
  val filtro=" WHERE "+where.joinToString(" AND ")
  val total=db.queryForObject("SELECT COUNT(*) FROM lote$filtro",Long::class.java,*args.toTypedArray())!!
  val filas=db.query("SELECT * FROM lote$filtro ORDER BY codigo LIMIT ? OFFSET ?",{r,_->lote(r)},*(args+listOf(t,p.toLong()*t)).toTypedArray())
  return pagina(filas,p,t,total)
 }
 private val sqlLabor="SELECT l.*,u.nombre responsable FROM labor l JOIN usuario u ON u.id=l.responsable_id"
 fun labor(id: Long) = db.query("$sqlLabor WHERE l.id=?",{r,_->labor(r)},id).firstOrNull() ?: throw Fallo(404,"La labor no existe")
 fun porClave(clave: String) = db.query("$sqlLabor WHERE l.clave_cliente=?",{r,_->labor(r)},clave).firstOrNull()
 fun labores(id: Long,p: Int,t: Int): Pagina<Labor> {
  lote(id)
  val total=db.queryForObject("SELECT COUNT(*) FROM labor WHERE lote_id=?",Long::class.java,id)!!
  return pagina(db.query("$sqlLabor WHERE lote_id=? ORDER BY fecha DESC,l.id DESC LIMIT ? OFFSET ?",{r,_->labor(r)},id,t,p.toLong()*t),p,t,total)
 }
 private fun <T> pagina(l: List<T>,p: Int,t: Int,total: Long) = Pagina(l,p,t,total,((total+t-1)/t).toInt(),(p.toLong()+1)*t>=total)
}

