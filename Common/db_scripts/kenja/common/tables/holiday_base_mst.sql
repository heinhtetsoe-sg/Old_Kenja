-- $Id: ce77dc7b8330d1397ebb34008ac87b50f2d354d1 $

drop table HOLIDAY_BASE_MST

create table HOLIDAY_BASE_MST \
    (SCHOOL_KIND                    varchar(2)  not null, \
     YEAR                           varchar(4)  not null, \
     LEGAL_HOLIDAY_FLG              varchar(1), \
     FIRST_SATURDAY_FLG             varchar(1), \
     SECOND_SATURDAY_FLG            varchar(1), \
     THIRD_SATURDAY_FLG             varchar(1), \
     FOUR_SATURDAY_FLG              varchar(1), \
     FIVE_SATURDAY_FLG              varchar(1), \
     BEFORE_SPRING_VACATION_FLG     varchar(1), \
     BEFORE_SPRING_VACATION_SDATE   date, \
     BEFORE_SPRING_VACATION_EDATE   date, \
     SUMMER_VACATION_FLG            varchar(1), \
     SUMMER_VACATION_SDATE          date, \
     SUMMER_VACATION_EDATE          date, \
     AUTUMN_VACATION_FLG            varchar(1), \
     AUTUMN_VACATION_SDATE          date, \
     AUTUMN_VACATION_EDATE          date, \
     WINTER_VACATION_FLG            varchar(1), \
     WINTER_VACATION_SDATE          date, \
     WINTER_VACATION_EDATE          date, \
     AFTER_SPRING_VACATION_FLG      varchar(1), \
     AFTER_SPRING_VACATION_SDATE    date, \
     AFTER_SPRING_VACATION_EDATE    date, \
     REGISTERCD                     varchar(8), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HOLIDAY_BASE_MST add constraint pk_holiday_bas_mst primary key (SCHOOL_KIND, YEAR)
