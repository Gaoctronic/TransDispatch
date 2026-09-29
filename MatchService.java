// MatchService.java
@Service
public class MatchService {

    // 距离衰减参考值 km（超过此距离得 0 分）
    private static final double MAX_DISTANCE_KM = 500.0;
    // 匹配阈值
    private static final double MATCH_THRESHOLD = 60.0;

    private final DemandRepository demandRepository;
    private final VehicleRepository vehicleRepository;

    public MatchService(DemandRepository demandRepository,
                        VehicleRepository vehicleRepository) {
        this.demandRepository = demandRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public MatchResponse calculateMatch(Integer demandId, Integer vehicleId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("车辆不存在"));

        // 1. 载重得分（40 分）
        double remainingLoad = vehicle.getVehicleType().getMaxLoadT()
                - (vehicle.getCurrentLoadT() == null ? 0 : vehicle.getCurrentLoadT());
        double cargoWeight = demand.getCargo().getWeight();
        double loadScore = cargoWeight <= remainingLoad ? 40.0
                : 40.0 * Math.max(0, remainingLoad) / cargoWeight;

        // 2. 距离得分（30 分）：车辆 → 需求起点
        Poi startPoi = demand.getStartPoi();
        double pickupDistance = haversine(
                vehicle.getCurrentLatitude(), vehicle.getCurrentLongitude(),
                startPoi.getLatitude(), startPoi.getLongitude());
        double distanceScore = 30.0 * Math.max(0, 1 - pickupDistance / MAX_DISTANCE_KM);

        // 3. 燃油得分（20 分）：续航能否覆盖 接货距离 + 运输距离
        Poi endPoi = demand.getEndPoi();
        double transDistance = haversine(
                startPoi.getLatitude(), startPoi.getLongitude(),
                endPoi.getLatitude(), endPoi.getLongitude());
        double needDistance = pickupDistance + transDistance;
        Double fuelL100km = vehicle.getVehicleType().getFuelL100km();
        double range = (vehicle.getCurrentFuelL() == null ? 0 : vehicle.getCurrentFuelL())
                / fuelL100km * 100;
        double fuelScore = range >= needDistance ? 20.0
                : 20.0 * Math.max(0, range) / needDistance;

        // 4. 状态得分（10 分）
        double statusScore = "IDLE".equalsIgnoreCase(vehicle.getStatus()) ? 10.0 : 0.0;

        double totalScore = loadScore + distanceScore + fuelScore + statusScore;

        MatchResponse resp = new MatchResponse();
        resp.setDemandId(demandId);
        resp.setVehicleId(vehicleId);
        resp.setLoadScore(loadScore);
        resp.setDistanceScore(distanceScore);
        resp.setFuelScore(fuelScore);
        resp.setStatusScore(statusScore);
        resp.setTotalScore(totalScore);
        resp.setMatched(totalScore >= MATCH_THRESHOLD);
        return resp;
    }

    // Haversine 公式计算两经纬度间球面距离（km）
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}