# S.E.R.S. Project

## Disclaimer

This system is developed exclusively for educational and academic purposes as part of a school project. It is not intended for commercial use or production deployment.

## Overview

A Java Swing school event registration system using Maven and MongoDB.

## Requirements

- JDK 21 or newer
- Maven, or NetBeans with Maven support
- MongoDB running locally at `mongodb://localhost:27017`

The application uses the `SERS_DB` database. On a fresh database, it prompts you to create the initial `ADMIN001` administrator account.

## Build and run

```powershell
mvn clean package
mvn exec:java
```

You can also build and run `javamongo1.sersproject.SERSProject` from NetBeans.

## Create the Windows app image

Build the project, then run `package-windows.ps1` in PowerShell. The script requires a JDK containing `jpackage` and downloaded Maven dependencies. If your JDK is installed in a different location, update `$jdkBin` in the script.

## Uploading to GitHub

Generated files in `target/`, `dist/`, and `packaging/input/` are excluded by `.gitignore`. Do not commit real user or attendance data, database exports, passwords, or private connection strings.
