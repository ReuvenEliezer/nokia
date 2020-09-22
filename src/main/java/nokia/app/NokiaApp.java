package nokia.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "nokia.config",
        "nokia.controllers",
        "nokia.services",
})
public class NokiaApp {

    public static void main(String[] args) {
        SpringApplication.run(NokiaApp.class, args);
    }
}
