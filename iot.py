import wiotp.sdk.device
import RPi.GPIO as GPIO
# import Adafruit_DHT
import adafruit_dht
import board
import time
from time import sleep

myConfig = {
    "identity": {
        "orgId": "ymmd2u",
        "typeId": "Raspberry_Pi",
        "deviceId": "kyong_pi"
    },
    "auth": {
        "token": "qwer1234"
    }
}

# def myCommandCallback(msg):
# sensor = Adafruit_DHT.DHT11
trig = 12
echo = 17
danger_led = 20
# tem_hum = 4
tem_check = time.time()
switch = 23
flag = 0
loc = 0
safe_time = time.time()
danger_time = time.time()
GPIO.setmode(GPIO.BCM)
GPIO.setup(trig, GPIO.OUT)
GPIO.setup(echo, GPIO.IN)
GPIO.setup(danger_led, GPIO.OUT)
GPIO.setup(switch, GPIO.IN, GPIO.PUD_UP)
client = wiotp.sdk.device.DeviceClient(config=myConfig, logHandlers=None)
client.connect()


dhtDevice = adafruit_dht.DHT11(board.D18)

temperature_c = dhtDevice.temperature
humidity = dhtDevice.humidity

while True:
    myData = {"Name": "ChoonSam", "Age": 68,"Phone":"010-1987-2021"}
    if loc == 0:
        GPIO.output(trig, False)
        sleep(0.5)
        GPIO.output(trig, True)
        sleep(0.00001)
        GPIO.output(trig, False)

        while GPIO.input(echo) == 0:
            start = time.time()
        while GPIO.input(echo) == 1:
            stop = time.time()
        myData["IO"]="Indoor"
        time_interval = stop - start
        distance = time_interval * 17000
        distance = round(distance, 2)

        print(distance, "cm")
        myData["Distance"] = distance
        
        
        
        # humidity, temperature = Adafruit_DHT.read_retry(sensor,tem_hum)

        # print(
        #     "Temp: {:.1f} C    Humidity: {}% ".format(
        #         temperature_c, humidity
        #     )
        # )
        
        # myData["Temperature"] = temperature_c
        # myData["Humidity"] = humidity
        if distance < 20:
            flag = 0
            safe_time = time.time()
            # myData = {"Name":"ChoonSam","Age":68,"I/O":"Indoor","distancce":distance,"safety":"safe"}
            # myData["Safety"] = "Safe"
            # myData["Time"]=0
            # client.publishEvent(eventId="us_dist",msgFormat="json",data=myData,qos=0,onPublish=None)
            GPIO.output(danger_led, GPIO.LOW)
        elif time.time()-safe_time > 10:
            # myData = {"Name":"ChoonSam","Age":68,"I/O":"Indoor","distance":distance,"safety":"danger"}
            # myData["Safety"] = "Dangerous"
            # if flag == 0:
            #     danger_time = time.time()
            #     flag = 1
            # myData["Time"] = time.time() - danger_time
            # client.publishEvent(eventId="us_dist",msgFormat="json",data=myData,qos=0,onPublish=None)
            GPIO.output(danger_led, GPIO.HIGH)
        else:
            # myData["Time"] = time.time()-safe_time
            pass
        if GPIO.input(switch) == 0:
            # myData = {"Name":"ChoonSam","Age":68,"I/O":"Indoor","distance":distance,"safety":"safe"}

            safe_time = time.time()
            # client.publishEvent(eventId="us_dist",msgFormat="json",data=myData,qos=0,onPublish=None)
            # myData["Safety"] = "Safe"
            # myData["Time"] = 0
            loc = not loc
            GPIO.output(danger_led, GPIO.LOW)
        
        
    else:
        myData["IO"] ="Outdoor"
        myData["Distance"] = -1
        if GPIO.input(switch) == 0:
            loc = not loc
    if time.time() - tem_check >= 10:
        try:
            temperature_c = dhtDevice.temperature
            humidity = dhtDevice.humidity
            print(
                "Temp: {:.1f} C    Humidity: {}% ".format(
                    temperature_c, humidity
                )
            )
        except RuntimeError as error:
            print(error.args[0])
            sleep(2.0)
            continue
        except Exception as error:
            dhtDevice.exit()
            raise error
        tem_check = time.time()
    myData["Temperature"] = temperature_c
    myData["Humidity"] = humidity
    client.publishEvent(eventId="us_dist", msgFormat="json",
                            data=myData, qos=0, onPublish=None)
    sleep(0.5)
client.disconnect()
