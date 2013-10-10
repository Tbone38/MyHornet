select decode(substring(imagedata from 3),'base64'), memberid, lastupdated, created from image where  
	substring(imagedata,1,2) = '1|' and length(imagedata)>200
