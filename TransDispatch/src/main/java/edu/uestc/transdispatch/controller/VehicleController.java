package edu.uestc.transdispatch.controller;


import edu.uestc.transdispatch.entity.Vehicle;
import edu.uestc.transdispatch.service.VehicleService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 车辆接口
 */
@RestController
@RequestMapping("/vehicles")
public class VehicleController {


    private final VehicleService vehicleService;


    public VehicleController(
            VehicleService vehicleService
    ){

        this.vehicleService = vehicleService;

    }



    /**
     * 查询所有车辆
     *
     * GET /vehicles
     */
    @GetMapping
    public List<Vehicle> getAllVehicles(){

        return vehicleService.getAllVehicles();

    }



    /**
     * 根据id查询车辆
     *
     * GET /vehicles/{id}
     */
    @GetMapping("/{id}")
    public Vehicle getVehicleById(
            @PathVariable Integer id
    ){

        return vehicleService
                .getVehicleById(id)
                .orElse(null);

    }



    /**
     * 新增车辆
     *
     * POST /vehicles
     */
    @PostMapping
    public Vehicle createVehicle(
            @RequestBody Vehicle vehicle
    ){

        return vehicleService
                .saveVehicle(vehicle);

    }

    /**
     * 删除车辆
     * DELETE /vehicles/{id}
     */
    @DeleteMapping("/{id}")
    public void deleteVehicle(
            @PathVariable Integer id
    ) {
        vehicleService.deleteVehicle(id);
    }

    @PostMapping
    public Vehicle createVehicle(
            @RequestParam Integer routeId,
            @RequestBody Vehicle vehicle
    ) {
        return vehicleService.createVehicleOnRoute(
                routeId,
                vehicle
        );
    }

}
