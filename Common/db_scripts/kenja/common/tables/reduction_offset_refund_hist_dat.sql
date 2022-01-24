-- $Id: a70dd78993286ad7ef716175b3796013318cf7d6 $

drop table REDUCTION_OFFSET_REFUND_HIST_DAT
create table REDUCTION_OFFSET_REFUND_HIST_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    PROCESS_DATE            date        not null, \
    REDUCTION_KIND          varchar(1)  not null, \
    OFFSET_REFUND_DIV       varchar(1)  not null, \
    MONTH_FROM              varchar(2), \
    MONTH_TO                varchar(2), \
    SELECT_GRADE            varchar(2), \
    SELECT_COURSECD         varchar(1), \
    SELECT_MAJORCD          varchar(3), \
    SELECT_COURSECODE       varchar(4), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_OFFSET_REFUND_HIST_DAT add constraint PK_RED_OF_RE_HI_D primary key(SCHOOLCD, SCHOOL_KIND, YEAR, PROCESS_DATE, REDUCTION_KIND, OFFSET_REFUND_DIV)
