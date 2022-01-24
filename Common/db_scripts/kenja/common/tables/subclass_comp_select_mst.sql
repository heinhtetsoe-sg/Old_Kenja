-- kanji=漢字
-- $Id: 36a8fcd07a1b9100a405c63af1c0573a23dd1772 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE SUBCLASS_COMP_SELECT_MST
CREATE TABLE SUBCLASS_COMP_SELECT_MST( \
    YEAR       varchar(4)    not null, \
    GRADE      varchar(2)    not null, \
    COURSECD   varchar(1)    not null, \
    MAJORCD    varchar(3)    not null, \
    COURSECODE varchar(4)    not null, \
    GROUPCD    varchar(3)    not null, \
    NAME       varchar(60), \
    ABBV       varchar(9), \
    CREDITS    smallint, \
    JOUGEN     smallint, \
    KAGEN      smallint, \
    REGISTERCD varchar(10), \
    UPDATED    timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUBCLASS_COMP_SELECT_MST ADD CONSTRAINT PK_SUBCLASS_CSM PRIMARY KEY (YEAR,GRADE, COURSECD, MAJORCD, COURSECODE,GROUPCD)