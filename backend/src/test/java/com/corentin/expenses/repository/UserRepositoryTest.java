package com.corentin.expenses.repository;

import com.corentin.expenses.entity.Role;
import com.corentin.expenses.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_assignsIdAuditDatesAndDefaultRole() {
        UserEntity saved = userRepository.saveAndFlush(user("john@doe.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        userRepository.saveAndFlush(user("john@doe.com"));

        Optional<UserEntity> found = userRepository.findByEmail("john@doe.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getPassword_hash()).isEqualTo("hashed");
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        assertThat(userRepository.findByEmail("unknown@doe.com")).isEmpty();
    }

    @Test
    void existsByEmail_reflectsPresenceOfUser() {
        userRepository.saveAndFlush(user("john@doe.com"));

        assertThat(userRepository.existsByEmail("john@doe.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@doe.com")).isFalse();
    }

    private static UserEntity user(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword_hash("hashed");
        return user;
    }
}
