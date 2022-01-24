drop function conv_col_row 

CREATE FUNCTION Conv_Col_Row \
( \
    in_attendno		varchar(2), \
    in_colrowcd         Integer \
) \
 RETURNS varchar(5) \
 SPECIFIC conv_col_row \
 LANGUAGE SQL \
 CONTAINS SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE   Wk_attendno	integer default 0; \
 DECLARE   Wk_char      varchar(5); \
    set WK_attendno = integer(in_attendno)  -  1; \
 \
 checkloop: \
 while Wk_attendno <> 0 do \
        IF (WK_attendno / 7) < 1 THEN \
	   LEAVE checkloop; \
	ELSE \
	   IF Wk_char <> '' THEN \
              set Wk_char     = substr(char(MOD(Wk_attendno, 7)),1,1) || Wk_char; \
	   ELSE \
              set Wk_char    = substr(char(MOD(Wk_attendno,7)),1,1); \
	   END IF; \
           set Wk_attendno = (Wk_attendno - MOD(Wk_attendno, 7)) / 7; \
       END IF; \
 end while checkloop; \ 
\
  IF Wk_char <> '' THEN \
     set Wk_char = CHAR(RIGHT('000' || substr(char(Wk_attendno),1,1) || Wk_char, 3),3); \
  ELSE \
     set Wk_char = CHAR(RIGHT('000' || substr(char(Wk_attendno),1,1),3),3); \
  END IF; \
   \
  IF IN_colrowcd = 2 THEN \
     	  set Wk_char = substr(digits(integer(SUBSTR(Wk_char, 1, 2)) + 1),9,2); \
  ELSE \
       set Wk_char = substr(digits(integer(SUBSTR(Wk_char, 3, 1)) + 1),9,2); \
  END IF; \
   RETURN  Wk_char; \
 END 

