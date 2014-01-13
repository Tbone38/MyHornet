-- This script creates tables that are needed on the central server for handling
-- new features of the Hornet(GymMaster Mobile) client.
        -- The new cloud-based dynamic building of databases. (meta-table containing info on each database).
        -- The Logging of database sync errors.
CREATE TABLE IF NOT EXISTS meta_db_list(id SERIAL PRIMARY KEY,
weburl TEXT,email_address TEXT,
db_server TEXT,db_name TEXT,
username TEXT, password TEXT,
organisation TEXT,
created DATE, lastupdate TIMESTAMP WITHOUT TIME ZONE,
note TEXT);
 
CREATE TABLE IF NOT EXISTS hornet_errorlog(id SERIAL PRIMARY KEY,
error TEXT, lastupdate TIMESTAMP WITHOUT TIME ZONE,
uploader TEXT);


--CREATE THE FUNCTIONS.
BEGIN TRANSACTION;

	CREATE OR REPLACE FUNCTION hornet_errorlog_update() RETURNS trigger AS $$
	BEGIN
        	SELECT now() INTO new.lastupdate;
		RETURN new;
	END;
	$$ LANGUAGE plpgsql;
 
	CREATE TRIGGER hornet_errorlog_update BEFORE UPDATE ON hornet_errorlog
        	FOR EACH ROW EXECUTE PROCEDURE hornet_errorlog_update();

	CREATE OR REPLACE FUNCTION generate_password() RETURNS TEXT AS $$
	DECLARE
		result TEXT;
	BEGIN
		select array_to_string(array(select substr('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', 
			trunc(random() * 61)::integer + 1, 1) 
			FROM generate_series(1, 6)), '')
		INTO result;

		RETURN result;
	END;
	$$ LANGUAGE plpgsql;

	CREATE OR REPLACE FUNCTION generate_username(organisation TEXT) RETURNS TEXT AS $$
	DECLARE
		username TEXT;
	BEGIN
		--how do we generate this?
		RETURN username;
	END;
	$$ LANGUAGE plpgsql;

	--password reset calls this function.
	CREATE OR REPLACE FUNCTION reset_password(input_email TEXT) RETURNS VOID AS $$
	BEGIN
		IF input_email IS NOT NULL THEN
			UPDATE meta_db_list SET (password) = ((SELECT generate_password())) WHERE
				email = input_email;
		END IF;
	END;
	$$ LANGUAGE plpgsql;

	CREATE OR REPLACE FUNCTION meta_db_list_insert() RETURNS trigger AS $$
	BEGIN
		SELECT now()::DATE INTO new.created;
		SELECT now() INTO new.lastupdate;
		SELECT generate_password() INTO new.password;
		--how do we generate the username?
		--probably going to notify someone here to create us a database.
		--?
		RETURN new;
	END;
	$$ LANGUAGE plpgsql;

	CREATE TRIGGER meta_db_list_insert BEFORE INSERT ON meta_db_list
		FOR EACH ROW EXECUTE PROCEDURE meta_db_list_insert();

COMMIT TRANSACTION;

