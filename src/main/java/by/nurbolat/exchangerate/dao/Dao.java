package by.nurbolat.exchangerate.dao;

import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;

import java.util.List;

public interface Dao<E> {
    E save(String name,String code, String sign) throws DatabaseAccessException,RuntimeException;

    E findByCode(String code) throws DatabaseAccessException;

    List<E> findAll() throws DatabaseAccessException;

}
