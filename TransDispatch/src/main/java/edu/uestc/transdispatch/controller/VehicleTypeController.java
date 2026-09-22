package edu.uestc.transdispatch.controller;


import edu.uestc.transdispatch.entity.VehicleType;
import edu.uestc.transdispatch.service.VehicleTypeService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 车辆类型接口
 */
@RestController
@RequestMapping("/vehicle-types")
public class VehicleTypeController {


    private final VehicleTypeService vehicleTypeService;


    public VehicleTypeController(
            VehicleTypeService vehicleTypeService
    ){

        this.vehicleTypeService = vehicleTypeService;

    }



    /**
     * 查询所有车辆类型
     *
     * GET /vehicle-types
     */
    @GetMapping
    public List<VehicleType> getAllVehicleTypes(){

        return vehicleTypeService
                .getAllVehicleTypes();

    }



    /**
     * 根据id查询车辆类型
     *
     * GET /vehicle-types/{id}
     */
    @GetMapping("/{id}")
    public VehicleType getVehicleTypeById(
            @PathVariable Integer id
    ){

        return vehicleTypeService
                .getVehicleTypeById(id)
                .orElse(null);

    }



    /**
     * 新增车辆类型
     *
     * POST /vehicle-types
     */
    @PostMapping
    public VehicleType createVehicleType(
            @RequestBody VehicleType vehicleType
    ){

        return vehicleTypeService
                .saveVehicleType(vehicleType);

    }



    /**
     * 删除车辆类型
     *
     * DELETE /vehicle-types/{id}
     */
    @DeleteMapping("/{id}")
    public void deleteVehicleType(
            @PathVariable Integer id
    ){

        vehicleTypeService
                .deleteVehicleType(id);

    }


}