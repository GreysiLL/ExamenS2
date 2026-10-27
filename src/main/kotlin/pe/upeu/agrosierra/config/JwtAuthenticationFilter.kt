package pe.upeu.agrosierra.config
import jakarta.servlet.*
import jakarta.servlet.http.*
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import tools.jackson.databind.ObjectMapper
@Component
class JwtAuthenticationFilter(private val jwt: JwtService,private val usuarios: UsuarioRepository,private val mapper: ObjectMapper): OncePerRequestFilter() {
 override fun doFilterInternal(r: HttpServletRequest,s: HttpServletResponse,c: FilterChain) {
  if(r.requestURI=="/api/v1/auth/login" && r.method=="POST") { c.doFilter(r,s);return }
  val h=r.getHeader("Authorization")
  if(h==null || !h.startsWith("Bearer ")) { escribirError(mapper,r,s,401,"Inicia sesiÃ³n para continuar");return }
  try {
   val u=usuarios.porId(jwt.usuarioId(h.substring(7))) ?: throw IllegalArgumentException()
   SecurityContextHolder.getContext().authentication=UsernamePasswordAuthenticationToken(u,null,u.privilegios.map(::SimpleGrantedAuthority))
  } catch(e: Exception) { SecurityContextHolder.clearContext();escribirError(mapper,r,s,401,"SesiÃ³n vencida o token invÃ¡lido");return }
  c.doFilter(r,s)
 }
}
