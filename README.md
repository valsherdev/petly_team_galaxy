# Team Galaxy - Makers Students Final Project

We are in our third and final project of the Makers Software Engineering bootcamp specialising in Java for this part of the course.

 - Valeryia Sherman
 - Trouli Kambouri
 - Naima Ahsan
 - Seb Block

# Welcome to Petly!

### What is Petly?

Petly is a platform connection pet owners with services for pets. Think of it as a one stop shop for pet needs.

[See a demo of use](https://www.canva.com/design/DAHUJybw69o/v-Y_QB83QcxXMgasG8BSiA/watch?utm_content=DAHUJybw69o&utm_campaign=designshare&utm_medium=link2&utm_source=uniquelinks&utlId=hd8e74fbbf1)

This was our demo which we presented over - will update with talked through demo

It is also deployed on Render - click the link below:

[Petly](https://petly-team-galaxy.onrender.com/dashboard/provider)


## Existing features

This app has these features implemented
* A user can sign up using Auth0
* A signed up user can sign in
* A signed in user then has to choose whether they are using the app as a Pet Owner or Service Provider on the account
* There are 2 different user experiences when choosing your role type - As a service provider you can:
  * Create a service using a form
  * Edit the service and delete the service
  * See booking requests - both pending and confirmed, service provider can accept or decline
  * See messages from pet owners about a service

  And as a Pet Owner:
  * Create a Pet using a form (multiple if you so wish)
  * Edit and remove a pet
  * See services - search by type of service needed which will bring up distance to pet owner location from nearest to furthest away
  * Book a service - request sent to book a service which will either be approved or declined
  * Message function to message a service if more information is needed or any questions
  * See all bookings including pending, upcoming, past and declined with ability to rebook a past booking and also leave a 1-5 star review for the service

# Setup

## QuickStart Instructions

- Click "Use this template" to create a copy of this repo on your GitHub account.
- Open the codebase in an IDE like InteliJ or VSCode
  - If using IntelliJ, accept the prompt to install the Lombok plugin (if you don't get prompted, press command and comma
  to open the Settings and go to Plugins and search for Lombok made by Jetbrains and install).
- Create two new Postgres databases called `petly_dev` and `petly_test`
- Install Maven `brew install maven`
- [Set up Auth0]([https://journey.makers.tech/pages/auth0](https://auth0.com/docs/secure/application-credentials) you will need to create an Auth0 application regular web application and use the client keys to apply to the application.yml file. You will also need to add http://localhost:8081/login/oauth2/code/okta and http://localhost:8080/login/oauth2/code/okta to the allowed callback urls and http://localhost:8081 and http://localhost:8080 to the allowed logout urls
- Build the app and start the server, using the Maven command `mvn spring-boot:run`
> The database migrations will run automatically at this point
- Visit `http://localhost:8081/` to sign up

## Running the tests

- Install chromedriver using `brew install chromedriver`
- Start the server in a terminal session `mvn spring-boot:run -Dspring-boot.run.profiles=test`
- Open a new terminal session and navigate to the Petly directory
- Run your tests in the second terminal session with `mvn test`
- You can run the feature tests using `mvn verify` but do not have the server running when using this

> All the tests should pass. If one or more fail, read the next section.

## Common Setup Issues

### The application is not running

For the feature tests to execute properly, you'll need to have the server running in one terminal session and then use a second terminal session to run the tests.

### Chromedriver is in the wrong place

Selenium uses Chromedriver to interact with the Chrome browser. If you're on a Mac, Chromedriver needs to be in `/usr/local/bin`. You can find out where it is like this `which chromedriver`. If it's in the wrong place, move it using `mv`.

### Chromedriver can't be opened

Your Mac might refuse to open Chromedriver because it's from an unidentified developer. If you see a popup at that point, dismiss it by selecting `Cancel`, then go to `System Preferences`, `Security and Privacy`, `General`. You should see a message telling you that Chromedriver was blocked and, if so, there will be an `Open Anyway` button. Click that and then re-try your tests.


## Design

The application uses:
- `maven` to build the project
- `thymeleaf` for templating
- `flyway` to manage `postgres` db migrations
- `selenium` for feature testing
- `faker` to generate fake names for testing
- `junit4` for unit testing
- `auth0` and `spring-security` for authentication and user management
- `lombok` to generate getters and setters for us
- `tailwind` for styling the pages




