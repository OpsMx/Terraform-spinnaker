# TerraSpin

A microservice to integrate with Spinnaker for planning, applying and destroying Terraform plans
[TerraSpin Docs](https://docs.opsmx.com/codelabs/terraform-spinnaker) 
## How to Build and Run
clone this repository and go inside TerraSpin directory run this command

to build cmd- mvn clean install

After buliding maven will put jar in target folder of TerraSpin directory

to run microservice cmd- java -Dspring.config.location=application.properties -jar TerraSpin.jar


