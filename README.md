# Claims
[![Build Status](https://img.shields.io/github/actions/workflow/status/Grabsky/Claims/gradle.yml?style=for-the-badge&logo=github&logoColor=white&label=%20)]()
[![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/Grabsky/Claims/main?style=for-the-badge&logo=codefactor&logoColor=white&label=%20)]()

Modern and clean, but opinionated claims plugin based on **[WorldGuard](https://github.com/EngineHub/WorldGuard)** for **[PaperMC/Paper](https://github.com/PaperMC/Paper)** servers. Maintained for use on servers I develop for and no public release is planned as of now.

<br />

> [!IMPORTANT]
> Default configuration and translations are in Polish language, making the plugin not suitable for external use. This is something I may work on in the future, but no promises. Contributions are always welcome.

<br />

## Requirements
Requires **Java 21** (or higher) and **Paper 1.21.3** (or higher).

<br />

## Building
Some dependencies use **[GitHub Gradle Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)** and thus may require extra configuration steps for the project to build properly.

```shell
# Cloning the repository.
$ git clone https://github.com/Grabsky/Claims.git
# Entering the cloned repository.
$ cd ./Claims
# Compiling and building artifacts.
$ gradlew shadowJar
```