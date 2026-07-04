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

### Backend
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

### Testing y calidad
![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=java&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-BB0A30?style=for-the-badge)
![SonarQube](https://img.shields.io/badge/SonarQube-4E9BCD?style=for-the-badge&logo=sonarqube&logoColor=white)

### DevOps
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---

## Estructura del proyecto

```text
raceflow-auth-service/
├── .github/workflows/
├── .env.example
├── .gitignore
├── Dockerfile
├── pom.xml
└── src/main/java/edu/eci/arsw/raceflow/auth/
    ├── AuthApplication.java
    ├── config/
    │   └── SecurityConfig.java
    ├── controller/
    │   └── AuthController.java
    ├── dto/
    │   ├── LoginRequest.java
    │   ├── RegisterRequest.java
    │   └── AuthResponse.java
    ├── model/
    │   └── User.java
    ├── repository/
    │   └── UserRepository.java
    └── service/
        ├── AuthService.java
        └── JwtService.java
```

---

## Configuracion local

### 1. Clonar el repositorio
```bash
git clone https://github.com/RaceFlowECI/raceflow-auth-service.git
cd raceflow-auth-service
```

### 2. Compilar
```bash
mvn clean install
```

### 3. Configurar variables de entorno
```bash
cp .env.example .env
```
```env
DB_HOST=localhost
DB_USER=raceflow
DB_PASSWORD=secret
JWT_SECRET=raceflow-dev-secret-key-for-local-dev-only-32chars
```

### 4. Ejecutar
```bash
mvn spring-boot:run
```
> [!TIP]
> El servicio arranca en `http://localhost:8081`. Requiere PostgreSQL en localhost:5432.

---

## Endpoints REST

---

## Pruebas y calidad

---

## CI/CD

