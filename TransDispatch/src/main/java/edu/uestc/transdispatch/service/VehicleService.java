package edu.uestc.transdispatch.service;

import edu.uestc.transdispatch.entity.Demand;
import edu.uestc.transdispatch.entity.Route;
import edu.uestc.transdispatch.entity.Vehicle;
import edu.uestc.transdispatch.entity.Poi;
import edu.uestc.transdispatch.repository.DemandRepository;
import edu.uestc.transdispatch.repository.PoiRepository;
import edu.uestc.transdispatch.repository.VehicleRepository;
import edu.uestc.transdispatch.repository.RouteRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 车辆业务服务
 */
@Service
public class VehicleService {


    private final VehicleRepository vehicleRepository;
    private final PoiRepository poiRepository;
    private final RouteRepository routeRepository;
    private final DemandRepository demandRepository;


    public VehicleService(
            VehicleRepository vehicleRepository,
            PoiRepository poiRepository,
            RouteRepository routeRepository,
            DemandRepository demandRepository
    ){
        this.vehicleRepository = vehicleRepository;
        this.poiRepository = poiRepository;
        this.routeRepository = routeRepository;
        this.demandRepository = demandRepository;
    }



    /**
     * 查询所有车辆
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findByDeletedFalse();
    }



    /**
     * 根据id查询车辆
     */
    public Optional<Vehicle> getVehicleById(Integer id) {
        return vehicleRepository.findByIdAndDeletedFalse(id);
    }



    /**
     * 保存车辆
     */
    public Vehicle saveVehicle(
            Vehicle vehicle){

        return vehicleRepository.save(vehicle);

    }



    /**
     * 删除车辆
     */
    public void deleteVehicle(Integer id) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow();

        vehicle.setDeleted(true);

        vehicleRepository.save(vehicle);
    }



    public Vehicle createVehicleOnRoute(
            Integer routeId,
            Vehicle vehicle
    ) {
        Route route = routeRepository
                .findById(routeId)
                .orElseThrow();

        vehicle.setCurrentRoute(route);

        return vehicleRepository.save(vehicle);
    }


    
    /**
     * 任务4：失效车辆（软删除）
     */
    public boolean invalidVehicle(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
        if (vehicle != null) {
            vehicle.setStatus("失效"); // 只改状态，不删数据
            vehicleRepository.save(vehicle); // 更新回数据库
            return true;
        }
        return false;
    }

    public Vehicle updateLocation(
            Integer vehicleId,
            Integer poiId,
            Integer routeId
    ) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndDeletedFalse(vehicleId)
                .orElseThrow();

        Poi poi = poiRepository
                .findById(poiId)
                .orElseThrow();

        Route route = routeRepository
                .findById(routeId)
                .orElseThrow();

        vehicle.setCurrentPoi(poi);
        vehicle.setCurrentRoute(route);

        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateStatus(
            Integer vehicleId,
            String status
    ) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndDeletedFalse(vehicleId)
                .orElseThrow();

        vehicle.setStatus(status);

        return vehicleRepository.save(vehicle);
    }

    public Double calculateMatchScore(
            Integer demandId,
            Integer vehicleId
    ) {
        Demand demand = demandRepository
                .findByIdAndDeletedFalse(demandId)
                .orElseThrow();

        Vehicle vehicle = vehicleRepository
                .findByIdAndDeletedFalse(vehicleId)
                .orElseThrow();

        double score = 0.0;

        // 车辆类型匹配
        if (demand.getCargo() != null
                && demand.getCargo().getSuitableVehicleTypes() != null
                && demand.getCargo().getSuitableVehicleTypes()
                .contains(vehicle.getVehicleType())) {
            score += 50;
        }

        // 车辆当前位置与需求起点一致
        if (vehicle.getCurrentPoi() != null
                && demand.getStartPoi() != null
                && vehicle.getCurrentPoi().getId()
                .equals(demand.getStartPoi().getId())) {
            score += 30;
        }

        // 车辆当前路线与需求路线方向暂时匹配
        if (vehicle.getCurrentRoute() != null
                && demand.getStartPoi() != null
                && demand.getEndPoi() != null
                && vehicle.getCurrentRoute().getStartPoi() != null
                && vehicle.getCurrentRoute().getEndPoi() != null
                && vehicle.getCurrentRoute().getStartPoi().getId()
                .equals(demand.getStartPoi().getId())
                && vehicle.getCurrentRoute().getEndPoi().getId()
                .equals(demand.getEndPoi().getId())) {
            score += 20;
        }

        return score;
    }
}
