# SC-B: supervisor de calidad

La asignación vive en rol y rol_privilegio. SUPERVISOR_CALIDAD tiene LOTE_LEER, LABOR_LEER y LABOR_EDITAR; edicion_global=true amplía el alcance de corrección a labores ajenas y antiguas. No recibe LABOR_CREAR, LABOR_ELIMINAR ni CAMPANIA_CERRAR. El filtro y los controladores no comparan el nombre del rol.

Cuenta de demostración: calidad1 / Agro2026*. La aplicación obtiene edicionGlobal y privilegios desde usuarios/me: ofrece Corregir y oculta Registrar y Cerrar sin agregar condiciones por rol.

Verificación: 7 pruebas de integración aprobadas, incluida corrección de una labor de otro usuario con más de 24 horas y rechazo 403 al crear o cerrar. Arranque con base existente: el registro del nuevo rol es idempotente y no elimina datos.
