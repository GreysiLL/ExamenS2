package pe.upeu.agrosierra.dominio
import java.time.LocalDate
import java.time.LocalDateTime
data class Lote(val id: Long, val codigo: String, val nombre: String, val hectareas: Double, val cultivo: String, val valle: String, val campaniaCerrada: Boolean, val dosisMaximaPorHectarea: Double)
enum class TipoLabor { RIEGO, FUMIGACION, COSECHA }
data class Labor(val id: Long, val loteId: Long, val tipo: TipoLabor, val fecha: LocalDate, val responsable: String, val cantidad: Double, val observacion: String, val responsableId: Long, val creadoEn: LocalDateTime)
