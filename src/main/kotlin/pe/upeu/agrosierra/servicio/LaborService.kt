package pe.upeu.agrosierra.servicio
import pe.upeu.agrosierra.dominio.*
import pe.upeu.agrosierra.repositorio.AgroRepository
import pe.upeu.agrosierra.seguridad.Usuario
import pe.upeu.agrosierra.web.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.*
@Service
class LaborService(private val repo: AgroRepository,private val tx: TransactionTemplate) {
 private fun permiso(u: Usuario,p: String) { if(p !in u.privilegios) throw Fallo(403,"No tienes el privilegio $p") }
 private fun validar(p: LaborPeticion): Pair<TipoLabor,LocalDate> {
  val e=mutableMapOf<String,String>()
  val tipo=TipoLabor.entries.find { it.name==p.tipo };if(tipo==null)e["tipo"]="Selecciona RIEGO, FUMIGACION o COSECHA"
  val fecha=runCatching { LocalDate.parse(p.fecha) }.getOrNull();if(fecha==null)e["fecha"]="Usa una fecha válida AAAA-MM-DD"
  if(!p.cantidad.isFinite() || p.cantidad<=0)e["cantidad"]="La cantidad debe ser mayor que cero"
  if(!p.cantidadJornales.isFinite() || p.cantidadJornales<0)e["cantidadJornales"]="Los jornales deben ser cero o un número positivo"
  if(p.observacion.length>500)e["observacion"]="Máximo 500 caracteres"
  if(p.loteId<=0)e["loteId"]="Selecciona un lote"
  if(e.isNotEmpty())throw Fallo(400,"Datos inválidos",e)
  return tipo!! to fecha!!
 }
 private fun reglas(p: LaborPeticion,t: TipoLabor) {
  val l=repo.lote(p.loteId)
  if(l.campaniaCerrada)throw Fallo(409,"La campaña de este lote está cerrada")
  if(t==TipoLabor.FUMIGACION && p.cantidad>l.dosisMaximaPorHectarea*l.hectareas)throw Fallo(400,"La dosis supera el máximo del lote",mapOf("cantidad" to "Máximo permitido: ${l.dosisMaximaPorHectarea*l.hectareas}"))
 }
 fun crear(p: LaborPeticion,u: Usuario,clave: String?): Labor = tx.execute {
  permiso(u,"LABOR_CREAR")
  if(clave!=null) {
   if(clave.length>100)throw Fallo(400,"Clave de idempotencia inválida")
   repo.porClave(clave)?.let { if(it.responsableId!=u.id)throw Fallo(409,"Clave de otra operación");return@execute it }
  }
  val(t,f)=validar(p);repo.lote(p.loteId)
  repo.db.queryForObject("SELECT id FROM lote WHERE id=? FOR UPDATE",Long::class.java,p.loteId)
  reglas(p,t)
  val key=org.springframework.jdbc.support.GeneratedKeyHolder()
  repo.db.update({ c -> c.prepareStatement("INSERT INTO labor(lote_id,tipo,fecha,responsable_id,cantidad,observacion,creado_en,clave_cliente,cantidad_jornales) VALUES(?,?,?,?,?,?,?,?,?)",arrayOf("id")).apply { setLong(1,p.loteId);setString(2,t.name);setObject(3,f);setLong(4,u.id);setDouble(5,p.cantidad);setString(6,p.observacion);setObject(7,LocalDateTime.now());setString(8,clave);setDouble(9,p.cantidadJornales) } },key)
  repo.labor(key.key!!.toLong())
 }!!
 fun editar(id: Long,p: LaborPeticion,u: Usuario): Labor = tx.execute {
  permiso(u,"LABOR_EDITAR");val anterior=repo.labor(id)
  if(!u.edicionGlobal && (anterior.responsableId!=u.id || Duration.between(anterior.creadoEn,LocalDateTime.now()).seconds>=86400))throw Fallo(403,"Solo puedes corregir tus labores dentro de las primeras 24 horas")
  val(t,f)=validar(p)
  if(p.loteId!=anterior.loteId)throw Fallo(400,"No puedes mover la labor a otro lote",mapOf("loteId" to "Conserva el lote original"))
  repo.db.queryForObject("SELECT id FROM lote WHERE id=? FOR UPDATE",Long::class.java,p.loteId);reglas(p,t)
  repo.db.update("UPDATE labor SET tipo=?,fecha=?,cantidad=?,observacion=?,cantidad_jornales=? WHERE id=?",t.name,f,p.cantidad,p.observacion,p.cantidadJornales,id);repo.labor(id)
 }!!
 fun eliminar(id: Long,u: Usuario) { permiso(u,"LABOR_ELIMINAR");repo.labor(id);repo.db.update("DELETE FROM labor WHERE id=?",id) }
 fun cerrar(id: Long,u: Usuario): Lote = tx.execute {
  permiso(u,"CAMPANIA_CERRAR");repo.lote(id)
  repo.db.queryForObject("SELECT id FROM lote WHERE id=? FOR UPDATE",Long::class.java,id)
  if(repo.db.queryForObject("SELECT COUNT(*) FROM labor WHERE lote_id=? AND tipo='COSECHA'",Long::class.java,id)==0L)throw Fallo(409,"Registra al menos una cosecha antes de cerrar la campaña")
  repo.db.update("UPDATE lote SET campania_cerrada=TRUE WHERE id=?",id);repo.lote(id)
 }!!
}
