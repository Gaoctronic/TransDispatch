package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VehicleTypeRepository
        extends JpaRepository<VehicleType,Integer> {


}