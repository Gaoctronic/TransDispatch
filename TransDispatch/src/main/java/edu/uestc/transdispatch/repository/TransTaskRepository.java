package edu.uestc.transdispatch.repository;

import edu.uestc.transdispatch.entity.TransTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TransTaskRepository
        extends JpaRepository<TransTask,Integer> {


}