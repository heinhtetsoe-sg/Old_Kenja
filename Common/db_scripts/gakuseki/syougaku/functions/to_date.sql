--
-- DB2 UDB UDF(User Defined Function) Samples for Migration
--
-- 2001/11/06
--
-- Name of UDF: TO_DATE
--
-- Used UDF: None 
--
-- Pre-Requisites: DB2 UDB V7.2 / V7.1 + FixPak 3
--                   Because dynamic SQL procedure statements are used in this function.
-- 

--(1) ���ץꥱ������󡦥ҡ��� (APPLHEAPSZ) ���礭������ͭ���ˤ��뤿��ǡ����١������ޥ͡����㡼��ƻ�ư
--    Expand Application heap (APPLHEAPSZ), then restart database manager to enable it.

-- update db cfg for sample using applheapsz 512


--(2) �ؿ�������ȼ¹���
--    Definition of function and example of using it.

------------------------------------- ���ϥ��ޥ�� -------------------------------------
drop specific function to_dateOracle
---------------------------------------------------
--DB20000I  SQL ���ޥ�ɤ�����˽�λ���ޤ�����


CREATE FUNCTION TO_DATE (CH Varchar(254), FMT Varchar(254)) \
 RETURNS Timestamp \
 SPECIFIC TO_DATEOracle \
 LANGUAGE SQL \
 CONTAINS SQL \
 EXTERNAL ACTION \
 NOT DETERMINISTIC \
BEGIN ATOMIC \
DECLARE Pos       Integer     default 1; \
DECLARE Cpos      Integer     default 1; \
DECLARE RetVal    timestamp   default timestamp('00010101000000'); \
DECLARE FMTCase   Integer     default 0; \
DECLARE FMInd     Integer     default 0; \
DECLARE FXInd     Integer     default 0; \
DECLARE Flen      Integer     default 0; \
DECLARE Dlen      Integer     default 0; \
DECLARE Clen      Integer     default 0; \
DECLARE Cstr      VarChar(10) default ''; \
DECLARE int_month Integer     default 0; \
 \
MainLoop: WHILE Pos <= length(FMT) DO \
  SET FMTCase = \
    CASE \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'FM'     THEN 0 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'AD'     THEN 1 \
      WHEN upper(substr(FMT || '    ',Pos,4)) = 'A.D.'   THEN 2 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'AM'     THEN 3 \
      WHEN upper(substr(FMT || '    ',Pos,4)) = 'A.M.'   THEN 4 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'BC'     THEN 5 \
      WHEN upper(substr(FMT || '    ',Pos,4)) = 'B.C.'   THEN 6 \
      WHEN upper(substr(FMT || '    ',Pos,3)) = 'DDD'    THEN 9 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'DD'     THEN 10 \
