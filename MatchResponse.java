// RevenueService.java
@Service
public class RevenueService {

    // 运价：元 / 吨·公里（可放入配置文件）
    private static final double PRICE_PER_TON_KM = 2.0;
    // 油价：元 / L
    private static final double FUEL_PRICE = 7.5;

    private final DemandRepository demandRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;

    public RevenueService(DemandRepository demandRepository,
                          VehicleRepository vehicleRepository,
                          RouteRepository routeRepository) {
        this.demandRepository = demandRepository;
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
    }

    public RevenueResponse calculateRevenue(Integer demandId, Integer vehicleId, Integer routeId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("车辆不存在"));
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("路线不存在"));

        Double distance = route.getDistance();
        Double cargoWeight = demand.getCargo().getWeight();
        Double fuelL100km = vehicle.getVehicleType().getFuelL100km();

        // 收入 = 货物重量 × 距离 × 单价
        double income = cargoWeight * distance * PRICE_PER_TON_KM;
        // 燃油成本 = 距离 / 100 × 百公里油耗 × 油价
        double fuelCost = distance / 100 * fuelL100km * FUEL_PRICE;
        // 路线固定成本（路桥费等）
        double routeCost = route.getTransCost() == null ? 0 : route.getTransCost();

        double totalCost = fuelCost + routeCost;
        double profit = income - totalCost;

        RevenueResponse resp = new RevenueResponse();
        resp.setDemandId(demandId);
        resp.setVehicleId(vehicleId);
        resp.setRouteId(routeId);
        resp.setDistance(distance);
        resp.setCargoWeight(cargoWeight);
        resp.setIncome(income);
        resp.setFuelCost(fuelCost);
        resp.setRouteCost(routeCost);
        resp.setTotalCost(totalCost);
        resp.setProfit(profit);
        return resp;
    }
}
