#!/bin/bash
set -e

# InfluxDB 2.x Initialization Script
# 배치 write 방식: 모든 포인트를 단일 influx write 호출로 처리
echo "Starting InfluxDB data initialization..."

NOW=$(date +%s)

# 온도 패턴 배열 (case 대신 배열 사용)
# 형식: "TEMP HUM CO2 LIGHT"
PATTERN_01=(
  "24.2 58.1 412 0"
  "24.5 57.8 415 0"
  "24.8 57.5 418 50"
  "25.3 56.9 420 200"
  "26.1 56.2 422 400"
  "27.0 55.5 425 600"
  "27.8 54.8 428 800"
  "28.2 54.3 430 700"
  "27.5 55.0 427 500"
  "26.5 55.8 423 300"
  "25.5 56.5 419 100"
  "24.8 57.2 414 10"
)

PATTERN_02=(
  "23.5 60.2 408 0"
  "23.8 59.8 410 0"
  "24.2 59.3 413 150"
  "24.9 58.7 416 350"
  "25.7 58.0 419 550"
  "26.3 57.4 421 650"
  "25.8 57.9 418 450"
  "24.5 59.0 412 50"
)

PATTERN_03=(
  "20.5 65.1 405 0"
  "20.8 64.8 407 0"
  "21.2 64.3 410 100"
  "21.8 63.7 413 300"
  "22.5 63.0 416 500"
  "23.0 62.4 418 600"
  "22.7 62.8 417 500"
  "22.0 63.5 414 300"
  "21.4 64.1 411 100"
  "20.8 64.7 408 10"
)

# 배치 데이터 생성 후 단일 write 호출
{
  # PET-HOUSE-01: 최근 24시간, 10분(600초) 간격
  DEVICE="PET-HOUSE-01"
  LEN=${#PATTERN_01[@]}
  for i in $(seq 0 10 1440); do
    T=$((NOW - i * 60))
    IDX=$(( (i / 10) % LEN ))
    read -r TEMP HUM CO2 LIGHT <<< "${PATTERN_01[$IDX]}"
    echo "sensor_data,device_id=${DEVICE} temperature=${TEMP},humidity=${HUM},co2=${CO2},light=${LIGHT} ${T}"
  done

  # PET-HOUSE-02: 24시간 전 ~ 12시간 전 구간, 10분 간격
  DEVICE="PET-HOUSE-02"
  LEN=${#PATTERN_02[@]}
  OFFSET=$((24 * 60))
  for i in $(seq 0 10 720); do
    T=$((NOW - OFFSET * 60 + i * 60))
    IDX=$(( (i / 10) % LEN ))
    read -r TEMP HUM CO2 LIGHT <<< "${PATTERN_02[$IDX]}"
    echo "sensor_data,device_id=${DEVICE} temperature=${TEMP},humidity=${HUM},co2=${CO2},light=${LIGHT} ${T}"
  done

  # PET-HOUSE-03: 최근 24시간, 15분(900초) 간격
  DEVICE="PET-HOUSE-03"
  LEN=${#PATTERN_03[@]}
  for i in $(seq 0 15 1440); do
    T=$((NOW - i * 60))
    IDX=$(( (i / 15) % LEN ))
    read -r TEMP HUM CO2 LIGHT <<< "${PATTERN_03[$IDX]}"
    echo "sensor_data,device_id=${DEVICE} temperature=${TEMP},humidity=${HUM},co2=${CO2},light=${LIGHT} ${T}"
  done

  # 모션 이벤트 - PET-HOUSE-01
  DEVICE="PET-HOUSE-01"
  for i in 5 30 90 180 360 480 720 900 1080; do
    T=$((NOW - i * 60))
    echo "motion_event,device_id=${DEVICE} detected=1 ${T}"
  done

  # 모션 이벤트 - PET-HOUSE-03
  DEVICE="PET-HOUSE-03"
  for i in 10 60 200 500 800; do
    T=$((NOW - i * 60))
    echo "motion_event,device_id=${DEVICE} detected=1 ${T}"
  done

} | influx write \
    -b "${DOCKER_INFLUXDB_INIT_BUCKET}" \
    -o "${DOCKER_INFLUXDB_INIT_ORG}" \
    -t "${DOCKER_INFLUXDB_INIT_ADMIN_TOKEN}" \
    --precision s

echo "InfluxDB data initialization completed."
