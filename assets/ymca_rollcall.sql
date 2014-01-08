--SQL to create roll-tables in the YMCA database.
-- Create our two tables.
CREATE TABLE IF NOT EXISTS roll (
	id SERIAL PRIMARY KEY,
	name TEXT,
	datetime TIMESTAMP WITHOUT TIME ZONE,
	created  TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
	);
--stuff hangs when broken queries are left altering the database.
CREATE TABLE IF NOT EXISTS roll_item (
	id SERIAL PRIMARY KEY NOT NULL,
	rollid INTEGER NOT NULL REFERENCES roll(id),
	memberid INTEGER NOT NULL REFERENCES member(id),
	attended BOOLEAN DEFAULT FALSE,
	created TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
	lastupdate TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW());

BEGIN TRANSACTION;
--create triggers;
CREATE OR REPLACE FUNCTION roll_delete() RETURNS trigger AS $$
BEGIN
	INSERT INTO deleted_record(tablename, columnname, deletedid) VALUES ('roll', 'id', old.id);
	RETURN old;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER roll_delete AFTER DELETE ON roll
	FOR EACH ROW EXECUTE PROCEDURE roll_delete();

CREATE OR REPLACE FUNCTION roll_item_delete() RETURNS trigger AS $$
BEGIN
	INSERT INTO deleted_record(tablename, columnname, deletedid) VALUES ('roll_item','id', old.id);
	RETURN old;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER roll_item_delete AFTER DELETE ON roll_item
	FOR EACH ROW EXECUTE PROCEDURE roll_item_delete();

CREATE OR REPLACE FUNCTION roll_item_update() RETURNS TRIGGER AS $$
BEGIN
	SELECT now() INTO new.lastupdate;
	RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER roll_item_update BEFORE UPDATE ON roll_item
	FOR EACH ROW EXECUTE PROCEDURE roll_item_update();

COMMIT TRANSACTION;
