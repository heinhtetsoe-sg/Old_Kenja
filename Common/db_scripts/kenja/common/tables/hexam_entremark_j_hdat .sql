-- kanji=����
-- $Id: 71c67ac927a0ee7b26dfd821bad26da10de9961a $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEXAM_ENTREMARK_J_HDAT

create table HEXAM_ENTREMARK_J_HDAT \
    (SCHREGNO           varchar(8) not null, \
     COMMENTEX_A_CD     varchar(1), \
     DISEASE            varchar(259), \
     DOC_REMARK         varchar(90), \
     TR_REMARK          varchar(159), \
     TOTALSTUDYACT      varchar(746), \
     TOTALSTUDYVAL      varchar(845), \
     BEHAVEREC_REMARK   varchar(845), \
     HEALTHREC          varchar(845), \
     SPECIALACTREC      varchar(845), \
     TRIN_REF           varchar(1248), \
     REMARK             varchar(1500), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEXAM_ENTREMARK_J_HDAT add constraint PK_HEX_ENTRMRK_J_H primary key (SCHREGNO)

 