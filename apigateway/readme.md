# API Gateway con Vault, Keycloak y Encriptación AES256

Este es un proyecto base de un API Gateway que utiliza Vault, Keycloak y encriptación AES256 para el manejo seguro de tokens a través de un filtro de la aplicación. Además, realiza llamadas a un servicio de prueba llamado Chassis, que tiene un demo de conexión a una base de datos MongoDB.

## Características

- API Gateway que actúa como punto de entrada para las solicitudes de API.
- Integración de Vault para la gestión de secretos.
- Integración de Keycloak para la autenticación y autorización de usuarios.
- Consul: Servicio de registro y Balanceador de carga para distribuir el tráfico entre los servicios.
- Uso de encriptación AES256 para asegurar la información confidencial en los tokens.
- Llamadas al servicio de prueba Chassis que demuestra la conexión a una base de datos MongoDB.

## Requisitos previos

Asegúrate de tener instalados y configurados los siguientes componentes en tu entorno de desarrollo:

- Vault: Herramienta de gestión de secretos. Puedes obtener más información sobre cómo instalar y configurar Vault en [la documentación oficial de Vault](https://www.vaultproject.io/docs/install).
- Keycloak: Plataforma de gestión de identidad y acceso. Puedes obtener más información sobre cómo instalar y configurar Keycloak en [la documentación oficial de Keycloak](https://www.keycloak.org/documentation.html).
- MongoDB: Base de datos NoSQL. Asegúrate de tener una instancia de MongoDB en ejecución y configurada para el servicio Chassis.

## Configuración

Antes de ejecutar la aplicación, asegúrate de realizar los siguientes pasos de configuración:

1. Configura las propiedades de Vault:
   - Define las claves de acceso a los secretos requeridos para la aplicación, como claves de API, credenciales de base de datos, etc. en Vault.

2. Configura las propiedades de Keycloak:
   - Configura la URL de acceso al servidor de Keycloak y las credenciales de cliente en el archivo de configuración de la aplicación.

3. Genera la llave para la encriptación AES256:
   - Ejecuta el siguiente comando en tu terminal para generar la llave:
     ```
     keytool -genkeypair -alias apigateway_key -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore apigateway_key.p12 -validity 3650
     ```
     Esto generará un archivo `apigateway_key.p12` que contiene la llave para la encriptación AES256.

## Ejecución

Sigue los siguientes pasos para ejecutar la aplicación:

1. Clona este repositorio en tu máquina local:

git clone <URL_DEL_REPOSITORIO>


2. Navega al directorio del proyecto:

cd <DIRECTORIO_DEL_PROYECTO>


3. Construye el proyecto usando Maven y crea la imagen Docker:

``mvn clean install package -DskipTests``

``docker build -t supercontainerapps/sg-openapis-apigateway .``

Este paso puede ser opcional dependiendo si ya se encuentra en supercontainerapps, en caso de generarsen nuevos cambios
en el repositorio considere previamente aprobación del lider realizar el push de la imagen: supercontainerapps/sg-openapis-apigateway, en caso de ser la primera ves que descarga la imagen, al ejecutar el docker compose se tendra en cuenta la ultima imagen de supercontainerapps/sg-openapis-apigateway


4. Ejecuta el docker compose que se encuentra en el repositorio: https://gitlab.codesa.com.co/supergiros/openapis/openapis_microservicio_chassis.git

## Ejecución en ambiente local

Si se desea se puede ejcutar el apigateway o el proyecto chassis separado de docker sin necesidad de desplegarlo en el composer, teniendo los demas componentes en el docker (puede ser para efectos de pruebas) y que no se demore la compilación, configure las variables de entorno del editor de la siguiente manera:

```markdown
AES256_SALT=jw9Bu7rs8KXyhfk77fXiG2at1ivWrcVOjoT3ttyUmwA=
AES256_SALT_BODY=rVIu/2Uks6i4S4c7fBrRNcaK2tLl92DYKV1ZxgBLYrE=
AES256_SECRET_KEY=034fa9badce4ed139b7f12d932044c82faef96f467e8f2d5d5fcd509aaf13063
AES256_SECRET_KEY_BODY=acb39d81a90ad5cae907af65b37fab25e8b00d62a396985b28805d6572d1f575
clientId=spring-cloud-gateway-client
clientSecret=0859426e-bb03-451d-be15-5e41c4240e1c
grantType=password
SERVER_PORT=8081
SERVER_SSL_KEY_ALIAS=apigateway_key
SERVER_SSL_KEY_STORE=classpath:apigateway_key.p12
SERVER_SSL_KEY_STORE_PASSWORD=password
SERVER_SSL_KEY_STORE_TYPE=PKCS12
SERVER_TOMCAT_MAX_THREADS=1000
SPRING_APPLICATION_NAME=ms-api-gateway
SPRING_CLOUD_CONSUL_HOST=localhost
SPRING_CLOUD_CONSUL_PORT=8500
SPRING_CLOUD_GATEWAY_ROUTES_0_PREDICATES_0_ARGS_PATTERN=/productos/**
SPRING_CLOUD_GATEWAY_ROUTES_0_PREDICATES_0_NAME=Path
SPRING_CLOUD_GATEWAY_ROUTES_0_URI=http://localhost:8082
SPRING_CLOUD_GATEWAY_ROUTES_0_FILTERS_0_NAME=TokenDecrypt
SPRING_MAIN_WEB_APPLICATION_TYPE=reactive
TZ=America/Bogota
url=http://localhost:9090/realms/mycompany-microservices-realm/protocol/openid-connect/token
URL_MS_UPDATE=http://localhost:8088/update
```

para intellij idea se configuran en el apartado environment variables de la configuración del run, se puede pegar en la caja de texto en el formato CLAVE=valor;CLAVE1;valor.  

Nota: Estas configuraciones cambian de acuerdo a las necesidades de cada proyecto.
