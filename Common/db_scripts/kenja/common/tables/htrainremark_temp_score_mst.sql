
drop table HTRAINREMARK_TEMP_SCORE_MST
create table HTRAINREMARK_TEMP_SCORE_MST( \
    YEAR             varchar(4)    not null, \
    GRADE            varchar(2)    not null, \
    DATA_DIV         varchar(2)    not null, \
    PATTERN_CD       varchar(2)    not null, \
    REMARK           varchar(1500), \
    FROM_SCORE       smallint, \
    TO_SCORE         smallint, \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HTRAINREMARK_TEMP_SCORE_MST add constraint PK_HTRAINREMARK_TEMP_SCORE_MST primary key (YEAR,GRADE,DATA_DIV,PATTERN_CD)