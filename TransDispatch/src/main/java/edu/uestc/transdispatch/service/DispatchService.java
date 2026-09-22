package edu.uestc.transdispatch.service;


import edu.uestc.transdispatch.entity.*;
import edu.uestc.transdispatch.repository.TransTaskRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 调度核心服务
 *
 * 负责:
 * Demand -> Vehicle -> Route -> TransTask
 */
@Service
public class DispatchService {


    private final VehicleService vehicleService;

    private final RouteService routeService;

    private final DemandService demandService;

    private final TransTaskService transTaskService;



    public DispatchService(
            VehicleService vehicleService,
            RouteService routeService,
            DemandService demandService,
            TransTaskService transTaskService
    ){

        this.vehicleService = vehicleService;
        this.routeService = routeService;
        this.demandService = demandService;
        this.transTaskService = transTaskService;

    }

    /**
     * 根据运输需求生成任务
     */
    public TransTask dispatchDemand(
            Demand demand
    ){


        /*
         * 1.
         * 查询车辆
         */
        List<Vehicle> vehicles =
                vehicleService.getAllVehicles();



        /*
         * 2.
         * 简单选择第一辆车辆
         */
        Vehicle vehicle =
                vehicles.get(0);



        /*
         * 3.
         * 查询路线
         */
        Route route =
                routeService
                        .getAllRoutes()
                        .get(0);



        /*
         * 4.
         * 创建运输任务
         */
        TransTask task =
                new TransTask();


        task.setDemand(demand);

        task.setCargo(
                demand.getCargo()
        );

        task.setVehicle(vehicle);

        task.setRoute(route);


        task.setStatus(
                "ASSIGNED"
        );


        task.setStartTime(
                LocalDateTime.now()
        );



        /*
         * 5.
         * 保存任务
         */
        transTaskService.saveTask(task);



        /*
         * 6.
         * 更新车辆状态
         */
        vehicle.setStatus(
                "RUNNING"
        );


        vehicleService.saveVehicle(vehicle);



        return task;

    }


}