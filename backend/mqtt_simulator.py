import json
import random
import time

import paho.mqtt.client as mqtt

BROKER_ADDRESS = "127.0.0.1"
BROKER_PORT = 1883

HOUSE_ID = 8
DEVICE_ID = "DEV-20260520-DEV-501"

TOPIC_PUB_SENSOR = f"pet/{HOUSE_ID}/sensor/data"
TOPIC_SUB_COMMAND = f"pet/{HOUSE_ID}/device/command"
TOPIC_PUB_ACK = f"pet/{HOUSE_ID}/device/ack"

def on_connect(client, userdata, flags, reason_code, properties=None):
    print(f"✅ MQTT 연결됨 (code={reason_code})")
    client.subscribe(TOPIC_SUB_COMMAND)
    print(f"📥 구독: {TOPIC_SUB_COMMAND}")

def on_message(client, userdata, msg):
    pass

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
                "tem_val": round(random.uniform(20.0, 25.0), 1),
                "hum_val": round(random.uniform(40.0, 60.0), 1),
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
