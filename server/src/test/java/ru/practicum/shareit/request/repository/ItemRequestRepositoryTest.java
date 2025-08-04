package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User requestor1;
    private User requestor2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        // инициализация данных
        requestor1 = userRepository.save(User.builder()
                .name("requestor 1")
                .email("requestor1@email.com")
                .build());

        requestor2 = userRepository.save(User.builder()
                .name("requestor 2")
                .email("requestor2@email.com")
                .build());

        request1 = itemRequestRepository.save(ItemRequest.builder()
                .description("description1")
                .requestor(requestor1)
                .created(LocalDateTime.now().minusDays(2))
                .build());

        request2 = itemRequestRepository.save(ItemRequest.builder()
                .description("description2")
                .requestor(requestor1)
                .created(LocalDateTime.now().minusDays(1))
                .build());

        request3 = itemRequestRepository.save(ItemRequest.builder()
                .description("description3")
                .requestor(requestor2)
                .created(LocalDateTime.now())
                .build());
    }

    @Test
    void findAllByRequestorIdOrderByCreatedDescForKnownUserTest() {
        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(requestor1.getId());

        assertThat(requests)
                .hasSize(2)
                .extracting(ItemRequest::getId)
                .containsExactly(request2.getId(), request1.getId());
    }

    @Test
    void findAllByRequestorIdOrderByCreatedDescForUnknownUserTest() {
        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(999L);

        assertThat(requests).isEmpty();
    }

    @Test
    void findAllByRequestorIdNotOrderByCreatedDescForOtherUsersTest() {
        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequestorIdNot(requestor1.getId(),Sort.by(Sort.Direction.DESC, "created"));

        assertThat(requests)
                .hasSize(1)
                .extracting(ItemRequest::getId)
                .containsExactly(request3.getId());
    }

    @Test
    void findAllByRequestorIdNotOrderByCreatedDescForUnknownUserTest() {
        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequestorIdNot(100L,Sort.by(Sort.Direction.DESC, "created"));

        assertThat(requests)
                .hasSize(3)
                .extracting(ItemRequest::getId)
                .containsExactly(request3.getId(), request2.getId(), request1.getId());
    }
}