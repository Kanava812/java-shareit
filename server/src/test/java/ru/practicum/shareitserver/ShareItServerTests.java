package ru.practicum.shareitserver;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.practicum.shareit.ShareItServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ShareItServer.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShareItServerTests {

	@Test
	void contextLoads() {
	}

}
