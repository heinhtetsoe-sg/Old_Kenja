-- kanji=漢字
-- $Id: 455e77aa2508697ccd7bd6f41741232cbb1291cb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計細目マスタ
DROP TABLE LEVY_S_MST \

CREATE TABLE LEVY_S_MST \
( \
        "SCHOOLCD"            varchar(12) not null, \
        "SCHOOL_KIND"         varchar(2)  not null, \
        "YEAR"                varchar(4)  not null, \
        "LEVY_L_CD"           varchar(2) not null, \
        "LEVY_M_CD"           varchar(2) not null, \
        "LEVY_S_CD"           varchar(3) not null, \
        "LEVY_S_NAME"         varchar(90), \
        "LEVY_S_ABBV"         varchar(90), \
        "REPAY_DIV"           varchar(1), \
        "BENEFIT"             varchar(1), \
        "REMARK"              varchar(60), \
        "REGISTERCD"          varchar(10), \
        "UPDATED"             timestamp default current timestamp, \
        "MAX_BENEFIT"         integer \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_S_MST \
ADD CONSTRAINT PK_LEVY_S_MST \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, LEVY_L_CD, LEVY_M_CD, LEVY_S_CD)
