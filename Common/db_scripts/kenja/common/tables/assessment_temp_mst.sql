-- $Id: 4f00e1386072617dfd2c2615bf2efd8ad36b157a $

drop table ASSESSMENT_TEMP_MST
create table ASSESSMENT_TEMP_MST( \
    YEAR        varchar(4)  not null, \
    GRADE       varchar(2)  not null, \
    DATA_DIV    varchar(2)  not null, \
    REMARK      varchar(500), \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_TEMP_MST add constraint PK_ASSESSMENT_TMP primary key (YEAR, GRADE, DATA_DIV)
