-- $Id: a6d3ae5c7db22dd1f088613595e38ddebf8b0383 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ENTEXAM_INTERVIEW_HALL_DAT_OLD
create table ENTEXAM_INTERVIEW_HALL_DAT_OLD like ENTEXAM_INTERVIEW_HALL_DAT
insert into ENTEXAM_INTERVIEW_HALL_DAT_OLD select * from ENTEXAM_INTERVIEW_HALL_DAT

drop table ENTEXAM_INTERVIEW_HALL_DAT
create table ENTEXAM_INTERVIEW_HALL_DAT( \
    ENTEXAMYEAR               varchar(4)    not null, \
    APPLICANTDIV              varchar(1)    not null, \
    TESTDIV                   varchar(1)    not null, \
    EXAMNO                    varchar(5)    not null, \
    TEST_ROOM                 varchar(2), \
    INTERVIEW_SETTIME         varchar(2), \
    INTERVIEW_ENDTIME         varchar(2), \
    INTERVIEW_WAITINGROOM     varchar(2), \
    INTERVIEW_ROOM            varchar(2), \
    INTERVIEW_GROUP           varchar(2), \
    REGISTERCD                varchar(10), \
    UPDATED                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_INTERVIEW_HALL_DAT add constraint PK_INTERVIEW_HALL primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAMNO)

insert into ENTEXAM_INTERVIEW_HALL_DAT \
select \
    ENTEXAMYEAR, \
    APPLICANTDIV, \
    TESTDIV, \
    EXAMNO, \
    TEST_ROOM, \
    INTERVIEW_SETTIME, \
    cast(null as varchar(2)) AS INTERVIEW_ENDTIME, \
    INTERVIEW_WAITINGROOM, \
    INTERVIEW_ROOM, \
    INTERVIEW_GROUP, \
    REGISTERCD, \
    UPDATED \
from \
    ENTEXAM_INTERVIEW_HALL_DAT_OLD
