<div align="center">
  <h2 align="center">Encryption &amp; Decryption</h2>
  <div align="left">

![Repo Views](https://visitor-badge.laobi.icu/badge?page_id=makesspence/Encryption-and-Decryption)
</div>

  <p align="center">
    A Java project with a modern Swing desktop app and a deploy-ready web API for encryption/decryption with multiple algorithms.
    <br />
    <br />
    <a href="https://github.com/makesspence/Encryption-and-Decryption/issues">Report Bug</a>
    ·
    <a href="https://github.com/makesspence/Encryption-and-Decryption/issues">Request Feature</a>
  </p>
</div>


<!-- PROJECT SHIELDS -->
<div align="center">

![License](https://img.shields.io/github/license/makesspence/Encryption-and-Decryption?style=for-the-badge)
![Contributors](https://img.shields.io/github/contributors/makesspence/Encryption-and-Decryption?style=for-the-badge)
![Forks](https://img.shields.io/github/forks/makesspence/Encryption-and-Decryption?style=for-the-badge)
![Stargazers](https://img.shields.io/github/stars/makesspence/Encryption-and-Decryption?style=for-the-badge)
![Issues](https://img.shields.io/github/issues/makesspence/Encryption-and-Decryption?style=for-the-badge)
![Last Commit](https://img.shields.io/github/last-commit/makesspence/Encryption-and-Decryption?style=for-the-badge)
![Repo Size](https://img.shields.io/github/repo-size/makesspence/Encryption-and-Decryption?style=for-the-badge)
![Platform](https://img.shields.io/badge/platform-Desktop-lightgrey.svg?style=for-the-badge)
![API](https://img.shields.io/badge/API-HTTP-0ea5e9.svg?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-blue.svg?style=for-the-badge)
![Swing](https://img.shields.io/badge/Swing-GUI-0ea5e9.svg?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36.svg?style=for-the-badge)
![JUnit](https://img.shields.io/badge/JUnit-Testing-25A162.svg?style=for-the-badge)
![Encryption](https://img.shields.io/badge/Encryption-AES%20%7C%20DES%20%7C%20RSA%20%7C%20Caesar-1d4ed8.svg?style=for-the-badge)
</div>


## 📑 Table of Contents
- [Overview](#overview)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Demo](#demo)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [API Examples](#api-examples)
- [Deploy to Render](#deploy-to-render)
- [Testing](#testing)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Run Desktop App](#run-desktop-app)
  - [Run API Locally](#run-api-locally)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
  - [Contributors](#contributors)
- [License](#license)
- [Contact](#contact)

## Overview
**Encryption-and-Decryption** includes:
- A Java Swing desktop UI for interactive demos
- A tiny JSON web API for deployment (Render-ready)

Users can:
- Select an algorithm
- Generate encryption keys
- Encrypt/decrypt text via UI or HTTP API
- Copy output instantly
- Toggle dark/light theme

## Technologies Used
- Java
- Swing (Desktop GUI)
- Java `HttpServer` (Web API)
- Java Cryptography APIs (`javax.crypto`, `java.security`)
- Maven
- JUnit 4
- Checkstyle

## Features
- 🔐 Encrypt/decrypt text using AES, DES, RSA, and Caesar
- 🎛 Algorithm dropdown for fast switching
- 🔑 One-click key generation
- 🌐 Tiny JSON API: `/api/key`, `/api/encrypt`, `/api/decrypt`
- ⚙️ JDK-only backend (`HttpServer` + Java crypto APIs)
- 🚀 Render deployment via `render.yaml`
- 📋 Copy output button for shareable demo results
- 🌙 Dark/light theme toggle
- ✅ Status feedback for success/error states

## Demo
Coming Soon!

## Project Structure
```bash
encryption-and-decryption/
├── README.md
├── LICENSE
├── pom.xml
├── Dockerfile
├── render.yaml
├── checkstyle.xml
├── .github/
│   └── workflows/
│       └── ci.yml
└── src/
    ├── main/
    │   └── java/
    │       ├── ApiServer.java
    │       ├── CryptoService.java
    │       ├── JsonUtil.java
    │       ├── Main.java
    │       └── MyFrame.java
    └── test/
        └── java/
            ├── ButtonActionsTests.java
            ├── CryptoServiceTests.java
            ├── FunctionalityTests.java
            └── GuiRenderingTests.java
```

## API Endpoints
- `GET /` - basic service metadata + endpoint list
- `GET /health` - health check
- `POST /api/key` - generate algorithm-specific key material
- `POST /api/encrypt` - encrypt plaintext
- `POST /api/decrypt` - decrypt ciphertext

Example request payload:
```json
{
  "algorithm": "AES",
  "text": "Hello World",
  "key": "optional-base64-key",
  "publicKey": "optional-base64-rsa-public-key",
  "privateKey": "optional-base64-rsa-private-key",
  "shift": 7
}
```

Quick `curl` flow:
```bash
# 1) Generate an AES key
curl -X POST http://localhost:8080/api/key \
  -H "Content-Type: application/json" \
  -d '{"algorithm":"AES"}'

# 2) Encrypt with AES key (replace KEY_VALUE)
curl -X POST http://localhost:8080/api/encrypt \
  -H "Content-Type: application/json" \
  -d '{"algorithm":"AES","text":"Hello API","key":"KEY_VALUE"}'
```

## API Examples
AES round-trip:
```bash
# Generate key
curl -s -X POST http://localhost:8080/api/key \
  -H "Content-Type: application/json" \
  -d '{"algorithm":"AES"}'
# => {"success":true,"algorithm":"AES","key":"..."}

# Encrypt
curl -s -X POST http://localhost:8080/api/encrypt \
  -H "Content-Type: application/json" \
  -d '{"algorithm":"AES","text":"portfolio-ready","key":"<AES_KEY_BASE64>"}'
# => {"success":true,"algorithm":"AES","result":"<IV_BASE64>:<CIPHERTEXT_BASE64>","key":"..."}

# Decrypt
curl -s -X POST http://localhost:8080/api/decrypt \
  -H "Content-Type: application/json" \
  -d '{"algorithm":"AES","text":"<IV_BASE64>:<CIPHERTEXT_BASE64>","key":"<AES_KEY_BASE64>"}'
# => {"success":true,"algorithm":"AES","result":"portfolio-ready"}
```

RSA note:
- If `publicKey` is omitted on `/api/encrypt`, the API generates a new RSA key pair and returns both keys.
- `/api/decrypt` requires `privateKey` for RSA payloads.

## Deploy to Render
This repo includes a Render Blueprint file (`render.yaml`).

1. Push this project to GitHub.
2. In Render, choose `New +` -> `Blueprint`.
3. Select the repo and deploy.
4. Render builds and runs from `Dockerfile` (`runtime: docker` in `render.yaml`).
5. Verify deployment at:
```text
https://<your-render-service>.onrender.com/health
```

Local Docker run (optional):
```bash
docker build -t encryption-api .
docker run --rm -p 8080:10000 -e PORT=10000 encryption-api
```

Environment:
- Render injects `PORT`; locally the API defaults to `8080`.

## Testing
Automated tests are included for:
- GUI rendering
- Button actions
- Core encryption/decryption flows

Run tests:
```bash
mvn test
```

## Getting Started
### Prerequisites
- JDK 21
- Maven 3.9+
- macOS, Linux, or Windows with Java GUI support

### Installation
1. Clone the repo:
```bash
git clone https://github.com/makesspence/Encryption-and-Decryption.git
```
2. Move into the project:
```bash
cd Encryption-and-Decryption
```
3. Build:
```bash
mvn clean compile
```

### Run Desktop App
Build and launch:
```bash
mvn clean compile
java -cp target/classes Main
```
- Click `Generate Key`
- Enter text in the input box
- Click `Encrypt` or `Decrypt`
- Use `Copy` to copy results

### Run API Locally
Build a runnable jar and start the API:
```bash
mvn clean package -DskipTests
java -jar target/EncrptionProgram-1.0-SNAPSHOT.jar
```
Then test:
```bash
curl http://localhost:8080/health
```

## Usage
Use this project for:
- Desktop demonstrations of encryption workflows
- Backend/API demos with deployable crypto endpoints
- Portfolio projects showing full-stack delivery (local app + cloud API)

## Roadmap
- [ ] Add file encryption/decryption support
- [ ] Add key export/import options
- [ ] Add persistence for recent operations
- [ ] Expand automated test coverage
- [ ] Package native installers for macOS/Windows/Linux

See open issues for a full list of proposed features (and known issues).

## Contributing
Contributions are welcome! Feel free to submit issues or pull requests with bug fixes, improvements, or new features.
- Fork the Project
- Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
- Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
- Push to the Branch (`git push origin feature/AmazingFeature`)
- Open a Pull Request

### Contributors
<a href="https://github.com/makesspence/Encryption-and-Decryption/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=makesspence/Encryption-and-Decryption"/>
</a>


## License
Distributed under the MIT License. See LICENSE for more information.


## Contact
Spencer Jones
📧 [jonesspencer99@icloud.com](mailto:jonesspencer99@icloud.com)  
🔗 [GitHub Profile](https://github.com/makesspence)  
🔗 [Project Repository](https://github.com/makesspence/Encryption-and-Decryption)
