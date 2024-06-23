from argparse import ArgumentParser
from json import dumps, loads
from logging import basicConfig, getLogger, INFO
from sys import exit
from urllib3 import make_headers, PoolManager

GRAFANA_BASE_URL = 'http://admin:admin@localhost:3000'
SUPERSET_BASE_URL = 'http://localhost:8088'
LOG_FORMAT = '%(asctime)s %(levelname)s %(message)s'
BASE_DIR = '/'.join(__file__.split('/')[0:-1])

log = getLogger()
basicConfig(level=INFO, format=LOG_FORMAT)

def handle_error(exception):
    log.error(exception)

def get_non_filtered_vs_filtered_dashboard(data_source_uid):
    dashboard = None
    with open(f'{BASE_DIR}/grafana/non-filtered-vs-filtered.json') as f:
        dashboard_text = f.read()
        dashboard_text_replaced = dashboard_text.replace('DATASOURCE_UID_PLACEHOLDER', data_source_uid)
        dashboard = loads(dashboard_text_replaced)
    return dashboard

def initialize():
    log.info('Initializing setup')

    try:
        parser = ArgumentParser()
        parser.add_argument('action', choices=['create', 'delete'], nargs=1, help='Action on the resource')

        args = parser.parse_args()

        http = PoolManager()
    except Exception as e:
        handle_error(e)

    return args, http

def create_grafana_datasource(http, name, data):
    log.info(f'Creating datasource "{name}" for the Grafana')
    try:
        api = '/api/datasources'

        url = GRAFANA_BASE_URL + api

        headers = make_headers(basic_auth='admin:admin')
        headers['Content-Type'] = 'application/json'

        encoded_body = dumps(data)

        response_data = loads(http.request('POST', url, headers=headers, body=encoded_body).data)
        message = response_data['message']

        if message != 'Datasource added':
            raise Exception(message)
        log.info(response_data)

    except Exception as e:
        handle_error(e)

def delete_grafana_datasource(http, datasource_name):
    log.info(f'Deleting datasource "{datasource_name}" from the Grafana')
    try:
        api = f'/api/datasources/name/{datasource_name}'

        url = GRAFANA_BASE_URL + api

        headers = make_headers(basic_auth='admin:admin')

        response_data = loads(http.request('DELETE', url, headers=headers).data)
        message = response_data['message']

        if message != 'Data source deleted':
            raise Exception('Datasource not found')
        log.info(response_data)

    except Exception as e:
        handle_error(e)

def create_prometheus_datasource(http):
    name = 'prometheus'
    data = {
      'name': 'prometheus',
      'type': 'prometheus',
      'url': 'http://data-toolbox-prometheus-1:9090',
      'access': 'proxy',
      'basicAuth': True
    }
    create_grafana_datasource(http, name, data)

def create_postgres_datasource(http):
    name = 'postgres'
    data = {
        'name': 'postgres',
        'type': 'postgres',
        'url': 'data-toolbox-postgres-1',
        'access': 'proxy',
        'user': 'postgres',
        'database': 'postgres',
        'jsonData': {
          'sslmode': 'disable'
        },
        'secureJsonData': {
            'password': 'postgres'
        }
    }
    create_grafana_datasource(http, name, data)

def get_grafana_datasource_uid(http, name):
    log.info(f'Get uid for datasource "{name}"')
    try:
        api = f'/api/datasources/name/{name}'

        url = GRAFANA_BASE_URL + api

        headers = make_headers(basic_auth='admin:admin')
        headers['Content-Type'] = 'application/json'

        response_data = loads(http.request('GET', url, headers=headers).data)

        if 'message' in response_data:
            raise Exception(f'Could not find datasource "{name}"')
        return response_data['uid']
    except Exception as e:
        handle_error(e)

def create_grafana_dashboard(http, name, data):
    log.info(f'Creating dashboard "{name}" for the Grafana')
    try:
        api = '/api/dashboards/db'

        url = GRAFANA_BASE_URL + api

        headers = make_headers(basic_auth='admin:admin')
        headers['Content-Type'] = 'application/json'

        encoded_body = dumps(data)

        response_data = loads(http.request('POST', url, headers=headers, body=encoded_body).data)
        if 'message' in response_data:
            raise Exception(response_data)
        log.info(response_data)

    except Exception as e:
        handle_error(e)

def create_grafana_non_filtered_vs_filtered_dashboard(http):
    datasource_name = 'postgres'
    data_source_uid = get_grafana_datasource_uid(http, datasource_name)
    dashboard = get_non_filtered_vs_filtered_dashboard(data_source_uid)
    data = {
        'dashboard': dashboard,
        "overwrite": False
    }
    name = 'non-filtered-vs-filtered'
    create_grafana_dashboard(http, name, data)

def delete_prometheus_datasource(http):
    name = 'prometheus'
    delete_grafana_datasource(http, name)

