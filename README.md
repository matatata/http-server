# http-server
When I was studying computer science at TU-Darmstadt there was this assignment to implement an http-server that proxies requests and caches responses (maybe in 2003 or 2004). In the same year I took the http-parts and turned it into a simple http-server with no caching functionality. Over a decade later I now put this to github (after having mevenized it).


Build it with `mvn clean install`

Unnzip the zip-archive http-server-*-dist.zip in target/ and unpack it to a destination of your choice. In that directory run it with


`java -jar http-server-*.jar 8080`

Then visit [http://localhost:8080/index.html](http://localhost:8080/index.html)

