package com.example.firstspringapi.repositories;

import com.example.firstspringapi.model.Catogory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Catogory, Long> {

    Optional<Catogory> findByName(String name);
}
