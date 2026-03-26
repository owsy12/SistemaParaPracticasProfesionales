package Logic.Interface;

import Logic.DTOs.User;

import java.util.List;

public interface IUserDAO {
    boolean saveUser(User user);
    User findById(int id);
    User findByMatricula(String matricula);
    List<User> findAll();
    boolean update(User user);
    boolean delete(int id);
}