import psycopg

'''
This helper script creates the
iot table into the Postgres database.
'''

with psycopg.connect('host=data-toolbox-postgres-1 port=5432 dbname=postgres user=postgres password=postgres') as db_connection:
    with db_connection.cursor() as db_cursor:
        db_cursor.execute('''
                    CREATE TABLE iot (
                        id text,
                        timestamp TIMESTAMP,
                        lon float8,
                        lat float8,
                        filtered_lon float8,
                        filtered_lat float8,
                        PRIMARY KEY (id, timestamp))
                    ''')

        db_connection.commit()