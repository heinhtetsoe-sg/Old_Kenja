-- kanji=漢字
-- $Id: staff_class_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table STAFF_CLASS_HIST_DAT

create table STAFF_CLASS_HIST_DAT \
(  \
    YEAR            varchar(4) not null, \
    SEMESTER        varchar(1) not null, \
    GRADE           varchar(3) not null, \
    HR_CLASS        varchar(3) not null, \
    TR_DIV          varchar(1) not null, \
    FROM_DATE       date not null, \
    TO_DATE         date, \
    STAFFCD         varchar(8), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STAFF_CLASS_HIST_DAT add constraint PK_STF_CLASS_HIST \
primary key (YEAR, SEMESTER, GRADE, HR_CLASS, TR_DIV, FROM_DATE)
