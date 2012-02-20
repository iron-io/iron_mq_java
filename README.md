IronMQ Java Client
----------------

Getting Started
===============

[Download the IronMQ jar](https://github.com/iron-io/iron_mq_java/downloads). 
Or build from source with [Apache Buildr](http://buildr.apache.org):

    buildr package

The .jar file will appear under the target directory.

The API is documented [here](http://iron-io.github.com/iron_mq_java/).

The Basics
==========
**Initialize** a client and get a queue object:

    Client client = new Client("my project", "my token");
    Queue queue = client.queue("my_queue");

**Push** a message on the queue:

    queue.Push("Hello, world!");

**Pop** a message off the queue:

    Message msg = queue.get();

When you pop/get a message from the queue, it will *not* be deleted. It will
eventually go back onto the queue after a timeout if you don't delete it. (The
default timeout is 60 seconds.)

**Delete** a message from the queue:

    queue.deleteMessage(msg);
