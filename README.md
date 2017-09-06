# http-server
When I was studying computer science at TU-Darmstadt there was this assignment to implement an http-server that proxies requests and caches responses (maybe in 2003 or 2004). In the same year I separated the http-parts and turned them into a simple http-server with no caching functionality. Over a decade later I now put this to github (after having mevenized it).


Build it with
`mvn clean install`

Unzip the zip-archive found in `target/http-server-*-dist.zip` and unpack it to a destination of your choice. In that directory run it with

`java -jar http-server-*.jar 8080`

You'll find a tiny interactive console:


	Will try to listen on port 8080
	HTTP/1.x Server
	
	Commands:
	quit - gracefully quits the server.
	help - displays this text.
	gc - run garbage-collector
	status - status info
	?>


Then you might want to visit [http://localhost:8080/index.html](http://localhost:8080/index.html) or whatever you put in the htdocs-directory.

