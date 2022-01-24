drop function DateCnv

CREATE FUNCTION DateCnv(in_datedata date, in_style integer) \
 RETURNS varchar(256) \
 SPECIFIC DateCnv \
 LANGUAGE SQL \
 CONTAINS SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE	wk_year		smallint; \
 DECLARE	ret_val		varchar(256) default ''; \
\
	IF	in_style = 1 	THEN \
		SET wk_year = YEAR(in_datedata); \
		IF MONTH(in_datedata) >= 1 \
		AND MONTH(in_datedata) <= 3 THEN \
			SET wk_year = wk_year - 1; \
		END IF; \
		SET ret_val = CHAR(char(wk_year), 4); \
	ELSE \
		SET ret_val = ''; \ 
	END IF; \
	RETURN ret_val; \
 END 

