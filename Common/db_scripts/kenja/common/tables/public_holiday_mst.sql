-- $Id: 1db900bed2f2769fc26347314b015f03779ab352 $

drop table PUBLIC_HOLIDAY_MST

create table PUBLIC_HOLIDAY_MST \
    (YEAR                varchar(4)  not null, \
     SEQ                 smallint    not null, \
     HOLIDAY_DIV         varchar(1)  not null, \
     HOLIDAY_MONTH       varchar(2)  not null, \
     HOLIDAY_DAY         varchar(2), \
     HOLIDAY_WEEK_PERIOD varchar(1), \
     HOLIDAY_WEEKDAY     varchar(1), \
     HOLIDAY_NAME        varchar(75), \
     HOLIDAY_KIND        varchar(1),  \
     REGISTERCD          varchar(10), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PUBLIC_HOLIDAY_MST add constraint pk_pub_holiday_mst primary key (YEAR,SEQ)
