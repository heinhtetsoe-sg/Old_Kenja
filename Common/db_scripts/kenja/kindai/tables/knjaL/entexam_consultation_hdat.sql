-- kanji=´Á»ú
-- $Id: entexam_consultation_hdat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_consultation_hdat

CREATE TABLE entexam_consultation_hdat \
( \
    entexamyear     varchar(4)  not null, \
    testdiv         varchar(1)  not null, \
    acceptno        varchar(4)  not null, \
    create_date     date        not null, \
    name            varchar(63), \
    name_kana       varchar(243), \
    sex             varchar(1), \
    ps_updated      date, \
    ps_acceptno     varchar(4), \
    ps_cd           varchar(7), \
    ps_item1        smallint, \
    ps_item2        decimal(4,1), \
    ps_item3        decimal(4,1), \
    ps_item4        decimal(4,1), \
    ps_item5        decimal(4,1), \
    fs_updated      date, \
    fs_acceptno     varchar(4), \
    fs_cd           varchar(7), \
    fs_item1        smallint, \
    fs_item2        decimal(4,1), \
    fs_item3        decimal(4,1), \
    fs_item4        decimal(4,1), \
    fs_item5        decimal(4,1), \
    registercd      varchar(8),  \
    updated         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_consultation_hdat add constraint \
pk_entexam_consh primary key (entexamyear,testdiv,acceptno,create_date)
