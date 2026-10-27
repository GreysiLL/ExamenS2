# SC-C

cantidadJornales es Double, admite cero o valores positivos finitos y viaja en la petición y la respuesta. La base del servidor agrega cantidad_jornales sin eliminar filas. Los datos semilla nuevos incluyen 1.5 jornales; filas anteriores conservan sus datos con el valor por defecto cero. La capa de servicio valida y persiste el campo tanto al crear como al corregir.

Verificación: nueve pruebas de integración aprobadas; CRUD comprueba la cantidad recibida y un valor negativo devuelve 400 bajo cantidadJornales. También se comprueba el cierre positivo con cosecha en una fila temporal eliminada al terminar.
