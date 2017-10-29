# daemon-lib
A shared library used by any application, but primarily service daemons.

## Controller
The controller manages the lifecycle and acts as facade between services, allowing services to implement interfaces, with other services using the interface to interact with a service. Services consist of starting and stopping, with the ability to wait on the controller for the overall lifecycle state to transition - allowing for services to wait for other services to start up.

A program's main thread can efficiently wait for the lifecycle of the controller to stop, before e.g. exiting.


## Rest Service Handlers
A REST service handler easily allows REST requests to be served, with support for JSON, but this can support any HTTP interaction.

By implementing the ``RestServiceHandler`` interface, you can attach the rest handler to a controller instance through ``RestService.addRestHandlerToControllerRuntime``. Alternatively your services can implement this interface and be added to your controller. These services can then all be hooked by invoking ``RestService.attachControllerRestHandlerServices`` with the current controller instance.
