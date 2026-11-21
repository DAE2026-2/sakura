# Diagrama de clases - Sakura

```mermaid
classDiagram
    class Categoria {
        -Long id
        -String nombre
        -String descripcion
        -int orden
    }

    class Producto {
        -Long id
        -String nombre
        -String descripcion
        -BigDecimal precio
        -boolean disponible
        -boolean destacado
        -int orden
        -String imagen
    }

    class Mesa {
        -Long id
        -int numero
        -int capacidad
        -String ubicacion
        -EstadoMesa estado
    }

    class Usuario {
        -Long id
        -String nombre
        -String email
        -String passwordHash
        -Rol rol
        -boolean activo
        -OffsetDateTime creadoEn
    }

    class Pedido {
        -Long id
        -EstadoPedido estado
        -BigDecimal total
        -String notas
        -OffsetDateTime creadoEn
        -OffsetDateTime actualizadoEn
    }

    class DetallePedido {
        -Long id
        -int cantidad
        -BigDecimal precioUnitario
        -BigDecimal subtotal
    }

    class Reserva {
        -Long id
        -String codigo
        -LocalDate fecha
        -LocalTime hora
        -int personas
        -String nombre
        -String email
        -String telefono
        -String notas
        -EstadoReserva estado
        -OffsetDateTime creadaEn
    }

    class Turno {
        -Long id
        -LocalTime hora
        -int capacidadMax
        -boolean activo
    }

    class Cierre {
        -Long id
        -LocalDate fecha
        -String motivo
    }

    class Experiencia {
        -Long id
        -String nombre
        -String descripcion
        -BigDecimal precio
        -int personas
        -String etiqueta
        -int orden
    }

    class Faq {
        -Long id
        -String pregunta
        -String respuesta
        -int orden
    }

    class EstadoReserva {
        <<enumeration>>
        PENDIENTE
        CONFIRMADA
        ATENDIDA
        CANCELADA
    }

    class EstadoMesa {
        <<enumeration>>
        DISPONIBLE
        OCUPADA
    }

    class EstadoPedido {
        <<enumeration>>
        PENDIENTE
        EN_PREPARACION
        SERVIDO
        PAGADO
        CANCELADO
    }

    class Rol {
        <<enumeration>>
        ADMIN
        MESERO
        CLIENTE
    }

    Categoria "1" --> "0..*" Producto : contiene
    Mesa "1" --> "0..*" Pedido : tiene
    Usuario "0..1" --> "0..*" Pedido : registra
    Pedido "1" *-- "0..*" DetallePedido : contiene
    Producto "1" --> "0..*" DetallePedido : aparece en
    Mesa --> EstadoMesa : estado
    Pedido --> EstadoPedido : estado
    Usuario --> Rol : rol
    Mesa "0..1" <-- "0..*" Reserva : se asigna a
    Reserva --> EstadoReserva : estado
    Turno "1" .. "0..*" Reserva : misma hora y cupo
```

`Turno` y `Reserva` se relacionan por la hora (no hay clave foránea): el turno marca cuántas personas caben a esa hora.
`Cierre` son los días sin servicio. `Experiencia` y `Faq` son contenido de la landing.
