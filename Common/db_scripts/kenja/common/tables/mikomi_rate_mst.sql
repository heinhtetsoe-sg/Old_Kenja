-- kanji=漢字
-- $Id: 5273d3f33eaa5427e8c7ad9809a8a96b20e4a54b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MIKOMI_RATE_MST

create table MIKOMI_RATE_MST ( \
    YEAR                    varchar(4) not null, \
    SCHOOL_KIND             varchar(2) not null, \
    GRADE                   varchar(2) not null, \
    MIKOMI_RATE             smallint , \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MIKOMI_RATE_MST add constraint PK_MIKOMI_RATE_MST \
        primary key (YEAR, SCHOOL_KIND, GRADE)
