-- kanji=漢字
-- $Id: 5643f40716591f1aff16d8c2b0307dc54160b1c0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計科目マスタ

DROP TABLE LEVY_GROUP_DAT

CREATE TABLE LEVY_GROUP_DAT ( \
        "SCHOOLCD"      varchar(12) not null, \
        "SCHOOL_KIND"   varchar(2)  not null, \
        "YEAR"          varchar(4) not null, \
        "LEVY_GROUP_CD" varchar(4) not null, \
        "LEVY_L_CD"     varchar(2) not null, \
        "REGISTERCD"    varchar(10), \
        "UPDATED"       timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_GROUP_DAT ADD CONSTRAINT PK_LEVY_GROUP_DAT PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, YEAR, LEVY_GROUP_CD, LEVY_L_CD)
