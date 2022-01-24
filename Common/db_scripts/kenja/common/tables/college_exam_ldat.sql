-- $Id: 5b2a6bfd5ca6ca182011559acacb58f7adf1b21b $

drop table COLLEGE_EXAM_LDAT
create table COLLEGE_EXAM_LDAT( \
    YEAR        varchar(4)  not null, \
    L_CD        varchar(2)  not null, \
    L_NAME      varchar(75) not null, \
    L_ABBV      varchar(75), \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_EXAM_LDAT add constraint PK_COLLEGE_EXAML primary key (YEAR, L_CD)
