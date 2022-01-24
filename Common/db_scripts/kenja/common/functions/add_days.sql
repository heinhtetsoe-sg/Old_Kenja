--
-- Name of UDF: ADD_DAYS (timestamp, addDays)
--
--
-- Used UDF: None
--
--

CREATE FUNCTION ADD_DAYS (D date,addDays integer) \
 RETURNS date \
 SPECIFIC ADD_DAYS \
 LANGUAGE SQL \
 CONTAINS SQL \
 NO EXTERNAL ACTION \
 BEGIN ATOMIC \
	DECLARE wkDate date; \
	IF addDays>=0 THEN \
		SET wkDate=D + year(dec(addDays,8,0)) years + month(dec(addDays,8,0)) months + day(dec(addDays,8,0)) days; \
	ELSE \
		SET wkDate=D - day(dec(abs(addDays),8,0)) days - month(dec(abs(addDays),8,0)) months - year(dec(abs(addDays),8,0)) years; \
	END IF; \
	return wkDate; \
 END
	
