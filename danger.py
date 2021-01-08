import wiotp.sdk.application
import wiotp.sdk.application as app
import json
import time

appConfig = {
    "auth": {
        "key": "a-ymmd2u-aov7srgf6g",
        "token": "y7P0bwvO+Hhlef8Qwa"
    }
}

safeTime = time.time()


def myEventCallback(event):
    global safeTime, in_out_status
    str = "%s event '%s' received from device [%s]: %s"
    data = event.data

    pub = {}
#    pub["Name"] = data["Name"]
#    pub["Age"] = data["Age"]
    pub["IO"] = data["IO"]
    pub["Temperature"] = data["Temperature"]
    pub["Humidity"] = data["Humidity"]

    if data["IO"] == "Indoor":
        in_out_status = True
        if data["Distance"] < 20:
            safeTime = time.time()

        # 움직임x, 온도, 습도 문제
        elif time.time()-safeTime > 10 and pub["Temperature"] not in range(17, 26) and pub["Humidity"] >= 50:
            dm_time = time.time() - safeTime

            pub["Time"] = dm_time
            pub["Danger_code"] = "3"
            app_client.publishEvent(typeId="Raspberry_Pi", deviceId="kyong_pi",
                                    eventId="danger_signal", msgFormat="json", data=pub, qos=0, onPublish=None)

            print(pub)
        elif pub["Temperature"] not in range(17, 26) and pub["Humidity"] >= 50:
            dm_time = time.time() - safeTime

            pub["Time"] = dm_time
            pub["Danger_code"] = "2"
            app_client.publishEvent(typeId="Raspberry_Pi", deviceId="kyong_pi",
                                    eventId="danger_signal", msgFormat="json", data=pub, qos=0, onPublish=None)

            print(pub)

        elif time.time()-safeTime > 10:
            dm_time = time.time() - safeTime

            pub["Time"] = dm_time
            pub["Danger_code"] = "1"
            app_client.publishEvent(typeId="Raspberry_Pi", deviceId="kyong_pi",
                                    eventId="danger_signal", msgFormat="json", data=pub, qos=0, onPublish=None)

            print(pub)

    else:
        if in_out_status:
            app_client.publishEvent(typeId="Raspberry_Pi", deviceId="kyong_pi",
                                    eventId="danger_signal", msgFormat="json", data=pub, qos=0, onPublish=None)
            in_out_status = False

    # print(str % (event.format, event.eventId,
    #             event.device, json.dumps(event.data)))


in_out_status = True
app_client = app.ApplicationClient(config=appConfig)
app_client.connect()
app_client.deviceEventCallback = myEventCallback
app_client.subscribeToDeviceEvents(
    typeId='Raspberry_Pi', deviceId='kyong_pi', eventId='us_dist')


while True:
    pass
