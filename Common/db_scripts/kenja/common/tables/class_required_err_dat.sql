-- kanji=����
-- $Id: 1634bc04c0a4c5e77aa1e32660a6366045dd6519 $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table CLASS_REQUIRED_ERR_DAT

create table CLASS_REQUIRED_ERR_DAT( \
     SCHREGNO       VARCHAR(8) NOT NULL, \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table CLASS_REQUIRED_ERR_DAT add constraint PK_SUBREQUIRE primary key (SCHREGNO, CLASSCD, SCHOOL_KIND)
