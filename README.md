# RACEFLOW — Auth Service

> [!IMPORTANT]
> Este repositorio contiene el **Auth Service** de RaceFlow: autenticacion JWT y gestion de usuarios.

> Para informacion general consulta el [perfil de la organizacion](https://github.com/RaceFlowECI).

---

## Tabla de contenido
- [Descripcion general](#descripcion-general)
- [Stack tecnologico](#stack-tecnologico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuracion local](#configuracion-local)
- [Endpoints REST](#endpoints-rest)
- [Pruebas y calidad](#pruebas-y-calidad)
- [CI/CD](#cicd)

---

## Descripcion general

> [!NOTE]
> Microservicio de autenticacion y autorizacion. Gestiona el ciclo de vida de los usuarios, emite tokens JWT firmados y expone un endpoint de validacion consumido por el API Gateway.

### Responsabilidades principales

| Responsabilidad | Descripcion |
|---|---|
| **Registro** | Crea cuentas de usuario con password hasheado via BCrypt. |
| **Login** | Valida credenciales y emite un JWT firmado con el secret compartido. |
| **Perfil** | Expone `/auth/me` para que el cliente obtenga los datos del usuario autenticado. |
| **Validacion** | El API Gateway llama internamente para verificar la firma del token. |

---

## Stack tecnologico

---

## Estructura del proyecto

---

## Configuracion local

---

## Endpoints REST

---

## Pruebas y calidad

---

## CI/CD

