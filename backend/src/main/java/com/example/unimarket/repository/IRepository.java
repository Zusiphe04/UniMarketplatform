package com.example.unimarket.repository;

import java.util.List;

/**
 * Generic repository abstraction defining the standard persistence
 * operations for every aggregate in UniMarket.
 *
 * <p>Every concrete repository is reached through this abstraction so that
 * services depend on the contract rather than on a persistence technology.
 *
 * @param <T>  the aggregate type
 * @param <ID> the aggregate identifier type
 */
public interface IRepository<T, ID> {

    /**
     * Persists a new aggregate.
     *
     * @return the stored aggregate
     */
    T create(T entity);

    /**
     * Reads a single aggregate by identifier.
     *
     * @return the aggregate, or {@code null} when no aggregate has that identifier
     */
    T read(ID id);

    /**
     * Persists changes to an existing aggregate.
     *
     * @return the updated aggregate
     */
    T update(T entity);

    /**
     * Deletes the aggregate with the given identifier.
     *
     * @return {@code true} when an aggregate was deleted
     */
    boolean delete(ID id);

    /**
     * Reads every aggregate.
     */
    List<T> getAll();
}
