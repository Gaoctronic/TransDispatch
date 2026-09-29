package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Vehicle数据库操作接口
 */
@Repository
public interface VehicleRepository
        extends JpaRepository<Vehicle,Integer> {
    List<Vehicle> findByDeletedFalse();

    Optional<Vehicle> findByIdAndDeletedFalse(Integer id);

}