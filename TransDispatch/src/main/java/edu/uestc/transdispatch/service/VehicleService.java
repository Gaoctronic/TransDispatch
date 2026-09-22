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


}