package com.ems.repository;

import com.ems.entity.SecurityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityGroupRepository extends JpaRepository<SecurityGroup, Long> {
    SecurityGroup findByName(String name);
}
