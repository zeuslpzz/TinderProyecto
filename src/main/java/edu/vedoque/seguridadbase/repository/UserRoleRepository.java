package edu.vedoque.seguridadbase.repository;

import edu.vedoque.seguridadbase.entity.Role;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByRole(Role role);
    List<UserRole> findByUser(User user);
    void deleteByUser(User user);
}