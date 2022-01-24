-- kanji=漢字
-- $Id: rep-entexam_wishdiv_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table ENTEXAM_WISHDIV_MST_OLD
create table ENTEXAM_WISHDIV_MST_OLD like ENTEXAM_WISHDIV_MST
insert into ENTEXAM_WISHDIV_MST_OLD select * from ENTEXAM_WISHDIV_MST

drop table ENTEXAM_WISHDIV_MST

create table ENTEXAM_WISHDIV_MST \
( \
    ENTEXAMYEAR     varchar(4)  not null, \
    TESTDIV         varchar(1)  not null, \
    DESIREDIV       varchar(2)  not null, \
    WISHNO          varchar(1)  not null, \
    COURSECD        varchar(1), \
    MAJORCD         varchar(3), \
    EXAMCOURSECD    varchar(4), \
    REGISTERCD      varchar(8),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_WISHDIV_MST add constraint \
pk_entexam_wish primary key (entexamyear,testdiv,desirediv,wishno)

insert into ENTEXAM_WISHDIV_MST \
( \
    select \
        ENTEXAMYEAR, \
        TESTDIV, \
        '0' || DESIREDIV, \
        WISHNO, \
        COURSECD, \
        MAJORCD, \
        EXAMCOURSECD, \
        REGISTERCD, \
        UPDATED \
    from \
        ENTEXAM_WISHDIV_MST_OLD \
)
