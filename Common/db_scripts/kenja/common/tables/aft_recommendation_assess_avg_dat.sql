-- kanji=����
-- $Id: ba9bc7e2d0276b22d0fc2643ae199909eb807283 $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table AFT_RECOMMENDATION_ASSESS_AVG_DAT

create table AFT_RECOMMENDATION_ASSESS_AVG_DAT ( \
    YEAR                   varchar(4) not null, \
    RECOMMENDATION_CD      varchar(4) not null, \
    SEQ                    smallint not null, \
    ASSESS_AVG             decimal(5, 2) not null, \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_ASSESS_AVG_DAT add constraint PK_AFT_RECOMMENDATION_ASSESS_AVG_DAT \
primary key (YEAR, RECOMMENDATION_CD, SEQ)
