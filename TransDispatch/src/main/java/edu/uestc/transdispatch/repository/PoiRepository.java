package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.Poi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PoiRepository
        extends JpaRepository<Poi,Integer> {
    List<Poi> findByDeletedFalse();

    Optional<Poi> findByIdAndDeletedFalse(Integer id);

}