def delete_postgres_datasource(http):
    name = 'postgres'
    delete_grafana_datasource(http, name)

def delete_grafana_non_filtered_vs_filtered_dashboard(http):
    title = 'Non-Filtered Path vs Filtered Path'
    uid = get_dashboard_uid(http, title)
    delete_grafana_dashboard(http, title, uid)

def delete_grafana_dashboard(http, title, uid):
    log.info(f'Deleting Grafana dashboard "{title}"')
    try:
        api = f'/api/dashboards/uid/{uid}'
        url = GRAFANA_BASE_URL + api

        headers = make_headers(basic_auth='admin:admin')

        response_data = loads(http.request('DELETE', url, headers=headers).data)
        message = response_data['message']

        if message == 'Dashboard not found':
            raise Exception(response_data)
        log.info(response_data)
    except Exception as e:
        handle_error(e)


def get_dashboard_uid(http, title):
    log.info(f'Searching for uid of a dashboard based on the title "{title}"')
    try:
        api = f'/api/search?query={title}'

        url = GRAFANA_BASE_URL + api

        headers = make_headers(basic_auth='admin:admin')

        response_data = loads(http.request('GET', url, headers=headers).data)
        if 'message' in response_data:
            raise Exception(response_data)
        log.info(response_data)
        if not len(response_data) > 0:
            raise Exception(f'Could not find a dashboard with title "{title}"')
        return response_data[0]['uid']

    except Exception as e:
        handle_error(e)


def get_superset_access_token(http):
    log.info(f'Getting access token for Superset')
    try:
        data = {
            'username': 'admin',
            'password': 'admin',
            'provider': 'db',
            'refresh': True
        }

        api = '/api/v1/security/login'

        url = SUPERSET_BASE_URL + api

        headers = {
            'Content-Type': 'application/json'
        }

        encoded_body = dumps(data)

        response_data = loads(http.request('POST', url, headers=headers, body=encoded_body).data)
        access_token = response_data['access_token']
        if access_token == None:
            log.error(response_data)
            raise Exception('Could not get access token for the Superset')
        return access_token

    except Exception as e:
        handle_error(e)

def create_superset_database_connection(http, access_token):
    log.info(f'Creating database connection to Postgres for Superset')
    try:
        data = {
            'database_name': 'postgres',
            'sqlalchemy_uri': 'postgresql+psycopg2://postgres:postgres@data-toolbox-postgres-1:5432/postgres',
            'expose_in_sqllab': True
        }
        api = '/api/v1/database/'

        url = SUPERSET_BASE_URL + api

        headers = {
            'Authorization': f'Bearer {access_token}',
            'Content-Type': 'application/json'
        }

        encoded_body = dumps(data)

        response_data = loads(http.request('POST', url, headers=headers, body=encoded_body).data)
        if not 'result' in response_data:
            log.error(response_data)
            raise Exception('Could not create database connection into the Superset')
        log.info(response_data)

    except Exception as e:
        handle_error(e)

def get_database_connection_ids_from_superset(http, access_token):
    log.info(f'Get database connection ids from the Superset')
    try:
        api = '/api/v1/database/'

        url = SUPERSET_BASE_URL + api

        headers = {
            'Authorization': f'Bearer {access_token}'
        }

        response_data = loads(http.request('GET', url, headers=headers).data)
        ids = response_data['ids']
        if ids == None:
            log.error(response_data)
            raise Exception('Could not get database connection ids from the Superset')
        return ids

    except Exception as e:
        handle_error(e)

def delete_database_connections_from_superset(http, access_token, ids):
    log.info(f'Deleting database connections from the Superset')
    try:
        api = '/api/v1/database/'

        url = SUPERSET_BASE_URL + api

        headers = {
            'Authorization': f'Bearer {access_token}'
        }

        for id in ids:
            url = SUPERSET_BASE_URL + api + str(id)
            response_data = loads(http.request('DELETE', url, headers=headers).data)
            message = response_data['message']
            if message != 'OK':
                log.error(response_data)
                raise Exception(f'Could not delete database connection "{id}" from the Superset')
            log.info(response_data)

    except Exception as e:
        handle_error(e)

def main():
    args, http = initialize()

    if args.action[0] == 'create':
        create_prometheus_datasource(http)
        create_postgres_datasource(http)
        create_grafana_non_filtered_vs_filtered_dashboard(http)
        access_token = get_superset_access_token(http)
        create_superset_database_connection(http, access_token)
    elif args.action[0] == 'delete':
        delete_grafana_non_filtered_vs_filtered_dashboard(http)
        delete_postgres_datasource(http)
        delete_prometheus_datasource(http)
        access_token = get_superset_access_token(http)
        ids = get_database_connection_ids_from_superset(http, access_token)
        if len(ids) > 0:
            delete_database_connections_from_superset(http, access_token, ids)

if __name__ == '__main__':
    main()
