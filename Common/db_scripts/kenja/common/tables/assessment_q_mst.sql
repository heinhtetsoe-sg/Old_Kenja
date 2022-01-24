-- $Id: 323fe1a18da1a108c4f2f19148bc7097e769b0d7 $

drop table ASSESSMENT_Q_MST
create table ASSESSMENT_Q_MST( \
    YEAR        varchar(4)  not null, \
    ASSESS_DIV  varchar(2)  not null, \
    ASSESS_CD   varchar(2)  not null, \
    QUESTION    varchar(100), \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_Q_MST add constraint PK_ASSESSMENT_Q primary key (YEAR, ASSESS_DIV, ASSESS_CD)
