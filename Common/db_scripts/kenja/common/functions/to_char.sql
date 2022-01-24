--
-- DB2 UDB UDF(User Defined Function) Samples for Migration
--
-- 2001/09/10, 09/13, 09/17, 11/05
--
-- Name of UDF: TO_CHAR (timestamp, fmt)
--
--
-- Used UDF: None
--
--

--(1) アプリケーション・ヒープ (APPLHEAPSZ) を大きくし、有効にするためデータベース・マネージャーを再始動
--    Expand Application heap (APPLHEAPSZ), then restart database manager to enable it.

-- update db cfg for sample using applheapsz 512


--(2) 関数の定義と実行例
--    Definition of function and example of using it.

------------------------------------- 入力コマンド -------------------------------------
drop specific function to_chartimestamp
---------------------------------------------------
--DB20000I  SQL コマンドが正常に終了しました。


------------------------------------- 入力コマンド -------------------------------------
CREATE FUNCTION TO_CHAR (D TImestamp, FMT Varchar(254)) \
 RETURNS Varchar(254) \
 SPECIFIC TO_CHARTimestamp \
 LANGUAGE SQL \
 CONTAINS SQL \
 NO EXTERNAL ACTION \
 NOT DETERMINISTIC \
 RETURN \ 
WITH \
Const_Tbl (ISOYear) AS \
(Values \
 CASE \
   WHEN locate('IW',upper(FMT)) > 0 THEN \
     CASE \
       WHEN month(D) = 12 and week_iso(D) =  1 THEN year(D) + 1 \
       WHEN month(D) =  1 and week_iso(D) > 50 THEN year(D) - 1 \
       ELSE year(D) \
     END \
   ELSE     year(D) \
 END \
) \
 \
