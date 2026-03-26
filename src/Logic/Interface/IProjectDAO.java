package Logic.Interface;

import Logic.DTOs.Proyect;

import java.util.List;

public interface IProjectDAO {
    Proyect findById(int id);
    List<Proyect> findAll();
    boolean save(Proyect project);
    boolean update(Proyect project);
    boolean delete(int id);
}