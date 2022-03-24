# Basic Messaging

Basic Messaging REST API Service

## Description

Basic Messaging Service allows clients to create a user and send messages to other users within the system. Also allows users to retrieve messages they have sent, messages they have received, and filter the received messages by sender.

In order to store the messages a PostgreSQL DB is used, additionaly messages are propagated to a RabbitMQ Exchange that is connected in fanout mode to a queue. 

## Features

- Create User
- Send messages to other user
- Retrieve sent messages
- Retrieve received messages (all messages or filtered by sender)

## Requirements

- Maven 3.8.5
- Java 1.8
- Docker 20.10
- docker-compose 1.29

## Run
In order to run the Messaging Services 3 containers are going to be created. One for the DB (PostgreSQL), one for the Broker (RabbitMQ), and one for the service itself. Please make sure port 8080 is free, as it is going to be used for receiving requests. (In case you want to see Broker UI, port 15672 is needed to be availabel as well) 

To run the service in Windows, simply double click the bat file named 'build_and_run.bat'. In case you are in other type of OS, open the bat file with a text editor and execute the instructions manually in the Command Prompt of your OS. 

To run tests in Windows, simply double click the bat file named 'run_tests.bat'. In case you are in other type of OS, open the bat file with a text editor and execute the instructions manually in the Command Prompt of your OS. 

## Use

The Basic Messaging service has 4 endpoints, detailed here below

| Purpose | Path | Method| Params | Header |
| ------ | ------ | ------ | ------ | ------ |
| Create User | /api/v1/users/ | POST |
| Send Message | /api/v1/messages/ | POST |  | x-user-id |
| Messages sent | /api/v1/messages/sent | GET |  | x-user-id |
| Messages Received | /api/v1/messages/received | GET | sent_by | x-user-id |

The last column, header, is used to hold the id of the logged user (the user performing actions, like send a message).

In the folder it is possible to find a Postman Collection with this four endpoints. Here below are described Curl examples for each on of them.

### Create User
```sh
curl --location --request POST 'localhost:8080/api/v1/users/' \
--header 'Content-Type: application/json' \
--data-raw '{
    "user_name": "user1"
}'
```
The response of this call is going to be the id of the created user and 201 HTTP Created if successful. In case of error the following:
- 409 HTTP Conflict in case the name alredy exists.
- 400 HTTP Bad Request in case the name is not present or blank.

### Send Message

```sh
curl --location --request POST 'localhost:8080/api/v1/messages/' \
--header 'X-User-Id: 1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "receiver_id": 2,
    "body": "this is message"
}'
```
Te response of this call is going to be a 201 HTTP Created if successful, or below defined errors:
- 404 HTTP Not Found in case either the Sender (logged user) or Reciever are not found
- 404 HTTP Not Found in case the Sender (logged user) Id is out of range or does not comply with type (int) (no endpoint found to process this call)
- 400 HTTP Bad Request if Receiver Id is either not present or out of range, Or if Message is not present or blank.

### Retrieve sent messages 
```sh
curl --location --request GET 'localhost:8080/api/v1/messages/sent' \
--header 'x-user-id: 1'
```
Te response of this call is going to be a 200 HTTP OK if successful, and is going to contain a JSON list with all the sent messages.
In case the request is not successful, below defined errors:
- 404 HTTP Not Found in case the logged user (Sender) is not found
- 404 HTTP Not Found in case the logged user (Sender) id is out of range 
- 400 HTTP Bad Request in case logged user (Sender) does not comply with type (int) 

### Retrieve received messages 
```sh
curl --location --request GET 'localhost:8080/api/v1/messages/received' \
--header 'x-user-id: 1'
```
Te response of this call is going to be a 200 HTTP OK if successful, and is going to contain a JSON list with all the received messages.
In case the request is not successful, below defined errors:
- 404 HTTP Not Found in case the logged user (Receiver) is not found
- 404 HTTP Not Found in case the logged user (Receiver) id is out of range 
- 400 HTTP Bad Request in case logged user (Receiver) does not comply with type (int) 

### Retrieve received messages 
```sh
curl --location --request GET 'localhost:8080/api/v1/messages/received?sent_by=5' \
--header 'x-user-id: 1'
```
Te response of this call is going to be a 200 HTTP OK if successful, and is going to contain a JSON list with all the received messages.
In case the request is not successful, besides the above defined errors for Received Messages, below additionals ones could be possible:
- 404 HTTP Not Found in case the Sender (sent_by) is not found
- 400 HTTP Bad Request in case Sender (sent_by) does not comply with type (int) or is out of range

## Next Steps
In order to continue improving this service, these are the next increments:
### Tech
- Observability: add mechanisms to know the health and performance of the service.
- Auth improvement: provide safer way to authenticate and have authorization for different actions.
- CI/CD: enable rapid way to add more capabilities in a sustainable way
- Security: add security layer (CORS for instance)
- Secret Management: improve how secrets are managed/handled (like DB credentials)

### Product
- Filter sent messages by receiver 
- User customization: enrich user profile, user to be not publicly reachable
- Retrieve list of publicly reachable users
- Create Groups of users

