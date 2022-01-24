-- kanji=漢字
-- $Id: fd151467efe14f6593e89a7bbb8d0bf1737791ed $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table ENTEXAM_DISTINCTION_MST_OLD
create table ENTEXAM_DISTINCTION_MST_OLD like ENTEXAM_DISTINCTION_MST
insert into ENTEXAM_DISTINCTION_MST_OLD select * from ENTEXAM_DISTINCTION_MST

drop table ENTEXAM_DISTINCTION_MST

create table ENTEXAM_DISTINCTION_MST( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    DISTINCT_ID     varchar(3)   not null, \
    DISTINCT_NAME   varchar(60)  not null, \
    TESTDIV         varchar(2)   not null, \
    EXAM_TYPE       varchar(2)   not null, \
    TEST_DATE       date         not null, \
    REGISTERCD      varchar(10),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_DISTINCTION_MST \
add constraint PK_ENT_DISTINCT_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, DISTINCT_ID)

insert into ENTEXAM_DISTINCTION_MST \
select \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        DISTINCT_ID, \
        DISTINCT_NAME, \
        '0' || TESTDIV, \
        EXAM_TYPE, \
        TEST_DATE, \
        REGISTERCD, \
        UPDATED \
from ENTEXAM_DISTINCTION_MST_OLD
