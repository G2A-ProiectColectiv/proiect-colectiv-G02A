package ro.rentalplatform;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    @ServiceConnection
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:17-alpine");

    static {
        postgreSQLContainer.start();
    }

    @Autowired
    Flyway flyway;

    @BeforeEach
    public void setup() {
        flyway.clean();
        flyway.migrate();
    }
}
