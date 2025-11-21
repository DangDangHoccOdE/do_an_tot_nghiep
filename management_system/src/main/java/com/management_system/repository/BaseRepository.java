package com.management_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    Optional<T> findByIdAndDeleteFlagFalse(ID id);

    List<T> findAllByDeleteFlagFalse(Sort sort);
}


