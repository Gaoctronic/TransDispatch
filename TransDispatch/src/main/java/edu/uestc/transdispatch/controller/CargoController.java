package edu.uestc.transdispatch.controller;

import edu.uestc.transdispatch.entity.Cargo;
import edu.uestc.transdispatch.service.CargoService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 货物接口
 */
@RestController
@RequestMapping("/cargos")
public class CargoController {


    private final CargoService cargoService;


    public CargoController(
            CargoService cargoService
    ){

        this.cargoService = cargoService;

    }



    /**
     * 查询所有货物
     *
     * GET /cargos
     */
    @GetMapping
    public List<Cargo> getAllCargos(){

        return cargoService.getAllCargos();

    }



    /**
     * 根据id查询货物
     *
     * GET /cargos/{id}
     */
    @GetMapping("/{id}")
    public Cargo getCargoById(
            @PathVariable Integer id
    ){

        return cargoService
                .getCargoById(id)
                .orElse(null);

    }



    /**
     * 创建货物
     *
     * POST /cargos
     */
    @PostMapping
    public Cargo createCargo(
            @RequestBody Cargo cargo
    ){

        return cargoService.saveCargo(cargo);

    }



    /**
     * 删除货物
     *
     * DELETE /cargos/{id}
     */
    @DeleteMapping("/{id}")
    public void deleteCargo(
            @PathVariable Integer id
    ){

        cargoService.deleteCargo(id);

    }


}