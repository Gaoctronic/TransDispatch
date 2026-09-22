package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RouteRepository
        extends JpaRepository<Route,Integer> {


}