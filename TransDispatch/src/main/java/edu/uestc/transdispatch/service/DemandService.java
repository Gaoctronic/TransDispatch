package edu.uestc.transdispatch.service;


import edu.uestc.transdispatch.dto.DemandCreateDTO;
import edu.uestc.transdispatch.entity.Cargo;
import edu.uestc.transdispatch.entity.Demand;
import edu.uestc.transdispatch.entity.Poi;
import edu.uestc.transdispatch.repository.CargoRepository;
import edu.uestc.transdispatch.repository.DemandRepository;
import edu.uestc.transdispatch.repository.PoiRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;


/**
 * 运输需求业务服务
 */
@Service
public class DemandService {


    private final DemandRepository demandRepository;

    private final PoiRepository poiRepository;

    private final CargoRepository cargoRepository;


    public DemandService(
            DemandRepository demandRepository,
            PoiRepository poiRepository,
            CargoRepository cargoRepository){

        this.demandRepository = demandRepository;

        this.poiRepository = poiRepository;

        this.cargoRepository = cargoRepository;

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
     * 创建运输需求
     */
    public Demand createDemand(DemandCreateDTO request){

        if (request.getStartPoiId() == null
                || request.getEndPoiId() == null
                || request.getCargoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startPoiId, endPoiId and cargoId are required"
            );
        }

        Poi startPoi = poiRepository
                .findById(request.getStartPoiId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Start POI not found: " + request.getStartPoiId()
                ));

        if (!"FACTORY".equals(startPoi.getPoiType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only a FACTORY POI can be used as the demand start POI"
            );
        }

        Poi endPoi = poiRepository
                .findById(request.getEndPoiId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "End POI not found: " + request.getEndPoiId()
                ));

        Cargo cargo = cargoRepository
                .findById(request.getCargoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cargo not found: " + request.getCargoId()
                ));

        Demand demand = new Demand();
        demand.setDemandName(request.getDemandName());
        demand.setStartPoi(startPoi);
        demand.setEndPoi(endPoi);
        demand.setCargo(cargo);
        demand.setStartTime(request.getStartTime());
        demand.setDeadline(request.getDeadline());
        demand.setStatus("ACTIVE");

        return demandRepository.save(demand);

    }



    /**
     * 将运输需求标记为失效（软删除）
     */
    public Demand invalidateDemand(Integer id){

        Demand demand = demandRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Demand not found: " + id
                ));

        demand.setStatus("INVALID");

        return demandRepository.save(demand);

    }



    /**
     * 删除需求
     */
    public void deleteDemand(Integer id){

        demandRepository.deleteById(id);

    }


}