--      WHEN upper(substr(FMT || '    ',Pos,3)) = 'DAY'    THEN 11 \
--      WHEN upper(substr(FMT || '    ',Pos,2)) = 'DY'     THEN 12 \
--      WHEN upper(substr(FMT          ,Pos,1)) = 'D'      THEN 13 \
      WHEN upper(substr(FMT || '    ',Pos,4)) = 'HH24'   THEN 16 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'HH'     THEN 18 \
      WHEN upper(substr(FMT          ,Pos,1)) = 'J'      THEN 24 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'MI'     THEN 25 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'MM'     THEN 26 \
      WHEN upper(substr(FMT || '    ',Pos,5)) = 'MONTH'  THEN 27 \
      WHEN upper(substr(FMT || '    ',Pos,3)) = 'MON'    THEN 28 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'RM'     THEN 32 \
      WHEN upper(substr(FMT || '    ',Pos,4)) = 'RRRR'   THEN 33 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'RR'     THEN 34 \
      WHEN upper(substr(FMT || '    ',Pos,5)) = 'SSSSS'  THEN 35 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'SS'     THEN 36 \
      WHEN upper(substr(FMT || '    ',Pos,5)) = 'Y,YYY'  THEN 39 \
      WHEN upper(substr(FMT || '    ',Pos,5)) = 'SYYYY'  THEN 42 \
      WHEN upper(substr(FMT || '    ',Pos,4)) = 'YYYY'   THEN 43 \
      WHEN upper(substr(FMT || '    ',Pos,3)) = 'YYY'    THEN 44 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'YY'     THEN 45 \
      WHEN upper(substr(FMT          ,Pos,1)) = 'Y'      THEN 46 \
      WHEN upper(substr(FMT || '    ',Pos,2)) = 'FX'     THEN 95 \
      ELSE                                                    99 \
    END; \
 \
  IF FMTCase = 99 AND FXInd = 1 AND substr(CH,Cpos,1) <> substr(FMT,Pos,1) THEN  \
     SIGNAL SQLSTATE 'U1861' SET MESSAGE_TEXT = '��ƥ�뤬��ʸ����Ȱ��פ��ޤ���'; \
  END IF; \
 \
  SEt Flen = \
    CASE \
      WHEN FMTCase in ( 0,95) THEN 0 \
      WHEN FMTCase in (13,46,99) THEN 1 \
      WHEN FMTCase in ( 1, 5,10,12,16,18,25,26,33,36,45) THEN 2 \
      WHEN FMTCase in ( 9,28,44) THEN 3 \
      WHEN FMTCase in ( 2, 3, 4, 6,27,32,34,43) THEN 4 \
      WHEN FMTCase in (35,39,42) THEN 5 \
      WHEN FMTCase = 11 THEN 6 \
      WHEN FMTCase = 24 THEN 7 \
    END; \
  SEt Dlen =  \
    CASE \
      WHEN substr(CH || 'x',Cpos+1,1) not between '0' and '9' THEN 1 \
      WHEN substr(CH || 'xx',Cpos+2,1) not between '0' and '9' THEN 2 \
      WHEN substr(CH || 'xxx',Cpos+3,1) not between '0' and '9' THEN 3 \
      WHEN substr(CH || 'xxxx',Cpos+4,1) not between '0' and '9' THEN 4 \
      WHEN substr(CH || 'xxxxx',Cpos+5,1) not between '0' and '9' THEN 5 \
      ELSE 6 \
    END; \
 \
  SET Clen = \
    CASE \
      WHEN FMTCase in ( 9,10,16,18,25,26,35,36) \
       AND Dlen < Flen \
      THEN Dlen \
      WHEN FMTCase = 27 \
      THEN Dlen + 2 \
      ELSE Flen \
    END; \
  SET Cstr = substr(CH,Cpos,Clen); \
 \
  IF FMTCase <> 27 AND FXInd = 1 AND FMInd = 0 AND Clen <> Flen THEN \
     SIGNAL SQLSTATE 'U1862' SET MESSAGE_TEXT = '���ͤ��񼰹��ܤ�Ĺ���Ȱ��פ��ޤ���'; \
  END IF; \
 \
  IF FMTCase in (3,4) AND Cstr NOT in ('����','���') THEN \
     SIGNAL SQLSTATE 'U1855' SET MESSAGE_TEXT = 'AM/A.M.�ޤ���PM/P.M.��ɬ�פǤ���'; \
  END IF; \
 \
  IF FMTCase in (1,2,5,6) AND Cstr <>'����' THEN \
     SIGNAL SQLSTATE 'U1856' SET MESSAGE_TEXT = 'BC/B.C.�ޤ���AD/A.D.��ɬ�פǤ���'; \
  END IF; \
 \
