package ru.vlad.orm.support;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorHandlerTest {
    private final ApiErrorHandler handler = new ApiErrorHandler();

    @Test
    void domainErrorsKeepStatusAndMessage() {
        var problem = handler.domain(ApiException.notFound("Course not found"));
        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getDetail()).isEqualTo("Course not found");
    }

    @Test
    void integrityErrorsDoNotLeakDatabaseDetails() {
        var problem = handler.integrity(new DataIntegrityViolationException("private DB metadata"));
        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getDetail()).doesNotContain("private DB metadata");
    }

    @Test
    void malformedRequestsAreBadRequests() {
        assertThat(handler.malformed(new IllegalArgumentException()).getStatus()).isEqualTo(400);
    }
}
