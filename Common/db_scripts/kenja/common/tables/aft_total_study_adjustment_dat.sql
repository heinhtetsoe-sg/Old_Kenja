-- $Id: 578c511ec1e4d1b73167909bc5ba6cd1bfc8e88e $

drop table AFT_TOTAL_STUDY_ADJUSTMENT_DAT

create table AFT_TOTAL_STUDY_ADJUSTMENT_DAT( \
    YEAR        varchar(4)      not null , \ 
    SCHREGNO    varchar(8)      not null , \ 
    GRADE       varchar(2)      not null , \ 
    HR_CLASS    varchar(3)      not null , \ 
    ATTENDNO    varchar(3)      not null , \ 
    SCORE       integer                  , \ 
    REGISTERCD  varchar(10)              , \ 
    UPDATED     timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table AFT_TOTAL_STUDY_ADJUSTMENT_DAT add constraint PK_AFT_TOTAL_STUDY_ADJUSTMENT_DAT primary key (YEAR, SCHREGNO)
