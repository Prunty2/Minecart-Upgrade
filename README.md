# Minecart Revamp

Minecart Revamp is a Fabric mod for Minecraft 1.21.10 that raises the maximum speed minecarts can travel on rails. The mod currently hardcodes a higher cap to showcase the effect; future updates can add a config or gamerule.

## Setup

1. Install a JDK 21 distribution (Adoptium Temurin is recommended).
2. Import this Gradle project into your IDE (IntelliJ IDEA works best with Fabric Loom).
3. Let Loom download the Minecraft client/server the first time you run a Gradle task.

## Development

- Launch the client from the command line:

```sh
./gradlew runClient
```

- Launch the dedicated server for MP testing:

```sh
./gradlew runServer
```

## License

This project uses the MIT License. See [LICENSE](LICENSE) for details.
