# S.E.R.S. Project

School Event Registration System built with Java Swing, Maven, and MongoDB. This is a school project intended for educational use.

## Requirements

- JDK 21 or newer (the project compiles for Java 21)
- Maven, or NetBeans with Maven support
- MongoDB running locally at `mongodb://localhost:27017`

The application uses the `SERS_DB` database and creates the initial `ADMIN001` account interactively the first time it runs against an empty database.

## Build and run

From the project directory:

```powershell
mvn clean package
mvn exec:java
```

If Maven's exec plugin is unavailable, run `javamongo1.sersproject.SERSProject` from NetBeans after building the project.

## Windows app image

Run `package-windows.ps1` from PowerShell after building. The script requires a JDK that includes `jpackage` and Maven dependencies already downloaded. If your JDK is installed outside the script's default location, update `$jdkBin` in the script.

## GitHub upload

The GitHub repository currently shown in the project screenshot contains only a README. In this project folder, initialize Git and connect that remote, then push the source:

```powershell
git init
git add .
git status
git commit -m "Add SERS project source"
git branch -M main
git remote add origin https://github.com/Void266/S.E.R.S-Project.git
git push -u origin main
```

If `origin` is already configured, skip `git remote add origin`. If the GitHub README commit and local history do not match, first use GitHub's documented instructions for pushing an existing repository or pull/reconcile the README commit before pushing; do not force-push unless you intend to replace the remote history.

## Data and credentials

The default database connection points only to local MongoDB. Do not commit real attendance/user data, database exports, passwords, private connection strings, or `.env`/local configuration files. The `.gitignore` excludes generated build and packaging output.
