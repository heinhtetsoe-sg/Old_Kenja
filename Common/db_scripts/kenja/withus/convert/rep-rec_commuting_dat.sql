-- kanji=漢字
-- $Id: rep-rec_commuting_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table REC_COMMUTING_DAT_OLD

create table REC_COMMUTING_DAT_OLD like REC_COMMUTING_DAT

insert into REC_COMMUTING_DAT_OLD select * from REC_COMMUTING_DAT

drop table REC_COMMUTING_DAT

create table REC_COMMUTING_DAT \
(  \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8) not null, \
    ATTEND_DATE     date not null, \
    PERIODCD        varchar(2) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_COMMUTING_DAT  \
add constraint PK_REC_COMMUTING_D \
primary key  \
(YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ATTEND_DATE, PERIODCD)

insert into REC_COMMUTING_DAT \
select \
    YEAR, \
    CLASSCD, \
    CURRICULUM_CD, \
    SUBCLASSCD, \
    SCHREGNO, \
    ATTEND_DATE, \
    PERIODCD, \
    REGISTERCD, \
    UPDATED \
FROM \
    REC_COMMUTING_DAT_OLD
