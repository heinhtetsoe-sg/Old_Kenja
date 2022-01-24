-- kanji=漢字
-- $Id: 24c599c6e05280eeabbea5c6f183001ef42841bc $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table MOCK_SUBCLASS_ASSESS_DAT

create table MOCK_SUBCLASS_ASSESS_DAT \
(  \
    YEAR                varchar (4) not null, \
    GRADE               varchar (2) not null, \
    MOCK_SUBCLASS_CD    varchar (6) not null, \
    SEQ                 smallint not null, \
    ASSESSMARK          varchar (9), \
    ASSESSLOW           decimal (4,1), \
    ASSESSHIGH          decimal (4,1), \
    REGISTERCD          varchar (8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_SUBCLASS_ASSESS_DAT add constraint PK_MOCK_ASSESS_DAT \
primary key (YEAR, GRADE, MOCK_SUBCLASS_CD, SEQ)
