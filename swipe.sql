
/* TODO: 	reproduce the SWIPE deny/grant logic on SQLite. then create a function to 
		handle the insertion with timestamps.*/
--   _     _                   _     __                  _   _
--  (_) __| | ___ __ _ _ __ __| |   / _|_   _ _ __   ___| |_(_) ___  _ __  ___
--  | |/ _` |/ __/ _` | '__/ _` |  | |_| | | | '_ \ / __| __| |/ _ \| '_ \/ __|
--  | | (_| | (_| (_| | | | (_| |  |  _| |_| | | | | (__| |_| | (_) | | | \__ \
--  |_|\__,_|\___\__,_|_|  \__,_|  |_|  \__,_|_| |_|\___|\__|_|\___/|_| |_|___/
--

-- Manually cause a swipe action for the member-id specified. 
-- called from membership.xml
CREATE OR REPLACE FUNCTION upd_visit(card INTEGER, msid INTEGER, visit1 INTEGER, visit2 INTEGER) RETURNS INTEGER AS $_$
DECLARE
	cno INTEGER;
	mid INTEGER;
BEGIN
	IF visit1 != visit2 THEN
		IF card IS NULL THEN
			SELECT member.cardno INTO cno FROM member,membership 
			WHERE member.id = membership.memberid AND membership.id = msid;
		ELSE
			cno = card;
		END IF;
		SELECT member.id INTO mid FROM member WHERE cardno=cno;

		IF cno IS NOT NULL THEN
			INSERT INTO entryexit(cardno, memberid, membershipid,access) values(cno, mid, msid);
		END IF;
	END IF;
	RETURN 0;
END; $_$ LANGUAGE plpgsql;

-- called from dead code in dashboard.xml.
-- called from swipe_manual()
CREATE OR REPLACE FUNCTION swipe_manual_ms(doornumber INTEGER, msid INTEGER, mid INTEGER) RETURNS INTEGER AS $$
DECLARE
	rval        INTEGER;
	happ        INTEGER;
	icons       TEXT := '';
	confrec     config;
	doorrec     door;
	cardid      INTEGER;
	msg         TEXT := '';
	msg2        TEXT := '';
	msg3        TEXT;
	cardtmp     INTEGER;
	book        RECORD;
	rek         RECORD;
	owe         MONEY;
	sound       INTEGER := 0;
	msgcol      TEXT := '00ff00';
	resp        RECORD;
	rejectnotes TEXT;
BEGIN
	--Reject if no membership is selected.
	IF msid IS NULL THEN 
		RAISE EXCEPTION 'Cannot check in member without valid membership';
		RETURN NULL;
	END IF;
	--Reject if no door is selected
	IF doornumber IS NULL THEN
		RAISE EXCEPTION 'Must select a door to check in the member';
		RETURN NULL;
	END IF;

	-- config stuff
	SELECT * INTO confrec FROM config ORDER BY id ASC LIMIT 1;
	SELECT * INTO doorrec FROM door WHERE id = doornumber LIMIT 1;

	--~ SELECT INTO conf warningonowe AS warn, stoponowe, showstats, showdollar,
	--~ stopafterowe, warnafterowe, showvisitstats, swipe_timeout, treatcustom1asname,
	--~ door.*,
	--~ graceperiod * interval '1 day' as graceperiod
	--~ FROM config, door WHERE door.id = doornumber;

	IF doorrec.checkout = TRUE THEN
		-- Bypassing most of the tests if the door is for check out only.
		PERFORM swipe_checkout_membership(msid);
		EXECUTE 'NOTIFY opendoor'||doornumber;
		RAISE NOTICE 'swipe_manual_ms - Detected swipe at checkout door.';
		RETURN NULL;
	END IF;

	rval = swipe_verifymembership(doornumber, msid, true, null);

	IF doorrec.booking_checkin > 0 THEN
		book := swipe_checkbooking(msid, NULL);
		msg2 := msg2 || book.msg2;
	END IF;

	IF rval < 0 THEN
		-- ACCESS DENIED
		rejectnotes := 'Check-in: ' || (SELECT text_value FROM lookup_default_value WHERE id = (2000-rval));
		rejectnotes := coalesce(rejectnotes, 'Check-in:  Unknown rejection reason.');
	END IF;

	-- Add happiness to icons
	SELECT happiness INTO happ FROM member WHERE member.id = memberid;
	IF happ = -1 THEN
		icons = icons || 'W';
	ELSIF happ = 0 THEN
		icons = icons || 'N';
	ELSIF happ = 1 THEN
		icons = icons || 'C';
	END IF;

	-- cannot charge member before checkowing and checksuspend.
	SELECT swipe_getmsg(msid, false, NULL) INTO msg;

	SELECT swipe_checkowing(doornumber, NULL, msid, NULL, msg) INTO msg2;
	IF msg2 IS NULL THEN
		--~ RETURN NEXT 'MSG01SEE RECEPTION';
		--~ RETURN NEXT 'BUZ005';
		--~ RETURN NEXT 'RLYJJ+b-r';
		--~ RAISE NOTICE 'See Reception, member owes money';
		--~ RETURN 0;
		rejectnotes := 'Check-in debtor';
	END IF;

	SELECT swipe_checksuspend(doornumber, NULL, msid, msg) INTO msg3;
	IF msg3 IS NULL THEN
		--~ RETURN NEXT 'MSG00SUSPENDED';
		--~ RETURN NEXT 'BUZ005';
		--~ RETURN NEXT 'RLYJJ+b-r';
		--~ RAISE NOTICE 'Suspened membership';
		--~ RETURN 0;
		rejectnotes := 'Check-in suspended member';
	END IF;
	msg2 := msg2 || msg3;

	SELECT INTO rek
		member.id AS memberid,
		membership.concession AS visits,
		membership.history,
		membership.enddate AS expiry,
		membership.numrenewals AS renewals,
		membership.concession,
		member.dob,
		programme.name AS prog,
		programmebasisid AS ptype,
		programme.concession AS vlimit,
		paymentdue AS pcost
	FROM membership
		INNER JOIN member ON member.id = membership.memberid
		INNER JOIN programme ON programme.id = membership.programmeid
	WHERE membership.id = msid;

	IF confrec.showvisitstats = true THEN
		msg2 := msg2 || member_visitstats(msid);
	END IF;
	-- Check for any task reminder messages
	msg2 := msg2 || member_reminders(rek.memberid, msid);

	-- Add charge for "Charge per visit" memberships.
	-- NOTE: programmebasis.id, 5 = "Charge Per Visit".
	IF rek.ptype = 5 THEN
		msg := msg || ', ' || 'Charge per Visit of ' || COALESCE(rek.pcost, '0'::MONEY)::TEXT;
		INSERT INTO debitjournal (membershipid, memberid, origin, note, debit)
		VALUES (msid, rek.memberid, 'Visit', rek.prog || ' - Visit ' || rek.concession, COALESCE(rek.pcost, '0'::MONEY));
	END IF;

	IF rejectnotes IS NULL AND ((rek.history = true) OR (rek.expiry < now()::DATE)) THEN
		-- This code should never be run, as expiry should of been picked up
		-- earlier. But the code is here as an added warning to us so if 
		-- messages do come up in orange we know there is a problem with the
		-- system.
		msgcol := 'ff8800';
		msg    := msg || E'\nMembership is expired but I''m letting them in anyway.\n';
		msgcol := 'ffdd00';
		icons  := icons || 'G';
	END IF;

	-- Birthday Check
	IF extract(month FROM now()) = extract(month FROM rek.dob)
		AND extract(day FROM now()) = extract(day FROM rek.dob)
	THEN
		RAISE NOTICE 'swipe_manual_ms - Today is the birthday of this member';
		icons := icons || 'B';
		msg2  := msg2 || E'\nHappy Birthday\n';
	END IF;

	-- Record reject reason for manual checkin.
	IF rejectnotes IS NOT NULL THEN
		msg := rejectnotes;
	END IF;

	-- TODO: Fix icons
	RAISE NOTICE 'swipe_granted %',(doornumber, NULL, msid, rek.memberid, msg, msg2, icons, true, rek.visits, false);
	rval := swipe_granted(doornumber, NULL, msid, rek.memberid, msg, msg2, icons, true, rek.visits, false);
	EXECUTE 'NOTIFY opendoor'||doornumber;
	RAISE NOTICE 'swipe_manual_ms - Granted';
	perform swipe_processlog(rval);
	RETURN 1;
END; 
$$ LANGUAGE 'plpgsql';

-- called from booking.xml
CREATE OR REPLACE FUNCTION swipe_manual(doornumber INTEGER, membership_id INTEGER, member_id INTEGER, booking_id INTEGER) RETURNS 
INTEGER AS $$
DECLARE
	msrec   RECORD;
	msg     TEXT;
	msg2    TEXT;
	mcardno INTEGER;
	owe_pos INTEGER := 0;
BEGIN
	RAISE NOTICE 'swipe_manual(%, %, %, %)', doornumber, membership_id, member_id, booking_id;
	IF membership_id IS NOT NULL THEN
		-- A membership_id provided, just do a swipe with that given membership.
		PERFORM swipe_manual_ms(doornumber, membership_id, member_id);
	ELSE
		-- No membership_id provided, assuming no current membership found.
		-- Checking for any expired memberships.
		SELECT id, concession, cardno INTO msrec
		FROM membership 
		WHERE memberid = member_id
			AND history = true
		ORDER BY startdate DESC
		LIMIT 1;
		IF msrec.id IS NOT NULL THEN
			-- Member has no current memberships but has an expired one.
			-- Need to check owing for expired membership.
			SELECT swipe_checkowing(doornumber, msrec.cardno, msrec.id, NULL, msg) INTO msg;
			IF msg IS NULL THEN
			 	RAISE EXCEPTION 'See Reception, Member Owes Money';
			ELSE
				msg2 := member_reminders(member_id, msrec.id);
				PERFORM swipe_granted(doornumber, msrec.cardno, msrec.id, member_id, 'Check-in with expired membership', 
					msg2, '', true, msrec.concession, false);
			END IF;
		ELSE
			-- Member has never had any memberships at all.
			SELECT cardno INTO mcardno FROM member WHERE id = member_id;
			msg2 := member_reminders(member_id, NULL);
			PERFORM swipe_granted(doornumber, mcardno, null, member_id, 'Check-in without membership',
				msg2, '', true, 0, false);
		END IF;
		EXECUTE 'NOTIFY opendoor'||doornumber;
		RAISE NOTICE 'swipe_manual - Granted';
	END IF;

	IF booking_id IS NOT NULL THEN
		-- Regardless of the swipe result, flag the booking with the given id as checked-in.
		IF (SELECT arrival FROM booking WHERE id = booking_id) > current_date THEN
			-- Can't checkin a booking that doesn't start on todays date.
			RAISE EXCEPTION 'Could not check in booking, as booking is in the future';
			RETURN 1;
		-- booking.result may be set in swipe_manual_ms if something failed
		ELSE IF (SELECT result FROM booking WHERE id = booking_id) NOT IN (20, 21) THEN
			RAISE NOTICE 'swipe_manual - Flagging booking entry as checked-in.';
			UPDATE booking
			SET result = (CASE WHEN booking.arrival+booking.starttime < now() THEN 21 ELSE 20 END),
				checkin = now()
			WHERE id = booking_id;
		END IF;
	END IF;
	RETURN 0; 
END; 
$$ LANGUAGE 'plpgsql';


-- called from swipe_verifymembership() swipe_manual_ms() swipe()
CREATE OR REPLACE FUNCTION swipe_getmsg(mshipid integer, docharge boolean, additionalcardname text) returns text as $_$
declare
	rek record;
	msg text;
begin
	msg = '';
	SELECT INTO rek
		get_name(firstname,surname) as name
		,member.dob, programme.name AS prog
		,member.id as memberid, membership.concession AS visits
		,membership.numrenewals AS renewals
		,membership.enddate as expiry
		,programmebasisid AS ptype, programme.concession AS vlimit
		,customtext1, treatcustom1asname
	  ,paymentdue as pcost
		FROM membership, member, programme, config 
		WHERE member.id = memberid and programme.id = programmeid
		AND membership.id = mshipid;

	IF rek.treatcustom1asname = true and rek.customtext1 is not null then
		msg := msg|| ', '|| rek.customtext1;
	elsif additionalcardname IS NOT NULL THEN
		msg := msg|| ', '|| additionalcardname;
		msg := msg|| ' ('|| rek.name||')';
	elsif rek.name IS NOT NULL THEN
		msg := msg|| ', '|| rek.name;
	END IF;
        IF rek.prog IS NOT NULL THEN
                msg := msg || E'\n'|| rek.prog;
        END IF;
	
	-- show end date.
	IF rek.expiry IS NOT NULL THEN
		msg := msg|| ', '||to_char(rek.expiry,'DD Mon YY');
       	END IF;

	if rek.vlimit is not null and rek.vlimit > 0 then
		msg := msg || ', '|| COALESCE(rek.visits,0) + 1
			|| '/' || rek.vlimit *(rek.renewals+1); 
	end if;
	-- trim the , from front of string.
	IF char_length(msg) >= 2 THEN
		msg := substring(msg from 3 );
	END IF; 

       	RETURN msg;
END; $_$ LANGUAGE plpgsql;
	
-- called from swipe()
CREATE OR REPLACE FUNCTION swipe_checkout(cardid integer) returns void as $_$
begin
	UPDATE entryexit SET checkout=now() where 
		cardno=cardid and time>now()-'12 hours'::interval 
		and checkout is null;
	-- open the door always, as assume member is valid
       	RETURN ;
END; $_$ LANGUAGE plpgsql;

-- called from swipe_manual_ms()
CREATE OR REPLACE FUNCTION swipe_checkout_membership(msid integer) returns void as $_$
begin
	UPDATE entryexit SET checkout=now() where 
		membershipid=msid and time>now()-'12 hours'::interval 
		and checkout is null;
	-- open the door always, as assume member is valid
       	RETURN ;
END; $_$ LANGUAGE plpgsql;

-- called from swipe()
CREATE OR REPLACE FUNCTION swipe_newcard(doornumber integer, cardserial text, msgcol text) returns void as $_$
DECLARE
	cardid integer;
BEGIN
	IF numeric_cardserial from config THEN
		cardid=idtxt(substring(regexp_replace(cardserial,'^W[0-9][0-9]-','WIG'),'[0-9]+'));
	END IF;
	IF cardserial LIKE 'BAR'||(SELECT barcode_prefix FROM config)||'%' THEN
		cardid = replace(cardserial, 'BAR'||(SELECT barcode_prefix FROM config), '');
	END IF;
	IF cardid IS NULL THEN
		cardid=nextval('idcard_id_seq'::regclass);
	END IF;
	while id IS NOT NULL FROM idcard WHERE id=cardid
	LOOP
		cardid=nextval('idcard_id_seq'::regclass);
	END LOOP;
	INSERT INTO idcard (id,serial) VALUES (cardid,cardserial);
	INSERT INTO entryexit (cardno, door, access) 
		VALUES (cardid, doornumber , 23 );
	INSERT INTO doormsg (doorid, message,state,colour,message2,
		colour2,alerts,soundid,cardno) VALUES (  doornumber, 
		stripnonascii('<<NEW>> #=' || cardid ), 23, msgcol, '', 'ffffff', 
		'', 3,cardid);
	RETURN ;
END; $_$ LANGUAGE plpgsql;

-- returns membershipid or negative membershipid with preference to a good if several good,
-- preferral of cost-per-visit only on concession doors else free per visit preferred.
-- called from swipe()
CREATE OR REPLACE FUNCTION swipe_findmembership(doornumber INTEGER, cardid INTEGER) RETURNS INTEGER AS $$
DECLARE
	doorrec	door;
	good integer;
	badmembership integer;
	alreadygood integer;
	rek record;
	preferconcession boolean;
	mshipid integer;
	foundbooking integer;
BEGIN
--	raise notice '% %',$1,$2;
	doorrec= door from door where door.id=doornumber;
	preferconcession=doorrec.concessionhandling=2;
	raise notice '% % %',$1,$2,preferconcession;
	FOR rek IN
		SELECT
			membership.id AS msid,
			memberid
		FROM membership
			JOIN programme ON membership.programmeid=programme.id
			JOIN programmebasis ON programme.programmebasisid=programmebasis.id
		WHERE membership.cardno = cardid
			AND NOT membership.history
		ORDER BY CASE programmebasis.swipe_cpv = preferconcession WHEN true THEN 1 ELSE 2 END,
			programmebasis.priority DESC,
			membership.id DESC
	LOOP
		-- For efficiency reasons, don't need to recalculate swipe_find_booking for each comparison to id. 
		foundbooking := swipe_find_booking(rek.memberid);
		mshipid = (select membershipid from booking where id = foundbooking);
		raise notice 'DBG1 % % % % %','>>',good,alreadygood,badmembership,rek;
		-- Fix bookings here now... To do good things!.
		good = swipe_checkmembership(doornumber,rek.msid);
		IF good >= 0 
		THEN
			if mshipid is not null and rek.msid = mshipid then
				return -rek.msid;
			end if;
			IF alreadygood != 0 -- got several good memberships
			THEN 
				if mshipid is null then
					RETURN -alreadygood ;	
				end if;
			END IF;
			alreadygood=rek.msid; -- got one so far.
		ELSE
			IF badmembership is NULL
			THEN
				badmembership=rek.msid;
			END IF;
		END IF;
	END LOOP;

	--raise notice 'DBG2 % % % % %','>>',good,alreadygood,badmembership,rek;

	IF alreadygood IS NOT NULL -- got a single good membership
	THEN return alreadygood ;
	END IF;
	
	IF badmembership IS NOT NULL 
	THEN return badmembership ;
	END IF;

	badmembership=id from membership where cardno=cardid order by enddate desc limit 1;
	
	IF badmembership IS NOT NULL 
	THEN return badmembership ;
	END IF;

	badmembership=membership.id from membership join member on member.id=membership.memberid where member.cardno=cardid 
	order by enddate desc limit 1;
	return badmembership ; 
END;
$$ LANGUAGE plpgsql;

-- returns NULL on success or a reason to deny on failure.
-- checks if members of a programme should have access tp a given door.

CREATE OR REPLACE FUNCTION swipe_checkprogramme(doornumber integer, progid INTEGER) returns integer as $_$
declare
	rek record;
begin
	SELECT programme.id as pid, programme.concession AS vlimit
		,programme_door.doorid, only_offpeak, programme_door.access24hour 
		,opentime.id as otid
		,opentime.*
		,door.concessionhandling
		INTO rek
		FROM programme
		LEFT JOIN programme_door on (programme.id = programme_door.programmeid and doorid = doornumber)
		LEFT JOIN opentime ON (daymatch(opentime.dayofweek::text,current_date))
		INNER JOIN door ON (door.id = doornumber) 
		WHERE programme.id = progid;

	IF NOT FOUND THEN
		raise notice 'programme not found! pg=% dn=%',progid,doornumber;
		return -1; -- no such membership (possibly should be critical error)
	end if;

	if rek.doorid is null then
		return -2; -- DENIED (No Access To Door)
	end if;

	if rek.otid is null 
		OR (rek.only_offpeak AND (
			   current_time     BETWEEN rek.openoffpeak  AND rek.closeoffpeak  -- mid day off peak
			OR rek.closeoffpeak BETWEEN current_time     AND rek.openoffpeak   -- before end of midnight offpeak
			OR rek.openoffpeak  BETWEEN rek.closeoffpeak AND current_time      -- after start of midnight offpeak
			)) 
		OR (NOT rek.only_offpeak AND (
			   current_time  BETWEEN rek.opentime  AND rek.closetime  -- normal open time
			OR rek.closetime BETWEEN current_time  AND rek.opentime   -- before end of midnight opentime
			OR rek.opentime  BETWEEN rek.closetime AND current_time   -- after start of midnight opentime
			)) 
		OR 
		rek.access24hour = true 
	then
	-- take no action
	else
		-- bad time to come in
		if rek.only_offpeak = true then
			return -4; -- CLOSED (Offpeak Only)
		end if;
			return -3; -- CLOSED (Facility Closed)
	end if;
	
	PERFORM id FROM programme_denytime WHERE programmeid = rek.pid and 
		current_time BETWEEN denystart_at AND denyend_at;
	IF FOUND 
	THEN
		RETURN -12; -- CLOSED (denytime)
	END IF;

	return NULL;
END; $_$ LANGUAGE plpgsql;

-- does what it says checks a membership to see if it's good.
-- called from swipe_findmembership() swipe_verifymembership()
CREATE OR REPLACE FUNCTION swipe_checkmembership(doornumber integer, msid INTEGER) returns integer as $_$
declare
	rek record;
	reason integer;
	sdate     DATE;
	edate     DATE;
	periodlen INTERVAL;
begin
	SELECT
		membership.id AS _mshipid, membership.concession AS visits
		,programmeid, programme.concession AS vlimit
		,membership.startdate AS startdate, membership.enddate AS expiry
		,membership.history,membership.numrenewals
		,member.gender
		,door.womenonly, membership.completed
		,member.id as mid		
		INTO rek
		FROM membership
		INNER JOIN programme on membership.programmeid=programme.id
		JOIN member ON membership.memberid=member.id
		INNER JOIN door ON (door.id = doornumber) 
		WHERE membership.id = msid;

	IF NOT FOUND THEN
		raise notice 'not found! ms=% dn=%',msid,doornumber;
		RETURN -1; -- no such membership (possibly should be critical error)
	END IF;

	IF rek.womenonly = true and upper(rek.gender) != 'F' then
		raise notice 'WORG:%',rek.gender;
		RETURN -8; -- DENIED (Women Only)
	END IF;

	IF rek.completed = 'f' AND (SELECT stop_incompleted_ms FROM config LIMIT 1) = 't' THEN
		-- incompleted membership
		RETURN -11; -- DENIED (Incompleted Membership)
	END IF;

	reason=swipe_checkprogramme(doornumber,rek.programmeid);
	IF reason IS NOT NULL THEN RETURN reason; END IF;

	IF coalesce(rek.expiry::timestamp,'infinity') < current_date::timestamp or rek.history=true then  -- infinity was 2059-01-01 - can't immagine why!
			RETURN -5; -- DENIED (Expired)
	END IF;
	raise notice 'VLIM % % %', rek.vlimit,rek.expiry,current_date;

	IF  rek.vlimit > 0 and (rek.expiry <= current_date  
		OR rek.visits >= rek.vlimit * (rek.numrenewals+1))
	THEN -- HUH?
		IF NOT swipe_checkdoubleswipe(rek.mid, msid, NULL )
		THEN
			RAISE NOTICE 'DEPLETED!';
			RETURN -6; -- DENIED (Depleted Card)
		ELSE
			RAISE NOTICE 'OK: depleted but recently used.';
		END IF;
	END IF;

	IF rek.startdate > current_date then
			RETURN -7; -- DENIED (Membership Yet To Start)
	END IF;

	-- the _membership_ is OK  error -9 (stop MEMBER at gate has not been tested yet)
    
    SELECT maxvisitsperiodlen INTO periodlen FROM programme WHERE id=(SELECT programmeid FROM membership WHERE id=msid);
    IF periodlen = '1 week'::INTERVAL THEN
        sdate := (now()::DATE - (extract(dow FROM now()::DATE) || ' days')::INTERVAL)::DATE;
        edate := (now()::DATE - (extract(dow FROM now()::DATE) || ' days')::INTERVAL + '6 days'::INTERVAL)::DATE;
    ELSEIF periodlen = '1 month'::INTERVAL THEN
        sdate := now()::DATE - (extract( day FROM now())-1 || ' days')::INTERVAL;
        edate := now()::DATE - (extract( day FROM now())-1 || ' days')::INTERVAL + '1 month'::INTERVAL - '1 day'::INTERVAL;
    ELSEIF periodlen IS NOT NULL THEN
        RAISE NOTICE 'Unsupported periodlen detected: %', periodlen;
    END IF;
    IF (SELECT count(*) FROM entryexit WHERE membershipid = msid AND access=1 and time::DATE BETWEEN sdate AND edate) >= NULLIF((SELECT maxvisitsperperiod FROM programme WHERE id=(SELECT programmeid FROM membership WHERE id=msid)),0) THEN
        RETURN -6; -- DENIED (Maximum monthly visits on this programme reached)
    END IF;
    
    RETURN 0;
END;
$_$ LANGUAGE plpgsql;


-- verifies that the membership is valid and where manual is false logs failed attempts to doormsg and entryexit
-- called from swipe() swipe_manual_ms()
CREATE OR REPLACE FUNCTION swipe_verifymembership(doornumber integer, msid INTEGER, manual BOOLEAN,cardid integer) returns integer as $_$
declare
	dmsg TEXT := '';
	result integer;
	icons text='';
	mid integer;
	msg text;
	msg2 text;
	msgcol text='ff7777';
begin

	mid=memberid from membership where id=msid;	

	result=swipe_checkmembership(doornumber,msid);

	PERFORM description  FROM task, tasktype --check for "stopgate"
		WHERE tasktypeid = tasktype.id AND cleared = false AND deleted=false
		AND atgate > 1  
		AND task.memberid = mid limit 1;
	IF found THEN 
		result = -9;
	END IF;

	--only generate doormsg errors for auto swipe (not for manual)

	IF NOT manual AND result < 0 then

		icons = icons || case happiness
				WHEN -1 THEN  'W'
				WHEN  0 THEN  'N'
				ELSE 			  'C'
			END FROM member WHERE member.id = mid;

		raise notice 'REZZ %',result;
		select swipe_getmsg(msid,false,null) into msg;
		msg2 := coalesce(msg2,'') || member_reminders(mid,msid);

		-- NOTE: Starting from id 2000 is just where we arbitrarily decided to put them into lookup_default_value or no particular reason.
		--  I assume we're not using a less magic numbery method like "SELECT text_value FROM lookup_default_value WHERE name = 'denyaccess' AND integer_value = result;" due to performance.
		dmsg = 'DENIED ( '||text_value||' )' from lookup_default_value where id=2000-result;

		if result in (-2,-3,-4,-7,-12) then
			icons := icons || 'S';
			INSERT INTO doormsg (doorid, message,state,memberid,
				membershipid,colour,message2,colour2,alerts,soundid,cardno)
				VALUES ( doornumber
					, stripnonascii(dmsg ||coalesce(' ['||cardid||'] ',' ') || msg )
					, 10-result, mid , msid, msgcol, stripnonascii(msg2)
					, 'ffffff',icons,2,cardid);
		elsif result in (-6,-8,-10,-11) then
			icons := icons || 'D';
			INSERT INTO doormsg (doorid, message,state,memberid,
				membershipid,colour,message2,colour2,alerts,soundid,cardno)
				VALUES ( doornumber
					, stripnonascii(dmsg ||coalesce(' ['||cardid||'] ',' ')  || msg )
					, 10-result, mid , msid, msgcol, stripnonascii(msg2)
					, 'ffffff',icons,2,cardid);
		elsif result in (-9) then
			icons := icons || 'P';
			INSERT INTO doormsg (doorid, message,state,memberid,
				membershipid,colour,message2,colour2,alerts,soundid,cardno)
				VALUES ( doornumber
					, stripnonascii(dmsg ||coalesce(' ['||cardid||'] ',' ') || msg )
					, 10-result, mid , msid, msgcol, stripnonascii(msg2)
					, 'ffffff',icons,2,cardid);
		elsif result = -5 then
			icons := icons || 'E';
			INSERT INTO doormsg (doorid, message,state,memberid,
				membershipid,colour,message2,colour2,alerts,soundid,cardno)
				VALUES ( doornumber
					, stripnonascii(dmsg||coalesce(' ['||cardid||'] ',' ')  || msg )
					, 10-result, mid, msid, msgcol, stripnonascii(msg2)
					, 'ffffff',icons,2,cardid);
		else 
			dmsg=coalesce(dmsg,'DENIED - reason not given');
			INSERT INTO doormsg (doorid, message,state,memberid,
				membershipid,colour,message2,colour2,alerts,soundid,cardno)
				VALUES ( doornumber
					, stripnonascii(dmsg||coalesce(' ['||cardid||'] ',' ')  || msg )
					, 10-result, mid, msid, msgcol, stripnonascii(msg2)
					, 'ffffff',icons,2,cardid);
		END IF;

		raise notice 'Denied for membership % with %',msid,result;
		-- NOTE: Sooo lets arbitrarily have entryexit.access be 10-result...
		INSERT INTO entryexit (cardno, door, access, membershipid, mid)
			values (cardid, doornumber, 10-result, msid);
	END IF;

	if result < 0 then
		return result;
	else
		return msid;
	end if;
END; $_$ LANGUAGE plpgsql;

-- called from swipe(),swipe_manual_ms()
CREATE OR REPLACE FUNCTION swipe_checksuspend(doornumber integer, cardid integer, mshipid integer, msg text) returns text as $_$
declare
        rec record;
        icons text;
        msg2 text;
        msgcol text;
		  mid integer;
begin
        msgcol = 'ff0000';
        icons = '';
        msg2 = '';
        select membership_suspend.* INTO rec
                FROM membership_suspend, member, membership
                WHERE membership_suspend.memberid = member.id
                AND membership.memberid = member.id
                AND membership.id = mshipid
                AND promotion = false
                AND current_date between membership_suspend.startdate and
                membership_suspend.startdate + membership_suspend.howlong;
	
        if not found then
                return '';
        end if;
	mid = rec.memberid;

        msg2 = 'Membership was frozen till '||(rec.startdate + rec.howlong)::date;
        --IF rec.allowentry = FALSE THEN
        IF rec.allowentry THEN
                perform truncate_memberships(rec.memberid);
                msg2 = E'\nMembership has been unfrozen and now is active.';
        ELSE
		msg2 = null;
		raise notice 'Member has suspesion set for denial of entry';
		INSERT INTO entryexit (cardno, door, access, membershipid, memberid) values (cardid, doornumber, 25, mshipid, mid);
		icons := icons || 'S';
		INSERT INTO doormsg (doorid, message, state, memberid, membershipid, colour, message2, colour2, alerts, soundid,cardno)
			VALUES (doornumber,
			stripnonascii('DENIED (Suspended) '||coalesce('['||cardid||']','') || msg)
			,24,mid,mshipid,'ff0000',stripnonascii(msg2),'ffffff',icons,2,cardid);
    		RETURN null;
                --msg2 = E'\nMembership has been frozen.';
        END IF;
        return msg2;
END; $_$ LANGUAGE plpgsql;


-- returns entryexit id 
-- called from swipe(),swipe_manual_ms(),swipe_manual(),swipe_manual()
CREATE OR REPLACE FUNCTION swipe_granted(doornumber integer, cardid integer, mshipid integer, mid integer, msg text, msg2 text, icons text, manual boolean,visits integer, ispending boolean) returns integer as $_$
declare
	repeatvisit	BOOLEAN;
	eeid integer;
begin
	if msg is null then 
		raise exception 'DENIED: Unknown reason.';
	end if;
	if icons is null then 
		raise exception 'DENIED, %',msg;
	end if;

	IF manual then
		repeatvisit=FALSE;
	ELSE
--		SELECT swipe_checkdoubleswipe(mid, mshipid, doornumber) INTO repeatvisit;
		SELECT swipe_checkdoubleswipe(mid, mshipid,NULL) INTO repeatvisit;
	END IF;

	INSERT INTO doormsg (doorid, message,state,memberid,membershipid,colour,
		message2,colour2,alerts,soundid, image,cardno) 
		VALUES (  doornumber ,stripnonascii('GRANTED '||coalesce('['||cardid||'] ','') || msg )
		    , 1 , mid , mshipid, '00ff00', stripnonascii(msg2),'ffffff',icons||'A',1,(visits + case when repeatvisit then 0 else 1 end )::text||'.tga',cardid);

	IF  repeatvisit then
		raise notice 'Double swipe of card, ignoring second swipe';
	END If;

	INSERT INTO entryexit (cardno, door, access, membershipid, pending,takeconcession, memberid)
		values (cardid, doornumber, 1, mshipid, ispending,NOT repeatvisit, mid) returning id into eeid;

	RETURN eeid;
END; $_$ LANGUAGE plpgsql;

-- called from swipe(),swipe_manual_ms(),swipe_manual()	
CREATE OR REPLACE FUNCTION swipe_checkowing(doornumber integer, cardid integer, mshipid integer, _mid integer, msg text) returns text as $_$
declare
	conf record;
	owe money;
	owe2 money;
	icons text;
	msg2 text;
	mid integer;
begin
	icons = '';
	msg2 = '';
	select config.* into conf from config;
	if _mid is null then 
		select memberid into mid from membership where id=mshipid;
	else 
		mid = _mid;
	end if;

	owe2 := member_owe(mid); -- this is potentially way slow...

	-- NOTE: Uniencoding in X for popup doesn't work, so there is no 
	-- currency symbols until unencoding can be made to work for popup
	-- without causing segfault.
	
 	owe := member_owe_at2(mid,(current_date - conf.warnafterowe)::date);
	if owe2 < owe then
		owe = owe2;
	end if;
	
	if owe > conf.warningonowe THEN
		icons := icons || 'P';
		IF conf.showdollar IS TRUE THEN
			msg2 := msg2 || E'Member owes ' || member_owe_no_unfinished(mid) || E'\n';
		END IF;
	--ELSIF owe < -conf.warningonowe THEN
	ELSIF owe < opposite_money(conf.warningonowe) THEN
		IF conf.showdollar IS TRUE THEN
			if member_owe_no_unfinished(mid) > '0'::money then
				-- paid in advance
				msg2 := msg2 || E'Account balance of ' || member_owe_no_unfinished(mid) || E'\n';
			elsif member_owe_no_unfinished(mid) < '0'::money then
				-- in credit ligitmently.
				msg2 := msg2 || E'Account in credit, ' || member_owe_no_unfinished(mid) || E'\n';
			end if;
		END IF;
	END IF;

	owe := member_owe_at2(mid,(current_date - conf.stopafterowe)::date);
	if owe2 < owe then
		owe = owe2;
	end if;
	IF owe > conf.stoponowe then  
		--check whether this member is allowed owing upto a certain date.
		IF coalesce(owingdeadline < current_date,true) FROM member WHERE id = mid
		THEN
			-- if not deny them
			raise notice 'Person owes way to much, can not let % in (Owes %)',mid,owe;
			INSERT INTO entryexit (cardno, door, access, membershipid, memberid) values (cardid, doornumber, 24, mshipid, mid);
			icons := icons || 'S';
			INSERT INTO doormsg (doorid, message, state, memberid, membershipid, colour, message2, colour2, alerts, soundid,cardno)
				VALUES (doornumber,stripnonascii(
				'DENIED (Overdue Account) '||coalesce('['||cardid||']','') || msg)
				,24,mid,mshipid,'ff0000',stripnonascii(msg2),'ffffff',icons,2,cardid);
	    		RETURN null;
		ELSE
			raise notice 'Person owes but doeadline not reached';
		END IF;
	END IF;

	return msg2;
END; $_$ LANGUAGE plpgsql;
	


DROP FUNCTION IF EXISTS swipe(integer,text,boolean);
-- --------- --
-- S W I P E --
-- --------- --
-- called from cardsentry,membership.xml, swipe_manual()!!!
CREATE OR REPLACE FUNCTION swipe(doornumber integer, rawcardserial text, manual boolean) 
RETURNS SETOF text AS $_$
DECLARE
-- access field  1 is access OK,  > 10 is access denied.

-- return syntax: KKKdvtext
-- KKK   RLY => relay  MS1 => top line message MS2 => bot line message 
--       MSG => double-height message BUZ => sound tone
-- d     delay after acting  :/0 no delay 1 1/10 sec Z 3.6 sec
-- v     value/offset 
-- text  text of message
--       text of buz tone - length of tone. silence. tone. silence etc...
--       buz sounds asynchronously to messages.
--       relay is synchronous.
--   replicant is for to use a "local" database table instead of 
--   replicated tables to log swipes - handy when the net goes down.
-- THIS IS DEPRICATED FEATURE - DO NOT PASS LOCAL.
	cardserial text;
	cardid integer ;
	real_cardid integer;
	msg text := '';
	msg3 text;
	icons text := '';
	additionalcardname text;
	book record;
	rek record;
	rval integer;
	mshipid integer;
	conf record;
	owe money;
	sound integer := 0;
	msgcol text := '00ff00';
	msgcol2 text := '00ff00';
	msg2 text := '';
	msid	INTEGER;
	mid	INTEGER;
	mrec	RECORD;
	repeatvisit	BOOLEAN;
--IC	deny BOOLEAN;
	pending	BOOLEAN := FALSE;
	mname text;
	icmid integer;
	icdeny BOOLEAN;
	memid INTEGER;
	mcardno INTEGER;

BEGIN

	  -- drop the inaccessible bits from the Mu serial 
	--raise log 'SWP: select * from swipe(%,%,%);' ,doornumber, quote_literal(rawcardserial), manual::text ;

	/*TODO: remove this, it's not needed.*/
	cardserial=CASE 
		WHEN rawcardserial LIKE 'Mu%' THEN 'Mv'||substring(rawcardserial,3,6)||substring(rawcardserial,11)
		ELSE rawcardserial 
		END;

	/*This looks useful. Keep it. */
	-- if NULL passed in as card number then deal with it nicely
	IF cardserial IS NULL THEN
		msgcol := 'ff0000';
		INSERT INTO doormsg (doorid, message,state,colour,message2,colour2,alerts,soundid,cardno) 
			VALUES (  doornumber, '<<NON-VALID CARD>>' , 0, msgcol, stripnonascii(msg), 'ffffff', icons, 2,NULL);
		RETURN NEXT 'MSG00NEW-CARD' ;
		RETURN NEXT 'RLYJ9+b-r';
		RETURN NEXT 'BUZ0011111';
		RETURN ;
	END IF;

	/*Also Useful, keep it*/
	-- validate door
	if count(*) < 1 from door where id=doornumber THEN 
		RETURN NEXT 'MSG00NO DOOR# '||doornumber;
		RETURN NEXT 'RLYJJ+b-r';
		RETURN NEXT 'BUZ0051117';
		RETURN;
	end if;


-- now the action starts
--  get id number from card,
	SELECT idcard.id,idcard.interclub_note,idcard.interclub_deny,idcard.interclub_memberid 
			,additional_idcard.mastercardid , additional_idcard. name
		INTO real_cardid,msg2,icdeny,icmid,cardid, additionalcardname
	FROM idcard 
		LEFT JOIN additional_idcard ON additional_idcard.childcardid=idcard.id
	WHERE idcard.serial = cardserial;

	if cardid is null then
		cardid=real_cardid;
		additionalcardname = null;
	end if;

	-- deal with blank new cards
	IF cardid IS NULL THEN
		perform swipe_newcard(doornumber,cardserial,msgcol);
		RETURN NEXT 'MSG00NEW-CARD' ;
		RETURN NEXT 'RLYJ9+b-r';
		RETURN NEXT 'BUZ0011111';		
		return ;
	end if;

	-- config stuff
	SELECT INTO conf warningonowe as warn, stoponowe, showstats, showdollar,
		stopafterowe, warnafterowe, showvisitstats, swipe_timeout, treatcustom1asname,
		door.*, 
		graceperiod * interval '1 day' as graceperiod
		FROM config, door WHERE door.id = doornumber;
	-- if the door is for check out only, ie exit door/turnstile.
	if conf.checkout = true then
		perform swipe_checkout(cardid);
		icons := icons || 'D';
		SELECT id INTO mid FROM member WHERE cardno=cardid;
		SELECT swipe_findmembership(doornumber, cardid) INTO msid;
		SELECT get_name(firstname,surname) INTO mname FROM member WHERE cardno=cardid;
		owe =  member_owe(mid);
		IF owe > 0::text::money THEN
			msgcol := 'ffff00';
			msgcol2 := 'ff0000';
			sound := 2;
			icons := icons||'D';
			msg2 := mname ||' owes ' || owe;
		ELSEIF owe = 0::text::money THEN
			msgcol := '00ff00';
			sound := 5;
			icons := icons||'A';
			msg2 := mname ||' account is balanced';
		ELSE
			msgcol := '00ff00';
			sound := 5;
			icons := icons||'A';
			msg2 := mname ||' has ' || owe || ' in credits';
		END IF;
		--INSERT INTO doormsg(doorid, message, state, colour, message2, colour2, alerts, soundid,cardno)
		--	VALUES(doornumber, stripnonascii(msg), 3, msgcol, '', msgcol, icons, 3,real_cardid);
			
		INSERT INTO doormsg (doorid, message,
		state,memberid,membershipid,colour,message2,colour2,alerts,soundid, image,cardno) 
		VALUES (doornumber ,stripnonascii('Check Out '||coalesce('['||cardid||'] ','') || msg )
		    , 1 , mid , mshipid, msgcol, stripnonascii(msg2),msgcol2,icons||'A',sound,'',cardid);
			
		RETURN NEXT 'MSG00SEE YOU SOON' ;
	 	RETURN NEXT 'BUZ001';
		RETURN NEXT 'RLYJA+rb';
		return ;
	end if;

	-- find membership

	IF icmid IS NOT NULL AND NOT icdeny THEN
		rval=swipe_checkprogramme(doornumber,guestprogramme) from config;
	ELSE
		msid=NULL;msg2='';
		SELECT swipe_findmembership(doornumber, cardid) INTO msid;
		IF msid = 0 or msid is null THEN -- either no member or no membership
			-- send message to reader
			RETURN NEXT 'MSG00NOT SUBSCRIBED';
			RETURN NEXT 'BUZ007';
			RETURN NEXT 'RLYJJ+b-r';
			SELECT idcard.id INTO mcardno from idcard where idcard.serial = cardserial;
			RAISE NOTICE 'Not Subscribed %', mcardno;
			
			
			--RAISE NOTICE 'Cardno is %', cardno;
			
			-- record in entryexit

			-- record deny access
			--get memberid from cardno
			
			SELECT id INTO memid FROM member WHERE member.cardno=mcardno;
			INSERT INTO entryexit (cardno, door, access, memberid) VALUES (real_cardid, doornumber, 11, memid);
	
			-- no member is found
	
			mname =  get_name(firstname,surname)||' (#'||id::text||')' from member where cardno=cardid;
			IF mname IS NULL THEN
				msg := '<<NEW>> #=' || real_cardid ||' No Member Associated';
			ELSE
				msg := '<<NEW>> #=' || real_cardid ||' No Current Membership Associated for member ' || mname || '.';
			END IF;
			INSERT INTO doormsg(doorid, message, state, colour, message2, colour2, alerts, soundid,cardno)
				VALUES(doornumber, stripnonascii(msg), 3, msgcol, '', 'ffffff', icons, 3,real_cardid);
			RETURN ;
		END IF;
		IF msid < 0 THEN -- more than 1 membership and has concession ms
			pending := TRUE;
			msid = msid * -1;

		END IF;
		-- check membership, record results
		 rval = swipe_verifymembership(doornumber,msid,false,real_cardid);
	END IF;

	-- ACCESS DENIED
	if rval < 0 then
		if rval = -1 then
			RETURN NEXT 'MSG00NOT SUBSCRIBED';
		elsif rval = -2 then
			RETURN NEXT 'MSG00NO DOOR ACCESS';
		elsif rval = -3 then
			RETURN NEXT 'MSG00WRONG TIME';
		elsif rval = -4 then
			RETURN NEXT 'MSG00OFFPEAK ONLY';
		elsif rval = -5 then
			RETURN NEXT 'MSG00EXPIRED CARD';
		elsif rval = -6 then
			RETURN NEXT 'MSG00DEPLETED CARD';
		elsif rval = -7 then
			RETURN NEXT 'MSG00YET TO START';
		elsif rval = -8 then
			RETURN NEXT 'MSG00WOMEN ONLY';
		elsif rval = -9 then -- stopgate
			RETURN NEXT 'MSG00SEE RECEPTION';
		elsif rval = -11 then 
			RETURN NEXT 'MSG00INCOMPLETE MEMBERSHIP';
		elsif rval = -12 then 
			RETURN NEXT 'MSG00DENY TIME';
		else
			RETURN NEXT 'MS100ACCESS DENIED';
			RETURN NEXT 'MS200REASON: #'||(-rval)::text ;
		end if;
		RETURN NEXT 'RLYJJ+b-r';
		RETURN NEXT 'BUZ005';
		raise notice 'door caccess denied %',rval;
		RETURN ;
	else
		mshipid = rval;
	end if;

	IF icmid IS NULL OR icdeny THEN	

		-- add happiness to icons
		icons = icons || case happiness
				WHEN -1 THEN  'W'
				WHEN  0 THEN  'N'
			ELSE 			  'C'
			END FROM member, membership WHERE member.id = memberid AND membership.id = mshipid;

		--don't charge member before check owing and suspend.
		select swipe_getmsg(mshipid, false, additionalcardname) into msg;


		select swipe_checkowing(doornumber, real_cardid, mshipid, null, msg) into msg2;
		if msg2 is null then
			RETURN NEXT 'MSG01SEE RECEPTION';
			RETURN NEXT 'BUZ005';
	  	    	RETURN NEXT 'RLYJJ+b-r';
			raise notice 'See Reception, member owes money.';
			RETURN ;
		end if;

		select swipe_checksuspend(doornumber, real_cardid, mshipid, msg) into msg3;
		if msg3 is null then
			RETURN NEXT 'MSG00SUSPENDED';
			RETURN NEXT 'BUZ005';
	  	    	RETURN NEXT 'RLYJJ+b-r';
			raise notice 'Suspend';
			RETURN ;
		end if;
		msg2 = msg2 || msg3;

	
	-- GRANTED 
		SELECT INTO rek
			member.id as memberid, membership.concession AS visits
			,membership.history, membership.enddate as expiry
			,membership.numrenewals AS renewals
			,member.dob, programme.name as prog
			,programmebasisid AS ptype, programme.concession AS vlimit
			,paymentdue AS pcost

			FROM membership, member, programme
			WHERE member.id = memberid and programme.id = programmeid
			AND membership.id = mshipid;

		IF conf.showvisitstats=true THEN
			msg2 := msg2 || member_visitstats(mshipid);
		END IF;
		--Check for any reminder messages
		msg2 := msg2 || member_reminders(rek.memberid,mshipid);

		SELECT swipe_checkdoubleswipe(rek.memberid, mshipid,NULL) INTO repeatvisit;

		--add charge for per visit membership.
		IF rek.ptype = 5 AND repeatvisit = 'f' THEN
			msg := msg || ', ' || 'Charge per Visit of ' || COALESCE(rek.pcost, '0'::MONEY)::TEXT;
			INSERT INTO debitjournal (membershipid, memberid, origin, note, debit)
				VALUES (mshipid, rek.memberid, 'Visit', 
				rek.prog || ' - Visit', COALESCE(rek.pcost, '0'::MONEY)::TEXT::MONEY);
			-- NOTE: Letting the swipe_processlog deal with incrementing the concession count.
			--UPDATE membership SET concession = concession + 1 WHERE id = mshipid;
		END IF;

		-- check booking
		IF conf.booking_checkin > 0 THEN
			book=swipe_checkbooking(msid,rek.memberid);
			msg2 = msg2 || book.msg2;
			icons=icons || book.icons;
		END IF;
		-- this code should never be run, as expiry should of been picked up
		-- earlier. But the code is here as an added warning to us so if 
		-- messages do come up in orange we know there is a problem with the
		-- system.
		IF (rek.history) or (rek.expiry < DATE(now())) THEN
			msgcol := 'ff8800';
			msg = msg || E'\nMembership is expired but im letting them in anyway.\n';
			msgcol := 'ffdd00';
			icons := icons || 'G';
		END IF;

		-- Birthday Check
		IF real_cardid=cardid
				AND EXTRACT(month FROM now()) = EXTRACT(month FROM rek.dob)
				AND EXTRACT(day FROM now()) = EXTRACT(day FROM rek.dob) THEN 
			RETURN NEXT 'MSG30HAPPY BIRTHDAY';
			  	RETURN NEXT 'BUZ00112131';
			RETURN NEXT 'RLY00-b+r';
			RETURN NEXT 'RLYJA+br';
			icons := icons || 'B';
			msg2 = msg2 || E'\nHappy Birthday\n';
		END IF;
		--todo Fix icons
		perform swipe_granted(doornumber, real_cardid, mshipid, rek.memberid, msg, msg2, icons, manual, rek.visits, pending);

		RETURN NEXT 'RLY0z+rb';
		RETURN NEXT 'BUZ001';

		IF rek.vlimit > 0 THEN
			RETURN NEXT 'MS102' || rek.prog;
			RETURN NEXT 'MS202' || rek.vlimit - COALESCE(rek.visits,0) - CASE WHEN repeatvisit THEN 0 ELSE 1 END
				|| ' of ' || rek.vlimit || ' remain';
		ELSE
			RETURN NEXT 'MS102' || rek.prog;
			IF (rek.expiry IS NOT NULL) THEN 
				RETURN NEXT 'MS202exp:' || to_char(rek.expiry,'DD Mon YY');
			END IF;
		END IF; 
	ELSE
		perform swipe_granted(doornumber, real_cardid, NULL, NULL, icmid::text, msg2, icons, manual, NULL, FALSE);
		RETURN NEXT 'RLY0z+rb';
		RETURN NEXT 'BUZ001';
		RETURN NEXT 'MS102' || 'Welcome';
		RETURN NEXT 'MS202' || msg2;
	END IF;
	RETURN NEXT 'MSG30OK';
	RETURN NEXT 'RLYJA+rb';
	RETURN ;
END;	
$_$ LANGUAGE plpgsql;

-- called from swipe()
CREATE OR REPLACE FUNCTION daymatch ( days text, wen timestamp ) RETURNS BOOLEAN AS $$
BEGIN
	RETURN position(EXTRACT(DOW FROM wen)::text in days) > 0;
END;
$$ LANGUAGE plpgsql;

-- called from swipe(), swipe_manual_ms()
CREATE OR REPLACE FUNCTION member_visitstats(mshipid integer) RETURNS text AS $$
DECLARE 
	msg text = '';
	rec record;
	v1 integer;
	v2 integer;
	nday date;
BEGIN
	SELECT count(id) FROM entryexit WHERE membershipid=mshipid AND
		time > (now() - '1 month'::interval) INTO v1;
	SELECT concession FROM membership WHERE id=mshipid INTO v2;
	-- Get annerviersy date of membership. 
	SELECT (to_char(now(),'YYYY-MM-')||'01')::date + 
		(interval '1 day' * 
		(date_part('day',startdate::timestamp)-1)) 
		INTO nday FROM membership WHERE id=mshipid;
	IF v1 IS NULL THEN
		v1 = 0;
	END IF;
	IF v2 IS NULL THEN
		v2 = 0;
	END IF;
	msg = 'Visits within the month : '||v1||E'\nTotal Visits : '||v2||E'\nMembership Anniversary : '||nday||E'\n';
	RETURN msg;
END;
$$ LANGUAGE plpgsql;

-- warning: calling this func may create a "returning member" task.
-- called from swipe(), swipe_manual(), swipe_manual(), swipe_manual_ms(), swipe_checkmembership()
CREATE OR REPLACE FUNCTION member_reminders(mid integer,msid integer) RETURNS text AS $$
DECLARE 
	msg text = '';
	rec record;
	weeksafter integer;
	weeksbefore integer;
BEGIN
	
	SELECT INTO weeksafter remindafter FROM config LIMIT 1;
	SELECT INTO weeksbefore remindbefore FROM config LIMIT 1;
	PERFORM check_returning_member_task(mid,msid);

	FOR rec IN
		SELECT description, cleared, flagon, name
		FROM task
			JOIN tasktype ON task.tasktypeid = tasktype.id
		WHERE memberid = mid
			AND atgate > 0 
			AND (flagon BETWEEN date(now())-(weeksbefore * '1 week'::interval) AND date(now())+(weeksafter * '1 week'::interval))
			AND NOT cleared
			AND NOT deleted
	LOOP
		msg := msg || coalesce(rec.description, rec.name, 'TASK') || coalesce (', ' || rec.flagon, '') || E'\n';
	END LOOP;
	RETURN msg;
END;
$$ LANGUAGE plpgsql;

-- called from swipe_granted(),swipe()
CREATE OR REPLACE FUNCTION swipe_checkdoubleswipe(mid INTEGER, msid INTEGER, doornumber INTEGER) RETURNS BOOLEAN AS $$
BEGIN --NOTE: mid is ignored...
	IF doornumber IS NOT NULL
	THEN
		PERFORM entryexit.id from entryexit,config 
			WHERE entryexit.membershipid = msid 
				AND entryexit.door = doornumber 
				AND entryexit.time > (now() - config.swipe_timeout)
				AND entryexit.takeconcession LIMIT 1;
		RAISE NOTICE 'swipe_checkdoubleswipe(%,%,%) = %',mid,msid,doornumber,found;
		RETURN FOUND;
	ELSE
		PERFORM entryexit.id from entryexit,config 
			WHERE entryexit.membershipid = msid 
				AND entryexit.time > (now() - config.swipe_timeout)
				AND entryexit.takeconcession LIMIT 1;
		RAISE NOTICE 'swipe_checkdoubleswipe(%,%,%) = %',mid,msid,doornumber,found;
		RETURN FOUND;
	END IF;
END
$$ LANGUAGE plpgsql;

create or replace function swipe_find_booking(mid integer) returns integer as $$
declare
	rec record;
begin
	SELECT * INTO rec FROM booking 
                WHERE memberid = mid 
                AND (booking.result BETWEEN 10 AND 19)
                AND arrival = current_date
                AND starttime > (now()::time - '2h'::interval)
                AND endtime < (now()::time + '4h'::interval)
                ORDER BY starttime limit 1;
	if found then
		return rec.id;
	end if;
	return null;
end
$$ LANGUAGE plpgsql;
-- called from swipe_manual_ms(),swipe()
CREATE OR REPLACE FUNCTION swipe_checkbooking(IN msid INTEGER, IN mid INTEGER, OUT msg2 TEXT, OUT icons TEXT ) AS $$
DECLARE
	book RECORD;	
BEGIN
	msg2='';
	icons='';
	SELECT * INTO book FROM booking 
		WHERE (memberid=(SELECT memberid FROM membership WHERE id=msid) OR memberid=mid)
		AND (booking.result BETWEEN 10 AND 19)
                AND arrival = current_date
                AND starttime > (now()::time - '2h'::interval)
                AND endtime < (now()::time + '4h'::interval)
                ORDER BY starttime limit 1 FOR UPDATE OF BOOKING;
	IF FOUND THEN
		raise notice 'Checking member in (%,%,%)',case when book.starttime < 'now'::time then 21 else 20 end,book.id, msid;
		icons = 'T';
		msg2 = 'Checking member in for booking';
		PERFORM booking_checkin_member((case when book.starttime < 'now'::time then 21 else 20 end), book.id, msid);
	END IF;
		-- add code here if an error is needed when no booking  
	EXCEPTION 
		WHEN RAISE_EXCEPTION	THEN
			msg2 = '**BOOKING ERROR** use booking form';
			icons = 'G';
END;
$$ LANGUAGE plpgsql;

--pre@:old_msid is a non-concession and non-charge per visit membership --WTF
-- called from dashboard.xml:  
CREATE OR REPLACE FUNCTION swipe_confirm_membership(eeid INTEGER, old_msid INTEGER, new_msid INTEGER) RETURNS INTEGER AS $$
DECLARE
	rec RECORD;
	old_vicnt INTEGER;
	new_vicnt INTEGER;
	msid INTEGER := 0;
BEGIN

	IF old_msid != new_msid THEN --change membership
		IF door.concessionhandling IN (0,2)	--if it's not a charge door making these charge changes is nonsense...
			AND entryexit.takeconcession     --if it was not charged making charge changes is nonsense...
			from entryexit join door on entryexit.door=door.id where entryexit.id = eeid
		THEN 
			SELECT concession INTO old_vicnt FROM membership WHERE id = old_msid;
			SELECT concession INTO new_vicnt FROM membership WHERE id = new_msid;
				old_vicnt := old_vicnt - 1;
				new_vicnt := new_vicnt + 1;
			UPDATE membership SET concession = old_vicnt WHERE id = old_msid;
			UPDATE membership SET concession = new_vicnt WHERE id = new_msid;
	
			SELECT * INTO rec 
				FROM membership LEFT JOIN programme ON membership.programmeid=programme.id
				WHERE membership.id = new_msid;
			IF rec.programmebasisid = 5 THEN --is a charge per visit membership
		 		INSERT INTO debitjournal (membershipid, memberid, origin, note, debit)
		 			VALUES (new_msid, rec.memberid, 'Visit', rec.name || ' - Visit', COALESCE(rec.paymentdue, '0'::MONEY)::TEXT::MONEY);
			END IF; 
			SELECT * INTO rec 
				FROM membership LEFT JOIN programme ON membership.programmeid=programme.id
				WHERE membership.id = old_msid;
			IF rec.programmebasisid = 5 THEN --was a charge per visit membership
				RAISE EXCEPTION 'bug 3501';
			END IF; 
		END IF;
		UPDATE entryexit SET pending = 'f', membershipid = new_msid WHERE id = eeid;
	ELSE
		UPDATE entryexit SET pending='f' WHERE id = eeid;
	END IF;	
	
	msid := new_msid;
 	RETURN msid;
END;
$$ lANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION swipe_findms(arg_doornum INTEGER, arg_memid INTEGER) RETURNS INTEGER AS $$
DECLARE
    doorrec          DOOR;
    good             INTEGER;
    badmembership    INTEGER;
    alreadygood      INTEGER;
    rek              RECORD;
    preferconcession BOOLEAN;
    mshipid          INTEGER;
    foundbooking     INTEGER;
BEGIN
    SELECT * INTO doorrec FROM door WHERE door.id=arg_doornum;
    preferconcession := doorrec.concessionhandling = 2;
    
    RAISE NOTICE 'swipe_findms(%, %) preferconession=%', arg_doornum, arg_memid, preferconcession;
    FOR rek IN
        SELECT
            membership.id as msid,
            memberid
        FROM membership
            JOIN programme ON membership.programmeid = programme.id
            JOIN programmebasis ON programme.programmebasisid = programmebasis.id
        WHERE membership.memberid = arg_memid AND NOT membership.history
        ORDER BY
            CASE programmebasis.swipe_cpv = preferconcession WHEN TRUE THEN 1 ELSE 2 END,
            membership.id DESC
    LOOP
        -- For efficiency reasons, don't need to recalculate swipe_find_booking for each comparison to id. 
        foundbooking := swipe_find_booking(rek.memberid);
        mshipid = (SELECT membershipid FROM booking WHERE id = foundbooking);
        RAISE NOTICE 'DBG1 % % % % %','>>',good,alreadygood,badmembership,rek;
        -- Fix bookings here now... To do good things!.
        good = swipe_checkmembership(arg_doornum, rek.msid);
        IF good >= 0 THEN
            IF mshipid IS NOT NULL AND rek.msid = mshipid THEN
                RETURN rek.msid;
            END IF;
            IF alreadygood != 0 THEN 
                IF mshipid IS NULL THEN
                    RETURN alreadygood;
                END IF;
            END IF;
            alreadygood := rek.msid;
        ELSE
            IF badmembership IS NULL THEN
                badmembership := rek.msid;
            END IF;
        END IF;
    END LOOP;
    
    IF alreadygood IS NOT NULL THEN
        RETURN alreadygood;
    END IF;
    
    IF badmembership IS NOT NULL THEN
        RETURN badmembership;
    END IF;
    
    SELECT id INTO badmembership FROM membership WHERE memberid=arg_memid ORDER BY enddate DESC LIMIT 1;
    RETURN badmembership ;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS phonecelllogin(TEXT, INTEGER);
CREATE OR REPLACE FUNCTION phonecelllogin(cellstr TEXT, doornum INTEGER) RETURNS BOOLEAN AS $$
DECLARE
    membercell   TEXT;
    inputcell    TEXT;
    msg          TEXT := '';
    msg2         TEXT := '';
    msg3         TEXT := '';
    icons        TEXT := '';
    rec          RECORD;
    rek          RECORD;
    conf         RECORD;
    book         RECORD;
    memshpid     INTEGER;
    swiperet     INTEGER;
    rval         INTEGER;
    repeatvisit  BOOLEAN;
    pending      BOOLEAN;
BEGIN
    inputcell := regexp_replace(cellstr, '[^[:digit:]]*', '', 'g');
    FOR rec in
        -- TODO: Filter this select down to only valid members.
        SELECT * FROM member
    LOOP
        membercell := regexp_replace(rec.phonecell, '[^[:digit:]]*', '', 'g');
        IF inputcell = membercell THEN
            -- NOTE: Could use swipe_findmembership but cardno of a member isn't always going to be the cardno of the memberships of that member.
            -- Get the oldest current membership.
            memshpid := swipe_findms(doornum, rec.id);
            IF memshpid IS NOT NULL THEN
                -- Swipe in using the oldest current membership for this member at the given door number.
                -- swiperet := swipe_manual_ms(doornum, memshpid); -- Bad, manual swipes always allow access.
                rval := swipe_verifymembership(doornum,memshpid,false,null);
                -- ACCESS DENIED
                IF rval < 0 THEN
                    RAISE NOTICE 'phone (cell) login denied %',rval;
                    RETURN FALSE;
                END IF;
                
                -- Don't charge member before checking owing and suspend.
                SELECT * FROM swipe_getmsg(memshpid, FALSE, NULL) INTO msg;
                
                
                SELECT swipe_checkowing(doornum, NULL, memshpid, NULL, msg) INTO msg2;
                IF msg2 IS NULL THEN
                    --RETURN NEXT 'MSG01SEE RECEPTION';
                    --RETURN NEXT 'BUZ005';
                    --RETURN NEXT 'RLYJJ+b-r';
                    RAISE NOTICE 'See Reception, member owes money.';
                    RETURN FALSE;
                END IF;
                
                SELECT swipe_checksuspend(doornum, NULL, memshpid, msg) into msg3;
                IF msg3 IS NULL THEN
                    RETURN FALSE;
                END IF;
                msg2 = msg2 || msg3;
                
                SELECT INTO rek
                    member.id AS memberid, membership.concession AS visits,
                    membership.history, membership.enddate AS expiry,
                    membership.numrenewals AS renewals,
                    member.dob, programme.name AS prog,
                    programmebasisid AS ptype, programme.concession AS vlimit,
                    paymentdue AS pcost
                FROM membership
                    LEFT JOIN member ON member.id = membership.memberid
                    LEFT JOIN programme ON programme.id = membership.programmeid
                WHERE membership.id = memshpid;
                
                SELECT INTO conf
                    warningonowe AS warn, stoponowe, showstats, showdollar,
                    stopafterowe, warnafterowe, showvisitstats, swipe_timeout, treatcustom1asname,
                    door.*, (graceperiod * '1 day'::INTERVAL) AS graceperiod
                FROM config, door
                WHERE door.id = doornum;
                
                IF conf.showvisitstats=TRUE THEN
                    msg2 := msg2 || member_visitstats(memshpid);
                END IF;
                -- Check for any reminder messages
                msg2 := msg2 || member_reminders(rek.memberid,memshpid);

                SELECT swipe_checkdoubleswipe(rek.memberid, memshpid,NULL) INTO repeatvisit;
                
                -- Add charge for charge per visit memberships.
                -- NOTE: The way we're selecting memberships is just "the oldest current", do you really want to be charging to that?
                IF rek.ptype = 5 AND repeatvisit = 'f' THEN
                    msg := msg || ', ' || 'Charge per Visit of ' || COALESCE(rek.pcost, '0'::MONEY)::TEXT;
                    INSERT INTO debitjournal (membershipid, memberid, origin, note, debit)
                    VALUES (memshpid, rek.memberid, 'Visit', rek.prog || ' - Visit', COALESCE(rek.pcost, '0'::MONEY)::TEXT::MONEY);
                    UPDATE membership SET concession = concession + 1 WHERE id = memshpid;
                END IF;
                
                -- check booking
                IF conf.booking_checkin > 0 THEN
                    book  := swipe_checkbooking(memshpid, rek.memberid);
                    msg2  := msg2 || book.msg2;
                    icons := icons || book.icons;
                END IF;
                
                -- TODO: Set pending to TRUE if more than one current membership, of which contains at least one concession membership.
                pending := FALSE;
                
                -- TODO: Fix icons.
                perform swipe_granted(doornum, NULL, memshpid, rek.memberid, msg, msg2, icons, FALSE, rek.visits, pending);
                
                IF rval >= 0 THEN
                    RETURN TRUE;
                END IF;
            END IF;
            RETURN FALSE;
        END IF;
    END LOOP;
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;

