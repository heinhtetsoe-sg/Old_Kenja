-- kanji=����
-- $Id: 3ca16e32bf8fd2e9dd9d87aedc4b06b04fe6a65f $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table HTRAINREMARK_P_HDAT

create table HTRAINREMARK_P_HDAT \
    (SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(534), \
     TOTALSTUDYVAL        varchar(802), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HTRAINREMARK_P_HDAT \
add constraint PK_HTRAINREMARKP_H \
primary key \
(SCHREGNO)
