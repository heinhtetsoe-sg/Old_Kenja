-- kanji=����
-- $Id: rateassess_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $
-- ɾ�껻���Ѵ��ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table RATEASSESS_MST

create table RATEASSESS_MST ( \
    YEAR               varchar(4) not null, \
    ASSESSLEVEL        smallint   not null, \
    RATE               smallint, \
    ASSESSLEVEL5       smallint, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp, \
    primary key ( YEAR,ASSESSLEVEL ) \
) in usr1dms index in idx1dms

