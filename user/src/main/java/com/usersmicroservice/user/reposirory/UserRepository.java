package com.usersmicroservice.user.reposirory;

import com.usersmicroservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByCompanyId(UUID companyId);

    void deleteByCompanyId(UUID companyId);
}
