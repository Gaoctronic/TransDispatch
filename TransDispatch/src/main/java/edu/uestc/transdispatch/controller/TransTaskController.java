package edu.uestc.transdispatch.controller;

import edu.uestc.transdispatch.entity.TransTask;
import edu.uestc.transdispatch.service.TransTaskService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 运输任务接口
 */
@RestController
@RequestMapping("/tasks")
public class TransTaskController {


    private final TransTaskService transTaskService;



    public TransTaskController(
            TransTaskService transTaskService
    ){

        this.transTaskService = transTaskService;

    }



    /**
     * 查询所有任务
     *
     * GET /tasks
     */
    @GetMapping
    public List<TransTask> getAllTasks(){

        return transTaskService
                .getAllTasks();

    }



    /**
     * 查询单个任务
     *
     * GET /tasks/{id}
     */
    @GetMapping("/{id}")
    public TransTask getTaskById(
            @PathVariable Integer id
    ){

        return transTaskService
                .getTaskById(id)
                .orElse(null);

    }



    /**
     * 创建任务
     * POST /tasks
     */
    @PostMapping
    public TransTask createTask(
            @RequestParam Integer demandId,
            @RequestBody List<Integer> vehicleIds
    ) {
        return transTaskService.createTask(
                demandId,
                vehicleIds
        );
    }

    @PutMapping("/{id}/revenue")
    public Double calculateRevenue(
            @PathVariable Integer id,
            @RequestParam Double income
    ) {
        return transTaskService.calculateRevenue(id, income);
    }


}