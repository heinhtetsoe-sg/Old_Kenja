
drop table HTRAINREMARK_SCORE_DAT
create table HTRAINREMARK_SCORE_DAT( \
    YEAR             varchar(4) not null, \
    SCHREGNO         varchar(8) not null, \
    DATA_DIV         varchar(2) not null, \
    SCORE            smallint, \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HTRAINREMARK_SCORE_DAT add constraint PK_HTRAINREMARK_SCORE_DAT primary key (YEAR, SCHREGNO, DATA_DIV)