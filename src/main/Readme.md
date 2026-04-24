# SMART CAMPUS SENSOR & ROOM MANAGEMENT API

## A fully RESTful JAX-RS (Jersey) API for managing university campus room and Iot Sensors. 

---
**Module Name & Code:** COSC022W – Client-Server Architectures, University of Westminster.

**Student Name:** Milsha Nikini Mihisarnai

**UoW ID:** W2121916

**IIT ID:** 20232393

---

**Table of Content**

1. Project Structure 
2. Analysis Report 

---
**1.Project Overview**

The Smart Sensor & Room Management API 
This project develops a fully RESTful web service for the module Client Server Architectures, simulating the backend infrastructure of a university smart campus system. It manages three interconnected sub-entities, including Rooms, Sensors, and Sensor Readings, and, through a versioned API rooted at /api/v1, built with JAX-RS, Jersey 2.39.1, and embedded Grizzly HTTP servers, all state is held in thread–safe in–memory structures. 
The work progresses across five stages, including Room and sensor life cycle management, which is implemented with deliberate referential integrity constraints that prevent, for instance, the deletion of rooms that still contain active sensors. Sensor readings are handled through the sub-resource locator pattern, with each new reading automatically updating the parent sensor’s current value. The final stage introduces structured error handling via custom exceptions and ExceptionMapper providers, ensuring consistent JSON error responses, complemented by a logging filter that observes every HTTP transaction without modifying any resource code. 
Throughout this coursework, the design adheres to established RESTful conventions, including proper HTTP semantics, meaningful status codes, and HATEOAS-driven resources discovery. 

---

**2.Analysis Report** 

1.1 Question: 

Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions

Answer: 

As introduced in the Tutorial Week 7, a JAX -RS application is configured via a class that extends javax.ws.rs.core.Application is annotated with @ApplicationPath. Resource classes are registered inside the getClassses() method. By default, we know that the JAX-RS runtime creates a brand-new instance of each resource class for every incoming HTTP request (Pre-request scope). Once the response is sent, the instance is discarded. 
This simply means that the instance variables on resource classes are entirely transient, any data stored as an instance field is lost after each request. In Tutorial 8, we learned that data must live in a centralized store (MockDatabase) completely separate from resource classes.
In this project, all shared mutable state lives in the DataStore file, which is created once at JVM startup and shared across all requests. This directly mirrors the 3-Tier Architecture discussed in the Tutorial Week 2, where the data Tier is completely separated from the Business Logic tier. 
Because multiple HTTP requests arrive concurrently, each on its own thread or link, as mentioned in the Tutorial Week 2, multi-threading part, the DataSource must be thread-safe.  A plain HashMap risks data corruption when two threads write or run simultaneously. In this coursework, ConcurrentHashMap is used instead, which achieves thread safety through fine-grained interlocking without requiring explicit synchronized blocks on every method.

----
1.2 Question: 

Why is the provision of” Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? 
How does this approach benefit client developers compared to static documentation?

Answer: 

Here we know HATEOAS Hypermedia as the engineer of the application states, because it is the highest maturity level of REST design, and responses include not just data but hyperlinks, describing what the client can do in the next step. 
In the above-mentioned JAX-RS resource paths via @Path annotations, HATEOAS extends this by making the API self-describing at runtime. Rather than a client hardcoding every URL form response body, this is exactly like a user navigating a website by following links. 
In this smart-campus-api project, GET/api/v1 returns links to api/v1/rooms and api/v1/sensors. As mentioned before, a client starting from this one entry point can navigate the entire API. 
As benefits over static documentation, 
1.	Discoverability: When it comes to a single-entry point that reveals the Full API Structure dynamically, it makes it useful for work. 
2.	Decoupling: If a URL changes server-side, clients following links or links used by clients will be adapted automatically. 
3.	Version resilience: New resource collections appear in the discovery responded without breaking existing clients. 
4.	Reduced the knowledge of clients: The server drives thew e workflow, reducing tight coupling between client and the server. 

----

2.1 Question:

When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing

Answer: 

