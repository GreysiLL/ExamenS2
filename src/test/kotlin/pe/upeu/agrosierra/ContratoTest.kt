package pe.upeu.agrosierra
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.*
import java.time.*
import java.util.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties=["spring.datasource.url=jdbc:h2:mem:agrotest;DB_CLOSE_DELAY=-1"])
class ContratoTest {
 @Value("\${local.server.port}") private var puerto: Int=0
 @Autowired private lateinit var json: ObjectMapper
 @Autowired private lateinit var db: JdbcTemplate
 private val http=HttpClient.newHttpClient()
 private fun pedir(m: String,ruta: String,body: String?=null,token: String?=null,clave: String?=null): HttpResponse<String> {
  val b=HttpRequest.newBuilder(URI("http://localhost:$puerto/api/v1/$ruta")).header("Content-Type","application/json")
  if(token!=null)b.header("Authorization","Bearer $token");if(clave!=null)b.header("Idempotency-Key",clave)
  b.method(m,if(body==null)HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(body))
  return http.send(b.build(),HttpResponse.BodyHandlers.ofString())
 }
 private fun login(usuario: String="tecnico1"): String { val r=pedir("POST","auth/login","""{"usuario":"$usuario","contrasena":"Agro2026*"}""");assertEquals(200,r.statusCode(),r.body());return json.readTree(r.body())["token"].asText() }
 private fun labor(lote: Long=2,cantidad: Double=1.0,tipo: String="RIEGO")="""{"loteId":$lote,"tipo":"$tipo","fecha":"2026-10-27","cantidad":$cantidad,"observacion":"Prueba de contrato"}"""
 @Test fun loginCifradoYExpiracionDeUnaHora() {
  val t=login();val payload=json.readTree(String(Base64.getUrlDecoder().decode(t.split('.')[1])));assertEquals(3600,payload["exp"].asLong()-payload["iat"].asLong());assertTrue(payload["privilegios"].isArray)
  val hash=db.queryForObject("SELECT password_hash FROM usuario WHERE usuario='tecnico1'",String::class.java)!!;assertTrue(hash.startsWith("$2"));assertNotEquals("Agro2026*",hash)
  assertEquals(401,pedir("POST","auth/login","""{"usuario":"tecnico1","contrasena":"incorrecta"}""").statusCode())
 }
 @Test fun ausenteAlteradoYVencidoSon401() {
  assertEquals(401,pedir("GET","lotes").statusCode());val partes=login().split('.');val alterado=partes[0]+"."+partes[1]+"."+"x".repeat(partes[2].length);assertEquals(401,pedir("GET","lotes",token=alterado).statusCode())
  val key=Keys.hmacShaKeyFor(Base64.getDecoder().decode(db.queryForObject("SELECT valor FROM configuracion WHERE clave='jwt'",String::class.java)))
  val vencido=Jwts.builder().subject("1").issuedAt(Date(System.currentTimeMillis()-7200000)).expiration(Date(System.currentTimeMillis()-3600000)).signWith(key).compact()
  assertEquals(401,pedir("GET","lotes",token=vencido).statusCode())
 }
 @Test fun permisosEnServidorY403NoInvalidaSesion() {
  val admin=login("admin");assertEquals(403,pedir("POST","labores",labor(),admin).statusCode());assertEquals(200,pedir("GET","usuarios/me",token=admin).statusCode())
  assertEquals(403,pedir("DELETE","labores/1",token=login()).statusCode())
 }
 @Test fun paginacionFiltrosYDatosSemilla() {
  val token=login();val r=pedir("GET","lotes?pagina=0&tamanio=3",token=token);val p=json.readTree(r.body());assertEquals(8,p["totalElementos"].asInt());assertEquals(3,p["contenido"].size());assertFalse(p["ultima"].asBoolean())
  val f=pedir("GET","lotes?valle=Mantaro&campaniaCerrada=false",token=token);assertEquals(200,f.statusCode());json.readTree(f.body())["contenido"].forEach { assertEquals("Mantaro",it["valle"].asText());assertFalse(it["campaniaCerrada"].asBoolean()) }
  assertEquals(404,pedir("GET","lotes/999999",token=token).statusCode());assertEquals(400,pedir("GET","lotes?tamanio=0",token=token).statusCode())
 }
 @Test fun crudRealIdempotenciaY204Vacio() {
  val token=login();val clave=UUID.randomUUID().toString();val c=pedir("POST","labores",labor(),token,clave);assertEquals(201,c.statusCode(),c.body());val id=json.readTree(c.body())["id"].asLong();val repetida=pedir("POST","labores",labor(),token,clave);assertEquals(id,json.readTree(repetida.body())["id"].asLong())
  val u=pedir("PUT","labores/$id",labor(cantidad=2.0),token);assertEquals(200,u.statusCode(),u.body());assertEquals(2.0,json.readTree(u.body())["cantidad"].asDouble())
  val d=pedir("DELETE","labores/$id",token=login("jefe1"));assertEquals(204,d.statusCode());assertEquals("",d.body());assertEquals(404,pedir("PUT","labores/$id",labor(),token).statusCode())
 }
 @Test fun reglasRN01aRN04YValidacionPorCampo() {
  val t=login();val j=login("jefe1")
  assertEquals(409,pedir("POST","labores",labor(lote=7),t).statusCode())
  val viejo=db.queryForObject("SELECT id FROM labor WHERE responsable_id=1 AND creado_en < DATEADD('HOUR',-24,CURRENT_TIMESTAMP) FETCH FIRST 1 ROW ONLY",Long::class.java)!!
  val lote=db.queryForObject("SELECT lote_id FROM labor WHERE id=?",Long::class.java,viejo)!!
  assertEquals(403,pedir("PUT","labores/$viejo",labor(lote=lote),t).statusCode())
  val ajena=db.queryForObject("SELECT id FROM labor WHERE responsable_id=2 FETCH FIRST 1 ROW ONLY",Long::class.java)!!
  assertEquals(403,pedir("PUT","labores/$ajena",labor(lote=1),t).statusCode())
  assertEquals(409,pedir("POST","lotes/2/cerrar-campania",token=j).statusCode())
  val exceso=pedir("POST","labores",labor(cantidad=10000.0,tipo="FUMIGACION"),t);assertEquals(400,exceso.statusCode());assertTrue(json.readTree(exceso.body())["validationErrors"].has("cantidad"))
  val invalida=pedir("POST","labores",labor(cantidad=-1.0),t);assertEquals(400,invalida.statusCode());assertTrue(json.readTree(invalida.body()).has("path"))
 }
 @Test fun supervisorCorrigeTodasSinCerrarNiCrear() {
  val t=login("calidad1")
  val me=json.readTree(pedir("GET","usuarios/me",token=t).body())
  assertTrue(me["edicionGlobal"].asBoolean())
  val viejo=db.queryForObject("SELECT id FROM labor WHERE responsable_id=1 AND creado_en < DATEADD('HOUR',-24,CURRENT_TIMESTAMP) AND lote_id=1 FETCH FIRST 1 ROW ONLY",Long::class.java)!!
  assertEquals(200,pedir("PUT","labores/$viejo",labor(lote=1),t).statusCode())
  assertEquals(403,pedir("POST","lotes/1/cerrar-campania",token=t).statusCode())
  assertEquals(403,pedir("POST","labores",labor(),t).statusCode())
 }
}
