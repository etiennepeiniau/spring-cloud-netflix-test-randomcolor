package io.peiniau.randomcolor;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@EnableCircuitBreaker
public class RandomColorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RandomColorApplication.class, args);
    }
}

@RestController
class RandomColorController {

    private static final Logger log = LoggerFactory.getLogger(RandomColorController.class);

    private static final int RGB_MAX = 256;

    private final RandomIntClient randomInt;

    @Autowired
    public RandomColorController(RandomIntClient randomInt) {
        this.randomInt = randomInt;
    }

    @GetMapping("/random")
    @ResponseBody
    public ColorValueResponse random() {
        // TODO sequential calls can be converted into reactive streams
        IntValueResponse r = randomInt.random(RGB_MAX);
        IntValueResponse g = randomInt.random(RGB_MAX);
        IntValueResponse b = randomInt.random(RGB_MAX);
        ColorValueResponse colorValueResponse = new ColorValueResponse(r.getValue(), g.getValue(), b.getValue());
        log.info("Color: {}", colorValueResponse);
        return colorValueResponse;
    }

}

@FeignClient(value = "randomint", fallback = RandomIntClientFallback.class)
interface RandomIntClient {

    @RequestMapping(method = RequestMethod.GET, value = "/random?bound={bound}")
    IntValueResponse random(@PathVariable("bound") Integer bound);

}

@Component
class RandomIntClientFallback implements RandomIntClient {

    @Override
    public IntValueResponse random(Integer bound) {
        IntValueResponse response = new IntValueResponse();
        response.setValue(0);
        return response;
    }

}

@Data
@AllArgsConstructor
class ColorValueResponse {

    private Integer r;
    private Integer g;
    private Integer b;

}

@Data
class IntValueResponse {

    private Integer value;

}