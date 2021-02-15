This project is all about the reproducing the issue connection getting closed unexpectedly on client side

Follow below steps in order to reproduce the issue
 1. Run the StartHttpServer which should start the http server on 8080 port
 2. Run the Client class which should hit the server 1000 times to get the content .
 
 After ~600 requests the logs will show up with errors of connection getting closed on 
 Increasing the max-retries to default (5) which seems solve this issue but this should not be the case as 
 the server is just serving the cached content.
 Expectation is setting max-retries to 1 and all the responses should be success and without error.