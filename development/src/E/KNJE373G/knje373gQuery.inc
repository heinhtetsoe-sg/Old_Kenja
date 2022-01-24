<?php

require_once('for_php7.php');

class knje373gQuery extends Query
{
    //CSV出力データ
    public function getCsvQuery($model, $year)
    {
        $query  = " WITH AFT_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEQ) AS SEQ, ";
        $query .= "         SCHREGNO, ";
        $query .= "         STAT_CD ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT ";
        $query .= "     WHERE ";
        if ($model->field["OUTDIV"] == "1" || $model->field["OUTDIV"] == "2") {
            $query .= "         SENKOU_KIND = '0' AND "; //受験先種別コード 0:学校 1:会社 2:家事手伝い
        } elseif ($model->field["OUTDIV"] == "3") {
            $query .= "         SENKOU_KIND = '1' AND "; //受験先種別コード 0:学校 1:会社 2:家事手伝い
        }
        $query .= "         DECISION = '1' "; //受験結果 1:合格
        if ($model->field["OUTDIV"] == "3") {
            $query .= "         AND PLANSTAT = '1' "; //進路状況 1:決定
        }
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         STAT_CD ";
        $query .= " ), COLLEGE_COUNT_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         AFTD.YEAR, ";
        $query .= "         AFTD.STAT_CD, ";
        $query .= "         SUM(CASE WHEN T1.KISOTU_FLAG = '0' THEN 1 ELSE 0 END) AS GENEKI_CNT, ";
        $query .= "         SUM(CASE WHEN T1.KISOTU_FLAG = '1' THEN 1 ELSE 0 END) AS KISOTU_CNT ";
        $query .= "     FROM ";
        $query .= "         AFT_DAT AFTD ";
        $query .= "         INNER JOIN ( ";
        $query .= "             (SELECT ";
        $query .= "                 SCHREGNO, ";
        $query .= "                 T1.GRADE, ";
        $query .= "                 '0' AS KISOTU_FLAG  ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT T1 ";
        $query .= "                 INNER JOIN SCHREG_REGD_GDAT GT1 ";
        $query .= "                      ON GT1.YEAR = T1.YEAR ";
        $query .= "                     AND GT1.GRADE = T1.GRADE ";
        $query .= "                     AND GT1.SCHOOL_KIND = 'H' ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR = '".$year."' AND ";
        $query .= "                 T1.SEMESTER = (SELECT MAX(TW.SEMESTER) FROM SCHREG_REGD_DAT TW WHERE TW.YEAR = T1.YEAR AND TW.SCHREGNO = T1.SCHREGNO) AND ";
        $query .= "                 VALUE(GT1.GRADE_CD, 0) = '3' ";
        $query .= "             ) ";
        $query .= "             UNION ";
        $query .= "             (SELECT ";
        $query .= "                 T2.SCHREGNO, ";
        $query .= "                 GRD_GRADE AS GRADE, ";
        $query .= "                 '1' AS KISOTU_FLAG ";
        $query .= "             FROM ";
        $query .= "                 AFT_GRAD_COURSE_DAT T2  ";
        $query .= "                 LEFT JOIN GRD_BASE_MST GMST ";
        $query .= "                     ON GMST.SCHREGNO = T2.SCHREGNO ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR = '".$year."' AND ";
        $query .= "                 NOT EXISTS( ";
        $query .= "                     SELECT ";
        $query .= "                         * ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREG_REGD_DAT.YEAR = '".$year."' AND ";
        $query .= "                         SCHREG_REGD_DAT.SEMESTER = (SELECT MAX(TW.SEMESTER) FROM SCHREG_REGD_DAT TW WHERE TW.YEAR = SCHREG_REGD_DAT.YEAR AND TW.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO) AND ";
        $query .= "                         T2.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "                 ) ";
        $query .= "             ) ";
        $query .= "         ) T1 ON T1.SCHREGNO = AFTD.SCHREGNO  ";
        $query .= "     WHERE ";
        $query .= "         AFTD.YEAR = '".$year."' ";
        $query .= "     GROUP BY ";
        $query .= "         AFTD.YEAR, ";
        $query .= "         AFTD.STAT_CD ";
        $query .= " ) ";

        if ($model->field["OUTDIV"] == "1" || $model->field["OUTDIV"] == "2") {
            $query .= " SELECT ";
            $query .= "     CNTD.YEAR, ";
            $query .= "     RIGHT('0000' || CM.SCHOOL_CD,4) AS SCHOOL_CD, ";
            $query .= "     CM.SCHOOL_NAME, ";
            $query .= "     CM.SCHOOL_GROUP, ";
            $query .= "     NME012.NAME1 AS SCHOOL_GROUP_NAME, ";
            $query .= "     (CNTD.GENEKI_CNT + CNTD.KISOTU_CNT) AS GK_SUM, ";
            $query .= "     CNTD.GENEKI_CNT, ";
            $query .= "     CNTD.KISOTU_CNT ";
            $query .= " FROM ";
            $query .= "     COLLEGE_COUNT_DAT CNTD ";
            $query .= "     INNER JOIN COLLEGE_MST CM ";
            $query .= "         ON CM.SCHOOL_CD = CNTD.STAT_CD ";
            $query .= "     LEFT JOIN NAME_MST NME012 ";
            $query .= "          ON NME012.NAMECD1 = 'E012' ";
            $query .= "         AND NME012.NAMECD2 = CM.SCHOOL_GROUP ";
            $query .= " WHERE ";
            if ($model->field["OUTDIV"] == "1") {
                $query .= "     CM.SCHOOL_GROUP IN ('01','02','03','04','05','08') "; //進学
            } elseif ($model->field["OUTDIV"] == "2") {
                $query .= "     CM.SCHOOL_GROUP IN ('06','07','09','99') "; //専門各種学校
            }
            $query .= " ORDER BY ";
            $query .= "     CM.SCHOOL_GROUP, ";
            $query .= "     CM.SCHOOL_CD ";
        } elseif ($model->field["OUTDIV"] == "3") {
            $query .= " SELECT ";
            $query .= "     CNTD.YEAR, ";
            $query .= "     CM.COMPANY_CD, ";
            $query .= "     CM.COMPANY_NAME, ";
            $query .= "     CM.SHUSHOKU_ADDR, ";
            $query .= "     '' AS KINMUCHI, ";
            $query .= "     (CNTD.GENEKI_CNT + CNTD.KISOTU_CNT) AS GK_SUM, ";
            $query .= "     CNTD.GENEKI_CNT, ";
            $query .= "     CNTD.KISOTU_CNT ";
            $query .= " FROM ";
            $query .= "     COLLEGE_COUNT_DAT CNTD ";
            $query .= "     INNER JOIN COMPANY_MST CM ";
            $query .= "         ON CM.COMPANY_CD = CNTD.STAT_CD ";
            $query .= " ORDER BY ";
            $query .= "     CM.COMPANY_CD ";
        }

        return $query;
    }
}
