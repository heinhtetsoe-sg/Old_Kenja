-- $Id: 6ba6cb0b81693286628762ccfdb2c1360c17fd0f $

DROP TABLE HTRAINREMARK_P_DAT
CREATE TABLE HTRAINREMARK_P_DAT( \
    YEAR             VARCHAR(4)    NOT NULL, \
    SCHREGNO         VARCHAR(8)    NOT NULL, \
    ANNUAL           VARCHAR(2)    NOT NULL, \
    TOTALSTUDYACT    VARCHAR(678), \
    TOTALSTUDYVAL    VARCHAR(678), \
    SPECIALACTREMARK VARCHAR(678), \
    TOTALREMARK      VARCHAR(1598), \
    ATTENDREC_REMARK VARCHAR(242), \
    VIEWREMARK       VARCHAR(226), \
    BEHAVEREC_REMARK VARCHAR(122), \
    CLASSACT         VARCHAR(300), \
    STUDENTACT       VARCHAR(218), \
    CLUBACT          VARCHAR(225), \
    SCHOOLEVENT      VARCHAR(218), \
    FOREIGNLANGACT1  VARCHAR(500), \
    FOREIGNLANGACT2  VARCHAR(400), \
    FOREIGNLANGACT3  VARCHAR(400), \
    FOREIGNLANGACT4  VARCHAR(400), \
    REGISTERCD       VARCHAR(10), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HTRAINREMARK_P_DAT ADD CONSTRAINT PK_HTRAINREMARKP PRIMARY KEY (YEAR,SCHREGNO)