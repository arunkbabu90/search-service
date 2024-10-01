# Search Service
A Report Generation Framework written in Spring Boot - Kotlin that generates reports on timesheet using the configuration via JSON. The configuration is a complex query expressed in JSON on what is to be included in the report

Technologies Used
* Elastic Search
* Logstash
* Kibana
* Spring Boot
* Kotlin
* Gradle
* PostgreSQL

# Setup
* Install PostgreSQL, Elastic, Logstash, Kibana (ELK); you can install it from official websites OR install via Docker
* I used a Docker based setup for ELK and PostgreSQL & if you want to follow that continue with the steps below:
    * I have included an environment file (**.env**) and **docker-compose.yml** file with the project, so if you haven't already, copy and paste those files into your project's root directory
    * Run the **docker-compose.yml** file using your container manager like Docker or Podman. This will install all the necessary dependencies for **ELK** stack and **PostgreSQL**

* Go to /src/main/resources/application.yml
* Replace the <port> in <b>jdbc:postgresql://localhost:<i><b><<port>port></b></i>/<database_name></b> with the port of your PostgreSQL and <b><i><database_name></i></b> with your own database name and also the elastic search host and port and the username and password

![SearchService1 1](https://github.com/user-attachments/assets/05f606c6-7332-46d6-aa92-5a84432bf182)

* Run using <b>SearchServiceApplication.kt</b>
