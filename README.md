# slideshow
Show a slideshow of interesting items owned by the IISH

### Running
The slideshow application is built with Spring Boot. Therefore the application can be run by:
- Simply running the main class
- Running with the Maven command: spring-boot:run
It will use a packaged servlet engine (Tomcat) that will be automatically configured. 

### Building
Building a WAR file for deployment with a servlet engine (Tomcat / Jetty / ...) is done by issuing the Maven *package* command.

### Config
You can add a new configuration file with the name *application.properties* or *application.yaml*. The configuration file can be placed in one of the following locations, where the application looks for configuration files by default: 
- classpath:
- classpath:/config
- file:
- file:config/
If the configuration file is placed somewhere else, the location can be specified using the argument *--spring.config.location=/location/of/config/files* or the environment variable *SPRING_CONFIG_LOCATION*. 

There is a sample configuration file, *application-example.properties*, located in the *classpath:/config* folder.

### Minimal requirements
The slideshow application currently requires the following:
- Java 7
- Servlet engine with API version 3.0.1 or higher (such as Tomcat 7)

