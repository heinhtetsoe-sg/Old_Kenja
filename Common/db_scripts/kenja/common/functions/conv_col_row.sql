drop function conv_col_row 

CREATE FUNCTION Conv_Col_Row \
( \
    in_attendno		varchar(3), \
    in_colrowcd         Integer \
) \
 RETURNS varchar(5) \
 SPECIFIC conv_col_row \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 CONTAINS SQL \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE   Wk_val integer default 0; \
 DECLARE   ret_val varchar(5) default NULL; \
 -- column \
 IF in_colrowcd = 1 THEN \
     set Wk_val = MOD(Integer(in_attendno), 7); \
     IF Wk_val = 0 THEN \
         set Wk_val = Wk_val + 7; \
     END IF; \ 
     set ret_val = substr(digits(integer(SUBSTR(CHAR(Wk_val), 1, 2))),9,2); \
 -- row \
 ELSEIF in_colrowcd = 2 THEN \
     set Wk_val = Integer(in_attendno) / 7; \
     IF MOD(Integer(in_attendno),7) <> 0 THEN \
	     set Wk_val = Wk_val + 1; \
     END IF; \
     set ret_val = substr(digits(integer(SUBSTR(CHAR(Wk_val), 1, 2))),9,2); \
 \
 END IF; \ 
 RETURN ret_val; \
END 

