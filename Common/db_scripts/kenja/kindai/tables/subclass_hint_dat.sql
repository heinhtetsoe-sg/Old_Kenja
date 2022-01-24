-- $Id: subclass_hint_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
-- �����R�}����(KNJB0090)�ŎQ�Ƃ���e�[�u��
-- �쐬��: 2005/01/29 19:39:00 - JST
-- �쐬��: tamura

-- �X�N���v�g�̎g�p���@: db2 +c -f <thisfile>

--/* ���l
-- *   �E�J���� HINTDIV �̒l�ƈӖ�
-- *       0 : �����R�}������s�Ȃ�Ȃ��Ȗ�(�Z���R�[�h�͎g��Ȃ�)
-- *       1 : �Z���Ɂu�Ɛ�v�ŃR�}���ꂷ��ȖځB
-- *       2 : �Z���Ɂu�D��v�ŃR�}���ꂷ��ȖځB
-- */

drop   table SUBCLASS_HINT_DAT

create table SUBCLASS_HINT_DAT ( \
    YEAR        varchar(4) not null, \
    SUBCLASSCD  varchar(6) not null, \
    HINTDIV     smallint not null check (HINTDIV in (0, 1, 2)), \
    PERIODCD    varchar(1) not null, \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp, \
    primary key ( YEAR, SUBCLASSCD, HINTDIV, PERIODCD ) \
) in usr1dms index in idx1dms

