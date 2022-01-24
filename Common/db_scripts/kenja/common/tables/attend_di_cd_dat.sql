-- kanji=漢字
-- $Id: 395ea9f83d5a6dfcf1928a44250f96d4f5f5bfe0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_DI_CD_DAT

create table ATTEND_DI_CD_DAT ( \
    YEAR             VARCHAR(4)   NOT NULL, \
    DI_CD            VARCHAR(3)   NOT NULL, \
    DI_NAME1         VARCHAR(60)  , \
    DI_NAME2         VARCHAR(60)  , \
    ATSUB_REPL_DI_CD VARCHAR(60)  , \
    DI_MARK          VARCHAR(30)  , \
    MULTIPLY         VARCHAR(30)  , \
    RESTRICT_FLG     VARCHAR(1)   , \
    ONEDAY_DI_CD     VARCHAR(3)   , \
    ORDER            VARCHAR(3)   , \
    PETITION_ORDER   VARCHAR(3)   , \
    REP_DI_CD        VARCHAR(3)   NOT NULL, \
    REMARK1          VARCHAR(30)  , \
    REMARK2          VARCHAR(30)  , \
    REMARK3          VARCHAR(30)  , \
    REGISTERCD       VARCHAR(10)  , \
    UPDATED          TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table ATTEND_DI_CD_DAT add constraint PK_ATDICD_D primary key (YEAR, DI_CD)

insert into ATTEND_DI_CD_DAT \
 select  \
     T2.YEAR, \
     T1.NAMECD2                  AS DI_CD, \
     T1.NAME1                    AS DI_NAME1, \
     T1.NAME2                    AS DI_NAME2, \
     T1.NAME3                    AS ATSUB_REPL_DI_CD, \
     T1.ABBV1                    AS DI_MARK, \
     T1.ABBV2                    AS MULTIPLY, \
     T1.ABBV3                    AS RESTRICT_FLG, \
     T1.NAMESPARE1               AS ONEDAY_DI_CD, \
     T1.NAMESPARE2               AS ORDER, \
     T1.NAMESPARE3               AS PETITION_ORDER, \
     T1.NAMECD2                  AS REP_DI_CD, \
     CAST(NULL AS VARCHAR(1))    AS REMARK1, \
     CAST(NULL AS VARCHAR(1))    AS REMARK2, \
     CAST(NULL AS VARCHAR(1))    AS REMARK3, \
     T1.REGISTERCD, \
     T1.UPDATED \
 FROM NAME_MST T1 \
 INNER JOIN NAME_YDAT T2 ON T2.NAMECD1 = T1.NAMECD1 \
 AND T2.NAMECD2 = T1.NAMECD2 \
WHERE \
 T1.NAMECD1 = 'C001'

