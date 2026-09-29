package edu.uestc.transdispatch.service;

import edu.uestc.transdispatch.entity.Vehicle;
import edu.uestc.transdispatch.repository.VehicleRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 车辆业务服务
 */
@Service
public class VehicleService {


    private final VehicleRepository vehicleRepository;



    public VehicleService(
            VehicleRepository vehicleRepository){

        this.vehicleRepository = vehicleRepository;

    }



    /**
     * 查询所有车辆
     */
    public List<Vehicle> getAllVehicles(){

        return vehicleRepository.findAll();

    }



    /**
     * 根据id查询车辆
     */
    public Optional<Vehicle> getVehicleById(
            Integer id){

        return vehicleRepository.findById(id);

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
    public void deleteVehicle(Integer id){

        vehicleRepository.deleteById(id);

    }



    /**
     * 任务3：增加车辆
     */
    public Vehicle addVehicle(Vehicle vehicle) {
        vehicle.setStatus("正常"); // 默认状态设为正常
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
}
