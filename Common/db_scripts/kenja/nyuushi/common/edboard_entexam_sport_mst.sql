-- $Id: 674222ed22744149bca15b8e8fd41a0b8e716f70 $

drop table EDBOARD_ENTEXAM_SPORT_MST
create table EDBOARD_ENTEXAM_SPORT_MST \
( \
    EDBOARD_SCHOOLCD    varchar(12)     not null, \
    ENTEXAMYEAR         varchar(4)      not null, \
    SPORT_CD            varchar(5)      not null, \
    SPORT_NAME          varchar(60), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table EDBOARD_ENTEXAM_SPORT_MST add constraint \
PK_ED_EEXAM_SPORT primary key (EDBOARD_SCHOOLCD, ENTEXAMYEAR, SPORT_CD)
