import json
import random
import time

import paho.mqtt.client as mqtt

BROKER_ADDRESS = "3.35.226.221"
BROKER_PORT = 1883

HOUSE_ID = 9
DEVICE_ID = "DEV-20260520-DEV-122"

TOPIC_PUB_SENSOR = f"pet/{HOUSE_ID}/sensor/data"
TOPIC_SUB_COMMAND = f"pet/{HOUSE_ID}/device/command"
TOPIC_PUB_ACK = f"pet/{HOUSE_ID}/device/ack"

def on_connect(client, userdata, flags, reason_code, properties=None):
    print(f"✅ MQTT 연결됨 (code={reason_code})")
    client.subscribe(TOPIC_SUB_COMMAND)
    print(f"📥 구독: {TOPIC_SUB_COMMAND}")

def on_message(client, userdata, msg):
    """백엔드에서 보낸 device/command 메시지를 수신하여 처리 후 ACK 응답"""
    try:
        payload = json.loads(msg.payload.decode())
        command_id = payload.get("commandId")
        action = payload.get("action")
        params = payload.get("params", {})

        print(f"\n📥 [명령 수신] topic={msg.topic}")
        print(f"    commandId={command_id}, action={action}, params={params}")

        if not command_id or not action:
            print("    ⚠️ commandId 또는 action이 없습니다. 무시합니다.")
            return

        # 명령 처리 시뮬레이션
        success = True
        message = ""

        if action == "SUPPLY_FOOD":
            amount = params.get("amount", "알 수 없음")
            unit = params.get("unitType", "g")
            print(f"    🍚 급식 실행 중... (amount={amount}{unit})")
            time.sleep(1)  # 급식 동작 시뮬레이션
            message = f"급식 완료: {amount}{unit}"
            print(f"    ✅ {message}")

        elif action == "SUPPLY_WATER":
            amount = params.get("amount", "알 수 없음")
            unit = params.get("unitType", "ml")
            print(f"    💧 급수 실행 중... (amount={amount}{unit})")
            time.sleep(1)  # 급수 동작 시뮬레이션
            message = f"급수 완료: {amount}{unit}"
            print(f"    ✅ {message}")

        elif action == "FAN_ON":
            speed = params.get("speed", "AUTO")
            print(f"    🌀 팬 켜기... (speed={speed})")
            time.sleep(0.5)
            message = f"팬 가동: speed={speed}"
            print(f"    ✅ {message}")

        elif action == "FAN_OFF":
            print("    🌀 팬 끄기...")
            time.sleep(0.5)
            message = "팬 정지 완료"
            print(f"    ✅ {message}")

        else:
            print(f"    ⚠️ 알 수 없는 action: {action}")
            success = False
            message = f"지원하지 않는 명령: {action}"

        # ACK 응답 발행
        ack_payload = json.dumps({
            "commandId": command_id,
            "success": success,
            "message": message,
        })
        client.publish(TOPIC_PUB_ACK, ack_payload)
        print(f"    📤 [ACK] {TOPIC_PUB_ACK} → {ack_payload}")

    except json.JSONDecodeError as e:
        print(f"    ❌ JSON 파싱 실패: {e}")
    except Exception as e:
        print(f"    ❌ 명령 처리 오류: {e}")

def main():
    client = mqtt.Client(
        mqtt.CallbackAPIVersion.VERSION2,
        client_id=f"VirtualPetHouse_{HOUSE_ID}",
    )
    client.on_connect = on_connect
    client.on_message = on_message

    print(f"🐾 가상 펫하우스 시작 (houseId={HOUSE_ID}, deviceId={DEVICE_ID})")
    client.connect(BROKER_ADDRESS, BROKER_PORT)
    client.loop_start()

    try:
        while True:
            sensor_data = {
                "device_id": DEVICE_ID,
                "tem_val": round(random.uniform(10.0, 60.0), 1),
                "hum_val": round(random.uniform(10.0, 60.0), 1),
                "co_val": round(random.uniform(400.0, 500.0), 1),
            }
            payload = json.dumps(sensor_data)
            client.publish(TOPIC_PUB_SENSOR, payload)
            print(f"📤 [센서] {TOPIC_PUB_SENSOR} → {payload}")
            time.sleep(5)
    except KeyboardInterrupt:
        print("\n🛑 종료")
    finally:
        client.loop_stop()
        client.disconnect()

if __name__ == "__main__":
    main()
