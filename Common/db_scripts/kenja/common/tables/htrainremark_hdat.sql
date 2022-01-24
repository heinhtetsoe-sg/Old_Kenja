-- kanji=����
-- $Id: 44d2f453d0acb6125e1100a89a78952c4744b05f $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table HTRAINREMARK_HDAT

create table HTRAINREMARK_HDAT \
    (SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(534), \
     TOTALSTUDYVAL        varchar(802), \
     TOTALSTUDYACT2       varchar(534), \
     TOTALSTUDYVAL2       varchar(802), \
     CREDITREMARK         varchar(802), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HTRAINREMARK_HDAT \
add constraint PK_HTRAINREMARK_H \
primary key \
(SCHREGNO)
