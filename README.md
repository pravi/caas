<p align="center"><img width=12.5% src="https://github.com/iNPUTmice/caas/blob/master/caas-web/src/main/resources/public/icons/android-chrome-192x192.png"></p>
<h1 align="center">XMPP Compliance Tester</h1>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

[![Build Status](https://travis-ci.org/iNPUTmice/caas.svg?branch=master)](https://travis-ci.org/iNPUTmice/caas)

This is a web service for checking and visualising compliance status of XMPP servers, made as a part of [Google Summer of Code 2018](https://summerofcode.withgoogle.com/projects/#5341326460059648) for Conversations.im by Rishi Raj.
## Why compliance?

XMPP is an extensible and living standard. Requirments shift over time and thus new extensions (called XEPs) get developed. While server implementors usually react quite fast and are able to cater to those needs it's the server operators who don't upgrade to the latest version or don't enable certain features.

Picking the right extensions to implement or enable isn't always easy. For this reason the XSF has published [XEP-0387 XMPP Compliance Suites 2018](https://xmpp.org/extensions/xep-0387.html) listing the most important extensions to date.

This app won't just help you to assess if your server supports those compliance profiles, but also give you some instructions on how to implement the profiles which are currently not supported (if you are using popular server softwares) 

## How to use
You can use the live version on [compliance.conversations.im](https://compliance.conversations.im).

Alternatively, you can run the project in these ways:-
* as a command line tool to quickly check the compliance of servers locally 
* as a full-fledged web service

## Build instructions

You need to have Java 10+ and [maven](https://maven.apache.org/)

To build both the command line tool and web service, simply run
```
mvn package
```
To build only the command line tool you can use
```
mvn package -pl caas-annotations,caas-app
```

### Run server
```
java -jar caas-web/target/caas-web.jar
```

### Run command line tool
```
java -jar caas-app/target/caas-app.jar [jid] [password]
```

## Contributing
Read [CONTRIBUTING.md](CONTRIBUTING.md)