When we considered Tutorial Week 7 and 8, what data to expose in the GET Response, 
Returning only IDs produces a lightweight payload, but it forces the client to make  N additional Get/rooms/{roomId} calls, because of the  N+1 problem. When it comes to a slow network, this type becomes prohibitively expensive. 
But returning a full object, our approach delivers all necessary data at once in one request.  A lightweight model with only the essential fields. Storing only sensorId or roomId keeps response size manageable while still being useful. 
But at production scale, pagination and sparse fieldsets would be appropriate.  Since this smart-campus-api project consists of a considerable small amount of rooms and sensors, returning full objects is the practical and performant choice. 

----

2.2 Question: 

Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple time.

Answer:

In the implementation, DELETE is idempotent at the start  at the state level but  not at the response level: 
Because first DELETE on an existing empty room, the room is removed and returns 200 OK. 
Second DELETE on the same Id, room no longer exists, ResourceNotFoundException is thrown and mapped to 404 NOT FOUND by the exception mapper, which we discussed in the Tutorial Week 9. 
The server state is idempotent, after any number of DELETE requests are made, the room is absent, and there is no existing room. HTTP RFC 7231 clearly defines idempotency is terms of server-state effects, not response codes, so according to the tutorials, returning 404 on a repeated delete is technically practical and more informative. 
In the Tutorial Week 9, the Exception mapper pattern enables this clean behavior of responding. Without it, a missing resource would produce a considerably large error or a blank response. Instead of that, in this case, ResourceNotFoundExceptionMapper interprets the exception, and it returns a structured JSON 404, which is more productive than displaying a blank output or a raw 500 error. 

----

3.1 Question:

We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

Answer:

