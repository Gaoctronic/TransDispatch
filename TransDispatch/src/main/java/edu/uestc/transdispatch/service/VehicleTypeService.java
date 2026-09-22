package edu.uestc.transdispatch.service;

import edu.uestc.transdispatch.entity.VehicleType;
import edu.uestc.transdispatch.repository.VehicleTypeRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 车辆类型业务服务
 */
@Service
public class VehicleTypeService {


    private final VehicleTypeRepository vehicleTypeRepository;


    /**
     * 构造器注入
     */
    public VehicleTypeService(
            VehicleTypeRepository vehicleTypeRepository){

        this.vehicleTypeRepository = vehicleTypeRepository;

    }



    /**
     * 查询所有车辆类型
     */
    public List<VehicleType> getAllVehicleTypes(){

        return vehicleTypeRepository.findAll();

    }



    /**
     * 根据id查询车辆类型
     */
    public Optional<VehicleType> getVehicleTypeById(
            Integer id){

        return vehicleTypeRepository.findById(id);

    }



    /**
     * 保存车辆类型
     */
    public VehicleType saveVehicleType(
            VehicleType vehicleType){

        return vehicleTypeRepository.save(vehicleType);

    }



    /**
     * 删除车辆类型
     */
    public void deleteVehicleType(Integer id){

        vehicleTypeRepository.deleteById(id);

    }

}