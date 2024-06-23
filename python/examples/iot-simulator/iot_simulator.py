import requests
import time
from datetime import datetime
from json import dumps
from random import uniform
from uuid import uuid4

HEADERS = {'content-type': 'application/json'}

def get_id():
    return uuid4()

def get_timestamp():
    return datetime.now().isoformat(timespec='milliseconds')

def get_coordinate(old):
    new = old + uniform(-0.5, 0.5)
    if new > 10:
        return 10
    if new < -10:
        return -10
    return new

DEVICE_COUNT = 5

DEVICE_STATUSES = {}

def main():
    for _ in range(0, DEVICE_COUNT):
        id = str(uuid4())
        ts = get_timestamp()
        lon = uniform(-10.0, 10.0)
        lat = uniform(-10.0, 10.0)
        DEVICE_STATUSES[id] = {'id': id, 'timestamp': ts, 'lon': lon, 'lat': lat}

    while True:
        for id, value in DEVICE_STATUSES.items():
            lon = get_coordinate(DEVICE_STATUSES[id]['lon'])
            lat = get_coordinate(DEVICE_STATUSES[id]['lat'])
            ts = get_timestamp()
            value['timestamp'] = ts
            value['lon'] = lon
            value['lat'] = lat
            response = requests.post('http://java-devcontainer:12345/api/v1/location',data=dumps(DEVICE_STATUSES[id]))
            print(f'{id}: response {response.status_code}')
        time.sleep(1)

if __name__ == '__main__':
    main()