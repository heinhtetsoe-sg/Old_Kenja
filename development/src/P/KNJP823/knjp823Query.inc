<?php

require_once('for_php7.php');

class knjp823query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/
    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["SELYEAR"]."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["SELYEAR"]}' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //減免区分複数登録チェック
    function selectCheckQuery($model) {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         HDAT.HR_NAME || T1.ATTENDNO || '番' AS HR_NAME_ATTNO, ";
        $query .= "         BASE.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN ( ";
        $query .= "                     SELECT ";
        $query .= "                         SCHREGNO, ";
        $query .= "                         max(SEMESTER) AS SEMESTER ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                             YEAR  = '{$model->field["SELYEAR"]}' ";
        $query .= "                         AND GRADE = '{$model->field["SELGRADE"]}' ";
        $query .= "                     GROUP BY ";
        $query .= "                         SCHREGNO ";
        $query .= "                     ) T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT HDAT ON T1.YEAR     = HDAT.YEAR ";
        $query .= "                                        AND T1.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                        AND T1.GRADE    = HDAT.GRADE ";
        $query .= "                                        AND T1.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$model->field["SELYEAR"]}' ";
        $query .= "         AND T1.GRADE    = '{$model->field["SELGRADE"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SHIP.SCHREGNO, ";
        $query .= "     SCHI.HR_NAME_ATTNO, ";
        $query .= "     SCHI.NAME, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT SHIP ";
        $query .= "     INNER JOIN SCH_INFO SCHI ON SHIP.SCHREGNO = SCHI.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         SHIP.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SHIP.SCHOOL_KIND = '{$model->field["SELSCHKIND"]}' ";
        $query .= "     AND '{$model->field["SELYEAR"]}' BETWEEN FISCALYEAR(SHIP.FROM_DATE) ";
        $query .= "                    AND FISCALYEAR(VALUE(SHIP.TO_DATE, '9999-12-31')) ";
        $query .= " GROUP BY ";
        $query .= "     SHIP.SCHREGNO, ";
        $query .= "     SCHI.HR_NAME_ATTNO, ";
        $query .= "     SCHI.NAME ";

        return $query;
    }

    //CSVデータ出力
    function selectQuery($model) {
        $getSglSchregField = $model->field["SELSCHKIND"] == "H" ? "_H" : "_J";
        $query  = " WITH SCHLIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         GDAT.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
        $query .= "              AND REGD.GRADE = GDAT.GRADE ";
        $query .= "     WHERE ";
        $query .= "             REGD.YEAR  = '".$model->field["SELYEAR"]."' ";
        $query .= "         AND REGD.GRADE = '".$model->field["SELGRADE"]."' ";
        $query .= " ), SCHOLASHIP as ( ";
        $query .= "     SELECT ";
        $query .= "         SHIP.SCHREGNO, ";
        $query .= "         SMAP.SGL_SCHOLARSHIP_CD, ";
        $query .= "         SMAP.SGL_SCHOLARSHIP_DIV ";
        $query .= "     FROM ";
        $query .= "         SCHREG_SCHOLARSHIP_HIST_DAT SHIP ";
        $query .= "         INNER JOIN COLLECT_SGL_SCHOLARSHIP_MAPPING_DAT SMAP ";
        $query .= "             ON SMAP.SCHOOLCD    = SHIP.SCHOOLCD ";
        $query .= "            AND SMAP.SCHOOL_KIND = SHIP.SCHOOL_KIND ";
        $query .= "            AND SMAP.YEAR        = '{$model->field["SELYEAR"]}' ";
        $query .= "            AND SMAP.SCHOLARSHIP = SHIP.SCHOLARSHIP ";
        $query .= "     WHERE ";
        $query .= "             SHIP.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "         AND SHIP.SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= "         AND SHIP.SCHREGNO IN (SELECT SCHLIST.SCHREGNO FROM SCHLIST) ";
        $query .= "         AND '{$model->field["SELYEAR"]}' BETWEEN FISCALYEAR(SHIP.FROM_DATE) ";
        $query .= "                                              AND FISCALYEAR(VALUE(SHIP.TO_DATE, '9999-12-31')) ";
        $query .= " ), NOT_SCHOLASHIP as ( ";
        $query .= "     SELECT ";
        $query .= "         SCHL.SCHREGNO, ";
        $query .= "         SMAP.SGL_SCHOLARSHIP_CD, ";
        $query .= "         SMAP.SGL_SCHOLARSHIP_DIV ";
        $query .= "     FROM ";
        $query .= "         SCHLIST SCHL ";
        $query .= "         INNER JOIN COLLECT_SGL_SCHOLARSHIP_MAPPING_DAT SMAP ";
        $query .= "             ON SMAP.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "            AND SMAP.SCHOOL_KIND = SCHL.SCHOOL_KIND ";
        $query .= "            AND SMAP.YEAR        = '{$model->field["SELYEAR"]}' ";
        $query .= "            AND SMAP.SCHOLARSHIP = '00' ";
        $query .= "     WHERE ";
        $query .= "             SCHL.SCHREGNO NOT IN (SELECT SHIP.SCHREGNO FROM SCHOLASHIP SHIP) ";
        $query .= " ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     SGL_SCH.SCHREGNO{$getSglSchregField} AS SCHREGNO, ";
        $query .= "     SHIP.SGL_SCHOLARSHIP_CD, ";
        $query .= "     SHIP.SGL_SCHOLARSHIP_DIV ";
        $query .= " FROM ";
        $query .= "     SCHOLASHIP SHIP ";
        $query .= "     LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT SGL_SCH ON SHIP.SCHREGNO = SGL_SCH.SCHREGNO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SGL_SCH.SCHREGNO{$getSglSchregField} AS SCHREGNO, ";
        $query .= "     SHIP.SGL_SCHOLARSHIP_CD, ";
        $query .= "     SHIP.SGL_SCHOLARSHIP_DIV ";
        $query .= " FROM ";
        $query .= "     NOT_SCHOLASHIP SHIP ";
        $query .= "     LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT SGL_SCH ON SHIP.SCHREGNO = SGL_SCH.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     SGL_SCHOLARSHIP_CD, ";
        $query .= "     SGL_SCHOLARSHIP_DIV ";

        return $query;
    }

}
?>
