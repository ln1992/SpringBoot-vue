package com.boylegu.springboot_vue.repository;

import com.boylegu.springboot_vue.entities.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByIsValid(Boolean isValid);
}
