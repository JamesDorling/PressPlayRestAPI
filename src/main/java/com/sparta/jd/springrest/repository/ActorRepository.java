package com.sparta.jd.springrest.repository;

import com.sparta.jd.springrest.entity.ActorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends JpaRepository<ActorEntity, Integer> {
}
