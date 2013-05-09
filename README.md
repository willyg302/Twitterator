# Twitterator

Authors: William Gaul, Michael Terada  
Original Program By: Baohuy Ung  
Version: 0.5

---

## About

Twitterator is an attempt to allow for automated, remote collection of Twitter data to help the Hawaii Computer-Human Interaction (HICHI) group with their research. The project is designed to be self-contained, portable, and extensible. When launched, it spawns a web server, H2 database server, and Twitter stream, and handles the connections between these components.

## Packages

- [authentication](https://github.com/willyg302/twitterator/tree/master/src/authentication): classes for user authentication, primarily OAuth
- [db](https://github.com/willyg302/twitterator/tree/master/src/db): H2 database interface and info classes
- [export](https://github.com/willyg302/twitterator/tree/master/src/export): export modules for representing data in a variety of formats
- [twitterFeed](https://github.com/willyg302/twitterator/tree/master/src/twitterFeed): custom classes for handling the Twitter bridge (slated to be deprecated)
- [twitterator](https://github.com/willyg302/twitterator/tree/master/src/twitterator): core application classes that handle the web server, logging etc.
- [webapp](https://github.com/willyg302/twitterator/tree/master/src/webapp): HTML, CSS, and JavaScript resources

## SQL Statements

View the [SQL Statements](https://github.com/willyg302/twitterator/tree/master/src/SQL) to look at the structure of various tables in stored in the database.

## Authentication Keys

In order to run this product you will need to have a valid Twitter account and register a new Twitter application to this account. Authentication is a two-step process:

- Registering the App: requires a Consumer Key and Consumer Secret
- Registering the User: requires an Access Token and Secret Token

Registering a user allows the app to make requests on behalf of the user. In theory, this also allows multiple streaming connections.

## Build Instructions

1.  You must have a valid `config.properties` file located in the __src__ directory with the following entries:
    - ATP: Public Twitter account access token
    - ATS: Secret Twitter account access token
    - AKP: Public Twitter application key
    - AKS: Secret Twitter application key
    - SUPERUser: Default login username
    - SUPERPass: Default login password

    You may also have ATPTest, ATSTest, AKPTest, and AKSTest entries which correspond to a test Twitter account/application. That way you can continue to develop and test the product while also running a production server.
2.  Change the `DEBUG` variable in `Twitterator.java` to either true (test mode) or false (production mode)
3.  Build the project as a JAR
4.  You can now run the JAR from the command line and users should be able to connect via IP:Port to the server

## Build Notes

Twitterator is built with Java 7.0 in NetBeans, and is NOT compatible with Java 6.0. All dependencies are available in the [LIBS](https://github.com/willyg302/twitterator/tree/master/LIBS) folder.

## Credits

- __Thomas Mueller__: H2 Database Engine
- __Eclipse Foundation__: Jetty Web Server
- __Yusuke Yamamoto__: Twitter4J Java-Twitter API
- __Suhail Dawood__: CSS3 Slick Login Form
- __Alex Ehlke__: Tag-it Editing Widget
- __Haran__: Base website design