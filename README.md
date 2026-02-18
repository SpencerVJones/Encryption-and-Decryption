<div align="center">
  <h2 align="center">Encryption &amp; Decryption</h2>
  <div align="left">

![Repo Views](https://visitor-badge.laobi.icu/badge?page_id=SpencerVJones/Encryption-and-Decryption)
</div>

  <p align="center">
    A Java desktop app that encrypts and decrypts text with multiple algorithms through a modern Swing interface.
    <br />
    <br />
    <a href="https://github.com/SpencerVJones/Encryption-and-Decryption/issues">Report Bug</a>
    ·
    <a href="https://github.com/SpencerVJones/Encryption-and-Decryption/issues">Request Feature</a>
  </p>
</div>


<!-- PROJECT SHIELDS -->
<div align="center">

![License](https://img.shields.io/github/license/SpencerVJones/Encryption-and-Decryption?style=for-the-badge)
![Contributors](https://img.shields.io/github/contributors/SpencerVJones/Encryption-and-Decryption?style=for-the-badge)
![Forks](https://img.shields.io/github/forks/SpencerVJones/Encryption-and-Decryption?style=for-the-badge)
![Stargazers](https://img.shields.io/github/stars/SpencerVJones/Encryption-and-Decryption?style=for-the-badge)
![Issues](https://img.shields.io/github/issues/SpencerVJones/Encryption-and-Decryption?style=for-the-badge)
![Last Commit](https://img.shields.io/github/last-commit/SpencerVJones/Encryption-and-Decryption?style=for-the-badge)
![Repo Size](https://img.shields.io/github/repo-size/SpencerVJones/Encryption-and-Decryption?style=for-the-badge)
![Platform](https://img.shields.io/badge/platform-Desktop-lightgrey.svg?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-blue.svg?style=for-the-badge)
![Swing](https://img.shields.io/badge/Swing-GUI-0ea5e9.svg?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36.svg?style=for-the-badge)
![JUnit](https://img.shields.io/badge/JUnit-Testing-25A162.svg?style=for-the-badge)
![Checkstyle](https://img.shields.io/badge/Checkstyle-Linting-f59e0b.svg?style=for-the-badge)
![Encryption](https://img.shields.io/badge/Encryption-AES%20%7C%20DES%20%7C%20RSA%20%7C%20Caesar-1d4ed8.svg?style=for-the-badge)
</div>


## 📑 Table of Contents
- [Overview](#overview)
- [Technologies Used](#technologies-used)
- [Dataset](#dataset)
- [Features](#features)
- [Demo](#demo)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [How to Use](#how-to-use)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
	- [Contributors](#contributors)
- [License](#license)
- [Contact](#contact)

## Overview
**Encryption-and-Decryption** is a Java Swing application for encrypting and decrypting text. It supports multiple algorithms and provides a clean desktop UI for quick demos and experimentation.

Users can:
- Select an algorithm
- Generate encryption keys
- Encrypt/decrypt text
- Copy output instantly
- Toggle dark/light theme

## Technologies Used
- Java
- Swing (Desktop GUI)
- Java Cryptography APIs (`javax.crypto`, `java.security`)
- Maven
- JUnit 4
- Checkstyle

## Dataset
This project does not use an external dataset.

For Caesar and substitution-style operations, it works directly with printable ASCII characters and user-provided text.

## Features
- 🔐 Encrypt/decrypt text using AES, DES, RSA, and Caesar
- 🎛 Algorithm dropdown for fast switching
- 🔑 One-click key generation
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
├── checkstyle.xml
├── .github/
│   └── workflows/
│       └── ci.yml
└── src/
    ├── main/
    │   └── java/
    │       ├── Main.java
    │       └── MyFrame.java
    └── test/
        └── java/
            ├── ButtonActionsTests.java
            ├── FunctionalityTests.java
            └── GuiRenderingTests.java
```

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
git clone https://github.com/SpencerVJones/Encryption-and-Decryption.git
```
2. Move into the project:
```bash
cd Encryption-and-Decryption
```
3. Build:
```bash
mvn clean compile
```

###  How to Use
- Launch the desktop app:
```bash
java -cp target/classes Main
```
- Click `Generate Key`
- Enter text in the input box
- Click `Encrypt` or `Decrypt`
- Use `Copy` to copy results

## Usage
Use this app to demonstrate core encryption/decryption concepts in a visual desktop interface, including key generation workflows and algorithm comparison.

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
<a href="https://github.com/SpencerVJones/Encryption-and-Decryption/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=SpencerVJones/Encryption-and-Decryption"/>
</a>


## License
Distributed under the MIT License. See LICENSE for more information.


## Contact
Spencer Jones
📧 [SpencerVJones@outlook.com](mailto:SpencerVJones@outlook.com)  
🔗 [GitHub Profile](https://github.com/SpencerVJones)  
🔗 [Project Repository](https://github.com/SpencerVJones/Encryption-and-Decryption)
