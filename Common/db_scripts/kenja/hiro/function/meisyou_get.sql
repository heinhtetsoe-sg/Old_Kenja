drop function db2inst1.meisyou_get

CREATE FUNCTION db2inst1.Meisyou_Get \
( \
	in_namecode	varchar(4), \
	in_namecd	varchar(4), \
	in_pos		integer \
) RETURNS varchar(60) \
 READS SQL DATA \
 SPECIFIC Meisyou_get \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 LANGUAGE SQL \
 BEGIN ATOMIC \
\
--	IF in_namecode = '' OR in_namecode IS NULL THEN \
--		RETURN NULL; \
--	END IF; \
	IF in_pos = 1 THEN \
		RETURN select name1 from name_mst where namecd1 = in_namecd and namecd2 = in_namecode; \ 
	ELSEIF in_pos =	2 THEN \
		RETURN SELECT name2 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	ELSEIF in_pos = 3 THEN \
		RETURN SELECT name3 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	ELSEIF in_pos = 4 THEN \
		RETURN SELECT abbv1 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	ELSEIF in_pos = 5 THEN \
		RETURN SELECT abbv2 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	ELSEIF in_pos =  6 THEN \
		RETURN SELECT abbv3 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	ELSEIF in_pos = 7 THEN \
		RETURN SELECT namespare1 FROM db2inst1.name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	ELSEIF in_pos = 8 THEN \
		RETURN SELECT namespare2 FROM db2inst1.name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	ELSEIF in_pos = 9 THEN \
		RETURN SELECT namespare3 FROM db2inst1.name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode; \
	END IF; \
	RETURN NULL; \
END

