# Historias de usuario

Basadas en el documento del Sprint 1 (HU-01 a HU-08), más dos propias del proyecto (HU-09 y HU-10). Cada historia es
un Issue en GitHub.

| ID | Historia | Requerimientos | Responsable |
|---|---|---|---|
| HU-01 | Gestionar productos del menú | RF-01 | Angel |
| HU-02 | Gestionar categorías del menú | RF-02 | Angel |
| HU-03 | Crear un pedido asociado a una mesa | RF-03, RF-06 | Sebastián |
| HU-04 | Agregar productos a un pedido con total automático | RF-04, RF-05 | Sebastián |
| HU-05 | Consultar el historial de pedidos | RF-09 | Sebastián |
| HU-06 | Ver el menú disponible | RF-10 | Angel |
| HU-07 | Acceso por rol a las funciones administrativas | RF-08, RF-11 | Sebastián |
| HU-08 | Gestionar el estado de las mesas | RF-07 | Sebastián |
| HU-09 | Reservar mesa desde la web (extra) | — | Sebastián |
| HU-10 | Diseño de la base de datos | Sprint 1 | Angel y Sebastián |

---

### HU-01 · Gestionar productos del menú
**Como** administrador **quiero** registrar, editar y eliminar productos del menú **para** mantener actualizada la carta.

- [ ] El producto queda guardado con nombre, descripción, precio, categoría y disponibilidad.
- [ ] Nombre, categoría y precio son obligatorios; el precio no puede ser negativo.
- [ ] Los cambios se reflejan de inmediato en el menú.
- [ ] Un producto que ya está en pedidos no se elimina: se marca como no disponible.

`GET/POST /api/admin/productos` · `GET/PUT/DELETE /api/admin/productos/{id}` · Vista: `/admin/productos`

### HU-02 · Gestionar categorías del menú
**Como** administrador **quiero** gestionar las categorías del menú **para** organizar los productos de forma clara.

- [ ] Puedo crear, editar y eliminar categorías; el nombre no se repite.
- [ ] Cada producto queda asociado a una categoría existente.
- [ ] No se puede eliminar una categoría que tiene productos.

`GET/POST /api/admin/categorias` · `GET/PUT/DELETE /api/admin/categorias/{id}` · Vista: `/admin/categorias`

### HU-03 · Crear un pedido asociado a una mesa
**Como** mesero **quiero** crear un pedido asociado a una mesa **para** registrar lo que solicita el cliente.

- [ ] Solo se puede abrir un pedido en una mesa disponible; la mesa pasa a ocupada.
- [ ] El pedido empieza en estado PENDIENTE y guarda quién lo registró.
- [ ] Puedo cambiar el estado: PENDIENTE → EN_PREPARACION → SERVIDO → PAGADO, o CANCELADO.

`POST /api/pedidos` · `PATCH /api/pedidos/{id}/estado`

### HU-04 · Agregar productos a un pedido con total automático
**Como** mesero o cliente **quiero** agregar productos a un pedido y ver el total calculado automáticamente.

- [ ] Puedo agregar, cambiar la cantidad y quitar productos mientras el pedido no esté cerrado.
- [ ] Solo se pueden agregar productos disponibles.
- [ ] El detalle muestra producto, cantidad, precio unitario y subtotal, y el total se actualiza en cada cambio.

`POST /api/pedidos/{id}/detalles` · `PUT/DELETE /api/pedidos/{id}/detalles/{detalleId}`

### HU-05 · Consultar el historial de pedidos
**Como** administrador **quiero** consultar el historial de pedidos por fecha o mesa **para** llevar control de las ventas.

- [ ] Puedo filtrar por rango de fechas, número de mesa y cliente.
- [ ] Veo el estado y el total de cada pedido, y la suma de ventas pagadas del filtro.

`GET /api/admin/pedidos?desde=&hasta=&mesa=&usuario=`

### HU-06 · Ver el menú disponible
**Como** cliente **quiero** ver el menú disponible con precios desde la web **para** decidir qué pedir.

- [ ] El menú se carga desde la API REST, agrupado por categorías.
- [ ] Solo aparecen los productos marcados como disponibles.

`GET /api/menu` · Vista: sección "La carta" de la landing

### HU-07 · Acceso por rol a las funciones administrativas
**Como** administrador **quiero** que solo los usuarios autenticados con el rol correcto accedan a las funciones administrativas.

- [ ] Los usuarios tienen rol ADMIN, MESERO o CLIENTE y contraseña cifrada (BCrypt).
- [ ] Un cliente puede registrarse; meseros y administradores los crea un administrador.
- [ ] Un usuario sin rol ADMIN recibe 403 en `/api/admin/**` y no puede entrar al panel.

`POST /api/auth/registro` · `GET /api/auth/yo` · `GET/POST/PUT/DELETE /api/admin/usuarios`

### HU-08 · Gestionar el estado de las mesas
**Como** administrador **quiero** gestionar las mesas y su estado **para** organizar la atención al público.

- [ ] Puedo crear, editar y eliminar mesas (número único, capacidad, ubicación).
- [ ] El estado cambia automáticamente: OCUPADA al crear un pedido y DISPONIBLE al pagarlo o cancelarlo.

`GET /api/mesas` · `POST/PUT/DELETE /api/admin/mesas/{id}`

### HU-09 · Reservar mesa desde la web (extra)
**Como** cliente **quiero** reservar mesa desde la landing y poder consultar o cancelar la reserva con un código.

- [ ] Veo las horas libres según día y número de personas; recibo un código `SAK-XXXX`.
- [ ] Con el código y mi email puedo consultar o cancelar la reserva.
- [ ] El administrador ve las reservas del día y cambia su estado en `/admin`.

`GET /api/disponibilidad` · `POST /api/reservas` · `GET/DELETE /api/reservas/{codigo}` · `/api/admin/reservas`

### HU-10 · Diseño de la base de datos
**Como** equipo **queremos** un modelo de datos documentado **para** construir el backend sobre Supabase.

- [ ] Diagrama entidad-relación en `docs/base-de-datos.md`.
- [ ] Script `database/supabase.sql` con creación e inserción de datos, ejecutable en Supabase.
- [ ] RLS activo en todas las tablas.
