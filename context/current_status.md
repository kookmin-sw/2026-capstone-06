# Current Status (Updated 2026-05-02)

## Features Completed
- Implemented `/api/hospital` CRUD endpoints according to API documentation.
- Integrated dynamic QueryDSL pagination and sorting into `HospitalRepository` for optimized searches on `Hospital`.
- Created robust Request / Response DTOs using Java Records to exactly map external client signatures.
- **[2026-04-26]** Implemented `/api/dashboard/*` endpoints based on the `대시보드 데이터_DataAPI명세` API doc (Todo 002).
- **[2026-04-26]** Built `DashboardController` and `DashboardService` integrating `Device`, `Serial`, and `Code` domains.
- **[2026-04-26]** Integrated `InfluxDBClient` into newly created `DashboardSensorRepository` for active queries against the Influx sensor data timeseries.
- **[2026-04-26]** Implemented `/api/code/*` API module for hierarchical data categories (Todo 003). Recycled existing Entity and injected recursive map logic for tree rendering using CamelCase representations natively.
- **[2026-05-02]** Refactored controllers to remove redundant `try-catch` blocks, leveraging centralized exception handling via `GlobalExceptionHandler` (Todo 004).
- **[2026-05-02]** Refactored Pet info (object_code, name, birth) from `Device` to `PetHouse` entity. Established a 1:N relationship between `PetHouse` and `Device` (Todo 005).
- **[2026-05-02]** Simplified `SensorDataRes` DTO and `DashboardService` logic to remove non-sensor metadata fields and redundant DB queries.
- **[2026-05-02]** Optimized `DashboardService` by adding `findByMemberId` and `findByDeviceId` to `DeviceRepository`, replacing suboptimal `findAll().stream()` filtering.
- **[2026-05-02]** Refactored `DashboardController` to use Spring Validation (`@Valid`, `@NotBlank`) and removed manual validation and `try-catch` blocks.
- **[2026-05-02]** Added `MethodArgumentNotValidException` handler to `GlobalExceptionHandler` to support automatic validation error responses.
- **[2026-05-02]** Removed `seq` from `DeviceCreateReq` and replaced `upsertDevice` with `createDevice` in `DashboardService`.
- **[2026-05-02]** Enhanced `DashboardService.createDevice` to automatically link the new `Device` to the user's `PetHouse` (creating a default one if none exists).

## Next Steps
- Waiting for next `todo/` assignment.
- Build Integration tests for `CodeController`.
- Address any InfluxDB bucket/schema adjustments once IoT publishing (Task-4) begins testing.
- Continue ensuring `PET_API명세.xlsx` syncs with entity constraints.
