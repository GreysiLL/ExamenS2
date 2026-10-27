package pe.upeu.agrosierra.web
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import pe.upeu.agrosierra.repositorio.AgroRepository
import pe.upeu.agrosierra.servicio.LaborService
import pe.upeu.agrosierra.seguridad.Usuario
import pe.upeu.agrosierra.web.dto.*
@RestController
@RequestMapping("/api/v1")
class AgroController(private val repo: AgroRepository,private val servicio: LaborService) {
 private fun verificarPagina(p: Int,t: Int) { if(p<0 || t !in 1..100)throw Fallo(400,"Paginación inválida",mapOf("pagina" to "Página >= 0 y tamaño de 1 a 100")) }
 @GetMapping("/lotes") fun lotes(@RequestParam(defaultValue="0") pagina: Int,@RequestParam(defaultValue="20") tamanio: Int,@RequestParam(required=false) valle: String?,@RequestParam(required=false) campaniaCerrada: Boolean?) = run { verificarPagina(pagina,tamanio);repo.lotes(pagina,tamanio,valle,campaniaCerrada) }
 @GetMapping("/lotes/{id}") fun lote(@PathVariable id: Long) = repo.lote(id)
 @GetMapping("/lotes/{id}/labores") fun labores(@PathVariable id: Long,@RequestParam(defaultValue="0") pagina: Int,@RequestParam(defaultValue="20") tamanio: Int) = run { verificarPagina(pagina,tamanio);repo.labores(id,pagina,tamanio) }
 @PostMapping("/labores") fun crear(@RequestBody p: LaborPeticion,a: Authentication,@RequestHeader(value="Idempotency-Key",required=false) clave: String?) = ResponseEntity.status(201).body(servicio.crear(p,a.principal as Usuario,clave))
 @PutMapping("/labores/{id}") fun editar(@PathVariable id: Long,@RequestBody p: LaborPeticion,a: Authentication) = servicio.editar(id,p,a.principal as Usuario)
 @DeleteMapping("/labores/{id}") fun eliminar(@PathVariable id: Long,a: Authentication): ResponseEntity<Void> { servicio.eliminar(id,a.principal as Usuario);return ResponseEntity.noContent().build() }
 @PostMapping("/lotes/{id}/cerrar-campania") fun cerrar(@PathVariable id: Long,a: Authentication) = servicio.cerrar(id,a.principal as Usuario)
}
