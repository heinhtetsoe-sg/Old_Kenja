-- kanji=´Á»ú
-- $Id: 2063f50ab6b4c1c07ec7f7109c27da6abe9bb378 $

drop table RECORD_SCORE_DAT_BAK

create table RECORD_SCORE_DAT_BAK \
      (YEAR           varchar(4) not null, \
       SEMESTER       varchar(1) not null, \
       TESTKINDCD     varchar(2) not null, \
       TESTITEMCD     varchar(2) not null, \
       SCORE_DIV      varchar(2) not null, \
       SUBCLASSCD     varchar(6) not null, \
       SCHREGNO       varchar(8) not null, \
       CHAIRCD        varchar(7), \
       SCORE          smallint, \
       VALUE          smallint, \
       VALUE_DI       varchar(2), \
       GET_CREDIT     smallint, \
       ADD_CREDIT     smallint, \
       COMP_TAKESEMES varchar(1), \
       COMP_CREDIT    smallint, \
       REGISTERCD     varchar(8), \
       UPDATED        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

insert into RECORD_SCORE_DAT_BAK SELECT * FROM RECORD_SCORE_DAT

DELETE FROM RECORD_SCORE_DAT \
WHERE YEAR='2010' AND SEMESTER='9' AND TESTKINDCD='99' AND TESTITEMCD='00' AND SCORE_DIV='00'

insert into RECORD_SCORE_DAT \
WITH T_RECORD_SCORE AS ( \
    SELECT \
        T3.YEAR, \
        T3.SEMESTER, \
        T3.TESTKINDCD, \
        T3.TESTITEMCD, \
        T3.SCORE_DIV, \
        T3.SUBCLASSCD, \
        T3.SCHREGNO, \
        T3.CHAIRCD, \
        T3.SCORE, \
        L1.ASSESSLEVEL AS VALUE, \
        T3.VALUE_DI, \
        case when 1 < L1.ASSESSLEVEL then T4.CREDITS \
             when 1 = L1.ASSESSLEVEL then 0 \
             else NULL end AS GET_CREDIT, \
        T3.ADD_CREDIT, \
        T3.COMP_TAKESEMES, \
        case when 1 <= L1.ASSESSLEVEL then T4.CREDITS \
             else NULL end AS COMP_CREDIT, \
        T3.REGISTERCD, \
        SYSDATE() AS UPDATED \
    FROM \
        SCHREG_REGD_DAT T1 \
        INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO \
        INNER JOIN RECORD_SCORE_DAT_BAK T3 ON T3.SCHREGNO = T1.SCHREGNO \
                                AND T3.YEAR = '2010' \
                                AND T3.SEMESTER = '9' \
                                AND T3.TESTKINDCD = '99' \
                                AND T3.TESTITEMCD = '00' \
                                AND T3.SCORE_DIV = '00' \
        LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '2010' \
                               AND T4.COURSECD = T1.COURSECD \
                               AND T4.MAJORCD = T1.MAJORCD \
                               AND T4.GRADE = T1.GRADE \
                               AND T4.COURSECODE = T1.COURSECODE \
                               AND T4.SUBCLASSCD = T3.SUBCLASSCD \
        LEFT JOIN ASSESS_MST L1 ON L1.ASSESSCD = '3' \
                               AND T3.SCORE BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH \
    WHERE \
        T1.YEAR = '2010' AND \
        T1.SEMESTER = '3' \
    ) \
, T_COMBINED AS ( \
    SELECT T2.SCHREGNO, T1.COMBINED_SUBCLASSCD, \
           COUNT(T2.SUBCLASSCD) AS MOTO_CNT, \
           SUM(case when T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL then 1 else 0 end) AS CREDIT_CNT, \
           SUM(T2.COMP_CREDIT) AS COMP_CREDIT, \
           SUM(T2.GET_CREDIT) AS GET_CREDIT, \
           SUM(case when 1 < T2.VALUE then T2.GET_CREDIT \
                    when 1 = T2.VALUE then T2.COMP_CREDIT end) AS GET_CREDIT_Y \
    FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, \
           T_RECORD_SCORE T2 \
    WHERE  T1.YEAR = '2010' \
      AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD \
    GROUP BY T2.SCHREGNO, T1.COMBINED_SUBCLASSCD \
    ) \
, T_COMBINED_FLG AS ( \
    SELECT T1.COMBINED_SUBCLASSCD, T1.CALCULATE_CREDIT_FLG AS FLG, COUNT(T1.ATTEND_SUBCLASSCD) AS CNT \
    FROM   SUBCLASS_REPLACE_COMBINED_DAT T1 \
    WHERE  T1.YEAR = '2010' \
    GROUP BY T1.COMBINED_SUBCLASSCD, T1.CALCULATE_CREDIT_FLG \
    ) \
SELECT \
    T3.YEAR, \
    T3.SEMESTER, \
    T3.TESTKINDCD, \
    T3.TESTITEMCD, \
    T3.SCORE_DIV, \
    T3.SUBCLASSCD, \
    T3.SCHREGNO, \
    T3.CHAIRCD, \
    T3.SCORE, \
    case when (D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2)) then NULL \
         else T3.VALUE end AS VALUE, \
    T3.VALUE_DI, \
    case when (D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2)) and T6.FLG = '1' and 0 < T5.CREDIT_CNT and T6.CNT = T5.MOTO_CNT then T4.CREDITS \
         when (D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2)) and T6.FLG = '2' and 0 < T5.CREDIT_CNT and 0 < T5.MOTO_CNT then T5.GET_CREDIT \
         when (D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2)) then NULL \
         when 1 < T3.VALUE and T6.FLG = '1' then T4.CREDITS \
         when 1 < T3.VALUE and T6.FLG = '2' and D015.NAMESPARE1 = 'Y' then T5.GET_CREDIT_Y \
         when 1 < T3.VALUE and T6.FLG = '2' then T5.GET_CREDIT \
         when 1 = T3.VALUE then 0 \
         else NULL end AS GET_CREDIT, \
    T3.ADD_CREDIT, \
    T3.COMP_TAKESEMES, \
    case when (D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2)) and T6.FLG = '1' and 0 < T5.CREDIT_CNT and T6.CNT = T5.MOTO_CNT then T4.CREDITS \
         when (D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2)) and T6.FLG = '2' and 0 < T5.CREDIT_CNT and 0 < T5.MOTO_CNT then T5.COMP_CREDIT \
         when (D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2)) then NULL \
         when 1 <= T3.VALUE and T6.FLG = '1' then T4.CREDITS \
         when 1 <= T3.VALUE and T6.FLG = '2' then T5.COMP_CREDIT \
         else NULL end AS COMP_CREDIT, \
    T3.REGISTERCD, \
    T3.UPDATED \
