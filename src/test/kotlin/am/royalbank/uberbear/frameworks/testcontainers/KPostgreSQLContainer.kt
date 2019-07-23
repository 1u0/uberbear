package am.royalbank.uberbear.frameworks.testcontainers

import org.testcontainers.containers.PostgreSQLContainer

class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>("postgres:9.6.12")
