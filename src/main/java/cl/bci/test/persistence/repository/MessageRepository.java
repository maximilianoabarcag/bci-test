package cl.bci.test.persistence.repository;

import cl.bci.test.persistence.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, String> {
}
