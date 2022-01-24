-- kanji=漢字
-- $Id: ecb8f36dc3797c3859edf7c4d39848baace9957b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table NURSEOFF_ATTEND_DAT

create table NURSEOFF_ATTEND_DAT( \
    SCHOOLCD            varchar(12)         not null, \
    SCHOOL_KIND         varchar(2)          not null, \
    DATE                date                not null, \
    GRADE               varchar(2)          not null, \
    DI_CD               varchar(2)          not null, \
    CNT                 smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table NURSEOFF_ATTEND_DAT add constraint pk_nurse_att_dat primary key \
        (SCHOOLCD,SCHOOL_KIND,DATE,GRADE,DI_CD)
