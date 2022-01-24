-- $Id: rep-kojin_idou_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

drop table KOJIN_IDOU_DAT_OLD
create table KOJIN_IDOU_DAT_OLD like KOJIN_IDOU_DAT
insert into KOJIN_IDOU_DAT_OLD select * from KOJIN_IDOU_DAT

DROP TABLE KOJIN_IDOU_DAT
CREATE TABLE KOJIN_IDOU_DAT( \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    IDOU_DIV                 VARCHAR(2)    NOT NULL, \
    SHINSEI_DATE             DATE          NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    SHUUGAKU_NO              VARCHAR(7)    NOT NULL, \
    SEQ                      SMALLINT      NOT NULL, \
    IDOU_UKE_YEAR            VARCHAR(4), \
    IDOU_UKE_NO              VARCHAR(4), \
    IDOU_UKE_EDABAN          VARCHAR(3), \
    IDOU_DATE                DATE, \
    S_IDOU_DATE              DATE, \
    E_IDOU_DATE              DATE, \
    S_SAIKAI_YOTEI_YM        VARCHAR(7), \
    E_SAIKAI_YOTEI_YM        VARCHAR(7), \
    TENGAKUSAKI_HU_DIV       VARCHAR(1), \
    SCHOOL_CD                VARCHAR(7), \
    SCHOOL_CD_OLD            VARCHAR(7), \
    KATEI                    VARCHAR(2), \
    KATEI_OLD                VARCHAR(2), \
    TSUUGAKU_FLG             VARCHAR(1), \
    TSUUGAKU_DIV             VARCHAR(1), \
    TSUUGAKU_DIV_OLD         VARCHAR(1), \
    S_STOP_YOTEI_YM          VARCHAR(7), \
    E_STOP_YOTEI_YM          VARCHAR(7), \
    CHANGE_GK                INT, \
    S_CHANGE_GK_YM           VARCHAR(7), \
    E_CHANGE_GK_YM           VARCHAR(7), \
    REMARK                   VARCHAR(2400), \
    S_KETTEI_YM              VARCHAR(7), \
    E_KETTEI_YM              VARCHAR(7), \
    STOP_KETTEI_DATE         DATE, \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_IDOU_DAT ADD CONSTRAINT PK_K_IDOU_DAT PRIMARY KEY (KOJIN_NO, IDOU_DIV, SHINSEI_DATE, SHINSEI_YEAR, SHUUGAKU_NO, SEQ)

insert into \
KOJIN_IDOU_DAT \
( \
SELECT \
    T1.KOJIN_NO, \
    T1.IDOU_DIV, \
    T1.SHINSEI_DATE, \
    T1.SHINSEI_YEAR, \
    T1.SHUUGAKU_NO, \
    T1.SEQ, \
    T1.IDOU_UKE_YEAR, \
    T1.IDOU_UKE_NO, \
    T1.IDOU_UKE_EDABAN, \
    T1.IDOU_DATE, \
    T1.S_IDOU_DATE, \
    T1.E_IDOU_DATE, \
    T1.S_SAIKAI_YOTEI_YM, \
    T1.E_SAIKAI_YOTEI_YM, \
    T1.TENGAKUSAKI_HU_DIV, \
    T1.SCHOOL_CD, \
    SHINSEI.H_SCHOOL_CD, \
    T1.KATEI, \
    SHINSEI.KATEI, \
    T1.TSUUGAKU_FLG, \
    T1.TSUUGAKU_DIV, \
    KOJIN.TSUUGAKU_DIV, \
    T1.S_STOP_YOTEI_YM, \
    T1.E_STOP_YOTEI_YM, \
    T1.CHANGE_GK, \
    T1.S_CHANGE_GK_YM, \
    T1.E_CHANGE_GK_YM, \
    T1.REMARK, \
    T1.S_KETTEI_YM, \
    T1.E_KETTEI_YM, \
    T1.STOP_KETTEI_DATE, \
    T1.REGISTERCD, \
    T1.UPDATED \
FROM \
    KOJIN_IDOU_DAT_OLD T1 \
    LEFT JOIN ( \
        SELECT \
            LLLL1.* \
        FROM \
            ( \
            SELECT \
                LLL1.* \
            FROM \
                KOJIN_SHINSEI_HIST_DAT LLL1, \
                ( \
                SELECT \
                    MAIN.KOJIN_NO, \
                    MAIN.SHINSEI_YEAR, \
                    MAIN.ISSUEDATE, \
                    MIN(MAIN.SHIKIN_SHOUSAI_DIV) AS SHIKIN_SHOUSAI_DIV \
                FROM \
                    KOJIN_SHINSEI_HIST_DAT MAIN \
                WHERE \
                    MAIN.SHIKIN_SHOUSAI_DIV IN ('02', '08') \
                    AND EXISTS( \
                        SELECT \
                            'x' \
                        FROM \
                            ( \
                                SELECT \
                                    E1.KOJIN_NO, \
                                    E1.SHINSEI_YEAR, \
                                    MAX(E1.ISSUEDATE) AS ISSUEDATE \
                                FROM \
                                    KOJIN_SHINSEI_HIST_DAT E1 \
                                WHERE \
                                    E1.SHIKIN_SHOUSAI_DIV IN ('02', '08') \
                                GROUP BY \
                                    E1.KOJIN_NO, \
                                    E1.SHINSEI_YEAR \
                            ) SUB_MAIN \
                        WHERE \
                            SUB_MAIN.KOJIN_NO = MAIN.KOJIN_NO \
                            AND SUB_MAIN.SHINSEI_YEAR = MAIN.SHINSEI_YEAR \
                            AND SUB_MAIN.ISSUEDATE = MAIN.ISSUEDATE \
                    ) \
                GROUP BY \
                    MAIN.KOJIN_NO, \
                    MAIN.SHINSEI_YEAR, \
                    MAIN.ISSUEDATE \
                ) LL1 \
            WHERE \
                LL1.KOJIN_NO = LLL1.KOJIN_NO \
                AND LL1.SHINSEI_YEAR = LLL1.SHINSEI_YEAR \
                AND LL1.ISSUEDATE = LLL1.ISSUEDATE \
                AND LL1.SHIKIN_SHOUSAI_DIV = LLL1.SHIKIN_SHOUSAI_DIV \
            ) LLLL1 \
    ) SHINSEI ON T1.KOJIN_NO = SHINSEI.KOJIN_NO \
         AND T1.KOJIN_NO = SHINSEI.KOJIN_NO \
         AND T1.SHINSEI_YEAR = SHINSEI.SHINSEI_YEAR \
    LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO \
)