--  IF FMTCase = 27 THEN \
--     IF Cstr NOT in ('1��','2��','3��','4��','5��','6��','7��','8��','9��','10��','11��','12��') THEN \
--        SIGNAL SQLSTATE 'U1843' SET MESSAGE_TEXT = '���ꤷ���̵���Ǥ���'; \
--     ELSE \
--        SET RetVal = RetVal + (substr(Cstr,1,Dlen-2) - 1) month; \
--     END IF; \
--  END IF; \
 \
  IF FMTCase in (27,28,32) THEN \
     IF FMTCase = 27 THEN \
        SET int_month = (locate(Cstr,'1�� ,2�� ,3�� ,4�� ,5�� ,6�� ,7�� ,8�� ,9�� ,10��,11��,12��') + 4) / 5; \
     ELSEIF FMTCase = 28 THEN \
        SET int_month = (locate(upper(Cstr),'JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC') + 3) / 4; \
     ELSE \
       SET int_month = CASE \
                         WHEN upper(substr(Cstr,1,3)) = 'XII'  THEN 12 \
                         WHEN upper(substr(Cstr,1,2)) = 'XI'   THEN 11 \
                         WHEN upper(substr(Cstr,1,1)) = 'X'    THEN 10 \
                         WHEN upper(substr(Cstr,1,2)) = 'IX'   THEN  9 \
                         WHEN upper(Cstr)             = 'VIII' THEN  8 \
                         WHEN upper(substr(Cstr,1,3)) = 'VII'  THEN  7 \
                         WHEN upper(substr(Cstr,1,2)) = 'VI'   THEN  6 \
                         WHEN upper(substr(Cstr,1,1)) = 'V'    THEN  5 \
                         WHEN upper(substr(Cstr,1,2)) = 'IV'   THEN  4 \
                         WHEN upper(substr(Cstr,1,3)) = 'III'  THEN  3 \
                         WHEN upper(substr(Cstr,1,2)) = 'II'   THEN  2 \
                         WHEN upper(substr(Cstr,1,1)) = 'I'    THEN  1 \
                         ELSE 0 \
                       END; \
       SET Clen = CASE  \
                    WHEN int_month in (10, 5, 1)       THEN 1 \
                    WHEN int_month in (11, 9, 6, 4, 2) THEN 2 \
                    WHEN int_month in (12, 7, 3)       THEN 3 \
                    WHEN int_month = 8                 THEN 4 \
                    ELSE 4                  \
                  END; \
     END IF; \
     IF int_month < 1 THEN \
        SIGNAL SQLSTATE 'U1843' SET MESSAGE_TEXT = '���ꤷ���̵���Ǥ���'; \
     ELSE \
        SET RetVal = RetVal + (int_month - 1) month; \
     END IF; \
  END IF; \
 \
  SET RetVal = \
    CASE FMTCase \
      WHEN  3 THEN RetVal + \
                  (CASE Cstr \
                     WHEN '����' THEN 0 \
                     WHEN '���' THEN 12 \
                     ELSE int('Error FMTCase = ' || substr(char(FMTCase),1,1)) \
                   END) hours \
      WHEN  4 THEN RetVal + \
                  (CASE Cstr \
                     WHEN '����' THEN 0 \
                     WHEN '���' THEN 12 \
                     ELSE int('Error FMTCase = ' || substr(char(FMTCase),1,1)) \
                   END) hours \
      WHEN  9 THEN RetVal + (int(Cstr) - days(RetVal)) days \
      WHEN 10 THEN RetVal + (int(Cstr) - 1) days \
      WHEN 16 THEN RetVal + int(Cstr) hours \
      WHEN 18 THEN RetVal + int(Cstr) hours \
      WHEN 24 THEN RetVal + (int(Cstr) - julian_day(date(RetVal))) days  \
      WHEN 25 THEN RetVal + int(Cstr) minute \
      WHEN 26 THEN RetVal + (int(Cstr) - 1) month \
      WHEN 33 THEN RetVal + (int(Cstr) - 1) years \
      WHEN 34 \
        THEN RetVal \
           + ( ( int(year(current date)/100) \
               + CASE \
                   WHEN int(substr(char(year(current date)),3,2)) <= 49 AND int(Cstr) <= 49 \
                     OR int(substr(char(year(current date)),3,2)) >= 50 AND int(Cstr) >= 50 \
                   THEN  0 \
                   WHEN int(substr(char(year(current date)),3,2)) <= 49 AND int(Cstr) >= 50 \
                   THEN -1 \
                   ELSE  1 \
                 END \
               ) * 100 \
             + int(Cstr) - 1 \
             ) years \
      WHEN 35 THEN RetVal - hour(RetVal) hours - second(RetVal) seconds + int(Cstr) seconds \
      WHEN 36 THEN RetVal + int(Cstr) seconds \
      WHEN 39 THEN RetVal + (int(substr(Cstr,1,1) || substr(Cstr,3,3)) - 1) years \
      WHEN 42 THEN RetVal + (int(Cstr) - 1) years \
      WHEN 43 THEN RetVal + (int(Cstr) - 1) years \
      WHEN 44 THEN RetVal + (int(Cstr) + int(year(current date)/1000)*1000 - 1) years \
      WHEN 45 THEN RetVal + (int(Cstr) + int(year(current date)/100)*100 - 1) years \
      WHEN 46 THEN RetVal + (int(Cstr) + int(year(current date)/10)*10 - 1) years \
      ELSE RetVal \
    END; \
 \
  SET Pos = Pos + \
    CASE \
      WHEN FMTCase in (13,24,46,99) THEN 1 \
      WHEN FMTCase in (0,1,3,5,10,12,18,25,26,32,33,36,45,95) THEN 2 \
      WHEN FMTCase in (11,9,28,44) THEN 3 \
      WHEN FMTCase in (2,4,6,16,34,43) THEN 4 \
      WHEN FMTCase in (27,35,39,42) THEN 5 \
    END; \
 \
  SET Cpos = Cpos + Clen; \
 \
  SET FMInd = CASE FMTCase WHEN  0 THEN 1 - FMInd ELSE FMInd END; \
  SET FXInd = CASE FMTCase WHEN 95 THEN 1 - FXInd ELSE FXInd END; \
 \
END WHILE MainLoop; \
 \
RETURN RetVal; \
END


