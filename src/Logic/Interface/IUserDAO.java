package Logic.Interface;

import Logic.DTOs.UserDTO;

import java.util.List;

public interface IUserDAO {
    boolean save(UserDTO user);
    UserDTO findById(int id);
    UserDTO findByMatricula(String matricula);
    List<UserDTO> findAll();
    boolean update(UserDTO user);
    boolean delete(int id);
}