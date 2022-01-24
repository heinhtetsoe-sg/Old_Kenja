-- $Id: 458bf459f54effce03121ed9b55ffedbfc8a7660 $

drop table PUBLIC_HOLIDAY_MST_OLD
rename table PUBLIC_HOLIDAY_MST TO PUBLIC_HOLIDAY_MST_OLD
create table PUBLIC_HOLIDAY_MST( \
    YEAR                varchar(4)  not null, \
    SEQ                 smallint    not null, \
    HOLIDAY_DIV         varchar(1)  not null, \
    HOLIDAY_MONTH       varchar(2)  not null, \
    HOLIDAY_DAY         varchar(2),  \
    HOLIDAY_WEEK_PERIOD varchar(1),  \
    HOLIDAY_WEEKDAY     varchar(1),  \
    HOLIDAY_NAME        varchar(75), \
    HOLIDAY_KIND        varchar(1),  \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

insert into PUBLIC_HOLIDAY_MST \
    SELECT \
        YEAR, \
        SEQ, \
        HOLIDAY_DIV, \
        HOLIDAY_MONTH , \
        HOLIDAY_DAY , \
        HOLIDAY_WEEK_PERIOD , \
        HOLIDAY_WEEKDAY , \
        HOLIDAY_NAME , \
        '1' AS HOLIDAY_KIND, \
        REGISTERCD, \
        UPDATED \
    FROM \
        PUBLIC_HOLIDAY_MST_OLD

alter table PUBLIC_HOLIDAY_MST add constraint PK_PU_HOLIDAY_MST primary key (YEAR, SEQ)
