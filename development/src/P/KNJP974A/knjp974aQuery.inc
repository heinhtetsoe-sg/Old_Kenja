<?php

require_once('for_php7.php');


class knjp974aQuery extends Query {

    //年度
    function getYear() {
        $query .= " WITH YEAR_T(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."年度', '".CTRL_YEAR."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".(CTRL_YEAR + 1)."年度', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     YEAR_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSemesterMst($model) {
        $query   = " SELECT ";
        $query  .= "   SDATE, ";
        $query  .= "   EDATE ";
        $query  .= " FROM ";
        $query  .= "   SEMESTER_MST ";
        $query  .= " WHERE ";
        $query  .= "   YEAR = '".$model->field["YEAR"]."' ";
        $query  .= "   AND SEMESTER = '9' ";

        return $query;
    }

    /***********/
    /* CSV出力 */
    /***********/
    //学校名
    function getSchoolName($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     SCHOOLNAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";

        return $query;
    }

    //西暦→和暦変換
    function getWareki($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L007' ";
        $query .= "     AND '{$model->field["YEAR"]}' BETWEEN NAMESPARE1 AND ABBV3 ";

        return $query;
    }

    //科目マスタ名称
    function getLevyLmst($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     LEVY_L_CD, ";
        $query .= "     LEVY_L_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_L_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD = '{$model->schoolCd}' ";
        $query .= "     AND YEAR     = '{$model->field["YEAR"]}' ";

        return $query;
    }

    //項目マスタ名称
    function getLevyMmst($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     LEVY_L_CD, ";
        $query .= "     LEVY_M_CD, ";
        $query .= "     LEVY_M_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD = '{$model->schoolCd}' ";
        $query .= "     AND YEAR     = '{$model->field["YEAR"]}' ";

        return $query;
    }

    //細目マスタ名称
    function getLevySmst($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     LEVY_L_CD, ";
        $query .= "     LEVY_M_CD, ";
        $query .= "     LEVY_S_CD, ";
        $query .= "     LEVY_S_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD = '{$model->schoolCd}' ";
        $query .= "     AND YEAR     = '{$model->field["YEAR"]}' ";

        return $query;
    }

    //学年リスト
    function getGradeList($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["GRADE"] == "00") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //収入詳細
    function getIncomeInfo($model, $schoolKind, $grade) {
        $fromDate   = str_replace("/", "-", $model->field["FROM_DATE"]);
        $toDate     = str_replace("/", "-", $model->field["TO_DATE"]);

        $query  = " WITH SCH_INCOME_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         INC_S.INCOME_L_CD, ";
        $query .= "         INC_S.INCOME_M_CD, ";
        $query .= "         INC_D.COLLECT_DIV, ";
        $query .= "         INC_D.INCOME_DATE, ";
        $query .= "         INC_S.SCHREGNO, ";
        $query .= "         sum(INC_S.INCOME_MONEY) AS INCOME_MONEY ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_INCOME_SCHREG_DAT INC_S ";
        $query .= "         LEFT JOIN LEVY_REQUEST_INCOME_DAT INC_D ";
        $query .= "             ON INC_D.SCHOOLCD    = INC_S.SCHOOLCD ";
        $query .= "            AND INC_D.SCHOOL_KIND = INC_S.SCHOOL_KIND ";
        $query .= "            AND INC_D.YEAR        = INC_S.YEAR ";
        $query .= "            AND INC_D.INCOME_L_CD = INC_S.INCOME_L_CD ";
        $query .= "            AND INC_D.INCOME_M_CD = INC_S.INCOME_M_CD ";
        $query .= "            AND INC_D.REQUEST_NO  = INC_S.REQUEST_NO ";
        $query .= "            AND INC_D.INCOME_DATE BETWEEN '".$fromDate."' AND '".$toDate."' ";
        $query .= "     WHERE ";
        $query .= "             INC_S.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "         AND INC_S.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "         AND INC_S.YEAR        = '{$model->field["YEAR"]}' ";
        $query .= "         AND VALUE(INC_D.INCOME_APPROVAL, '0') = '1' ";
        $query .= "         AND VALUE(INC_D.INCOME_CANCEL, '0')  != '1' ";
        $query .= "         AND INC_S.SCHREGNO IN ( SELECT ";
        $query .= "                                     REGD.SCHREGNO ";
        $query .= "                                 FROM ";
        $query .= "                                     SCHREG_REGD_DAT REGD ";
        $query .= "                                 WHERE ";
        $query .= "                                         REGD.YEAR     = INC_S.YEAR ";
        $query .= "                                     AND REGD.GRADE    = '{$grade}' ";
        $query .= "                                ) ";
        $query .= "     GROUP BY ";
        $query .= "         INC_S.INCOME_L_CD, ";
        $query .= "         INC_S.INCOME_M_CD, ";
        $query .= "         INC_D.COLLECT_DIV, ";
        $query .= "         INC_D.INCOME_DATE, ";
        $query .= "         INC_S.SCHREGNO ";
        $query .= " ) ";
        //前年度繰越から作成した収入伺
        $query .= " SELECT ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD, ";
        $query .= "     INCOME_DATE, ";
        $query .= "     sum(INCOME_MONEY) AS INCOME_MONEY, ";
        $query .= "     1 as SCH_COUNT, ";
        $query .= "     '1' as SORT ";
        $query .= " FROM ";
        $query .= "     SCH_INCOME_DATA ";
        $query .= " WHERE ";
        $query .= "     VALUE(COLLECT_DIV, '0') = '2' ";
        $query .= " GROUP BY ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD, ";
        $query .= "     INCOME_DATE ";
        $query .= " UNION ALL ";
        //それ以外の収入伺
        $query .= " SELECT ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD, ";
        $query .= "     INCOME_DATE, ";
        $query .= "     INCOME_MONEY, ";
        $query .= "     COUNT(*) AS SCH_COUNT, ";
        $query .= "     '2' as SORT ";
        $query .= " FROM ";
        $query .= "     SCH_INCOME_DATA ";
        $query .= " WHERE ";
        $query .= "     VALUE(COLLECT_DIV, '0') != '2' ";
        $query .= " GROUP BY ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD, ";
        $query .= "     INCOME_DATE, ";
        $query .= "     INCOME_MONEY ";
        $query .= " ORDER BY ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD, ";
        $query .= "     sort, ";
        $query .= "     INCOME_DATE, ";
        $query .= "     INCOME_MONEY desc ";

        return $query;
    }

