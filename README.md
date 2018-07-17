# Bespoken Endpoint for Alexa
Endpoint for our BattleShip-game

## Running server

* install NodeJS and set environment variables accordingly
* install bespoken:
  * $ npm install -g bespoken-tools
* clone project from Github
  * $ git clone https://github.com/maximus-v/alexa-battle-ship.git
* compile and run project
  * $ mvn compile
  * $ mvn exec:java -Dexec.executable="java" -DdisableRequestSignatureCheck=true -Dexec.args=$@ 
  * project will run on port 9999
* start server
  * $ bst proxy http 9999
* create skill on Amazon Developer Console
  * use given IntentSchema
* when setting the endpoint use url provided by local bespoken server:
  * add /ship to the url
  * when setting certificate choose:  "My development endpoint is a subdomain of a domain that has a wildcard certificate from a certificate authority"
* test skill