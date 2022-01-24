drop table ENTEXAM_SPORT_MST

create table ENTEXAM_SPORT_MST \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    SPORT_CD            varchar(5)  not null, \
    SPORT_NAME          varchar(60), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SPORT_MST add constraint PK_ENTEXAM_SPORT primary key (ENTEXAMYEAR, SPORT_CD)
