import psycopg

'''
This helper script deletes the
iot table from the default postgres
database.
'''

with psycopg.connect('host=data-toolbox-postgres-1 port=5432 dbname=postgres user=postgres password=postgres') as db_connection:
    with db_connection.cursor() as db_cursor:
        db_cursor.execute('''
                    DROP TABLE iot
                    ''')

        db_connection.commit()