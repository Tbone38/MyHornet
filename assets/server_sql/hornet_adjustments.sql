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
note TEXT,
CONSTRAINT unique_username UNIQUE(username),
CONSTRAINT unique_db_name UNIQUE(db_name),
CONSTRAINT unique_email UNIQUE(email_address));
 
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

	DROP FUNCTION IF EXISTS generate_password();
	CREATE OR REPLACE FUNCTION generate_random(length INTEGER) RETURNS TEXT AS $$
	DECLARE
		result TEXT;
	BEGIN
		select array_to_string(array(select substr('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', 
			trunc(random() * 61)::integer + 1, 1) 
			FROM generate_series(1, length)), '')
		INTO result;

		RETURN result;
	END;
	$$ LANGUAGE plpgsql;

	CREATE OR REPLACE FUNCTION generate_username(organisation TEXT) RETURNS TEXT AS $$
	DECLARE
		username TEXT;
	BEGIN
		username := substring(organisation FROM 0 FOR char_length(organisation)+1);
		username := username||(SELECT generate_random(4));
		-- we grab the first x? characters from organisation, and append 3-4 random characters (from our generator).
		RETURN username;
	END;
	$$ LANGUAGE plpgsql;

	CREATE OR REPLACE FUNCTION generate_dbname(username TEXT) RETURNS TEXT AS $$
	DECLARE
		dbname TEXT;
	BEGIN
		dbname:='g_'||replace(username, ' ', '_');
		--db_names should be all lower-case?
		return lower(dbname);
	END;
	$$ LANGUAGE plpgsql;

	--password reset calls this function.
	CREATE OR REPLACE FUNCTION reset_password(input_email TEXT) RETURNS TEXT AS $$
	DECLARE
		passwd TEXT;
	BEGIN
		IF input_email IS NOT NULL THEN
			UPDATE meta_db_list SET (password) = ((SELECT generate_random(6))) WHERE
				email_address = input_email RETURNING password INTO passwd;
		END IF;
		RETURN passwd;
	END;
	$$ LANGUAGE plpgsql;
	
	CREATE OR REPLACE FUNCTION generate_weburl(name TEXT, server TEXT) RETURNS TEXT AS $$
	DECLARE
		weburl TEXT;
	BEGIN
		weburl := name||'.'||server;
		return weburl;
	END;
	$$ LANGUAGE plpgsql;

--TRIGGERS
	CREATE OR REPLACE FUNCTION meta_db_list_insert() RETURNS trigger AS $$
	BEGIN
		SELECT now()::DATE INTO new.created;
		SELECT now() INTO new.lastupdate;
		SELECT generate_random(6) INTO new.password;
		RETURN new;
	END;
	$$ LANGUAGE plpgsql;

	CREATE TRIGGER meta_db_list_insert BEFORE INSERT ON meta_db_list
		FOR EACH ROW EXECUTE PROCEDURE meta_db_list_insert();

	CREATE OR REPLACE FUNCTION meta_db_list_update() RETURNS trigger AS $$
	BEGIN
		SELECT generate_dbname(new.username) INTO new.db_name;
		SELECT generate_weburl(new.username, new.db_server) INTO new.weburl;
		SELECT now() INTO new.lastupdate;
		RETURN new;
	END;
	$$ LANGUAGE plpgsql;

	CREATE TRIGGER meta_db_list_update BEFORE UPDATE ON meta_db_list
		FOR EACH ROW WHEN (old.username IS NULL AND new.username IS NOT NULL
			AND old.db_server IS NULL AND new.db_server IS NOT NULL
			AND new.db_name IS NULL AND new.weburl IS NULL)
		EXECUTE PROCEDURE meta_db_list_update();

	CREATE OR REPLACE FUNCTION db_list_update_timestamp() RETURNS trigger AS $$
	BEGIN
		SELECT now() INTO new.lastupdate;
		RETURN new;
	END;
	$$ LANGUAGE plpgsql;

	CREATE TRIGGER db_list_update_timestamp BEFORE UPDATE ON meta_db_list
		FOR EACH ROW EXECUTE PROCEDURE db_list_update_timestamp();

--below insert test, prints when we violate a unique constraint.
/*DO
$do$
DECLARE 
v	INTEGER;
BEGIN	
	FOR i IN 1..125 LOOP
		v := i;
		INSERT INTO meta_db_list (email_address, organisation) VALUES ('callum@treshna.com', 'Treshna');
	END LOOP;
EXCEPTION WHEN unique_violation THEN
	RAISE NOTICE 'i = %',v; 
END
$do$;*/

COMMIT TRANSACTION;

