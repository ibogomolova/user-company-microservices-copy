package com.usersmicroservice.user.reposirory;

import com.usersmicroservice.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link com.usersmicroservice.user.entity.User}.
 * Использует Spring Data JPA для доступа к данным.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByCompanyId(UUID companyId);

    void deleteByCompanyId(UUID companyId);
}
