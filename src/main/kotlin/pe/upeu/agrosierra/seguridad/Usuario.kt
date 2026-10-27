package pe.upeu.agrosierra.seguridad
data class Usuario(val id: Long, val usuario: String, val nombre: String, val passwordHash: String, val rol: String, val privilegios: Set<String>, val edicionGlobal: Boolean)
data class Rol(val nombre: String, val edicionGlobal: Boolean)
data class Privilegio(val nombre: String)