    //支出詳細
    function getOutgoInfo($model, $schoolKind, $grade) {
        $fromDate   = str_replace("/", "-", $model->field["FROM_DATE"]);
        $toDate     = str_replace("/", "-", $model->field["TO_DATE"]);

        $query  = " SELECT ";
        $query .= "     OUT_D.INCOME_L_CD, ";
        $query .= "     OUT_D.INCOME_M_CD, ";
        $query .= "     OUT_D.OUTGO_DATE, ";
        $query .= "     OUT_MEISAI.OUTGO_L_CD, ";
        $query .= "     OUT_MEISAI.OUTGO_M_CD, ";
        $query .= "     OUT_MEISAI.OUTGO_S_CD, ";
        $query .= "     SUM(OUT_MEISAI.TOTAL_PRICE) AS TOTAL_PRICE ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_MEISAI_DAT OUT_MEISAI ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_DAT OUT_D ";
        $query .= "          ON OUT_MEISAI.SCHOOLCD    = OUT_D.SCHOOLCD ";
        $query .= "         AND OUT_MEISAI.SCHOOL_KIND = OUT_D.SCHOOL_KIND ";
        $query .= "         AND OUT_MEISAI.YEAR        = OUT_D.YEAR ";
        $query .= "         AND OUT_MEISAI.OUTGO_L_CD  = OUT_D.OUTGO_L_CD ";
        $query .= "         AND OUT_MEISAI.OUTGO_M_CD  = OUT_D.OUTGO_M_CD ";
        $query .= "         AND OUT_MEISAI.REQUEST_NO  = OUT_D.REQUEST_NO ";
        $query .= "         AND VALUE(OUT_D.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "         AND VALUE(OUT_D.OUTGO_CANCEL, '0')  != '1' ";
        $query .= "         AND OUT_D.OUTGO_DATE BETWEEN '".$fromDate."' AND '".$toDate."' ";
        $query .= " WHERE ";
        $query .= "         OUT_MEISAI.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "     AND OUT_MEISAI.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND OUT_MEISAI.YEAR        = '{$model->field["YEAR"]}' ";
        $query .= "     AND OUT_D.OUTGO_L_CD      != '99' "; //返金伝票除く
        $query .= "     AND OUT_D.INCOME_L_CD     != '98' "; //給付伝票除く
        $query .= "     AND OUT_MEISAI.REQUEST_NO IN ( ";
        $query .= "         SELECT ";
        $query .= "             OUT_SCH.REQUEST_NO ";
        $query .= "         FROM ";
        $query .= "             LEVY_REQUEST_OUTGO_SCHREG_DAT OUT_SCH ";
        $query .= "         WHERE ";
        $query .= "                 OUT_SCH.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "             AND OUT_SCH.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "             AND OUT_SCH.YEAR        = '{$model->field["YEAR"]}' ";
        $query .= "             AND EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT REGD ";
        $query .= "                 WHERE ";
        $query .= "                         REGD.YEAR     = OUT_SCH.YEAR ";
        $query .= "                     AND REGD.SCHREGNO = OUT_SCH.SCHREGNO ";
        $query .= "                     AND REGD.GRADE    = '{$grade}' ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             OUT_SCH.REQUEST_NO ";
        $query .= "         ) ";
        $query .= " GROUP BY ";
        $query .= "     OUT_D.INCOME_L_CD, ";
        $query .= "     OUT_D.INCOME_M_CD, ";
        $query .= "     OUT_D.OUTGO_DATE, ";
        $query .= "     OUT_MEISAI.OUTGO_L_CD, ";
        $query .= "     OUT_MEISAI.OUTGO_M_CD, ";
        $query .= "     OUT_MEISAI.OUTGO_S_CD ";
        $query .= " ORDER BY ";
        $query .= "     OUT_D.INCOME_L_CD, ";
        $query .= "     OUT_D.INCOME_M_CD, ";
        $query .= "     OUT_D.OUTGO_DATE, ";
        $query .= "     OUT_MEISAI.OUTGO_L_CD, ";
        $query .= "     OUT_MEISAI.OUTGO_M_CD, ";
        $query .= "     OUT_MEISAI.OUTGO_S_CD ";

        return $query;
    }

}
?>
