-- kanji=漢字
-- $Id: rep-entexam_newsflash_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table ENTEXAM_NEWSFLASH_DAT_OLD
create table ENTEXAM_NEWSFLASH_DAT_OLD like ENTEXAM_NEWSFLASH_DAT
insert into ENTEXAM_NEWSFLASH_DAT_OLD select * from ENTEXAM_NEWSFLASH_DAT

drop table ENTEXAM_NEWSFLASH_DAT

create table ENTEXAM_NEWSFLASH_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    TESTDIV             varchar(1)  not null, \
    EXAMNO              varchar(4)  not null, \
    RECEPT_DATE         date, \
    SHDIV               varchar(1), \
    DESIREDIV           varchar(2), \
    SEX                 varchar(1), \
    NATPUBPRIDIV        varchar(1), \
    REGISTERCD          varchar(8),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_NEWSFLASH_DAT add constraint \
pk_entexam_newsf primary key (entexamyear,testdiv,examno)

insert into ENTEXAM_NEWSFLASH_DAT \
( \
    select \
        ENTEXAMYEAR, \
        TESTDIV, \
        EXAMNO, \
        RECEPT_DATE, \
        SHDIV, \
        '0' || DESIREDIV, \
        SEX, \
        NATPUBPRIDIV, \
        REGISTERCD, \
        UPDATED \
    from \
        ENTEXAM_NEWSFLASH_DAT_OLD \
)
