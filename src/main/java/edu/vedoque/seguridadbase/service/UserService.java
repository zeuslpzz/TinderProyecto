package edu.vedoque.seguridadbase.service;

import edu.vedoque.seguridadbase.dto.UserDto;
import edu.vedoque.seguridadbase.entity.Role;
import edu.vedoque.seguridadbase.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    void saveUser(UserDto userDto);

    User findByEmail(String email);

    List<UserDto> findAllUsers();

    public UserDto convertEntityToDto(User user);
    public void save(User user);

    public List<Role> conseguirRolesByUser(User user);
    public void saveCifrandoPassword(User user);
    public User findById(Long id);
}
