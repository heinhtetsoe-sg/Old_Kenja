-- $Id: 20c092ba0e9d6ada96393cb2d9d1ee9c3f488f1f $

drop table COLLEGE_EXAM_SDAT
create table COLLEGE_EXAM_SDAT( \
    YEAR        varchar(4)  not null, \
    L_CD        varchar(2)  not null, \
    S_CD        varchar(3)  not null, \
    S_NAME      varchar(75) not null, \
    S_ABBV      varchar(75), \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_EXAM_SDAT add constraint PK_COLLEGE_EXAMS primary key (YEAR, L_CD, S_CD)
