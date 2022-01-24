-- kanji=漢字
-- $Id: schreg_base_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHREG_BASE_HIST_DAT

create table SCHREG_BASE_HIST_DAT \
(  \
    SCHREGNO          varchar(8) not null, \
    YEAR              varchar(4) not null, \
    SEMESTER          varchar(1) not null, \
    S_APPDATE         date not null, \
    E_APPDATE         date not null, \
    GRADE             varchar(3), \
    HR_CLASS          varchar(3), \
    COURSECD          varchar(1), \
    MAJORCD           varchar(3), \
    COURSECODE        varchar(4), \
    COURSE_DIV        varchar(1), \
    STUDENT_DIV       varchar(2), \
    GRD_SCHEDULE_DATE date, \
    NAME              varchar(60), \
    NAME_SHOW         varchar(30), \
    NAME_KANA         varchar(120), \
    NAME_KANA_SHOW    varchar(60), \
    REGISTERCD        varchar(8), \
    UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_BASE_HIST_DAT add constraint PK_SCHREG_HIST \
primary key (SCHREGNO, YEAR, SEMESTER, S_APPDATE, E_APPDATE)
