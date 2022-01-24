-- $Id: 0fa56fdfad9b8c3f0f6cc7729830fcef8c256be8 $

drop   table IBCLASS_YDAT
create table IBCLASS_YDAT ( \
    IBYEAR            VARCHAR(4) NOT NULL, \
    IBCLASSCD         VARCHAR(2) NOT NULL, \
    IBPRG_COURSE      VARCHAR(2) NOT NULL, \
    REGISTERCD        VARCHAR(8), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table IBCLASS_YDAT add constraint PK_IBCLASS_YDAT primary key (IBYEAR, IBCLASSCD, IBPRG_COURSE)

