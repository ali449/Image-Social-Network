# Image-Social-Network
A social media image like Instagram based on [Persian language.](https://en.wikipedia.org/wiki/Persian_language)

# Installation
Alert: This project requires download about 500 MB dependency! 
Before import project, please change the version of [Spring](https://spring.io/) with your favorite version.
You can run the project in an editor or use [docker](https://www.docker.com/) with below instructions.
1.build fat-jar file: run the following command for each service.
```bash
mvn clean package
```
2.Build image for all services:
```bash
./docker/build_image_all.bat
```
3.Add this images to docker: jdk or jre, rabbitmq, neo4j, Elasticsearch
4.Run docker-compose in docker folder.
Alert: I commented [Elasticsearch image](https://hub.docker.com/_/elasticsearch) in docker-compose file and I ran it without docker. For use it with dokcer, first uncomment all elasticsearch lines and then change ELASTICSEARCH-HOST to elasticsearch.

# Description
![architecture](/arch-overview.png?raw=true)

## Discovery Service
This service is responsible for identifying and registering the addresses of various examples of microservices. This project uses Netflix [Eureka](https://github.com/Netflix/eureka) (without ribbon).

## Gateway Service
A gateway acts as a reverse proxy to accept all API calls. Here we have used Netflix [Zuul](https://github.com/Netflix/zuul) for this.

## Authentication and Authorization
Integrated with gateway service for simplicity. If a user's account is private, only its subscribers are allowed to view and comment on its posts. All microservices must request this service to authorizing user.
Public end points has started by /s (e.g hrrp://base.url/s/users for public and http://base.url/posts for private). We use sqlite database (you must use a better database in the production environment).

![User-DB-Schema](/user-db.jpg?raw=true)


## Post Service
This service is responsible for the tasks related to presenting, creating and deleting posts, searching between post texts and suggesting tags. This service uses Elasticsearch database with below properties.

![Post DB Schema](/post-db.jpg?raw=true)

is_private field has been added to reduce the connection between this service and the authentication service.


## Spam Detection
If an ad post is detected, when it is displayed to the user, a tag is displayed next to it so that the user can click on it if they wish (Such as below).

![Spam Detection Example](/grid_shot.jpg?raw=true)


### Preprocess steps
1. Identify and replace web links with the word {Website}
2. Remove stop words
3. Normalization and tokenization using the [Hazm](https://github.com/mojtaba-khallash/JHazm) library

### Training
We have used 2000 persian SMS and emails for the data set. The Naive Bayes algorithm has also been used for training. The accuracy of the obtained model was about 90%. You can also train that with your own dataset.

## Score Service
This microservice is responsible for processing user likes and comments. When a request is received from user, it first examines the user's access (authorization) by sending a request to the authentication server and then performs an sentiment analysis of the request (if it was a comment).

![Score DB Schema](/score-db.jpg?raw=true)


## Sentiment Analysis
Preprocess steps:
1. Replace positive emojis with a positive word (like great) and negative emojis with a negative word.
2. Remove non-Persian words (because our only goal is to process Persian texts)
3. Remove stop words
4. Normalize the text using Hazm library
5. Text tokenization (Hazm)
6. Remove words with low repetition
7. lemmatization tokens (Hazm)
8. Join tokens together with a space character

### Training
For the dataset, we used [SentiPers corpus](https://github.com/phosseini/SentiPers), which contains a collection of user comments about digital products. Since in this project it is only important whether the opinion is negative or not (and to increase accuracy), neutral items were removed; This reduction of classes (from three to two) increased the accuracy of the obtained model by 10%.
We used [Weka](https://www.cs.waikato.ac.nz/ml/weka/) for training with String to Word Vector filter and N-Gram tokenization. For training, we used [NB-SVM](https://vukbatanovic.github.io/NBSVM-Weka/) algorithm with weka default parameters. The accuracy of this model was about 88% with cross-validation evaluation method. We used Persian dataset, but you can train it with your own dataset.

## Recommender Service
It recommends posts based on what the user has approved and the tags they have searched for. The relationships between the entities are shown in the figure below.

![Recommender DB Relationships](/rec-relationship.jpg?raw=true)

The properties of the entities are also showed in the figure below.

![Recommender DB Properties](/rec-db.jpg?raw=true)


Content recommendation steps:
1. Find the last three items that the user has approved (likes or non-negative comment) (a set)
2. Find the creators and approvers {a}, sort them by their reputations and select 10 of them (b)
3. Find other posts that {b} users have approved (c)
4. Find the last three tags that the user has searched for and sort them by number of views (d)
5. Find the posts of {c} and add it to {c} (e)
6. If posts in {e} has no connection with the current user, is displayed to the user in order and in a page layout.

## Microservice Communications
For [internal](https://docs.microsoft.com/en-us/dotnet/architecture/microservices/architect-microservice-container-applications/communication-in-microservice-architecture) authentication, the http rest method (reactive) and for asynchronous (notification) communications the amqp protocol is used. To authenticate microservices, we defined a specific password to restrict the sending of requests to the API gateway. This password is added by each microservice in the http request header. Of course, making the right settings (such as blocking unused ports) in firewall can also improve this method.
For notification communication (only message is sent, we do not wait for response) a direct exchange and 4 queues are used. There are a total of 10 types of events in the system; The information and their application can be seen in the table below.

| Queue | Publisher Service(s) | Subscriber Service(s) | Events |
| --- | --- | --- | --- |
| 1 | User-Score-Post | Recommender | create, delete user and post, follow, unfollow, approve, unapproved |
| 2 | User | Score-Post | User delete account |
| 3 | User | Client | Request follow private account |
| 4 | Post | Score | Create and delete post |

The same messaging server was used to receive the notification (currently the only message to allow follow private user) on the client side (Android application); In a background service, application listens to message broker server, and if it receives a message, checks whether the destination of the message is the current user; If yes, displays a push notification (please use Firebase for production environment).
The following figure shows the notification communications between microservices.

![Asynchronous Communications](/async-comunication.png?raw=true)
