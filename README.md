# Weather App Backend Challenge

## Project Overview

The purpose of this challenge was to create a simple API that will calculate
the average temperature and wind for a single city or a list of cities. 

## Challenge Requirements

- Spring Boot/Quarkus
- Reactive programming
- External API: https://goweather.herokuapp.com/weather/{city_name}
- A single endpoint accessible via an `HTTP GET` request 
at `/api/weather?city=Cluj-Napoca,Bucuresti,Craiova,Timisoara,Dej,Constanta,Baia-Mare,Arad,Bistrita,Iasi,Oradea`
- The API should return the average temperature and wind speed for each city, sorted by city name, temperature, and wind speed
```json
{
   "result": [
      {
         "name": "Arad",
         "temperature": "needs_to_be_determined",
         "wind": "needs_to_be_determined"
      },
      {
         "name": "Baia-Mare",
         "temperature": "needs_to_be_determined",
         "wind": "needs_to_be_determined"
      },
      {
         "name": "Bucuresti",
         "temperature": "needs_to_be_determined",
         "wind": "needs_to_be_determined"
      }
   ]
}
```
- The calculated average values should also be saved to a CSV file in the same order

## Technology Stack

- **Docker and Docker Compose**: Used for containerization and managing the deployment of the service.
- **Spring Boot**: Framework used for building the service, providing a robust and scalable development environment.
- **WebFlux**: Reactive programming library integrated into Spring Boot, enabling the development of non-blocking, reactive web services.
- **RestAssured**: Testing framework for API testing, ensuring the correctness of endpoints.
- **MockServer**: A tool for mocking external systems such as HTTP/HTTPS-based services.
- **OpenCSV**: A simple-to-use library for reading and writing CSV files in Java.

## Installation:

1. Clone the project from GitHub
2. Navigate to the project directory by running `cd weather-app`
3. The application can be run either by using docker (docker must be installed), or by
   using maven (maven must be installed). _Note: Port `8080` must be available on your system._
    - Using docker: run `docker compose up --build -d`
    - Using maven: run `mvn spring-boot:run`

We can now use the API by sending `GET` requests to `http://localhost:8080/api/weather?city={cities}`.