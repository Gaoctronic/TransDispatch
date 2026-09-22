package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.Poi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PoiRepository
        extends JpaRepository<Poi,Integer> {


}