package by.nurbolat.exchangerate.dao;

import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;
import by.nurbolat.exchangerate.exceptions.DuplicateKeyValueExceptions;

import java.util.List;

public interface Dao<E> {
    E save(E entity) throws DatabaseAccessException, RuntimeException, DuplicateKeyValueExceptions;

    E findByCode(String code) throws DatabaseAccessException;

    List<E> findAll() throws DatabaseAccessException;

}
