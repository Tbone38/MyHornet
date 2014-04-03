SELECT
    member.id AS memberid,
    member.cardno AS membercardno,
    member.gender AS gender, 
    entryexit.membershipid,
    entryexit.cardno as cardno,
    EXTRACT(epoch FROM entryexit.time) as datetime,
    to_char(entryexit.time,'DD Mon') AS sdate,
    to_char(entryexit.time,'HH12:MIam') AS stime12,
    to_char(entryexit.time,'HH24:MI') AS stime24,
    -- If no deny reason, should this be '' or 'Granted'?
    COALESCE(deny.text_value, 'Granted') AS denyreason,
    -- Liable to be NULL most of the time with gyms not requiring you swipe when you check out.
    to_char((entryexit.checkout - entryexit.time), 'HH24:MI') AS len,
    CASE
        WHEN membership.cardno != entryexit.cardno THEN
            coalesce('Family:'||additional_idcard.name,member.firstname)
        WHEN member.id IS NOT NULL THEN
            member.firstname
        ELSE 'card#'|| entryexit.cardno::text
    END AS fmname, 
    CASE
    	WHEN membership.cardno != entryexit.cardno THEN
		member.surname
	WHEN member.id IS NOT NULL THEN
	   member.surname
	ELSE 'card#'|| entryexit.cardno::text
    END AS lmname,
    CASE
        WHEN entryexit.membershipid IS NOT NULL THEN programme.name
        WHEN entryexit.access = 1 THEN '[InterClub Visitor]'
        ELSE 'Unknown Membership'
    END AS pname,
    to_char(membership.enddate,'DD Mon YY') AS msexpiry,
    membership.concession AS msvisits,
    -- For happiness, easier to use integer representations or the text emotes?
    CASE
        WHEN member.happiness = 1 THEN ':)'
        WHEN member.happiness = 0 THEN ':|'
        WHEN member.happiness <= -1 THEN ':('
        -- No idea what happiness == 2 is.
        WHEN member.happiness = 2 THEN '||'
        ELSE ''
    END AS happiness,
    CASE
        WHEN entryexit.access = 1 THEN false
        WHEN entryexit.access = 0 THEN false
        ELSE true
    END AS result,
    door.name AS doorname,
    (SELECT count(*) FROM task where task.memberid = member.id AND NOT cleared AND NOT deleted AND flagon >= entryexit.time::date) AS taskpending,
    (SELECT count(*) FROM booking where booking.memberid = member.id AND arrival = entryexit.time::date AND starttime BETWEEN entryexit.time::time AND entryexit.time::time+'3 hours'::interval) AS bookingpending,
    -- These colours are from the Last Visitors list in PC GymMaster, colouring for phone/tablet will be different?
    CASE
        WHEN EXISTS (SELECT 1 FROM task WHERE task.memberid = member.id AND NOT cleared AND NOT deleted AND flagon >= entryexit.time::date) THEN '#1919AA'
        WHEN pending = 't' THEN '#FF0000'
        WHEN member_owe_no_unfinished(member.id) > '0'::money THEN '#8B0000'
        WHEN access >= 2 THEN '#808000'
        ELSE '#000000'
    END AS fgcolour,
    member.phonehome AS mphhome, member.phonework AS mphwork, member.phonecell AS mphcell,
    member.email AS memail,
    membership.startdate AS msstart,
    (SELECT flagon::text||' - '||description FROM task WHERE memberid=member.id AND flagon <= now() ORDER BY flagon DESC limit 1) as task1,
    (SELECT flagon::text||' - '||description FROM task WHERE memberid=member.id AND flagon <= now() ORDER BY flagon DESC limit 1 OFFSET 1) as task2,
    (SELECT flagon::text||' - '||description FROM task WHERE memberid=member.id AND flagon <= now() ORDER BY flagon DESC limit 1 OFFSET 2) as task3,
    (SELECT COALESCE(classname,(SELECT name FROM bookingtype WHERE id=bookingtypeid))||' ('||(SELECT name FROM resource WHERE id=resourceid)||') at '||arrival::text||' '||starttime::text||'-'||endtime::text FROM booking WHERE memberid=member.id AND arrival+starttime > now() ORDER BY arrival, starttime LIMIT 1) AS booking1,
    (SELECT COALESCE(classname,(SELECT name FROM bookingtype WHERE id=bookingtypeid))||' ('||(SELECT name FROM resource WHERE id=resourceid)||') at '||arrival::text||' '||starttime::text||'-'||endtime::text FROM booking WHERE memberid=member.id AND arrival+starttime > now() ORDER BY arrival, starttime LIMIT 1 OFFSET 1) AS booking2,
    (SELECT COALESCE(classname,(SELECT name FROM bookingtype WHERE id=bookingtypeid))||' ('||(SELECT name FROM resource WHERE id=resourceid)||') at '||arrival::text||' '||starttime::text||'-'||endtime::text FROM booking WHERE memberid=member.id AND arrival+starttime > now() ORDER BY arrival, starttime LIMIT 1 OFFSET 2) AS booking3,
    (SELECT entryexit.time FROM entryexit LEFT JOIN membership ON membership.id=entryexit.membershipid WHERE membership.memberid=member.id ORDER BY time DESC LIMIT 1 OFFSET 1) AS lastvisit1,
    (SELECT entryexit.time FROM entryexit LEFT JOIN membership ON membership.id=entryexit.membershipid WHERE membership.memberid=member.id ORDER BY time DESC LIMIT 1 OFFSET 2) AS lastvisit2,
    (SELECT entryexit.time FROM entryexit LEFT JOIN membership ON membership.id=entryexit.membershipid WHERE membership.memberid=member.id ORDER BY time DESC LIMIT 1 OFFSET 3) AS lastvisit3
FROM entryexit
    LEFT JOIN door ON (door.id = entryexit.door)
    LEFT JOIN membership ON (entryexit.membershipid=membership.id)
    LEFT JOIN programme ON (programme.id = programmeid)
    LEFT JOIN member ON member.id = COALESCE(membership.memberid, (SELECT id FROM member WHERE member.cardno=entryexit.cardno LIMIT 1))
    LEFT JOIN lookup_default_value as deny on deny.id=entryexit.access+1990
    LEFT JOIN additional_idcard on additional_idcard.childcardid=entryexit.cardno
-- Assuming stationid is 1, configurable?
WHERE (entryexit.door in (select door.id from door, station where door.companyid = station.companyid and station.stationid = 1)
    OR (select count(*) from door, station where door.companyid = station.companyid and station.stationid = 1) = 0)
    -- Will need to add a check against entryexit.time to limit results to the last x days when not just using a test database.
    -- AND entryexit.time > now()-'4 days'::interval
ORDER BY entryexit.time desc
LIMIT 200;
