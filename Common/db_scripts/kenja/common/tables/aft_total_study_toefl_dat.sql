-- $Id: 34e5a2582ad3d24aa1b311ded42d72c7b521f205 $

drop table AFT_TOTAL_STUDY_TOEFL_DAT

create table AFT_TOTAL_STUDY_TOEFL_DAT( \
    YEAR        varchar(4)      not null , \ 
    SCHREGNO    varchar(8)      not null , \ 
    TEST_DATE   date            not null , \ 
    GRADE       varchar(2)      not null , \ 
    HR_CLASS    varchar(3)      not null , \ 
    ATTENDNO    varchar(3)      not null , \ 
    SCORE       integer                  , \ 
    REGISTERCD  varchar(10)              , \ 
    UPDATED     timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table AFT_TOTAL_STUDY_TOEFL_DAT add constraint PK_AFT_TOTAL_STUDY_TOEFL_DAT primary key (YEAR, SCHREGNO, TEST_DATE)
