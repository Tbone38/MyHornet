SELECT t.time as _id, name, booking, t2.time as etime
FROM Time t LEFT OUTER JOIN Booking b 
ON (t._id = b.starttime)
AND resourceid = 'input'
AND arrival = 'today'
LEFT JOIN Time t2 
ON (t2._id = b.etime)
--below where clause isn't used, would it work?
WHERE t.bookingid 
GROUP BY _id
ORDER BY _id ASC;
