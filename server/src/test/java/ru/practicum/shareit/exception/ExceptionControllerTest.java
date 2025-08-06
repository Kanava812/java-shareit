package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExceptionControllerTest {

    @InjectMocks
    private ExceptionController exceptionController;

    @Test
    void handleEntityNotFoundTest() {
        String errorMessage = "Объект не найден";
        EntityNotFoundException exception = new EntityNotFoundException(errorMessage);

        ErrorResponse response = exceptionController.handlerEntityNotFoundException(exception);

        assertThat(response)
                .isNotNull()
                .extracting(ErrorResponse::getError)
                .isEqualTo(errorMessage);
    }

    @Test
    void handleAlreadyExistsTest() {
        String errorMessage = "Email уже существует";
        AlreadyExistsException exception = new AlreadyExistsException(errorMessage);

        ErrorResponse response = exceptionController.handlerAlreadyExistsException(exception);

        assertThat(response)
                .isNotNull()
                .extracting(ErrorResponse::getError)
                .isEqualTo(errorMessage);
    }

    @Test
    void handleAccessDeniedTest() {
        String errorMessage = "Доступ запрещен.";
        AccessNotAllowedException exception = new AccessNotAllowedException(errorMessage);

        ErrorResponse response = exceptionController.handleAccessNotAllowedException(exception);

        assertThat(response)
                .isNotNull()
                .extracting(ErrorResponse::getError)
                .isEqualTo(errorMessage);
    }
}