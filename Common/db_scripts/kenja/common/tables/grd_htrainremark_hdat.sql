-- kanji=����
-- $Id: fb0bc03a2e6717fd265b444e0f74f367ccbc8d74 $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table GRD_HTRAINREMARK_HDAT

create table HTRAINREMARK_HDAT ( \
     SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(534), \
     TOTALSTUDYVAL        varchar(802), \
     TOTALSTUDYACT2       varchar(534), \
     TOTALSTUDYVAL2       varchar(802), \
     CREDITREMARK         varchar(802), \
     REGISTERCD           varchar(10), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE GRD_HTRAINREMARK_HDAT ADD CONSTRAINT PK_HTRAINR_D PRIMARY KEY (SCHREGNO)