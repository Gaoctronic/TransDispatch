package edu.uestc.transdispatch.service;


import edu.uestc.transdispatch.entity.TransTask;
import edu.uestc.transdispatch.repository.TransTaskRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 运输任务业务服务
 */
@Service
public class TransTaskService {


    private final TransTaskRepository transTaskRepository;


    /**
     * 构造器注入
     */
    public TransTaskService(
            TransTaskRepository transTaskRepository){

        this.transTaskRepository = transTaskRepository;

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


}