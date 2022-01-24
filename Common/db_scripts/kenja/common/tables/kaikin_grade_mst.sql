-- $Id: 4937fcecfed7c16140a0cb9278d54b1e2f12fa27 $

drop table KAIKIN_GRADE_MST

create table KAIKIN_GRADE_MST( \
    KAIKIN_CD               VARCHAR(2)      not null, \
    GRADE                   VARCHAR(2)      not null, \
    REMARK1                 VARCHAR(90)             , \
    REMARK2                 VARCHAR(90)             , \
    REMARK3                 VARCHAR(90)             , \
    REMARK4                 VARCHAR(90)             , \
    REMARK5                 VARCHAR(90)             , \
    REGISTERCD              VARCHAR(10)             , \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table KAIKIN_GRADE_MST add constraint PK_KAIKIN_GRADE_M primary key (KAIKIN_CD, GRADE)
