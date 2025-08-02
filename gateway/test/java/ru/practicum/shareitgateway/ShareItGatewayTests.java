package java.ru.practicum.shareitgateway;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.practicum.shareit.ShareItGateway;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ShareItGateway.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShareItGatewayTests {

	@Test
	void contextLoads() {
	}

}