As we discussed in the Tutorial Week 7, @Consumes declares the acceptable media type for a resource method and @Produces declares what it returns. These annotations are part of JAX-RS content. 
When a client sends a mismatched data formats like, plain or text types, the jersey runtime checks the header before invoking any resource method. Finding does not match with @Consumes(MediaType.APPLICATION_JSON and jersey immediately returns HTTP 415 Unsupported Media Type. The request body is never read, and the resource code is never executed. 
Can identify this as a key benefit of JAX-RS, because its declarative annotation model, discussed in the Tutorial Week 7, ensures that content-type enforcement happens at the framework level, not in business logic. Finally we can assure that @Produces(MediaType.APPLICATION_JSON handles JSON serialization  automatically while @Consumes is responsible for inbound equivalent protecting resource methods from unexpected input formats.

----

3.2 Question: 

You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Answer:

In the Tutorial Week 7, @PathParam is for extracting identifiers from URL path segments, the right tool when a segment identifies a specific unique resource. @QueryParam is so different from the previous one ant mainly it modifies or filter a collection view. 
In the @QueryParam, 
Semantic correctness – Path segments identify resources. /sensors/type/CO2 incorrectly implies CO2 is a discrete resource within the type.  Filtering is a view of modification, it s not a resource identifier. For optional filtering @QueryParam is a semantically correct choice. 
Optionality – Considering a single method with @QueryParam(“type”) handles both GET/sensors (all sensors) and GET/sensors?Type=CO2 (filtered), and with path parameters, two separate @GET methods would be needed, duplicating the logic. 
Composability: Multiple filters combine cleanly, such as sensors/type=CO2&status=ACTIVE , this is path based filtering, which produces increasingly complex and brittle URL patterns. 
In the Tutorial 8, in the GenericDAO, discovered that, getAll() returned everything and filtering was applied programmatically. Which means, same in @QueryParam – one endpoint, optional filtering systems, or type applied inside the methods in this coursework. 

----

4.1 Question : 

Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

Answer: 

The Sub-Locator is a JAX-RS feature where a method annotated only with @Path and not HTTP verb annotation, which returns an instance of another class that handles the remaining path segments. 
In this coursework, SensorReource.getReadingResource(sensorId) returns a SensorReadingResource, that handles all operations under /sensor/{sensorId}/readings. 
And this can be directly mapped to the class pattern discussed in the Tutorial 7, where TeacherResource, StudentResource and ModuleResouce were each separated focused classes. By enabling nested paths to have their own dedicated handle classes takes this further. 
As benefits of using sub-locators,
Signle responsibility – SensorResouce handles the CRUD of sensor and SensorReadingResource hadles reading history of the class. 
Manageable class size: Putting every nested path in one controller would produce a massive unreadable class, but using sub locators keep each level small, reachable and focused.
Contextual Encapsulation – SensorReadingResource receives sensorId via its constructor. All methods automatically operate in the correct context.
Testability- One of the most important benefits that each sub locator class can be executed and tested in isolation. 
Dynamic dispatch- the locator method runs real java code, so it ca return different implementation based on runtime conditions. 
Considering the above benefits and uses, we understand that using sub locators is beneficial rather than using every nested path in one logic. 

----

5.2 Question:

Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload? 

Answer:
In the Tutorial 9, the principle was introduced that error response must be meaningful and guide developers to correct the fix, its not just a single that something went wrong, but choosing the right HTTP status code is important. 
HTTP 404 Not Found means the resource identified by the request URL itself that it doesn’t exist. 
ex: GET/api/v1/rooms/FAKE-222  404 Not Found because the URL path refers to something that doesn’t exist or is missing. 
HTTP 422 Processable Entity means the URL is valid, HTTP methods is correct, and the JSON is syntactically well formed, but the semantic content of the payload cannot be processed. When a client POSTs to /api/sensors with {“roomId”: “FAKE-222”}, the URL is valid, and the JSON parses correctly. But the roomId references a non-existent room, and the meaning of the given data is broken. 
Returning 404 would mislead the client into thinking they sent request to the wrong ULR, but 422 tells them gracefully that “You reached the right endpoint, but the content is semantically invalid or wrong, fix the payload”. (Tutorial 9) 

----

5.4 Question:

From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Answer:

According to the Tutorial 9, GlobalExceptionMapper in the  project is directly addressing this. Here are some serious security issues,
Technology Fingerprinting, Stack traces reveal framework names and exact versions, those attacker’s ca n cross reference these against CVE databases and to find known exploits for those versions.  
Class structurers and internal packages, Class names and line numbers (org. smartcampus.mode.DataStroe.115) this reveals the internal architecture of the project and helping attackers to craft targeted payloads against some specific classes. 
File system paths, in some exception expose server directly paths, assign direct file inclusion attacks. 
Business logic leakage, here, method names and variable names expose how data is processed internally, potentially revealing authentication flows that attackers can use. 
Denial of service assistance, knowing which  inputs cause expensive failures, this lets attackers repeatedly trigger those conditions to crash the service. 
Here, the GlobalExcpetionMapper solves this by logging full details server-side using java.util.logging.Logger while returning only  a safe generic 500 Internal Server Error message to the client and this approach helps to keeping full diagnostic information available internally while exposing nothing to external consumers. 

----


5.5 Question:

Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method? 

Answer:

As we learned in the  Tutorial 1, java.util.logging.logger  as the standard java logging tool, and ContrainerRequestFilter and ContainerResponseFilter as the JAX-RS mechanism to  intercept all traffic in one place. In Tutorial 9, LoggingFilter automatically logged the HTTP method, URL and response status for every single request without touching any resource method. 
In this smart-campus-api, ApiLoggingFIlter implements the same pattern, Using filter is superior to manual logger.info() call for the following reasons, 
Separation of concerns, the methods should contain only business logic, not logging infrastructure.  
DRY principle, with 15+ endpoints, manually adding log statements everywhere is error-prone. A filter guarantees every request is logged without relying on developer actions or rather than manual logging. 
Consistency, a filter runs at the  same pipeline stage for every request, guaranteeing a uniform  log format. Manual logging, it produces inconsistent output across many Methods and Developers. 
Maintainability, adding a correlation Id or changing log levels requires editing one class not every resource method, so that, this method directly supports the production logging scenarios.
Extensibility, the filter pattern extends to authentication, CROS headers and rate limiting, all without modifying resource classes. 
The ApiLoggerFilter implements both ContainerRequestFilter and also the ContainerResponseFIlter in a single @Provider annotated class, and it’s clear that logging every incoming request’s method and URL and also every outgoing response’s status code, exactly matching the LoggingFilter structure demonstrated in Tutorial 9. 




