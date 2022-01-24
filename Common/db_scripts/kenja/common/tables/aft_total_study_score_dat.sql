-- $Id: fe1f73bef978625264449a3c10cc9a295e4f15eb $

drop table AFT_TOTAL_STUDY_SCORE_DAT

create table AFT_TOTAL_STUDY_SCORE_DAT( \
    YEAR        varchar(4)      not null , \ 
    SCHREGNO    varchar(8)      not null , \ 
    GRADE       varchar(2)      not null , \ 
    HR_CLASS    varchar(3)      not null , \ 
    ATTENDNO    varchar(3)      not null , \ 
    SCORE       integer                  , \ 
    REGISTERCD  varchar(10)              , \ 
    UPDATED     timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table AFT_TOTAL_STUDY_SCORE_DAT add constraint PK_AFT_TOTAL_STUDY_SCORE_DAT primary key (YEAR, SCHREGNO)
