-- kanji=漢字
-- $Id: 5dea31b121a08ca1989fa4ad610d4cdf2da6ee2e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table SCHREG_ATTENDREC_DAT

create table SCHREG_ATTENDREC_DAT \
    (SCHOOLCD             varchar(1) not null, \
     YEAR                 varchar(4) not null, \
     SCHREGNO             varchar(8) not null, \
     ANNUAL               varchar(2) not null, \
     SUMDATE              date, \
     CLASSDAYS            SMALLINT, \
     OFFDAYS              SMALLINT, \
     ABSENT               SMALLINT, \
     SUSPEND              SMALLINT, \
     MOURNING             SMALLINT, \
     ABROAD               SMALLINT, \
     REQUIREPRESENT       SMALLINT, \
     SICK                 SMALLINT, \
     ACCIDENTNOTICE       SMALLINT, \
     NOACCIDENTNOTICE     SMALLINT, \
     PRESENT              SMALLINT, \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_ATTENDREC_DAT \
add constraint PK_SCHREGATTENDREC \
primary key \
(SCHOOLCD, YEAR, SCHREGNO)

