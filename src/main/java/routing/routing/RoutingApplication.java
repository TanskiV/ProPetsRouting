package routing.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import routing.routing.filters.RoutFilter;

@SpringBootApplication
@EnableZuulProxy
public class RoutingApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutingApplication.class, args);
	}

	@Bean
	public RoutFilter filter() {
		return new RoutFilter();
	}

}
