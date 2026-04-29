# SMART CAMPUS SENSOR & ROOM MANAGEMENT API

## A fully RESTful JAX-RS (Jersey) API for managing university campus room and Iot Sensors. 

---
**Module Name & Code:** 5COSC022W – Client-Server Architectures, University of Westminster.

**Student Name:** Milsha Nikini Mihisarnai

**UoW ID:** W2121916

**IIT ID:** 20232393

---

## Table of Content

1. [API & Project Overview](#api--project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [How to Build and Run](#how-to-build--run)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Test Cases - Postman](#test-cases---postman)
7. [Screenshots of URL](#screenshots-of-url)
8. [Conceptual Report](#conceptual-report)

---

## API & Project Overview

The Smart Campus API provides a versioned RESTful interface for facilities managers and building automation systems to manage:

- **Rooms** - Physical spaces on campus (libraries, labs, auditoriums)
- **Sensors** - IoT devices deployed within rooms (temperature, CO2, occupancy)
- **Sensor Readings** - Historical timestamped data logs per sensor

Base URL: `http://localhost:8080/smart-campus-api/api/v1`

This project develops a fully RESTful web service for the module Client Server Architectures, simulating the backend infrastructure of a university smart campus system. It manages three interconnected sub-entities, including Rooms, Sensors, and Sensor Readings, and, through a versioned API rooted at /api/v1, built with JAX-RS, Jersey 2.39.1,  deployed on Apache Tomcat 9.x as a WAR file, with all state held in thread-safe in-memory ConcurrentHashMap structures.

The work progresses across five stages, including Room and sensor life cycle management, which is implemented with deliberate referential integrity constraints that prevent, for instance, the deletion of rooms that still contain active sensors. Sensor readings are handled through the sub-resource locator pattern, with each new reading automatically updating the parent sensor’s current value. The final stage introduces structured error handling via custom exceptions and ExceptionMapper providers, ensuring consistent JSON error responses, complemented by a logging filter that observes every HTTP transaction without modifying any resource code. 
Throughout this coursework, the design adheres to established RESTful conventions, including proper HTTP semantics, meaningful status codes, and HATEOAS-driven resources discovery. 

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 11+ |
| JAX-RS Implementation | Jersey 2.39.1 |
| HTTP Server | Tomcat 9.0+ (Servlet Container) |
| Dependancy | javax. servlet.api |
| JSON Serialization | Jackson (jersey-media-json-jackson) |
| Build Tool | Apache Maven 3.x |
| Data Storage | In-memory ConcurrentHashMap (no database) |

---

## Project Structure

```
smart-campus-api/
├── pom.xml                                        # Maven configuration (updated to <packaging>war</packaging>) [cite: 24]
└── src/
    └── main/
        ├── java/org/smartcampus/
        │   ├── application/
        │   │   └── SmartCampusApplication.java    # @ApplicationPath("/api/v1") bootstrap [cite: 20]
        │   ├── model/
        │   │   ├── Room.java                      # Room POJO [cite: 7]
        │   │   ├── Sensor.java                    # Sensor POJO [cite: 6]
        │   │   ├── SensorReading.java             # SensorReading POJO [cite: 5]
        │   │   └── DataStore.java                 # Thread-safe singleton [cite: 8]
        │   ├── resource/
        │   │   ├── DiscoveryResource.java         # GET /api/v1 — HATEOAS discovery (updated with /smart-campus-api context) [cite: 4]
        │   │   ├── RoomResource.java              # /api/v1/rooms management [cite: 3]
        │   │   ├── SensorResource.java            # /api/v1/sensors with @QueryParam filtering [cite: 1]
        │   │   └── SensorReadingResource.java     # Sub-resource for sensor data [cite: 2]
        │   ├── exception/
        │   │   ├── ErrorResponse.java             # JSON error body [cite: 19]
        │   │   ├── RoomNotEmptyException.java     # Custom exception [cite: 13]
        │   │   ├── LinkedResourceNotFoundException.java   # Custom exception (Tutorial Week 09)
        │   │   ├── SensorUnavailableException.java        # Custom exception (Tutorial Week 09)
        │   │   ├── ResourceNotFoundException.java         # Custom exception (Tutorial Week 09)
        │   │   ├── RoomNotEmptyExceptionMapper.java       # @Provider — 409 (Tutorial Week 09 — ExceptionMapper)
        │   │   ├── LinkedResourceNotFoundExceptionMapper.java  # @Provider — 422
        │   │   ├── SensorUnavailableExceptionMapper.java  # @Provider — 403
        │   │   ├── ResourceNotFoundExceptionMapper.java   # @Provider — 404
        │   │   ├── GlobalExceptionMapper.java     # @Provider — 500 catch-all [cite: 18]
        │   └── filter/
        │       └── ApiLoggingFilter.java          # ContainerRequest/Response logging filter [cite: 9]
        └── webapp/                                # New: Root for web application resources 
            ├── WEB-INF/
            │   └── web.xml                        # New: Servlet deployment descriptor for Tomcat 
            └── index.html                         # New: Professional API Gateway landing page
```



---

## How to Build & Run

### Prerequisites

- Java JDK 11 or higher (`java -version`)
- Apache Maven 3.6+ (`mvn -version`)

### Step 1 — Clone the repository

```bash
git clone https://github.com/Nikinii3/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the executable JAR

```bash
mvn clean package
```

This produces a WAR file at target/smart-campus-api-1.0.0.war ready for deployment on Apache Tomcat.

### Step 3 — Start the server

```bash
Right-click project in NetBeans → Run
Or deploy the generated WAR file to Tomcat manually:
Copy target/smart-campus-api-1.0.0.war to Tomcat's webapps/ folder
Start Tomcat and navigate to http://localhost:8080/smart-campus-api/api/v1
```

> **Note on Tomcat Context Path:** When deploying to Apache Tomcat, the
> project name is automatically used as the context path. This means all
> URLs are prefixed with /smart-campus-api/ before /api/v1.
> If you rename the project or WAR file, update the URLs accordingly.

### Step 4 — Test the API

The server runs on port 8080. Use the curl commands below or import the collection into Postman.

Press **ENTER** in the server terminal to stop it gracefully.


---

## API Endpoints Reference

### Part 1 — Discovery

| Method | Path | Description |
|--------|------|-------------|
| GET | `/smart-campus-api/api/v1` | API discovery — metadata, version, HATEOAS links |

### Part 2 — Rooms

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/smart-campus-api/api/v1/rooms` | List all rooms | 200 |
| POST | `/smart-campus-api/api/v1/rooms` | Create a new room | 201 |
| GET | `/smart-campus-api/api/v1/rooms/{roomId}` | Get a specific room | 200 / 404 |
| DELETE | `/smart-campus-api/api/v1/rooms/{roomId}` | Delete room (blocked if sensors exist) | 200 / 404 / 409 |

### Part 3 — Sensors

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/smart-campus-api/api/v1/sensors` | List all sensors (optional `?type=` filter) | 200 |
| POST | `/smart-campus-api/api/v1/sensors` | Register sensor (validates roomId) | 201 / 422 |
| GET | `/smart-campus-api/api/v1/sensors/{sensorId}` | Get a specific sensor | 200 / 404 |
| PUT | `/smart-campus-api/api/v1/sensors/{sensorId}` | Update sensor fields | 200 / 404 |
| DELETE | `/smart-campus-api/api/v1/sensors/{sensorId}` | Remove a sensor | 200 / 404 |

### Part 4 — Sensor Readings (Sub-Resource)

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/smart-campus-api/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor | 200 |
| POST | `/smart-campus-api/api/v1/sensors/{sensorId}/readings` | Record a new reading | 201 / 403 |
| GET | `/smart-campus-api/api/v1/sensors/{sensorId}/readings/{readingId}` | Get a specific reading | 200 / 404 |

---

## Sample curl Commands

> Pre-seeded data: rooms LIB-301, LAB-101, AUD-001 and sensors TEMP-001, CO2-001, OCC-001 (MAINTENANCE), TEMP-002.

### 1. Discover the API (Part 1)

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1 -H "Accept: application/json"
```

Expected: 200 OK with name, version, links, and capabilities.

### 2. Get all Rooms (Part 2)

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms -H "Accept: application/json"
```

Expected: 200 OK with array of 3 pre-seeded rooms.

### 3. Create a new Room (Part 2)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "CONF-005", "name": "Conference Room", "capacity": 20}'
```

Expected: 201 Created.

### 4. Delete a Room with sensors — 409 Conflict (Part 2 + Part 5.1)

```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-003
```

Expected: 409 Conflict — room still has sensors.

### 5. Create a Sensor with valid roomId (Part 3)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "currentValue": 750.0, "roomId": "AUDI-001"}'
```

Expected: 201 Created.

### 6. Create a Sensor with invalid roomId — 422 (Part 3 + Part 5.2)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "CO2-999", "type": "CO2", "status": "ACTIVE", "currentValue": 400.0, "roomId": "FAKE-ROOM"}'
```

Expected: 422 Unprocessable Entity.

### 7. Filter sensors by type (Part 3)

```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

Expected: 200 OK with only Temperature sensors.

### 8. Post a reading to an ACTIVE sensor (Part 4)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 100.02}'
```

Expected: 201 Created. Sensor currentValue updated to 23.7.

### 9. Post a reading to a MAINTENANCE sensor — 403 (Part 5.3)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 15.0}'
```

Expected: 403 Forbidden.

### 10. Verify currentValue was updated after reading (Part 4 side effect)

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001
```

Expected: 200 OK — currentValue should now be 23.7.

---

## Test Cases - Postman

<img width="2877" height="1712" alt="image" src="https://github.com/user-attachments/assets/ec754e53-c7c7-4d5f-a2ee-585732248ca0" />


---

## Screenshots of URL

<img width="2879" height="684" alt="image" src="https://github.com/user-attachments/assets/ae81957d-d7aa-4af8-9038-72716dffacda" />

<img width="2876" height="703" alt="image" src="https://github.com/user-attachments/assets/e01b0a67-581b-4360-9c1f-0ddb8eeacaec" />

<img width="2878" height="506" alt="image" src="https://github.com/user-attachments/assets/9e5b4442-3ba6-4f45-b039-90f203b2016a" />

<img width="2879" height="513" alt="image" src="https://github.com/user-attachments/assets/4093ea64-2518-4def-bb9f-e67f707de307" />

---

## Conceptual Report 

### Part 1.1 — JAX-RS Resource Lifecycle

**Question:**

Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions

**Answer**: 

As introduced in the Tutorial Week 7, a JAX -RS application is configured via a class that extends javax.ws.rs.core.Application is annotated with @ApplicationPath. Resource classes are registered inside the getClassses() method. By default, we know that the JAX-RS runtime creates a brand-new instance of each resource class for every incoming HTTP request (Pre-request scope). Once the response is sent, the instance is discarded. 
This simply means that the instance variables on resource classes are entirely transient; any data stored as an instance field is lost after each request. In Tutorial 8, we learned that data must live in a centralized store (MockDatabase) completely separate from resource classes.
In this project, all shared mutable state lives in the DataStore file, which is created once at JVM startup and shared across all requests. This directly mirrors the 3-Tier Architecture discussed in the Tutorial Week 2 where the data Tier is completely separated from the Business Logic tier. 
Because multiple HTTP requests arrive concurrently, each on its own thread or link, as mentioned in the Tutorial Week 2, multi-threading part, the DataSource must be thread-safe.  A plain HashMap risks data corruption when two threads write or run simultaneously. In this coursework, ConcurrentHashMap is used instead, which achieves thread safety through fine-grained interlocking without requiring explicit synchronized blocks on every method. 

Most importantly in this project, Apache Tomcat is used as the servlet container, Tomcat manages its own internal thread pool, assigning a thread from the pool to each incoming HTTP request. Basically, this means multiple requests can reach the data store simultaneously from different Tomcat threads and that makes concurrentHashMap essential. Unlike a plain HashMap, which is not thread-safe, ConcurrentHashMap uses fine-grained internal locking to allow concurrent reads while safely serialising writes and prevents data corruption without requiring explicit synchronized blocks on every method. 


----
### Part 1.2 — HATEOAS and Hypermedia

**Question:**

Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?

**Answer:**

Here we know HATEOAS Hypermedia as the engineer of the application states, because it is the highest maturity level of REST design, and responses include not just data but hyperlinks, describing what the client can do in the next step. 
In the above-mentioned JAX-RS resource paths via @Path annotations, HATEOAS extends this by making the API self-describing at runtime. Rather than a client hardcoding every URL form response body, this is exactly like a user navigating a website by following links. 

Note: When deployed on Apache Tomcat, all links are relative to the 
context path /smart-campus-api/, so the full URL for the rooms 
collection becomes http://localhost:8080/smart-campus-api/api/v1/rooms.
The HATEOAS links returned in responses use relative paths, making 
them portable across different deployments regardless of the 
context path.

In this smart-campus-api project, GET/api/v1 returns links to /smart-campus-api/api/v1/rooms and /smart-campus-api/api/v1/sensors. As mentioned before,
a client starting from this one entry point can navigate the entire API. 
As benefits over static documentation, 
1.	Discoverability: When it comes to a single-entry point that reveals the Full API Structure dynamically, it makes it useful for work. 
2.	Decoupling: If a URL changes server-side, clients following links or links used by clients will be adapted automatically. 
3.	Version resilience: New resource collections appear in the discovery responded without breaking existing clients. 
4.	Reduced the knowledge of clients: The server drives thew e workflow, reducing tight coupling between client and the server.

----

### Part 2.1 — Full Objects vs. IDs in List Responses

**Question:**

When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing

**Answer:**

When we considered Tutorial Week 7 and 8, what data to expose in the GET Response, 
Returning only IDs produces a lightweight payload, but it forces the client to make  N additional Get/rooms/{roomId} calls, because of the  N+1 problem. When it comes to a slow network, this type becomes prohibitively expensive. 
But returning a full object, our approach delivers all necessary data at once in one request.  A lightweight model with only the essential fields. Storing only sensorId or roomId keeps response size manageable while still being useful. 
But at production scale, pagination and sparse fieldsets would be appropriate.  Since this smart-campus-api project consists of a considerable small amount of rooms and sensors, returning full objects is the practical and performant choice. 

----

### Part 2.2 — Idempotency of DELETE

**Question:**

Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple time.

**Answer:**

In the implementation, DELETE is idempotent at the start  at the state level but  not at the response level: 

Because first DELETE on an existing empty room, the room is removed and returns 200 OK. 

Second DELETE on the same Id, room no longer exists, ResourceNotFoundException is thrown and mapped to 404 NOT FOUND by the exception mapper, which we discussed in the Tutorial Week 9. 

The server state is idempotent, after any number of DELETE requests are made, the room is absent, and there is no existing room. HTTP RFC 7231 clearly defines idempotency is terms of server-state effects, not response codes, so according to the tutorials, returning 404 on a repeated delete is technically practical and more informative. 

In the Tutorial Week 9, the Exception mapper pattern enables this clean behavior of responding. Without it, a missing resource would produce a considerably large error or a blank response. Instead of that, in this case, ResourceNotFoundExceptionMapper interprets the exception, and it returns a structured JSON 404, which is more productive than displaying a blank output or a raw 500 error. 

----

### Part 3.1 — @Consumes and Content-Type Mismatch

**Question:**

We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:**

As we discussed in the Tutorial Week 7, @Consumes declares the acceptable media type for a resource method and @Produces declares what it returns. These annotations are part of JAX-RS content.

When a client sends a mismatched data formats like, plain or text types, the jersey runtime checks the header before invoking any resource method. Finding does not match with @Consumes(MediaType.APPLICATION_JSON and jersey immediately returns HTTP 415 Unsupported Media Type. The request body is never read, and the resource code is never executed. 

Can identify this as a key benefit of JAX-RS, because its declarative annotation model, discussed in the Tutorial Week 7, ensures that content-type enforcement happens at the framework level, not in business logic. Finally we can assure that @Produces(MediaType.APPLICATION_JSON handles JSON serialization  automatically while @Consumes is responsible for inbound equivalent protecting resource methods from unexpected input formats.

----

### Part 3.2 — @QueryParam vs. @PathParam for Filtering

**Question:**

You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:**

In the Tutorial Week 7, @PathParam is for extracting identifiers from URL path segments, the right tool when a segment identifies a specific unique resource. @QueryParam is so different from the previous one ant mainly it modifies or filter a collection view. 

In the @QueryParam, 
Semantic correctness – Path segments identify resources. /sensors/type/CO2 incorrectly implies CO2 is a discrete resource within the type.  Filtering is a view of modification, it s not a resource identifier. For optional filtering @QueryParam is a semantically correct choice. 
Optionality – Considering a single method with @QueryParam(“type”) handles both GET/sensors (all sensors) and GET/sensors?Type=CO2 (filtered), and with path parameters, two separate @GET methods would be needed, duplicating the logic. 
Composability: Multiple filters combine cleanly, such as sensors/type=CO2&status=ACTIVE , this is path based filtering, which produces increasingly complex and brittle URL patterns. 
In the Tutorial 8, in the GenericDAO, discovered that, getAll() returned everything and filtering was applied programmatically. Which means, same in @QueryParam – one endpoint, optional filtering systems, or type applied inside the methods in this coursework. 

----

### Part 4.1 — Sub-Resource Locator Pattern

**Question:** 

Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:** 

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

### Part 5.2 — HTTP 422 vs. 404 for Missing References

**Question:**

Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload? 

**Answer:**

In the Tutorial 9, the principle was introduced that error response must be meaningful and guide developers to correct the fix, its not just a single that something went wrong, but choosing the right HTTP status code is important. 

HTTP 404 Not Found means the resource identified by the request URL itself that it doesn’t exist. 
ex: GET/api/v1/rooms/FAKE-222  404 Not Found because the URL path refers to something that doesn’t exist or is missing. 

HTTP 422 Processable Entity means the URL is valid, HTTP methods is correct, and the JSON is syntactically well formed, but the semantic content of the payload cannot be processed. When a client POSTs to /api/sensors with {“roomId”: “FAKE-222”}, the URL is valid, and the JSON parses correctly. But the roomId references a non-existent room, and the meaning of the given data is broken. 

Returning 404 would mislead the client into thinking they sent request to the wrong ULR, but 422 tells them gracefully that “You reached the right endpoint, but the content is semantically invalid or wrong, fix the payload”. (Tutorial 9) 

----

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

**Question:**

From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:**

According to the Tutorial 9, GlobalExceptionMapper in the  project is directly addressing this. Here are some serious security issues,
Technology Fingerprinting, Stack traces reveal framework names and exact versions, those attacker’s ca n cross reference these against CVE databases and to find known exploits for those versions.  
Class structurers and internal packages, Class names and line numbers (org. smartcampus.mode.DataStroe.115) this reveals the internal architecture of the project and helping attackers to craft targeted payloads against some specific classes. 

File system paths, in some exception expose server directly paths, assign direct file inclusion attacks. 
Business logic leakage, here, method names and variable names expose how data is processed internally, potentially revealing authentication flows that attackers can use. 

Denial of service assistance, knowing which  inputs cause expensive failures, this lets attackers repeatedly trigger those conditions to crash the service. 

Here, the GlobalExcpetionMapper solves this by logging full details server-side using java.util.logging.Logger while returning only  a safe generic 500 Internal Server Error message to the client and this approach helps to keeping full diagnostic information available internally while exposing nothing to external consumers. 

----

### Part 5.5 — Filters vs. Manual Logging

**Question:**

Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method? 

**Answer:**

As we learned in the  Tutorial 1, java.util.logging.logger  as the standard java logging tool, and ContrainerRequestFilter and ContainerResponseFilter as the JAX-RS mechanism to  intercept all traffic in one place. In Tutorial 9, LoggingFilter automatically logged the HTTP method, URL and response status for every single request without touching any resource method. 

In this smart-campus-api, ApiLoggingFIlter implements the same pattern, Using filter is superior to manual logger.info() call for the following reasons, 
Separation of concerns, the methods should contain only business logic, not logging infrastructure.  
DRY principle, with 15+ endpoints, manually adding log statements everywhere is error-prone. A filter guarantees every request is logged without relying on developer actions or rather than manual logging. 
Consistency, a filter runs at the  same pipeline stage for every request, guaranteeing a uniform  log format. Manual logging, it produces inconsistent output across many Methods and Developers. 
Maintainability, adding a correlation Id or changing log levels requires editing one class not every resource method, so that, this method directly supports the production logging scenarios.
Extensibility, the filter pattern extends to authentication, CROS headers and rate limiting, all without modifying resource classes. 

The ApiLoggerFilter implements both ContainerRequestFilter and also the ContainerResponseFIlter in a single @Provider annotated class, and it’s clear that logging every incoming request’s method and URL and also every outgoing response’s status code, exactly matching the LoggingFilter structure demonstrated in Tutorial 9. 




