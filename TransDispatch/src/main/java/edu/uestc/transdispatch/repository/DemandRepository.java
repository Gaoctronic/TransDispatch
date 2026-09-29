package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface DemandRepository
        extends JpaRepository<Demand, Integer> {

    List<Demand> findByDeletedFalse();

    Optional<Demand> findByIdAndDeletedFalse(Integer id);
}