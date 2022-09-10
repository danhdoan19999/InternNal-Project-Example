package com.nals.rw360.service.v1;

import com.nals.rw360.errors.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class BaseService<E, R extends JpaRepository<E, Long>> {

    private final R repository;

    protected R getRepository() {
        return repository;
    }

    public Optional<E> getById(final Long id) {
        log.info("Get by id #{}", id);
        return repository.findById(id);
    }

    public E getById(final Long id, final String resourceName) {
        log.info("Get by id #{}", id);
        return repository.findById(id).orElseThrow(() -> new ObjectNotFoundException(resourceName));
    }

    public E getById(final Long id, final String resourceName, final String errorCode) {
        log.info("Get by id #{}", id);
        return repository.findById(id).orElseThrow(() -> new ObjectNotFoundException(resourceName, errorCode));
    }

    public List<E> fetchAll() {
        return repository.findAll();
    }

    public List<E> fetchByIds(final Collection<Long> ids) {
        log.info("Fetch by ids #{}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return repository.findAllById(ids);
    }

    @Transactional
    public E save(final E entity) {
        return repository.save(entity);
    }

    @Transactional
    public void delete(final E entity) {
        log.info("Delete by entity #{}", entity.getClass());
        repository.delete(entity);
    }

    @Transactional
    public void deleteById(final Long id) {
        log.info("Delete by id #{}", id);
        repository.deleteById(id);
    }

    public boolean existsById(final Long id) {
        log.info("Check exists by id #{}", id);
        return repository.existsById(id);
    }
}
