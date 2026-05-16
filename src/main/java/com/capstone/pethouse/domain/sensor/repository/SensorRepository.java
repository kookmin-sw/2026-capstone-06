package com.capstone.pethouse.domain.sensor.repository;

import com.capstone.pethouse.domain.sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
}
