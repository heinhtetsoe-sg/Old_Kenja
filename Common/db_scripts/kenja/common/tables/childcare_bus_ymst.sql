-- kanji=����
-- $Id: 5494b7bb8d14ac7ec02f2595cf4d6e942555a1c5 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table CHILDCARE_BUS_YMST

create table CHILDCARE_BUS_YMST \
    (YEAR           varchar(4) not null, \
     COURSE_CD      varchar(2) not null, \
     SCHEDULE_CD    varchar(1) not null, \
     BUS_NAME       varchar(30), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CHILDCARE_BUS_YMST add constraint PK_CHILD_BUSYMST primary key (YEAR, COURSE_CD)


