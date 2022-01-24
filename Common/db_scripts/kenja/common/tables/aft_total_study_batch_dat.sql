-- $Id: 7ba3bbbc460a17978f73e35919aa161ef680b1d3 $

drop table AFT_TOTAL_STUDY_BATCH_DAT

create table AFT_TOTAL_STUDY_BATCH_DAT( \
    YEAR                varchar(4) not null , \ 
    SCHREGNO            varchar(8) not null , \ 
    CLASS_SCORE         integer, \ 
    ABILITY_SCORE       integer, \ 
    TOEFL_SCORE         integer, \ 
    QUALIFIED_SCORE     integer, \ 
    ADJUSTMENT_SCORE    integer, \ 
    REGISTERCD          varchar(10), \ 
    UPDATED             timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table AFT_TOTAL_STUDY_BATCH_DAT add constraint PK_AFT_STUDY_BATCH primary key (YEAR, SCHREGNO)