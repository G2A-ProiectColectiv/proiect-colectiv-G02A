package ro.rentalplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ro.rentalplatform.location.config.LocationProperties;
import ro.rentalplatform.security.JwtProperties;
import ro.rentalplatform.storage.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, StorageProperties.class, LocationProperties.class})
public class RentalPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalPlatformApplication.class, args);
    }

}
