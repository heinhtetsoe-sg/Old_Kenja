-- kanji=漢字
-- $Id: another_school_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ANOTHER_SCHOOL_HIST_DAT

create table ANOTHER_SCHOOL_HIST_DAT \
(  \
        "APPLICANTNO"           varchar(7) not null, \
        "SEQ"                   smallint not null, \
        "STUDENT_DIV"           varchar(1), \
        "FORMER_REG_SCHOOLCD"   varchar(11), \
        "MAJOR_NAME"            varchar(120), \
        "REGD_S_DATE"           date, \
        "REGD_E_DATE"           date, \
        "PERIOD_MONTH_CNT"      varchar(2), \
        "ABSENCE_CNT"           varchar(2), \
        "MONTH_CNT"             varchar(2), \
        "ENT_FORM"              varchar(1), \
        "REASON"                varchar(150), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ANOTHER_SCHOOL_HIST_DAT  \
add constraint PK_ANOTHER_HIST \
primary key  \
(APPLICANTNO, SEQ)
