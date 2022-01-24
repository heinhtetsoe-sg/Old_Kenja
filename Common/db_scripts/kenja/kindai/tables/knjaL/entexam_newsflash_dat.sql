-- kanji=´Á»ú
-- $Id: entexam_newsflash_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_newsflash_dat

create table entexam_newsflash_dat \
( \
    entexamyear         varchar(4)  not null, \
    testdiv             varchar(1)  not null, \
    examno              varchar(4)  not null, \
    recept_date         date, \
    shdiv               varchar(1), \
    desirediv           varchar(2), \
    sex                 varchar(1), \
    natpubpridiv        varchar(1), \
    registercd          varchar(8),  \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_newsflash_dat add constraint \
pk_entexam_newsf primary key (entexamyear,testdiv,examno)
