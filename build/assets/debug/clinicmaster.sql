select * from Time t
	LEFT OUTER JOIN Booking b
		ON (t._ID = b.stimeid)
		AND b.RID = <rid> AND b.result > 5
	LEFT OUTER Dates d
		ON ( d.dates = b.arrival)
		AND b.arrival = <date>
	LEFT OUTER JOIN opentime ot
		ON (ot.dayofweek = d.dayofweek)
WHERE t._ID >= ot.openid
	AND t._ID <= ot.closeid;
