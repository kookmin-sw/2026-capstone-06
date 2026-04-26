# Current Status (Updated 2026-04-26)

## Features Completed
- Implemented `/api/hospital` CRUD endpoints according to API documentation.
- Integrated dynamic QueryDSL pagination and sorting into `HospitalRepository` for optimized searches on `Hospital`.
- Created robust Request / Response DTOs using Java Records to exactly map external client signatures.
- **[2026-04-26]** Implemented `/api/dashboard/*` endpoints based on the `대시보드 데이터_DataAPI명세` API doc (Todo 002).
- **[2026-04-26]** Built `DashboardController` and `DashboardService` integrating `Device`, `Serial`, and `Code` domains.
- **[2026-04-26]** Integrated `InfluxDBClient` into newly created `DashboardSensorRepository` for active queries against the Influx sensor data timeseries.

## Next Steps
- Waiting for next `todo/` assignment.
- Build Integration tests for `DashboardController` and `DashboardService`.
- Address any InfluxDB bucket/schema adjustments once IoT publishing (Task-4) begins testing.
- Continue ensuring `PET_API명세.xlsx` syncs with entity constraints.
