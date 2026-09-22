package edu.uestc.transdispatch.service;

import edu.uestc.transdispatch.entity.Cargo;
import edu.uestc.transdispatch.repository.CargoRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 货物业务服务
 */
@Service
public class CargoService {


    private final CargoRepository cargoRepository;


    /**
     * 构造器注入
     */
    public CargoService(CargoRepository cargoRepository){

        this.cargoRepository = cargoRepository;

    }



    /**
     * 查询所有货物
     */
    public List<Cargo> getAllCargo(){

        return cargoRepository.findAll();

    }



    /**
     * 根据id查询货物
     */
    public Optional<Cargo> getCargoById(Integer id){

        return cargoRepository.findById(id);

    }



    /**
     * 保存货物
     */
    public Cargo saveCargo(Cargo cargo){

        return cargoRepository.save(cargo);

    }



    /**
     * 删除货物
     */
    public void deleteCargo(Integer id){

        cargoRepository.deleteById(id);

    }

}