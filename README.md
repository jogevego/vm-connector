# vm-connector

## Purpose

This project is a **standalone demo** intended for opening a ticket and reproducing the issue of using `bc-fips` with Ed25519 keys in the [mwiede/jsch](https://github.com/mwiede/jsch) fork.  
It is **not** a demonstration of a working setup, but a minimal example to help debug or report problems.

## Requirements

- **Java 8**
- **Maven**
- **bc-fips-2.1.1.jar** (download manually from BouncyCastle and place in the project root)

## How to Build and Run

```sh
mvn clean install
cp bc-fips-2.1.1.jar target
cd target
java -cp "ssh-tunnel-client-1.0-SNAPSHOT.jar:bc-fips-2.1.1.jar" com.example.SshTunnelDemo
```

**Note:**  
- Make sure `bc-fips-2.1.1.jar` is present in the `target` directory before running the `java` command.
- This project is for reproducing and reporting issues with BCFIPS and Ed25519 keys