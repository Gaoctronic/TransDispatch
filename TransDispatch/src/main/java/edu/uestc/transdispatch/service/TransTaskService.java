package edu.uestc.transdispatch.service;


import edu.uestc.transdispatch.entity.Demand;
import edu.uestc.transdispatch.entity.TransTask;
import edu.uestc.transdispatch.entity.Vehicle;
import edu.uestc.transdispatch.repository.DemandRepository;
import edu.uestc.transdispatch.repository.TransTaskRepository;

import edu.uestc.transdispatch.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 运输任务业务服务
 */
@Service
public class TransTaskService {
    private final TransTaskRepository transTaskRepository;
    private final DemandRepository demandRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * 构造器注入
     */
    public TransTaskService(
            TransTaskRepository transTaskRepository,
            DemandRepository demandRepository,
            VehicleRepository vehicleRepository
    ) {
        this.transTaskRepository = transTaskRepository;
        this.demandRepository = demandRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public TransTask createTask(
            Integer demandId,
            List<Integer> vehicleIds
    ) {

        // 1. 查询需求
        Demand demand = demandRepository
                .findByIdAndDeletedFalse(demandId)
                .orElseThrow();

        // 2. 一个需求只能生成一个任务
        if (transTaskRepository
                .findByDemandId(demandId)
                .isPresent()) {
            throw new IllegalStateException(
                    "该需求已经生成运输任务"
            );
        }

        // 3. 查询车辆
        List<Vehicle> vehicles =
                vehicleRepository.findAllById(vehicleIds);

        // 4. 检查车辆数量
        if (vehicles.size() != vehicleIds.size()) {
            throw new IllegalArgumentException(
                    "存在无效车辆"
            );
        }

        // 5. 创建任务
        TransTask task = new TransTask();

        task.setTaskCode(
                "TASK-" + System.currentTimeMillis()
        );

        task.setDemand(demand);
        task.setCargo(demand.getCargo());
        task.setVehicles(vehicles);
        task.setStatus("WAITING");
        task.setStartTime(demand.getStartTime());

        return transTaskRepository.save(task);
    }

    /**
     * 查询所有运输任务
     */
    public List<TransTask> getAllTasks(){

        return transTaskRepository.findAll();

    }



    /**
     * 根据id查询任务
     */
    public Optional<TransTask> getTaskById(
            Integer id){

        return transTaskRepository.findById(id);

    }



    /**
     * 保存任务
     */
    public TransTask saveTask(
            TransTask task){

        return transTaskRepository.save(task);

    }



    /**
     * 删除任务
     */
    public void deleteTask(Integer id){

        transTaskRepository.deleteById(id);

    }

    public Double calculateRevenue(Integer taskId, Double income) {
        TransTask task = transTaskRepository
                .findById(taskId)
                .orElseThrow();

        Double cost = task.getActualCost() == null
                ? 0.0
                : task.getActualCost();

        Double revenue = income - cost;

        task.setRevenue(revenue);
        transTaskRepository.save(task);

        return revenue;
    }


}