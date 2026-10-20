# Diagrama de casos de uso - Sakura

```mermaid
flowchart TB
    Admin["Administrador"]
    Mesero["Mesero"]
    Cliente["Cliente"]

    subgraph Sistema["Sistema Sakura"]
        UC1(["Iniciar sesión"])
        UC2(["Gestionar categorías"])
        UC3(["Gestionar productos"])
        UC4(["Consultar menú"])
        UC5(["Consultar pedidos"])
        UC6(["Gestionar pedidos"])
        UC7(["Registrar pedidos"])
        UC8(["Consultar estado del pedido"])
    end

    Admin --- UC1
    Admin --- UC2
    Admin --- UC3
    Admin --- UC4
    Admin --- UC5
    Admin --- UC6

    Mesero --- UC1
    Mesero --- UC4
    Mesero --- UC5
    Mesero --- UC6
    Mesero --- UC7

    Cliente --- UC4
    Cliente --- UC8
    Cliente --- UC7
