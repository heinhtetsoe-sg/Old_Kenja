-- kanji=����
-- $Id: 549bbd5b4d422aeb603fb53713f4bef531ed50fe $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_QUESTION_MST

create table HEALTH_QUESTION_MST \
        (QUESTIONCD             varchar(2)      not null, \
         CONTENTS               varchar(120), \
         SORT                   varchar(2), \
         REGISTERCD             varchar(10), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_QUESTION_MST add constraint pk_hea_que_mst primary key \
        (QUESTIONCD)
