-- kanji=漢字
-- $Id: 7a0470dafd4ea8c5ab615b58e24f3cac01628ffe $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計科目マスタ

DROP TABLE LEVY_GROUP_MST

CREATE TABLE LEVY_GROUP_MST ( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "LEVY_GROUP_CD"     varchar(4) not null, \
        "LEVY_GROUP_NAME"   varchar(60), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_GROUP_MST ADD CONSTRAINT PK_LEVY_GROUP_MST PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, LEVY_GROUP_CD)
