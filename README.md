IronMQ Java Client
----------------

Getting Started
===============
There are three ways to get this package.

1. Add it as a Maven dependency
   Your pom.xml will look something like:

```xml
    <dependencies>
        <!-- IronMQ message queue client -->
        <dependency>
            <groupId>io.iron.ironmq</groupId>
            <artifactId>ironmq</artifactId>
            <version>0.0.15</version>
        </dependency>
    </dependencies>
```

2. [Download the jar](https://github.com/iron-io/iron_mq_java/downloads).

   [Download the jar from Maven Repo](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.iron.ironmq%22).

3. Build from source with [Apache Buildr](http://buildr.apache.org):

    buildr package

The .jar file will appear under the target directory.

### Configure

**Initialize** a client and get a queue object:
```java
Client client = new Client("my project", "my token", Cloud.ironAWSUSEast);
Queue queue = client.queue("test-queue");
```

    Client client = new Client("my project", "my token", Cloud.IronAWSUSEast);
## The Basics
    Client client = new Client("my project", "my token", Cloud.ironAWSUSEast);

### Post a Message to the Queue

```java
queue.postMessage("Hello, IronMQ!");
```

More complex example:

```java
String body = "Hello, IronMQ!";
int timeout = 30;
int delay = 0;
int expiresIn = 0;
String messageId = queue.postMessage(body, timeout, delay, expiresIn);
```

Post multiple messages in one API call:

```java
String[] messages = {"c", "d"};
Ids ids = queue.postMessages(messages);
```

--

### Get a Message off the Queue

```java
Message msg = queue.get();
```

When you pop/get a message from the queue, it will NOT be deleted.
It will eventually go back onto the queue after a timeout if you don't delete it (default timeout is 60 seconds).

Get multiple messages in one API call:

```java
Message msg = queue.get(2);
```

--

### Delete a Message from the Queue

```java
Message msg = queue.get();
queue.deleteMessage(msg);
```
Delete a message from the queue when you're done with it.

Delete multiple messages in one API call:

```java
String[] messages = {"c", "d"};
Ids ids = queue.postMessages(messages);
queue.deleteMessages();
```
Delete multiple messages specified by messages id array.

--

## Queues

### List Queues

```java
Queues queues = new Queues(client);
ArrayList<QueueModel> allQueues = queues.getAllQueues();
```

--

### Retrieve Queue Information

```java
QueueModel infoAboutQueue = queue.getInfoAboutQueue();
```

--

### Delete a Message Queue

```java
queue.destroy();
```

--

### Post Messages to a Queue

**Single message:**

```java
queue.postMessage(body);
```

**Multiple messages:**

```java
String[] messages = {"c", "d"};
Ids ids = queue.postMessages(messages);
```

**Optional parameters (3rd, `array` of key-value pairs):**

* `timeout`: After timeout (in seconds), item will be placed back onto queue.
You must delete the message from the queue to ensure it does not go back onto the queue.
 Default is 60 seconds. Minimum is 30 seconds. Maximum is 86,400 seconds (24 hours).

* `delay`: The item will not be available on the queue until this many seconds have passed.
Default is 0 seconds. Maximum is 604,800 seconds (7 days).

* `expires_in`: How long in seconds to keep the item on the queue before it is deleted.
Default is 604,800 seconds (7 days). Maximum is 2,592,000 seconds (30 days).

--

### Get Messages from a Queue

**Single message:**

```java
Message msg = queue.get();
```

**Multiple messages:**

```java
int count = 5;
Message msg = queue.get(count);
```

**Optional parameters:**

* `count`: The maximum number of messages to get. Default is 1. Maximum is 100.

--

### Touch a Message on a Queue

Touching a reserved message extends its timeout by the duration specified when the message was created, which is 60 seconds by default.

```java
String messageId = "xxxxxxxxxxxxxx";
queue.touchMessage(messageId);
```

--

### Release Message

```java
int delay = 1;
queue.releaseMessage(id, delay);
```

**Optional parameters:**

* `delay`: The item will not be available on the queue until this many seconds have passed.
Default is 0 seconds. Maximum is 604,800 seconds (7 days).

--

### Delete a Message from a Queue

```java
Message msg = queue.get();
queue.deleteMessage(msg);
```

--

### Peek Messages from a Queue

Peeking at a queue returns the next messages on the queue, but it does not reserve them.

**Single message:**

```java
Messages msg = queue.peek();
```

**Multiple messages:**

```java
int count = 2;
Messages msg = queue.peek(count);
```

--

### Clear a Queue

```java
queue.clear();
```

--

### Add alerts to a queue. This is for Pull Queue only.

```java
queue.addAlertsToQueue(alertsArrayList);
```

### Replace current queue alerts with a given list of alerts. This is for Pull Queue only.

```java
queue.updateAlertsToQueue(alertsArrayList);
```

### Remove alerts from a queue. This is for Pull Queue only.

```java
queue.deleteAlertsFromQueue(alertsArrayList);
```

### Remove alert from a queue by its ID. This is for Pull Queue only.

```java
queue.deleteAlertFromQueueById(alertid);
```

--


## Push Queues

IronMQ push queues allow you to setup a queue that will push to an endpoint, rather than having to poll the endpoint. 
[Here's the announcement for an overview](http://blog.iron.io/2013/01/ironmq-push-queues-reliable-message.html). 

### Update a Message Queue

```java
String subscriberUrl1 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_1";
Subscriber subscriber = new Subscriber();
subscriber.url = subscriberUrl1;
subscriberArrayList.add(subscriber);

int retries = 3;
int retriesDelay = 3;
queue.updateQueue(subscriberArrayList, "multicast", retries, retriesDelay);
```

**The following parameters are all related to Push Queues:**

* `subscribers`: An array of subscriber hashes containing a вЂњurlвЂќ field.
This set of subscribers will replace the existing subscribers.
To add or remove subscribers, see the add subscribers endpoint or the remove subscribers endpoint.
See below for example json.
* `push_type`: Either `multicast` to push to all subscribers or `unicast` to push to one and only one subscriber. Default is `multicast`.
* `retries`: How many times to retry on failure. Default is 3. Maximum is 100.
* `retries_delay`: Delay between each retry in seconds. Default is 60.

--

### Add/Remove Subscribers on a Queue

Add subscriber to Push Queue:

```java
String subscriberUrl1 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_1";
Subscriber subscriber = new Subscriber();
subscriber.url = subscriberUrl1;
subscriberArrayList.add(subscriber);

queue.addSubscribersToQueue(subscriberArrayList);

queue.removeSubscribersFromQueue(subscribersToRemove);
```

--

### Get Message Push Status

```java
String[] messages = {"test1", "test2"};
Ids ids = queue.postMessages(messages);
SubscribersInfo subscribersInfo = queue.getPushStatusForMessage(ids.getId(0));
```

Returns an array of subscribers with status.

--

### Acknowledge / Delete Message Push Status

```java
String[] messages = {"test1", "test2"};
Ids ids = queue.postMessages(messages);
SubscribersInfo subscribersInfo = queue.getPushStatusForMessage(ids.getId(0));
queue.deletePushMessageForSubscriber(ids.getId(0), subscribersInfo.getSubscribers().get(0).id);
```

--

## Further Links

* [IronMQ Overview](http://dev.iron.io/mq/)
* [IronMQ REST/HTTP API](http://dev.iron.io/mq/reference/api/)
* [Push Queues](http://dev.iron.io/mq/reference/push_queues/)
* [Other Client Libraries](http://dev.iron.io/mq/libraries/)
* [Live Chat, Support & Fun](http://get.iron.io/chat)

-------------
В© 2011 - 2014 Iron.io Inc. All Rights Reserved.
