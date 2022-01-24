-- $Id: 58aa90177a7841106a0584707381a02dcb772cc5 $

drop table PRISCHOOL_CLASS_MST

create table PRISCHOOL_CLASS_MST \
    (PRISCHOOLCD             varchar(7) not null, \
     PRISCHOOL_CLASS_CD      varchar(7) not null, \
     PRISCHOOL_NAME          varchar(75),  \
     PRISCHOOL_KANA          varchar(75),  \
     PRINCNAME               varchar(60),  \
     PRINCNAME_SHOW          varchar(30),  \
     PRINCKANA               varchar(120), \
     DISTRICTCD              varchar(2),   \
     PRISCHOOL_ZIPCD         varchar(8),   \
     PRISCHOOL_ADDR1         varchar(150), \
     PRISCHOOL_ADDR2         varchar(150), \
     PRISCHOOL_TELNO         varchar(14),  \
     PRISCHOOL_FAXNO         varchar(14),  \
     ROSEN_1                 varchar(45),  \
     ROSEN_2                 varchar(45),  \
     ROSEN_3                 varchar(45),  \
     ROSEN_4                 varchar(45),  \
     ROSEN_5                 varchar(45),  \
     NEAREST_STATION_NAME1   varchar(75),  \
     NEAREST_STATION_KANA1   varchar(75),  \
     NEAREST_STATION_NAME2   varchar(75),  \
     NEAREST_STATION_KANA2   varchar(75),  \
     DIRECT_MAIL_FLG         varchar(1),   \
     REGISTERCD              varchar(10),  \
     UPDATED                 timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PRISCHOOL_CLASS_MST add constraint PK_PRISCHOOL_MST primary key (PRISCHOOLCD, PRISCHOOL_CLASS_CD)
