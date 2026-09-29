package edu.uestc.transdispatch.service;


import edu.uestc.transdispatch.entity.Demand;
import edu.uestc.transdispatch.entity.Poi;
import edu.uestc.transdispatch.repository.DemandRepository;

import edu.uestc.transdispatch.repository.PoiRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 运输需求业务服务
 */
@Service
public class DemandService {


    private final DemandRepository demandRepository;
    private final PoiRepository poiRepository;


    public DemandService(
            DemandRepository demandRepository,
            PoiRepository poiRepository
    ){

        this.demandRepository = demandRepository;
        this.poiRepository = poiRepository;
    }

    public List<Demand> getAllDemands(){
        return demandRepository.findByDeletedFalse();
    }

    public Optional<Demand> getDemandById(Integer id){
        return demandRepository.findByIdAndDeletedFalse(id);
    }


    /**
     * 保存需求
     */
    public Demand saveDemand(Demand demand){

        return demandRepository.save(demand);

    }


    /**
     * 基于工厂生成需求
     */
    public Demand createDemandFromFactory(
            Integer factoryId,
            Demand demand
    ){

        Poi factory = poiRepository
                .findById(factoryId)
                .orElseThrow();

        if (!"FACTORY".equals(factory.getPoiType())) {
            throw new IllegalArgumentException(
                    "指定的 POI 不是工厂"
            );
        }

        demand.setStartPoi(factory);
        demand.setStatus("WAITING");

        return demandRepository.save(demand);
    }

    public void deleteDemand(Integer id){
        Demand demand = demandRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow();

        demand.setDeleted(true);

        demandRepository.save(demand);
    }


}