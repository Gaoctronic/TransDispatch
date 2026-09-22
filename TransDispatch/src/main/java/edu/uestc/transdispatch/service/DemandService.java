package edu.uestc.transdispatch.service;


import edu.uestc.transdispatch.entity.Demand;
import edu.uestc.transdispatch.repository.DemandRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 运输需求业务服务
 */
@Service
public class DemandService {


    private final DemandRepository demandRepository;


    public DemandService(
            DemandRepository demandRepository){

        this.demandRepository = demandRepository;

    }



    /**
     * 查询所有需求
     */
    public List<Demand> getAllDemands(){

        return demandRepository.findAll();

    }



    /**
     * 根据id查询需求
     */
    public Optional<Demand> getDemandById(Integer id){

        return demandRepository.findById(id);

    }



    /**
     * 保存需求
     */
    public Demand saveDemand(Demand demand){

        return demandRepository.save(demand);

    }



    /**
     * 删除需求
     */
    public void deleteDemand(Integer id){

        demandRepository.deleteById(id);

    }


}