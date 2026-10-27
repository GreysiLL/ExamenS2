package pe.upeu.agrosierra
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
@SpringBootApplication(proxyBeanMethods = false)
class Aplicacion
fun main(args: Array<String>) { runApplication<Aplicacion>(*args) }
