package pe.upeu.agrosierra.config
import org.springframework.context.annotation.*
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.http.HttpMethod.*
import tools.jackson.databind.ObjectMapper
@Configuration(proxyBeanMethods=false)
class SecurityConfig {
 @Bean fun passwordEncoder() = BCryptPasswordEncoder()
 @Bean fun seguridad(http: HttpSecurity,filtro: JwtAuthenticationFilter,mapper: ObjectMapper): SecurityFilterChain {
  http.csrf { it.disable() }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
   .authorizeHttpRequests { a -> a.requestMatchers(POST,"/api/v1/auth/login").permitAll()
    .requestMatchers(GET,"/api/v1/usuarios/me").authenticated()
    .requestMatchers(GET,"/api/v1/lotes/*/labores").hasAuthority("LABOR_LEER")
    .requestMatchers(GET,"/api/v1/lotes","/api/v1/lotes/*").hasAuthority("LOTE_LEER")
    .requestMatchers(POST,"/api/v1/labores").hasAuthority("LABOR_CREAR")
    .requestMatchers(PUT,"/api/v1/labores/*").hasAuthority("LABOR_EDITAR")
    .requestMatchers(DELETE,"/api/v1/labores/*").hasAuthority("LABOR_ELIMINAR")
    .requestMatchers(POST,"/api/v1/lotes/*/cerrar-campania").hasAuthority("CAMPANIA_CERRAR")
    .anyRequest().authenticated() }
   .exceptionHandling { it.authenticationEntryPoint { r,s,_ -> escribirError(mapper,r,s,401,"Inicia sesiÃ³n para continuar") };it.accessDeniedHandler { r,s,_ -> escribirError(mapper,r,s,403,"No tienes el privilegio necesario para esta operaciÃ³n") } }
   .addFilterBefore(filtro,UsernamePasswordAuthenticationFilter::class.java)
  return http.build()
 }
}
