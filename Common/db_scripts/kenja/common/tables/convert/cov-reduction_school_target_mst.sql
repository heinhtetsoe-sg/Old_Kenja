-- kanji=漢字
-- $Id: 486e8fc392a1b5f430a436ccfaf469b36d4d5340 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
rename table REDUCTION_SCHOOL_LM_MST to REDUCTION_SCHOOL_LM_MST_OLD

drop table REDUCTION_SCHOOL_TARGET_MST

create table REDUCTION_SCHOOL_TARGET_MST( \
    SCHOOLCD            varchar(12)   not null, \
    SCHOOL_KIND         varchar(2)    not null, \
    YEAR                varchar(4)    not null, \
    REDUCTION_DIV_CD    varchar(2)    not null, \
    REDUCTION_TARGET    varchar(1)    not null, \
    MONEY_DIV           varchar(1), \
    NUMERATOR           smallint, \
    DENOMINATOR         smallint, \
    MONEY               integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SCHOOL_TARGET_MST add constraint PK_REDUC_SCHL_TARG primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_DIV_CD, REDUCTION_TARGET)


insert into REDUCTION_SCHOOL_TARGET_MST \
WITH MAX_LM AS ( \
SELECT \
    LM.SCHOOLCD, \
    LM.SCHOOL_KIND, \
    LM.YEAR, \
    LM.REDUCTION_DIV_CD, \
    CASE WHEN MM.GAKUNOKIN_DIV = '2' \
         THEN '2' \
         ELSE '1' \
    END AS REDUCTION_TARGET, \
    MIN(LM.COLLECT_L_CD || LM.COLLECT_M_CD) AS LM_CD \
FROM \
    REDUCTION_SCHOOL_LM_MST_OLD LM \
    INNER JOIN COLLECT_M_MST MM ON LM.SCHOOLCD = MM.SCHOOLCD \
          AND LM.SCHOOL_KIND = MM.SCHOOL_KIND \
          AND LM.YEAR = MM.YEAR \
          AND LM.COLLECT_L_CD || LM.COLLECT_M_CD = MM.COLLECT_L_CD || MM.COLLECT_M_CD \
GROUP BY \
    LM.SCHOOLCD, \
    LM.SCHOOL_KIND, \
    LM.YEAR, \
    LM.REDUCTION_DIV_CD, \
    CASE WHEN MM.GAKUNOKIN_DIV = '2' \
         THEN '2' \
         ELSE '1' \
    END \
) \
SELECT DISTINCT \
    LM.SCHOOLCD, \
    LM.SCHOOL_KIND, \
    LM.YEAR, \
    LM.REDUCTION_DIV_CD, \
    LM.REDUCTION_TARGET, \
    LM_OLD.MONEY_DIV, \
    LM_OLD.NUMERATOR, \
    LM_OLD.DENOMINATOR, \
    LM_OLD.MONEY, \
    'alpoki', \
    sysdate() \
FROM \
    MAX_LM LM \
    INNER JOIN REDUCTION_SCHOOL_LM_MST_OLD LM_OLD ON LM.SCHOOLCD = LM_OLD.SCHOOLCD \
          AND LM.SCHOOL_KIND = LM_OLD.SCHOOL_KIND \
          AND LM.YEAR = LM_OLD.YEAR \
          AND LM.REDUCTION_DIV_CD = LM_OLD.REDUCTION_DIV_CD \
          AND LM_OLD.COLLECT_L_CD || LM_OLD.COLLECT_M_CD = LM.LM_CD
