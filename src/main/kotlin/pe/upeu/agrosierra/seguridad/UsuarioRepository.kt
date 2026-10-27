package pe.upeu.agrosierra.seguridad
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
@Component
class UsuarioRepository(private val db: JdbcTemplate) {
 fun porNombre(nombre: String): Usuario? = buscar("u.usuario = ?", nombre)
 fun porId(id: Long): Usuario? = buscar("u.id = ?", id)
 private fun buscar(condicion: String, valor: Any): Usuario? {
  val filas = db.query("SELECT u.*, r.edicion_global FROM usuario u JOIN rol r ON u.rol=r.nombre WHERE $condicion", { r, _ -> Usuario(r.getLong("id"),r.getString("usuario"),r.getString("nombre"),r.getString("password_hash"),r.getString("rol"),emptySet(),r.getBoolean("edicion_global")) }, valor)
  return filas.firstOrNull()?.let { u -> u.copy(privilegios=db.queryForList("SELECT privilegio FROM rol_privilegio WHERE rol=?",String::class.java,u.rol).toSet()) }
 }
}

