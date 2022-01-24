-- $Id: f402f25defda2ffd26cf302b7438e66d2cbe4585 $

drop table ENTEXAM_SLIDE_GROUP_YMST
create table ENTEXAM_SLIDE_GROUP_YMST( \
    ENTEXAMYEAR    varchar(4)   not null,  \
    APPLICANTDIV   varchar(1)   not null,  \
    DESIREDIV      varchar(2)   not null,  \
    TESTDIV1       varchar(2)   not null,  \
    SUC_DESIREDIV  varchar(2)   not null,  \
    SUC_TESTDIV1   varchar(2)   not null,  \
    REGISTERCD     varchar(10) ,  \
    UPDATED        timestamp    default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SLIDE_GROUP_YMST add constraint PK_ENTEXAM_SL_GR_M PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, DESIREDIV, TESTDIV1, SUC_DESIREDIV, SUC_TESTDIV1)

