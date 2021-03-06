-- $Id: 945e1814a3e750611fb1b1d3515e27ced80c9ee7 $

DROP TABLE GRD_HTRAINREMARK_DAT
CREATE TABLE GRD_HTRAINREMARK_DAT( \
    YEAR             VARCHAR(4)    NOT NULL, \
    SCHREGNO         VARCHAR(8)    NOT NULL, \
    ANNUAL           VARCHAR(2)    NOT NULL, \
    TOTALSTUDYACT    VARCHAR(980), \
    TOTALSTUDYVAL    VARCHAR(980), \
    SPECIALACTREMARK VARCHAR(1000), \
    TOTALREMARK      VARCHAR(3500), \
    ATTENDREC_REMARK VARCHAR(400), \
    VIEWREMARK       VARCHAR(300), \
    BEHAVEREC_REMARK VARCHAR(122), \
    CLASSACT         VARCHAR(300), \
    STUDENTACT       VARCHAR(218), \
    CLUBACT          VARCHAR(225), \
    SCHOOLEVENT      VARCHAR(218), \
    REGISTERCD       VARCHAR(10), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR16DMS INDEX IN IDX1DMS

ALTER TABLE GRD_HTRAINREMARK_DAT ADD CONSTRAINT PK_HTRAINR_D PRIMARY KEY (YEAR,SCHREGNO)
