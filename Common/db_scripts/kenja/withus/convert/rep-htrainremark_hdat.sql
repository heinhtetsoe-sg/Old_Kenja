-- kanji=����
-- $Id: rep-htrainremark_hdat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table HTRAINREMARK_HDAT_OLD
create table HTRAINREMARK_HDAT_OLD like HTRAINREMARK_HDAT
insert into  HTRAINREMARK_HDAT_OLD select * from HTRAINREMARK_HDAT

drop table HTRAINREMARK_HDAT

create table HTRAINREMARK_HDAT \
    (SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(1000), \
     TOTALSTUDYVAL        varchar(1000), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HTRAINREMARK_HDAT \
add constraint PK_HTRAINREMARK_H \
primary key \
(SCHREGNO)

insert into  HTRAINREMARK_HDAT \
(select \
    SCHREGNO, \
    TOTALSTUDYACT, \
    TOTALSTUDYVAL, \
    REGISTERCD, \
    UPDATED \
 from HTRAINREMARK_HDAT_OLD)

