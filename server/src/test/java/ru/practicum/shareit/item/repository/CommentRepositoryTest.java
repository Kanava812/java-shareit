package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void findAllByItemIdTest() {
        User testUser = User.builder()
                .name("name")
                .email("mail@ya.ru")
                .build();

        userRepository.save(testUser);

        ItemRequest testRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("description")
                .requestor(testUser)
                .created(LocalDateTime.now())
                .build());

        Item testItem = Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(testUser)
                .request(testRequest)
                .build();

        itemRepository.save(testItem);

        Comment testComment1 = Comment.builder()
                .text("text")
                .item(testItem)
                .author(testUser)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(testComment1);

        Comment testComment2 = Comment.builder()
                .text("text2")
                .item(testItem)
                .author(testUser)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(testComment2);

        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(testItem.getId());

        assertThat(comments)
                .hasSize(2)
                .containsExactlyInAnyOrder(testComment1, testComment2);

    }
}