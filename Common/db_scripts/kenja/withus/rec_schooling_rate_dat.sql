-- kanji=漢字
-- $Id: rec_schooling_rate_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table REC_SCHOOLING_RATE_DAT

create table REC_SCHOOLING_RATE_DAT \
(  \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8) not null, \
    SCHOOLING_TYPE  varchar(2) not null, \
    RATE            smallint, \
    COMMITED_S      date, \
    COMMITED_E      date, \
    GRAD_REGURATE   varchar(1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_SCHOOLING_RATE_DAT  \
add constraint PK_SCHOOLING_RAT_D \
primary key  \
(YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCHOOLING_TYPE)
