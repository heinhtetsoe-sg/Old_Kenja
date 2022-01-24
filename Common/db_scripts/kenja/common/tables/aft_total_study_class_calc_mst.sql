-- $Id: 6a76a753ca628d8d7d8cebb4976e8c3095973851 $

drop table AFT_TOTAL_STUDY_CLASS_CALC_MST

create table AFT_TOTAL_STUDY_CLASS_CALC_MST( \
    YEAR                varchar(4)      not null , \ 
    CALC_PATTERN        varchar(2)      not null , \ 
    ROUND_DEC_PLACES    smallint        not null , \ 
    ROUND_PATTERN       varchar(1)      not null , \ 
    UPDATED     timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table AFT_TOTAL_STUDY_CLASS_CALC_MST add constraint PK_AFT_TOTAL_STUDY_CLASS_CALC_MST primary key (YEAR)
