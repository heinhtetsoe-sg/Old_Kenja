-- kanji=漢字
-- $Id: 95ff4b7611f03655ae76cbaa2369acbe756030d6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計項目マスタ
DROP TABLE LEVY_M_MST \

CREATE TABLE LEVY_M_MST \
( \
        "SCHOOLCD"            varchar(12) not null, \
        "SCHOOL_KIND"         varchar(2)  not null, \
        "YEAR"                varchar(4)  not null, \
        "LEVY_L_CD"           varchar(2) not null, \
        "LEVY_M_CD"           varchar(2) not null, \
        "LEVY_IN_OUT_DIV"     varchar(1) not null, \
        "LEVY_M_NAME"         varchar(90), \
        "LEVY_M_ABBV"         varchar(90), \
        "LEVY_S_EXIST_FLG"    varchar(1), \
        "ZATU_FLG"            varchar(1), \
        "YOBI_FLG"            varchar(1), \
        "KURIKOSI_FLG"        varchar(1), \
        "KURIKOSI_ALL"        varchar(1), \
        "KURIKOSI_MONEY"      integer, \
        "REMARK"              varchar(60), \
        "REGISTERCD"          varchar(10), \
        "UPDATED"             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_M_MST \
ADD CONSTRAINT PK_LEVY_M_MST \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, LEVY_L_CD, LEVY_M_CD)