,Repeat (Pos, RetStr, CatStr, FMInd, PreFMTCase, PreFMTChar) AS \
(Values (1, Varchar('',254), Varchar('',254), 0, 99, '     ') \
 Union All \
 Select \
 Pos + \
 CASE \
   WHEN FMTCase in (13,15,23,24,31,38,46,99) THEN 1 \
   WHEN FMTCase in (0,1,3,5,8,10,12,14,18,19,22,25,26,29,32,33,36,37,45,96,97) THEN 2 \
   WHEN FMTCase in (7,11,9,21,28,44) THEN 3 \
   WHEN FMTCase in (2,4,6,17,16,20,30,34,41,43) THEN 4 \
   WHEN FMTCase in (27,35,39,40,42) THEN 5 \
   WHEN FMTCase = 98 THEN locate('''',FMT,Pos+1)+1-Pos \
 END \
,RetStr || \
 CASE \
   WHEN PreFMTCase in (28,32,40,41) \
    AND locate(substr(CatStr,1,1),'ABCDEFGHIJKLMNOPQRSTUVWXYZ') > 0 THEN \
     CASE \
       WHEN substr(PreFMTChar,1,1) = upper(substr(PreFMTChar,1,1)) \
       THEN upper(substr(CatStr,1,1)) \
       ELSE lower(substr(CatStr,1,1)) \
     END || \
     CASE \
       WHEN substr(PreFMTChar,2,1) = upper(substr(PreFMTChar,2,1)) \
       THEN upper(substr(CatStr,2)) \
       ELSE lower(substr(CatStr,2)) \
     END \
   ELSE CatStr \
 END \
,CASE FMTCase \
   WHEN  0 THEN '' \
   WHEN  1 THEN '西暦' \
   WHEN  2 THEN '西暦' \
   WHEN  3 THEN CASE WHEN hour(D) < 12 THEN '午前' ELSE '午後' END \
   WHEN  4 THEN CASE WHEN hour(D) < 12 THEN '午前' ELSE '午後' END \
   WHEN  8 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values year(D)/100 + 1) AS Q(C)) \
   WHEN  9 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),8,3) ELSE rtrim(char(C)) END \
                 From  (Values dayofyear(D)) AS Q(C)) \
   WHEN 10 THEN CASE FMInd WHEN 0 THEN substr(digits(day(D)),9,2) ELSE rtrim(char(day(D))) END \
   WHEN 11 THEN dayname(D) \
   WHEN 12 THEN substr(dayname(D),1,2) \
   WHEN 13 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values dayofweek(D)) AS Q(C)) \
   WHEN 16 THEN CASE FMInd WHEN 0 THEN substr(digits(hour(D)),9,2) ELSE rtrim(char(hour(D))) END \
   WHEN 18 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values mod(hour(D)+11,12)+1) AS Q(C)) \
   WHEN 19 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values week_iso(D)) AS Q(C)) \
   WHEN 20 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),7,4) ELSE rtrim(char(C)) END \
                 From Const_Tbl AS Q(C)) \
   WHEN 21 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(ISOYear),8,3) \
                                   ELSE        rtrim(char(mod(ISOYear,1000))) END \
                 From  Const_Tbl) \
   WHEN 22 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(ISOYear),9,2) \
                                   ELSE        rtrim(char(mod(ISOYear,100))) END \
                 From  Const_Tbl) \
   WHEN 23 THEN (Select substr(digits(ISOYear),10,1) from Const_Tbl) \
   WHEN 24 THEN rtrim(char(julian_day(D))) \
   WHEN 25 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values minute(D)) AS Q(C)) \
   WHEN 26 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values month(D)) AS Q(C)) \
   WHEN 27 THEN monthname(D) \
   WHEN 28 THEN substr('JanFebMarAprMayJunJulAugSepOctNovDec',month(D)*3-2,3) \
   WHEN 29 THEN CASE WHEN hour(D) < 12 THEN '午前' ELSE '午後' END \
   WHEN 30 THEN CASE WHEN hour(D) < 12 THEN '午前' ELSE '午後' END \
   WHEN 31 THEN rtrim(char(quarter(D))) \
   WHEN 32 THEN \
                CASE \
                  WHEN month(D) < 4 THEN substr('III', 1,month(D)) \
                  WHEN month(D) = 4 THEN        'IV' \
                  WHEN month(D) < 9 THEN substr('VIII',1,month(D)-4) \
                  WHEN month(D) = 9 THEN        'IX' \
                  WHEN 9 < month(D) THEN substr('XII', 1,month(D)-9) \
                END \
   WHEN 35 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),6,5) ELSE rtrim(char(C)) END \
                 From  (Values midnight_seconds(D)) AS Q(C)) \
   WHEN 36 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values second(D)) AS Q(C)) \
   WHEN 37 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),6,5) ELSE rtrim(char(C)) END \
                 From  (Values mod(dayofyear(D)-1,7)+1) AS Q(C)) \
   WHEN 38 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),6,5) ELSE rtrim(char(C)) END \
                 From  (Values mod(day(D)-1,7)+1) AS Q(C)) \
   WHEN 39 THEN (Select \
                 CASE \ 
                   WHEN FMInd = 0 OR C >= 1000 THEN substr(digits(C),7,1) || ',' || substr(digits(C),8,3) \
                   ELSE rtrim(char(C)) \
                 END \
                 From (Values year(D) ) AS Q(C)) \
   WHEN 43 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),7,4) ELSE rtrim(char(C)) END \
                 From  (Values year(D)) AS Q(C)) \
   WHEN 44 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),8,3) ELSE rtrim(char(C)) END \
                 From  (Values mod(year(D),1000) ) AS Q(C)) \
   WHEN 45 THEN (Select CASE FMInd WHEN 0 THEN substr(digits(C),9,2) ELSE rtrim(char(C)) END \
                 From  (Values mod(year(D),100) ) AS Q(C)) \
   WHEN 46 THEN  substr(digits(year(D)),10,1) \
   WHEN 96 THEN '' \
   WHEN 97 THEN \
     CASE \
       WHEN PreFMTCase in (7,8,9,10,13,16,17,18,19,20,21,22,23,24,25,26,31,33,34,35,36,37,38,39,42,43,44,45,46) THEN \
\
         CASE CASE PreFMTCase \ 
                WHEN 39 THEN integer(replace(CatStr,',','')) \
                ELSE integer(CatStr) \
              END \
           WHEN 1 THEN 'st' \
           WHEN 2 THEN 'nd' \
           WHEN 3 THEN 'rd' \
           ELSE        'th' \
         END \
       ELSE            '' \
     END \
   WHEN 98 THEN substr(FMT,Pos+1,locate('''',FMT,Pos+1)-Pos-1) \
   WHEN 99 THEN substr(FMT,Pos,1) \
   ELSE '' \
 END \
,CASE FMTCase WHEN 0 THEN 1 - FMInd ELSE FMInd END \
,FMTCase \
,varchar(replace(substr(FMT || '    ',Pos,5),'.',''),5) \
 From \
  (Select Pos ,RetStr ,CatStr ,FMInd, PreFMTCase, PreFMTChar \
  ,CASE \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'FM'     THEN 0 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'AD'     THEN 1 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'A.D.'   THEN 2 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'AM'     THEN 3 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'A.M.'   THEN 4 \
--     WHEN upper(substr(FMT || '    ',Pos,2)) = 'BC'     THEN 5 \
--     WHEN upper(substr(FMT || '    ',Pos,4)) = 'B.C.'   THEN 6 \
     WHEN upper(substr(FMT || '    ',Pos,3)) = 'SCC'    THEN 7 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'CC'     THEN 8 \
     WHEN upper(substr(FMT || '    ',Pos,3)) = 'DDD'    THEN 9 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'DD'     THEN 10 \
     WHEN upper(substr(FMT || '    ',Pos,3)) = 'DAY'    THEN 11 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'DY'     THEN 12 \
     WHEN upper(substr(FMT          ,Pos,1)) = 'D'      THEN 13 \
--     WHEN upper(substr(FMT || '    ',Pos,2)) = 'EE'     THEN 14 \
--     WHEN upper(substr(FMT          ,Pos,1)) = 'E'      THEN 15 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'HH24'   THEN 16 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'HH12'   THEN 17 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'HH'     THEN 18 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'IW'     THEN 19 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'IYYY'   THEN 20 \
     WHEN upper(substr(FMT || '    ',Pos,3)) = 'IYY'    THEN 21 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'IY'     THEN 22 \
     WHEN upper(substr(FMT          ,Pos,1)) = 'I'      THEN 23 \
     WHEN upper(substr(FMT          ,Pos,1)) = 'J'      THEN 24 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'MI'     THEN 25 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'MM'     THEN 26 \
     WHEN upper(substr(FMT || '    ',Pos,5)) = 'MONTH'  THEN 27 \
     WHEN upper(substr(FMT || '    ',Pos,3)) = 'MON'    THEN 28 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'PM'     THEN 29 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'P.M.'   THEN 30 \
     WHEN upper(substr(FMT          ,Pos,1)) = 'Q'      THEN 31 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'RM'     THEN 32 \
--     WHEN upper(substr(FMT || '    ',Pos,4)) = 'RRRR'   THEN 33 \
--     WHEN upper(substr(FMT || '    ',Pos,2)) = 'RR'     THEN 34 \
     WHEN upper(substr(FMT || '    ',Pos,5)) = 'SSSSS'  THEN 35 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'SS'     THEN 36 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'WW'     THEN 37 \
     WHEN upper(substr(FMT          ,Pos,1)) = 'W'      THEN 38 \
     WHEN upper(substr(FMT || '    ',Pos,5)) = 'Y,YYY'  THEN 39 \
     WHEN upper(substr(FMT || '    ',Pos,5)) = 'SYEAR'  THEN 40 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'YEAR'   THEN 41 \
     WHEN upper(substr(FMT || '    ',Pos,5)) = 'SYYYY'  THEN 42 \
     WHEN upper(substr(FMT || '    ',Pos,4)) = 'YYYY'   THEN 43 \
     WHEN upper(substr(FMT || '    ',Pos,3)) = 'YYY'    THEN 44 \
     WHEN upper(substr(FMT || '    ',Pos,2)) = 'YY'     THEN 45 \
     WHEN upper(substr(FMT          ,Pos,1)) = 'Y'      THEN 46 \
     WHEN upper(substr(FMT || ' '   ,Pos,2)) = 'SP'     THEN 96 \
     WHEN upper(substr(FMT || ' '   ,Pos,2)) = 'TH'     THEN 97 \
     WHEN upper(substr(FMT          ,Pos,1)) = ''''     THEN 98 \
     ELSE                                                    99 \
   END \
   From  Repeat \
   Where Pos <= length(FMT) \
  ) AS Q (Pos, RetStr, CatStr, FMInd, PreFMTCase, PreFMTChar, FMTCase) \
) \
SELECT RetStr || CatStr \
FROM  Repeat \
WHERE Pos = (Select max(Pos) From Repeat) \

