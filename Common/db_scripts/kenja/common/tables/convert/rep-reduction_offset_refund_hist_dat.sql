-- $Id: 4cf18d92dba986ac480d95fe9c39ad39483eed4f $

drop table REDUCTION_OFFSET_REFUND_HIST_DAT_OLD
create table REDUCTION_OFFSET_REFUND_HIST_DAT_OLD like REDUCTION_OFFSET_REFUND_HIST_DAT
insert into  REDUCTION_OFFSET_REFUND_HIST_DAT_OLD select * from REDUCTION_OFFSET_REFUND_HIST_DAT

drop table REDUCTION_OFFSET_REFUND_HIST_DAT
create table REDUCTION_OFFSET_REFUND_HIST_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    PROCESS_DATE            date        not null, \
    REDUCTION_KIND          varchar(1)  not null, \
    OFFSET_REFUND_DIV       varchar(1)  not null, \
    SELECT_GRADE            varchar(2)  not null, \
    SELECT_COURSECD         varchar(1)  not null, \
    SELECT_MAJORCD          varchar(3)  not null, \
    SELECT_COURSECODE       varchar(4)  not null, \
    MONTH_FROM              varchar(2), \
    MONTH_TO                varchar(2), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_OFFSET_REFUND_HIST_DAT add constraint PK_RED_OF_RE_HI_D primary key(SCHOOLCD, SCHOOL_KIND, YEAR, PROCESS_DATE, REDUCTION_KIND, OFFSET_REFUND_DIV, SELECT_GRADE, SELECT_COURSECD, SELECT_MAJORCD, SELECT_COURSECODE)

insert into REDUCTION_OFFSET_REFUND_HIST_DAT \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    PROCESS_DATE, \
    REDUCTION_KIND, \
    OFFSET_REFUND_DIV, \
    '00' AS SELECT_GRADE, \
    '0' AS SELECT_COURSECD, \
    '000' AS SELECT_MAJORCD, \
    '0000' AS SELECT_COURSECODE, \
    MONTH_FROM, \
    MONTH_TO, \
    REGISTERCD, \
    UPDATED \
FROM REDUCTION_OFFSET_REFUND_HIST_DAT_OLD
