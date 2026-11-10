package com.carvaluation.vehicle;

import com.carvaluation.auth.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getVehiclesByUser(User user) {
        return vehicleRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public Optional<Vehicle> getVehicleByIdAndUser(Long id, User user) {
        return vehicleRepository.findByIdAndUserId(id, user.getId());
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    @Transactional
    public Vehicle saveVehicle(Vehicle vehicle, User user) {
        vehicle.setUser(user);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id, User user) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe hoặc bạn không có quyền xóa"));
        vehicleRepository.delete(vehicle);
    }
}
