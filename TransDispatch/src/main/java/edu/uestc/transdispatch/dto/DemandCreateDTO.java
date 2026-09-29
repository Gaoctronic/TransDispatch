package edu.uestc.transdispatch.dto;

import java.time.LocalDateTime;

/**
 * 创建运输需求时使用的通用参数。
 */
public class DemandCreateDTO {

    private Integer startPoiId;

    private Integer endPoiId;

    private Integer cargoId;

    private String demandName;

    private LocalDateTime startTime;

    private LocalDateTime deadline;

    public Integer getStartPoiId() {
        return startPoiId;
    }

    public void setStartPoiId(Integer startPoiId) {
        this.startPoiId = startPoiId;
    }

    public Integer getEndPoiId() {
        return endPoiId;
    }

    public void setEndPoiId(Integer endPoiId) {
        this.endPoiId = endPoiId;
    }

    public Integer getCargoId() {
        return cargoId;
    }

    public void setCargoId(Integer cargoId) {
        this.cargoId = cargoId;
    }

    public String getDemandName() {
        return demandName;
    }

    public void setDemandName(String demandName) {
        this.demandName = demandName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
