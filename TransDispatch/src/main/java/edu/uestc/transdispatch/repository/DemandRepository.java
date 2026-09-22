package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DemandRepository
        extends JpaRepository<Demand,Integer> {


}