-- kanji=����
-- $Id: 84c448038f5e91f781d692e64312ff5da43d40d0 $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table STATION_YDAT

create table STATION_YDAT ( \
    YEAR        varchar(4) not null, \
    STATIONCD   varchar(7) not null, \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp, \
    primary key ( YEAR,STATIONCD ) \
) in usr1dms index in idx1dms

