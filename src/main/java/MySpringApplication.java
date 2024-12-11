import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan("com.ftpl")
@SpringBootApplication
@EnableScheduling
public class MySpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(MySpringApplication.class);
    }
}
