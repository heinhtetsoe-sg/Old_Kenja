-- kanji=漢字
-- $Id: 982386fd05e9df67bf236f54f3cc3f295da9abc7 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table NURSEOFF_ATTEND_CAMPUS_DAT

create table NURSEOFF_ATTEND_CAMPUS_DAT( \
    SCHOOLCD            varchar(12)         not null, \
    SCHOOL_KIND         varchar(2)          not null, \
    CAMPUS_DIV          varchar(2)          not null, \
    DATE                date                not null, \
    GRADE               varchar(2)          not null, \
    DI_CD               varchar(2)          not null, \
    CNT                 smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table NURSEOFF_ATTEND_CAMPUS_DAT add constraint pk_nurse_att_cam primary key \
        (SCHOOLCD,SCHOOL_KIND,CAMPUS_DIV,DATE,GRADE,DI_CD)
