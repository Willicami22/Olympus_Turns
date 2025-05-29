# Módulo de Turnos - ECI Bienestar Total
## 📋 Descripción

El **Módulo de Turnos** es un sistema de gestión de citas médicas diseñado para la **Escuela Colombiana de Ingeniería Julio Garavito (ECI)**. Este sistema permite administrar turnos de atención para diferentes especialidades médicas como Psicología, Medicina General y Odontología, con soporte para usuarios con discapacidades y priorización automática.

## ✨ Características Principales

- **Gestión de Turnos**: Creación, modificación y seguimiento de turnos médicos
- **Sistema de Prioridades**: Priorización automática para usuarios con discapacidades
- **Múltiples Especialidades**: Soporte para Psicología, Medicina General y Odontología
- **Autenticación JWT**: Sistema de autenticación seguro con tokens JWT
- **Generación de Reportes**: Reportes detallados con exportación a Excel y PDF
- **API REST**: Documentación completa con Swagger/OpenAPI
- **Base de Datos MongoDB**: Almacenamiento NoSQL escalable
- **Gestión de Archivos**: Subida y descarga de documentos

## 🛠️ Tecnologías Utilizadas

- **Java 17**: Lenguaje de programación principal
- **Spring Boot 3.4.5**: Framework principal
- **Spring Data MongoDB**: Integración con MongoDB
- **JWT (Auth0)**: Autenticación y autorización
- **Apache POI**: Generación de archivos Excel
- **iText 7**: Generación de archivos PDF
- **Swagger/OpenAPI**: Documentación de API
- **JUnit 5 & Mockito**: Testing
- **JaCoCo**: Cobertura de código
- **Maven**: Gestión de dependencias

## 🏗️ Arquitectura del Sistema
src/

├── main/

│   ├── java/edu/eci/cvds/EciBienestarTotal/ModuloTurnos/

│   │   ├── Controller/          # Controladores REST

│   │   ├── Service/             # Lógica de negocio

│   │   ├── Repository/          # Acceso a datos

│   │   ├── Entitie/             # Entidades de dominio

│   │   ├── DTO/                 # Objetos de transferencia

│   │   └── Enum/                # Enumeraciones

│   └── resources/

│       └── application.properties

└── test/                        # Pruebas unitarias

## 📦 Instalación y Configuración

### Prerrequisitos

- Java 17 o superior
- Maven 3.6+
- MongoDB Atlas (o instancia local)
- IDE compatible (IntelliJ IDEA, Eclipse, VS Code)

### Configuración

1. **Clonar el repositorio**
```
git clone https://github.com/tu-usuario/ModuloTurnos.git
cd ModuloTurnos
```

2. **Configurar MongoDB**
Editar src/main/resources/application.properties:
```
spring.data.mongodb.uri=mongodb+srv://usuario:password@cluster.mongodb.net/ShiftModule
```
3. **Compilar el proyecto**
```
mvn clean compile
```
4. **Ejecutar Pruebas**
```
mvn clean test
```
5. **Ejecutar la aplicacion**
```
mvn spring-boot:run
```
La aplicación estará disponible en http://localhost:8080

## Documentación de la API
Una vez ejecutada la aplicación, puedes acceder a la documentación interactiva de la API en:

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/api-docs

## Autenticacion
El sistema utiliza tokens JWT para la autenticación. Para acceder a los endpoints protegidos, incluye el token en el header:
```
Authorization: Bearer <tu-token-jwt>
```
### Estructura del token JWT
```
{
  "id": "user-id",
  "userName": "nombre-usuario",
  "email": "email@ejemplo.com", 
  "name": "Nombre Completo",
  "role": "Medical_Secretary",
  "specialty": "especialidad"
}
```

## Tipos de Usuario y Roles

- **Secretariao medica:** Administrador del sistema, acceso completo
- **Medico:** Administra sus turnos segun la profesion
- **Estudiante:** Usuario básico
- **Docente:** Usuario académico
- **Administrativo:** Personal administrativo
- **ServiciosGenerales:** Personal de servicios

## Especialidades Médicas

- Psicología (Código: P-)
- Medicina General (Código: M-)
- Odontología (Código: O-)

## Sistema de Discapacidades
El sistema reconoce y prioriza usuarios con:

- **Mayor de Edad:** Usuarios de la tercera edad
- **Disfunción Motriz:** Limitaciones físicas
- **Embarazo:** Mujeres en estado de gestación
- **Otra:** Otras condiciones especiales
- **No Tiene:** Sin discapacidades

## Reportes y Análisis
El sistema genera reportes detallados que incluyen:
- Total de turnos por período
- Turnos completados vs. pendientes
- Tiempo promedio de espera y atención
- Distribución por roles de usuario
- Análisis de discapacidades por rol
- Exportación a Excel y PDF

##  Testing
Ejecutar todas las pruebas

```
mvn test
```
Generar reporte de cobertura
```
mvn jacoco:report
```
El reporte estará disponible en target/site/jacoco/index.html

**Cobertura Mínima Requerida**

- Cobertura de clases: 85%

## Despliegue
Azure App Service

El proyecto incluye configuración para despliegue automático en Azure:

- **Pipeline CI/CD:** .github/workflows/main_eciturnos.yml
- **Entorno de producción:** Configurado para Azure Web Apps
- **Java Runtime:** OpenJDK 17