FROM \
    SCHREG_REGD_DAT T1 \
    INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO \
                                 AND T2.GRD_DATE IS NULL \
                                 AND T2.GRD_DIV IS NULL \
    INNER JOIN T_RECORD_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO \
    INNER JOIN T_COMBINED_FLG T6 ON T6.COMBINED_SUBCLASSCD = T3.SUBCLASSCD \
    LEFT JOIN T_COMBINED T5 ON T5.SCHREGNO = T3.SCHREGNO \
                           AND T5.COMBINED_SUBCLASSCD = T3.SUBCLASSCD \
    LEFT JOIN V_NAME_MST D015 ON D015.YEAR = T1.YEAR \
                             AND D015.NAMECD1 = 'D015' \
                             AND D015.NAMECD2 = '01' \
    LEFT JOIN V_NAME_MST D008 ON D008.YEAR = T1.YEAR \
                             AND D008.NAMECD1 = 'D008' \
                             AND D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) \
    LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '2010' \
                           AND T4.COURSECD = T1.COURSECD \
                           AND T4.MAJORCD = T1.MAJORCD \
                           AND T4.GRADE = T1.GRADE \
                           AND T4.COURSECODE = T1.COURSECODE \
                           AND T4.SUBCLASSCD = T3.SUBCLASSCD \
WHERE \
    T1.YEAR = '2010' AND \
    T1.SEMESTER = '3' \
UNION ALL \
SELECT \
    T3.YEAR, \
    T3.SEMESTER, \
    T3.TESTKINDCD, \
    T3.TESTITEMCD, \
    T3.SCORE_DIV, \
    T3.SUBCLASSCD, \
    T3.SCHREGNO, \
    T3.CHAIRCD, \
    T3.SCORE, \
    T3.VALUE, \
    T3.VALUE_DI, \
    T3.GET_CREDIT, \
    T3.ADD_CREDIT, \
    T3.COMP_TAKESEMES, \
    T3.COMP_CREDIT, \
    T3.REGISTERCD, \
    T3.UPDATED \
FROM \
    T_RECORD_SCORE T3 \
WHERE \
    T3.SUBCLASSCD NOT IN (SELECT T6.COMBINED_SUBCLASSCD FROM T_COMBINED_FLG T6)
