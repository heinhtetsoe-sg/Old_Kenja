-- $Id: 72f65ce8a4fd9654605abfbec4e9f0c221ac0a39 $

DROP TABLE NYUGAKU_LIST_DAT_OLD
RENAME TABLE NYUGAKU_LIST_DAT TO NYUGAKU_LIST_DAT_OLD
CREATE TABLE NYUGAKU_LIST_DAT( \
    ENTERYEAR           VARCHAR(4)    NOT NULL, \
    SCHREGNO            VARCHAR(8)    NOT NULL, \
    ENT_DIV             VARCHAR(1), \
    HR_CLASS            VARCHAR(3), \
    ATTENDNO            VARCHAR(3), \
    INOUTCD             VARCHAR(1), \
    COURSECD            VARCHAR(1), \
    MAJORCD             VARCHAR(3), \
    COURSECODE          VARCHAR(4), \
    TEMP1               VARCHAR(30), \
    TEMP2               VARCHAR(30), \
    TEMP_STUDENT_ID     VARCHAR(30), \
    STUDENT_SEI         VARCHAR(30), \
    STUDENT_MEI         VARCHAR(30), \
    TEMP6               VARCHAR(60), \
    TEMP7               VARCHAR(60), \
    STUDENT_SEIKANA     VARCHAR(60), \
    STUDENT_MEIKANA     VARCHAR(60), \
    TEMP10              VARCHAR(60), \
    TSUMEI_FLG          VARCHAR(60), \
    STUDENT_FULLNAME    VARCHAR(60), \
    TEMP13              VARCHAR(60), \
    FROM_SCHOOL_CD      VARCHAR(12), \
    ZENREKI             VARCHAR(60), \
    TEMP16              VARCHAR(60), \
    TEMP17              VARCHAR(60), \
    TEMP18              VARCHAR(60), \
    TEMP19              VARCHAR(60), \
    TEMP20              VARCHAR(60), \
    TEMP21              VARCHAR(60), \
    TEMP22              VARCHAR(10), \
    TEMP23              VARCHAR(60), \
    TEMP24              VARCHAR(10), \
    TEMP25              VARCHAR(60), \
    TEMP26              VARCHAR(60), \
    TEMP27              VARCHAR(60), \
    TEMP28              VARCHAR(60), \
    TEMP29              VARCHAR(60), \
    TEMP30              VARCHAR(10), \
    TEMP31              VARCHAR(10), \
    TEMP32              VARCHAR(60), \
    TEMP33              VARCHAR(10), \
    TEMP34              VARCHAR(60), \
    TEMP35              VARCHAR(10), \
    TEMP36              VARCHAR(60), \
    TEMP37              VARCHAR(60), \
    TEMP38              VARCHAR(60), \
    TEMP39              VARCHAR(60), \
    TEMP40              VARCHAR(60), \
    TEMP41              VARCHAR(60), \
    TEMP42              VARCHAR(60), \
    TEMP43              VARCHAR(60), \
    TEMP44              VARCHAR(60), \
    TEMP45              VARCHAR(60), \
    TEMP46              VARCHAR(10), \
    TEMP47              VARCHAR(10), \
    TEMP48              VARCHAR(10), \
    TEMP49              VARCHAR(10), \
    TEMP50              VARCHAR(10), \
    TEMP51              VARCHAR(10), \
    TEMP52              VARCHAR(10), \
    TEMP53              VARCHAR(10), \
    TEMP54              VARCHAR(10), \
    TEMP55              VARCHAR(10), \
    TEMP56              VARCHAR(10), \
    TEMP57              VARCHAR(10), \
    TEMP58              VARCHAR(10), \
    TEMP59              VARCHAR(60), \
    TEMP60              VARCHAR(10), \
    TEMP61              VARCHAR(10), \
    TEMP62              VARCHAR(10), \
    TEMP63              VARCHAR(10), \
    TEMP64              VARCHAR(10), \
    TEMP65              VARCHAR(10), \
    TEMP66              VARCHAR(10), \
    TEMP67              VARCHAR(10), \
    TEMP68              VARCHAR(10), \
    TEMP69              VARCHAR(10), \
    TEMP70              VARCHAR(10), \
    TEMP71              VARCHAR(10), \
    TEMP72              VARCHAR(10), \
    TEMP73              VARCHAR(60), \
    TEMP74              VARCHAR(10), \
    TEMP75              VARCHAR(10), \
    TEMP76              VARCHAR(10), \
    TEMP77              VARCHAR(10), \
    TEMP78              VARCHAR(10), \
    TEMP79              VARCHAR(10), \
    TEMP80              VARCHAR(10), \
    TEMP81              VARCHAR(10), \
    TEMP82              VARCHAR(10), \
    TEMP83              VARCHAR(10), \
    TEMP84              VARCHAR(10), \
    TEMP85              VARCHAR(10), \
    TEMP86              VARCHAR(10), \
    TEMP87              VARCHAR(10), \
    TEMP88              VARCHAR(10), \
    TEMP89              VARCHAR(10), \
    TEMP90              VARCHAR(10), \
    TEMP91              VARCHAR(10), \
    TEMP92              VARCHAR(10), \
    TEMP93              VARCHAR(10), \
    TEMP94              VARCHAR(10), \
    TEMP95              VARCHAR(60), \
    TEMP96              VARCHAR(60), \
    SEX_CD              VARCHAR(10), \
    TEMP98              VARCHAR(60), \
    TEMP99              VARCHAR(10), \
    TEMP100             VARCHAR(10), \
    BIRTHDAY_YMD        VARCHAR(10), \
    TEMP102             VARCHAR(60), \
    TEMP103             VARCHAR(60), \
    HOGOSHA_NAME        VARCHAR(60), \
    TEMP105             VARCHAR(60), \
    TEMP106             VARCHAR(60), \
    STUDENT_YUBIN       VARCHAR(10), \
    STUDENT_JUSHO       VARCHAR(150), \
    TEMP109             VARCHAR(150), \
    STUDENT_TEL         VARCHAR(60), \
    TEMP111             VARCHAR(60), \
    TEMP112             VARCHAR(60), \
    TEMP113             VARCHAR(60), \
    TEMP114             VARCHAR(60), \
    TEMP115             VARCHAR(60), \
    TEMP116             VARCHAR(60), \
    TEMP117             VARCHAR(60), \
    TEMP118             VARCHAR(60), \
    NYUGAKU_YMD         VARCHAR(60), \
    SOTSUGYO_MIKOMI_YMD VARCHAR(60), \
    HOGOSHA_KANA        VARCHAR(60), \
    HOGOSHA_YUBIN_NO    VARCHAR(60), \
    HOGOSHA_JUSHO       VARCHAR(150), \
    HOGOSHA_JUSHO2      VARCHAR(150), \
    HOGOSHA_TEL         VARCHAR(60), \
    KATEI_GAKKA_CD      VARCHAR(60), \
    NEN                 VARCHAR(60), \
    KUMI                VARCHAR(60), \
    BAN                 VARCHAR(60), \
    STUDENT_FULLKANA    VARCHAR(60), \
    CURRICULUM_YEAR     VARCHAR(4) \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO NYUGAKU_LIST_DAT \
    SELECT \
        * \
    FROM \
        NYUGAKU_LIST_DAT_OLD

ALTER TABLE NYUGAKU_LIST_DAT ADD CONSTRAINT PK_NYUGAKU_LIST_D PRIMARY KEY (ENTERYEAR,SCHREGNO)