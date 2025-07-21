# UninaSwap Project

UninaSwap is an educational project developed for a computer science degree. It is structured as a multi-module Maven project consisting of three main components: server, client, and common. The client module utilizes JavaFX for the user interface.

## Project Structure

```
UninaSwap
├── client
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── uninaswap
│           │           └── client
│           │               ├── controller
│           │               │   ├── ImageCropperController.java
│           │               │   ├── LoginController.java
│           │               │   ├── MainController.java
│           │               │   ├── ProfileController.java
│           │               │   └── RegisterController.java
│           │               ├── service
│           │               │   ├── AuthenticationService.java
│           │               │   ├── ImageService.java
│           │               │   ├── LocaleService.java
│           │               │   ├── NavigationService.java
│           │               │   ├── ProfileService.java
│           │               │   ├── UserSessionService.java
│           │               │   └── ValidationService.java
│           │               ├── UninaSwapApp.java
│           │               ├── util
│           │               │   └── WebSocketManager.java
│           │               └── websocket
│           │                   └── WebSocketClient.java
│           └── resources
│               ├── css
│               │   ├── cropper.css
│               │   └── styles.css
│               ├── fxml
│               │   ├── ImageCropperView.fxml
│               │   ├── LoginView.fxml
│               │   ├── MainView.fxml
│               │   ├── ProfileView.fxml
│               │   └── RegisterView.fxml
│               └── i18n
│                   └── messages.properties
├── common
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── uninaswap
│                       └── common
│                           ├── message
│                           │   ├── AuthMessage.java
│                           │   ├── ImageMessage.java
│                           │   ├── Message.java
│                           │   └── ProfileUpdateMessage.java
│                           └── model
│                               └── User.java
├── pom.xml
├── README.md
└── server
    ├── pom.xml
    └── src
        └── main
            ├── java
            │   └── com
            │       └── uninaswap
            │           └── server
            │               ├── config
            │               │   └── WebSocketConfig.java
            │               ├── controller
            │               │   └── ImageController.java
            │               ├── entity
            │               │   └── UserEntity.java
            │               ├── repository
            │               │   └── UserRepository.java
            │               ├── service
            │               │   ├── AuthService.java
            │               │   └── ImageService.java
            │               ├── UninaSwapServerApplication.java
            │               └── websocket
            │                   ├── AuthWebSocketHandler.java
            │                   ├── ImageWebSocketHandler.java
            │                   ├── ProfileWebSocketHandler.java
            │                   └── WebSocketMessageRouter.java
            └── resources
                └── application.properties
```
To update project structure run:
```
tree -I "test|target|uploads" .
```
## Modules

### Common Module
The common module contains shared classes, interfaces, and utilities that are used by both the server and client modules. It ensures code reusability and consistency across the project.

### Server Module
The server module is responsible for handling backend logic, including data processing, business logic, and communication with databases or external services. It includes controllers, services, and data access objects.

### Client Module
The client module provides the user interface for the application using JavaFX. It includes UI components, controllers, and resources such as FXML files for layout design.

## Setup Instructions

1. **Clone the Repository**
   ```
   git clone <repository-url>
   cd UninaSwap
   ```

2. **Build the Project**
   Use Maven to build the project and install dependencies:
   ```
   mvn clean install
   ```

3. **Run the Server**
   Navigate to the server module and run the application:
   ```
   cd server
   mvn spring-boot:run
   ```

4. **Run the Client**
   Navigate to the client module and run the application:
   ```
   cd client
   mvn javafx:run
   ```

## Usage Guidelines

- Ensure that the server is running before starting the client.
- Follow the documentation in each module for specific usage instructions and API details.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.



## Entity Diagrams

```
    +----------------+
    |    Listing     |
    |----------------|
    | id             |
    | title          |
    | description    |
    | createdAt      |
    | updatedAt      |
    | creator        |
    | status         |
    | imagePath      |
    | featured       |
    +-------+--------+
            ^
            |
  +---------+---------+-------------+
  |                   |             |
+-+---------+  +------+-----+  +----+-------+
|SellListing|  |TradeListing|  |GiftListing |
|-----------|  |------------|  |------------|
|price      |  |desiredItems|  |pickupOnly  |
|currency   |  |acceptOther |  |restrictions|
+-----------+  +------------+  +------------+
                                     |
                              +------+-------+
                              |AuctionListing|
                              |--------------|
                              |startingPrice |
                              |reservePrice  |
                              |currency      |
                              |endTime       |
                              |minIncrement  |
                              |currentBid    |
                              |highestBidder |
                              +--------------+
```

