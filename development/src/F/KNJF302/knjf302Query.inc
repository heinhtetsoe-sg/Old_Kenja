<?php

require_once('for_php7.php');

class knjf302Query extends Query {

    //教育委員会判定
    function z010Abbv1() {
        $query  = " SELECT ";
        $query .= "      ABBV1 ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'Z010' ";
        $query .= "      AND NAMECD2 = '00' ";

        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //学期取得
    function getSemesMst($model)
    {
        $query  = "SELECT SEMESTER ";
        $query .= "  FROM SEMESTER_MST ";
        $query .= " WHERE YEAR     = '".$model->field["YEAR"]."'";
        $query .= "   AND '".str_replace("/", "-", $model->field["ADDITION_DATE"])."' BETWEEN SDATE AND EDATE ";
        $query .= " ORDER BY SEMESTER ";

        return $query;
    }

    //年度取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "      GRADE_NAME1 AS LABEL, ";
        $query .= "      GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "      SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //データ表示
    function ReadQuery($model) {
        $query  = "";
        $query .= " WITH T_SCHREG (GRADE, HR_CLASS, SCHREGNO) AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "         AND T1.SEMESTER = '".$model->semester."' ";
        if ($model->field["GRADE"]) {
            $query .= "         AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= "         AND NOT EXISTS( ";
        $query .= "                        SELECT ";
        $query .= "                            'x' ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_TRANSFER_DAT E1 ";
        $query .= "                        WHERE ";
        $query .= "                                E1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                            AND E1.TRANSFERCD IN ('1', '2') ";
        $query .= "                            AND '".str_replace("/", "-", $model->field["ADDITION_DATE"])."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "                       ) ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " , T_GRADE (GRADE, HR_CLASS, HR_NAME, TOTALSUM06) AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T3.HR_NAME, ";
        $query .= "         COUNT(*) ";
        $query .= "     FROM ";
        $query .= "         T_SCHREG T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "             ON  T3.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "             AND T3.SEMESTER = '".$model->semester."' ";
        $query .= "             AND T3.GRADE    = T1.GRADE ";
        $query .= "             AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     ) ";
        if ($model->cmd == "recalc") {
            //再計算
            $query .= " , PETITION_DAT AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAX(T2.SEQNO) AS SEQNO, ";
            $query .= "         T2.SCHREGNO, ";
            $query .= "         T2.ATTENDDATE, ";
            $query .= "         T2.PERIODCD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T2.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "         AND T2.ATTENDDATE = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."' ";
            $query .= "         AND EXISTS(SELECT  'X' FROM T_SCHREG W1 ";
            $query .= "                     WHERE  W1.SCHREGNO = T2.SCHREGNO) ";
            $query .= "     GROUP BY ";
            $query .= "         T2.SCHREGNO, ";
            $query .= "         T2.ATTENDDATE, ";
            $query .= "         T2.PERIODCD ";
            $query .= "     ) ";
            $query .= " , PETITION_HDAT AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD1 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD2 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD3 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD4 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD5 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD6 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD7 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD8 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD9 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SEQNO, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.DI_REMARK_CD10 AS DI_REMARK_CD ";
            $query .= "     FROM ";
            $query .= "         ATTEND_PETITION_HDAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     ) ";
            $query .= " , T_MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T2.ATTENDDATE, ";
            $query .= "         L1.NAMESPARE1 AS DI_REMARK_NO ";
            $query .= "     FROM ";
            $query .= "         PETITION_HDAT T1 ";
            $query .= "         INNER JOIN PETITION_DAT T2 ON T2.SEQNO = T1.SEQNO ";
            $query .= "                                   AND T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         INNER JOIN V_NAME_MST L1 ON L1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "                                 AND L1.NAMECD1 = 'C900' ";
            $query .= "                                 AND L1.NAMECD2 = T1.DI_REMARK_CD ";
            $query .= "                                 AND L1.NAMESPARE1 IS NOT NULL ";
            $query .= "     GROUP BY ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T2.ATTENDDATE, ";
            $query .= "         L1.NAMESPARE1, ";
            $query .= "         L1.NAMESPARE2 ";
            $query .= "     ) ";
            $query .= " , T_SYUTTEI AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T2.ATTENDDATE, ";
            $query .= "         L1.NAMESPARE2 AS DI_REMARK_NO2 ";
            $query .= "     FROM ";
            $query .= "         PETITION_HDAT T1 ";
            $query .= "         INNER JOIN PETITION_DAT T2 ON T2.SEQNO = T1.SEQNO ";
            $query .= "                                   AND T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         INNER JOIN V_NAME_MST L1 ON L1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "                                 AND L1.NAMECD1 = 'C900' ";
            $query .= "                                 AND L1.NAMECD2 = T1.DI_REMARK_CD ";
            $query .= "                                 AND L1.NAMESPARE2 IS NOT NULL ";
            $query .= "     GROUP BY ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T2.ATTENDDATE, ";
            $query .= "         L1.NAMESPARE1, ";
            $query .= "         L1.NAMESPARE2 ";
            $query .= "     ) ";
            $query .= " , T_RECALC_KESSEKI AS ( ";
            $query .= " SELECT ";
            $query .= "     T0.GRADE, ";
            $query .= "     T0.HR_CLASS, ";
            $query .= "     T_GRADE.HR_NAME, ";
            $query .= "     COUNT(DISTINCT L1.SCHREGNO) AS TOTAL, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '1' THEN 1 ELSE 0 END) AS ABSENCE01, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '2' THEN 1 ELSE 0 END) AS ABSENCE02, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '3' THEN 1 ELSE 0 END) AS ABSENCE03, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '4' THEN 1 ELSE 0 END) AS ABSENCE04, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '5' THEN 1 ELSE 0 END) AS ABSENCE05, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '6' THEN 1 ELSE 0 END) AS ABSENCE06, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '7' THEN 1 ELSE 0 END) AS ABSENCE07, ";
            $query .= "     SUM(CASE WHEN L1.DI_REMARK_NO = '8' THEN 1 ELSE 0 END) AS ABSENCE08, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '1' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND01, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '2' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND02, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '3' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND03, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '4' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND04, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '5' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND05, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '6' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND06, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '7' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND07, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '8' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND08, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '9' THEN 1 ELSE 0 END)  AS ATTENDSUSPEND09, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '10' THEN 1 ELSE 0 END) AS ATTENDSUSPEND10, ";
            $query .= "     SUM(CASE WHEN L2.DI_REMARK_NO2 = '11' THEN 1 ELSE 0 END) AS ATTENDSUSPEND11, ";
            $query .= "     0 AS TOTALSUM01, ";
            $query .= "     SUM(CASE WHEN VALUE(L1.DI_REMARK_NO, '9999') <> '9999' THEN 1 ELSE 0 END) AS TOTALSUM02, ";
            $query .= "     SUM(CASE WHEN VALUE(L2.DI_REMARK_NO2, '9999') <> '9999' THEN 1 ELSE 0 END) AS TOTALSUM03, ";
            $query .= "     0 AS TOTALSUM04, ";
            $query .= "     0 AS TOTALSUM04_PERCENT, ";
            $query .= "     0 AS TOTALSUM05, ";
            $query .= "     0 AS TOTALSUM05_PERCENT, ";
            $query .= "     COUNT(DISTINCT T0.SCHREGNO) AS TOTALSUM06 ";
            $query .= " FROM ";
            $query .= "     T_SCHREG T0 ";
            $query .= "     LEFT JOIN T_GRADE ON T_GRADE.GRADE = T0.GRADE ";
            $query .= "          AND T_GRADE.HR_CLASS = T0.HR_CLASS ";
            $query .= "     LEFT JOIN T_MAIN L1 ON L1.SCHREGNO = T0.SCHREGNO ";
            $query .= "     LEFT JOIN T_SYUTTEI L2 ON L2.SCHREGNO = T0.SCHREGNO ";
            $query .= " GROUP BY ";
            $query .= "     T0.GRADE, ";
            $query .= "     T0.HR_CLASS, ";
            $query .= "     T_GRADE.HR_NAME ";
            $query .= "     ) ";
            $query .= " , T_RECALC AS ( ";
            $query .= " SELECT ";
            $query .= "     T0.GRADE, ";
            $query .= "     T0.HR_CLASS, ";
            $query .= "     T0.HR_NAME, ";
            $query .= "     T0.TOTAL, ";
            $query .= "     T0.ABSENCE01, ";
            $query .= "     T0.ABSENCE02, ";
            $query .= "     T0.ABSENCE03, ";
            $query .= "     T0.ABSENCE04, ";
            $query .= "     T0.ABSENCE05, ";
            $query .= "     T0.ABSENCE06, ";
            $query .= "     T0.ABSENCE07, ";
            $query .= "     T0.ABSENCE08, ";
            $query .= "     T0.ATTENDSUSPEND01, ";
            $query .= "     T0.ATTENDSUSPEND02, ";
            $query .= "     T0.ATTENDSUSPEND03, ";
            $query .= "     T0.ATTENDSUSPEND04, ";
            $query .= "     T0.ATTENDSUSPEND05, ";
            $query .= "     T0.ATTENDSUSPEND06, ";
            $query .= "     T0.ATTENDSUSPEND07, ";
            $query .= "     T0.ATTENDSUSPEND08, ";
            $query .= "     T0.ATTENDSUSPEND09, ";
            $query .= "     T0.ATTENDSUSPEND10, ";
            $query .= "     T0.ATTENDSUSPEND11, ";
            $query .= "     T0.TOTALSUM01, ";
            $query .= "     T0.TOTALSUM02, ";
            $query .= "     T0.TOTALSUM03, ";
            $query .= "     T0.TOTALSUM04, ";
            $query .= "     T0.TOTALSUM04_PERCENT, ";
            $query .= "     T0.TOTALSUM05, ";
            $query .= "     T0.TOTALSUM05_PERCENT, ";
            $query .= "     T0.TOTALSUM06 ";
            $query .= " FROM ";
            $query .= "     T_RECALC_KESSEKI T0 ";
            $query .= "     ) ";
        } else {
            //登録テーブル
            $query .= " , T_ADDITION1 AS ( ";
            $query .= "     SELECT ";
            $query .= "         * ";
            $query .= "     FROM ";
            if ($model->fixedData) {
                $query .= "         MEDEXAM_DISEASE_ADDITION2_FIXED_DAT ";
            } else {
                $query .= "         MEDEXAM_DISEASE_ADDITION2_DAT ";
            }
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND YEAR = '".$model->field["YEAR"]."' ";
            if ($model->fixedData) {
                $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
            }
            $query .= "         AND ADDITION_DATE = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."' ";
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     L1.ABSENCE01, ";
        $query .= "     L1.ABSENCE02, ";
        $query .= "     L1.ABSENCE03, ";
        $query .= "     L1.ABSENCE04, ";
        $query .= "     L1.ABSENCE05, ";
        $query .= "     L1.ABSENCE06, ";
        $query .= "     L1.ABSENCE07, ";
        $query .= "     L1.ABSENCE08, ";
        $query .= "     L1.ATTENDSUSPEND01, ";
        $query .= "     L1.ATTENDSUSPEND02, ";
        $query .= "     L1.ATTENDSUSPEND03, ";
        $query .= "     L1.ATTENDSUSPEND04, ";
        $query .= "     L1.ATTENDSUSPEND05, ";
        $query .= "     L1.ATTENDSUSPEND06, ";
        $query .= "     L1.ATTENDSUSPEND07, ";
        $query .= "     L1.ATTENDSUSPEND08, ";
        $query .= "     L1.ATTENDSUSPEND09, ";
        $query .= "     L1.ATTENDSUSPEND10, ";
        $query .= "     L1.ATTENDSUSPEND11, ";
        $query .= "     L1.TOTALSUM01, ";
        $query .= "     L1.TOTALSUM02, ";
        $query .= "     L1.TOTALSUM03, ";
        $query .= "     L1.TOTALSUM04, ";
        $query .= "     L1.TOTALSUM04_PERCENT, ";
        $query .= "     L1.TOTALSUM05, ";
        $query .= "     L1.TOTALSUM05_PERCENT, ";
        $query .= "     CASE WHEN L1.GRADE IS NOT NULL ";
        $query .= "          THEN L1.TOTALSUM06 ";
        $query .= "          ELSE T1.TOTALSUM06 ";
        $query .= "     END AS TOTALSUM06 ";
        $query .= " FROM ";
        $query .= "     T_GRADE T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.GRADE = T1.GRADE AND L1.HR_CLASS = T1.HR_CLASS ";
        } else {
            $query .= "     LEFT JOIN T_ADDITION1 L1 ON L1.GRADE = T1.GRADE AND L1.HR_CLASS = T1.HR_CLASS ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION2_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR      = '".$model->field["YEAR"]."'";
        $query .= "   AND ADDITION_DATE      = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."'";
        if ($model->field["GRADE"]) {
            $query .= "   AND (GRADE      = '".$model->field["GRADE"]."' OR GRADE = '99') ";
        }
        $db->query($query);

        //INSERT
        $gradeTotal = array();
        $total = array();
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //学年・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = $model->field["YEAR"];
            $data["ADDITION_DATE"][DATE]        = $model->field["ADDITION_DATE"];
            $data["GRADE"][TEXT]                = $code_arr[0];
            $data["HR_CLASS"][TEXT]             = $code_arr[1];
            if ($code_arr[1] != "999") {
                foreach ($model->fields as $setKey => $setVal) {
                    $gradeTotal[$setKey] += $setVal[$i];
                    $total[$setKey] += $setVal[$i];
                }
                //人数
                $data["ABSENCE01"][NUMBER]          = $model->fields["ABSENCE01"][$i];
                $data["ABSENCE02"][NUMBER]          = $model->fields["ABSENCE02"][$i];
                $data["ABSENCE03"][NUMBER]          = $model->fields["ABSENCE03"][$i];
                $data["ABSENCE04"][NUMBER]          = $model->fields["ABSENCE04"][$i];
                $data["ABSENCE05"][NUMBER]          = $model->fields["ABSENCE05"][$i];
                $data["ABSENCE06"][NUMBER]          = $model->fields["ABSENCE06"][$i];
                $data["ABSENCE07"][NUMBER]          = $model->fields["ABSENCE07"][$i];
                $data["ABSENCE08"][NUMBER]          = $model->fields["ABSENCE08"][$i];
                $data["ATTENDSUSPEND01"][NUMBER]    = $model->fields["ATTENDSUSPEND01"][$i];
                $data["ATTENDSUSPEND02"][NUMBER]    = $model->fields["ATTENDSUSPEND02"][$i];
                $data["ATTENDSUSPEND03"][NUMBER]    = $model->fields["ATTENDSUSPEND03"][$i];
                $data["ATTENDSUSPEND04"][NUMBER]    = $model->fields["ATTENDSUSPEND04"][$i];
                $data["ATTENDSUSPEND05"][NUMBER]    = $model->fields["ATTENDSUSPEND05"][$i];
                $data["ATTENDSUSPEND06"][NUMBER]    = $model->fields["ATTENDSUSPEND06"][$i];
                $data["ATTENDSUSPEND07"][NUMBER]    = $model->fields["ATTENDSUSPEND07"][$i];
                $data["ATTENDSUSPEND08"][NUMBER]    = $model->fields["ATTENDSUSPEND08"][$i];
                $data["ATTENDSUSPEND09"][NUMBER]    = $model->fields["ATTENDSUSPEND09"][$i];
                $data["ATTENDSUSPEND10"][NUMBER]    = $model->fields["ATTENDSUSPEND10"][$i];
                $data["ATTENDSUSPEND11"][NUMBER]    = $model->fields["ATTENDSUSPEND11"][$i];
                $data["TOTALSUM01"][NUMBER]         = $model->fields["TOTALSUM01"][$i];
                $data["TOTALSUM02"][NUMBER]         = $model->fields["TOTALSUM02"][$i];
                $data["TOTALSUM03"][NUMBER]         = $model->fields["TOTALSUM03"][$i];
                $data["TOTALSUM04"][NUMBER]         = (int)$model->fields["TOTALSUM01"][$i] + (int)$model->fields["TOTALSUM02"][$i];
                $data["TOTALSUM04_PERCENT"][NUMBER] = $model->fields["TOTALSUM06"][$i] > 0 ? ($model->fields["TOTALSUM01"][$i] + (int)$model->fields["TOTALSUM02"][$i]) / (int)$model->fields["TOTALSUM06"][$i] : 0;
                $data["TOTALSUM05"][NUMBER]         = (int)$model->fields["TOTALSUM02"][$i] + (int)$model->fields["TOTALSUM03"][$i];
                $data["TOTALSUM05_PERCENT"][NUMBER] = $model->fields["TOTALSUM06"][$i] > 0 ? ($model->fields["TOTALSUM02"][$i] + (int)$model->fields["TOTALSUM03"][$i]) / (int)$model->fields["TOTALSUM06"][$i] : 0;
                $data["TOTALSUM06"][NUMBER]         = $model->fields["TOTALSUM06"][$i];
            } else {
                $setTotal = $code_arr[0] == "99" ? $total : $gradeTotal;
                //人数
                $data["ABSENCE01"][NUMBER]          = $setTotal["ABSENCE01"];
                $data["ABSENCE02"][NUMBER]          = $setTotal["ABSENCE02"];
                $data["ABSENCE03"][NUMBER]          = $setTotal["ABSENCE03"];
                $data["ABSENCE04"][NUMBER]          = $setTotal["ABSENCE04"];
                $data["ABSENCE05"][NUMBER]          = $setTotal["ABSENCE05"];
                $data["ABSENCE06"][NUMBER]          = $setTotal["ABSENCE06"];
                $data["ABSENCE07"][NUMBER]          = $setTotal["ABSENCE07"];
                $data["ABSENCE08"][NUMBER]          = $setTotal["ABSENCE08"];
                $data["ATTENDSUSPEND01"][NUMBER]    = $setTotal["ATTENDSUSPEND01"];
                $data["ATTENDSUSPEND02"][NUMBER]    = $setTotal["ATTENDSUSPEND02"];
                $data["ATTENDSUSPEND03"][NUMBER]    = $setTotal["ATTENDSUSPEND03"];
                $data["ATTENDSUSPEND04"][NUMBER]    = $setTotal["ATTENDSUSPEND04"];
                $data["ATTENDSUSPEND05"][NUMBER]    = $setTotal["ATTENDSUSPEND05"];
                $data["ATTENDSUSPEND06"][NUMBER]    = $setTotal["ATTENDSUSPEND06"];
                $data["ATTENDSUSPEND07"][NUMBER]    = $setTotal["ATTENDSUSPEND07"];
                $data["ATTENDSUSPEND08"][NUMBER]    = $setTotal["ATTENDSUSPEND08"];
                $data["ATTENDSUSPEND09"][NUMBER]    = $setTotal["ATTENDSUSPEND09"];
                $data["ATTENDSUSPEND10"][NUMBER]    = $setTotal["ATTENDSUSPEND10"];
                $data["ATTENDSUSPEND11"][NUMBER]    = $setTotal["ATTENDSUSPEND11"];
                $data["TOTALSUM01"][NUMBER]         = $setTotal["TOTALSUM01"];
                $data["TOTALSUM02"][NUMBER]         = $setTotal["TOTALSUM02"];
                $data["TOTALSUM03"][NUMBER]         = $setTotal["TOTALSUM03"];
                $data["TOTALSUM04"][NUMBER]         = (int)$setTotal["TOTALSUM01"] + (int)$setTotal["TOTALSUM02"];
                $data["TOTALSUM04_PERCENT"][NUMBER] = $setTotal["TOTALSUM06"] > 0 ? ((int)$setTotal["TOTALSUM01"] + (int)$setTotal["TOTALSUM02"]) / (int)$setTotal["TOTALSUM06"] : 0;
                $data["TOTALSUM05"][NUMBER]         = (int)$setTotal["TOTALSUM02"] + (int)$setTotal["TOTALSUM03"];
                $data["TOTALSUM05_PERCENT"][NUMBER] = $setTotal["TOTALSUM06"] > 0 ? ((int)$setTotal["TOTALSUM02"] + (int)$setTotal["TOTALSUM03"]) / (int)$setTotal["TOTALSUM06"] : 0;
                $data["TOTALSUM06"][NUMBER]         = $setTotal["TOTALSUM06"];
                $gradeTotal = array();
            }

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION2_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION2_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR      = '".$model->field["YEAR"]."'";
        $query .= "   AND ADDITION_DATE      = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."'";
        if ($model->field["GRADE"]) {
            $query .= "   AND (GRADE      = '".$model->field["GRADE"]."' OR GRADE = '99') ";
        }
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knjf302Query::getAddition2Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["ADDITION_DATE"][DATE]        = $row["ADDITION_DATE"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["HR_CLASS"][TEXT]             = $row["HR_CLASS"];

            //人数
            $data["ABSENCE01"][NUMBER]          = $row["ABSENCE01"];
            $data["ABSENCE02"][NUMBER]          = $row["ABSENCE02"];
            $data["ABSENCE03"][NUMBER]          = $row["ABSENCE03"];
            $data["ABSENCE04"][NUMBER]          = $row["ABSENCE04"];
            $data["ABSENCE05"][NUMBER]          = $row["ABSENCE05"];
            $data["ABSENCE06"][NUMBER]          = $row["ABSENCE06"];
            $data["ABSENCE07"][NUMBER]          = $row["ABSENCE07"];
            $data["ABSENCE08"][NUMBER]          = $row["ABSENCE08"];
            $data["ATTENDSUSPEND01"][NUMBER]    = $row["ATTENDSUSPEND01"];
            $data["ATTENDSUSPEND02"][NUMBER]    = $row["ATTENDSUSPEND02"];
            $data["ATTENDSUSPEND03"][NUMBER]    = $row["ATTENDSUSPEND03"];
            $data["ATTENDSUSPEND04"][NUMBER]    = $row["ATTENDSUSPEND04"];
            $data["ATTENDSUSPEND05"][NUMBER]    = $row["ATTENDSUSPEND05"];
            $data["ATTENDSUSPEND06"][NUMBER]    = $row["ATTENDSUSPEND06"];
            $data["ATTENDSUSPEND07"][NUMBER]    = $row["ATTENDSUSPEND07"];
            $data["ATTENDSUSPEND08"][NUMBER]    = $row["ATTENDSUSPEND08"];
            $data["ATTENDSUSPEND09"][NUMBER]    = $row["ATTENDSUSPEND09"];
            $data["ATTENDSUSPEND10"][NUMBER]    = $row["ATTENDSUSPEND10"];
            $data["ATTENDSUSPEND11"][NUMBER]    = $row["ATTENDSUSPEND11"];
            $data["TOTALSUM01"][NUMBER]         = $row["TOTALSUM01"];
            $data["TOTALSUM02"][NUMBER]         = $row["TOTALSUM02"];
            $data["TOTALSUM03"][NUMBER]         = $row["TOTALSUM03"];
            $data["TOTALSUM04"][NUMBER]         = $row["TOTALSUM04"];
            $data["TOTALSUM04_PERCENT"][NUMBER] = $row["TOTALSUM04_PERCENT"];
            $data["TOTALSUM05"][NUMBER]         = $row["TOTALSUM05"];
            $data["TOTALSUM05_PERCENT"][NUMBER] = $row["TOTALSUM05_PERCENT"];
            $data["TOTALSUM06"][NUMBER]         = $row["TOTALSUM06"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION2_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE AS LABEL, ";
        $query .= "     T1.EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_ADDITION2_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC ";

        return $query;
    }

    //確定コンボ
    function getFixed($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIXED_DATE AS LABEL, ";
        $query .= "     T1.FIXED_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION2_FIXED_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.FIXED_DATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.FIXED_DATE DESC ";

        return $query;
    }

    //文書番号
    function getTuutatu($model) {
        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ";
        $query .= "          ON T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.DOC_NUMBER   = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        $query .= "     T1.EDBOARD_SCHOOLCD     = '".$model->schoolcd."' AND ";
        $query .= "     L1.REQUEST_ANSWER_PRG   = '".PROGRAMID."' AND ";
        $query .= "     L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getAddition2Dat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION2_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "     AND ADDITION_DATE = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."'";
        if ($model->field["GRADE"]) {
            $query .= "     AND GRADE         = '".$model->field["GRADE"]."'";
        }

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getAddition2FixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION2_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        $query .= "     AND ADDITION_DATE = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."'";
        if ($model->field["GRADE"]) {
            $query .= "     AND GRADE         = '".$model->field["GRADE"]."'";
        }

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_DISEASE_ADDITION2_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_DISEASE_ADDITION2_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・感染症発生状況入力管理画面テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION2_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "   AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        $query .= "   AND ADDITION_DATE = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."'";
        if ($model->field["GRADE"]) {
            $query .= "   AND GRADE         = '".$model->field["GRADE"]."'";
        }
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（感染症発生状況入力管理画面）
        $query = knjf302Query::getAddition2FixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["ADDITION_DATE"][DATE]        = $row["ADDITION_DATE"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["HR_CLASS"][TEXT]             = $row["HR_CLASS"];
            //人数
            $data["ABSENCE01"][NUMBER]          = $row["ABSENCE01"];
            $data["ABSENCE02"][NUMBER]          = $row["ABSENCE02"];
            $data["ABSENCE03"][NUMBER]          = $row["ABSENCE03"];
            $data["ABSENCE04"][NUMBER]          = $row["ABSENCE04"];
            $data["ABSENCE05"][NUMBER]          = $row["ABSENCE05"];
            $data["ABSENCE06"][NUMBER]          = $row["ABSENCE06"];
            $data["ABSENCE07"][NUMBER]          = $row["ABSENCE07"];
            $data["ABSENCE08"][NUMBER]          = $row["ABSENCE08"];
            $data["ATTENDSUSPEND01"][NUMBER]    = $row["ATTENDSUSPEND01"];
            $data["ATTENDSUSPEND02"][NUMBER]    = $row["ATTENDSUSPEND02"];
            $data["ATTENDSUSPEND03"][NUMBER]    = $row["ATTENDSUSPEND03"];
            $data["ATTENDSUSPEND04"][NUMBER]    = $row["ATTENDSUSPEND04"];
            $data["ATTENDSUSPEND05"][NUMBER]    = $row["ATTENDSUSPEND05"];
            $data["ATTENDSUSPEND06"][NUMBER]    = $row["ATTENDSUSPEND06"];
            $data["ATTENDSUSPEND07"][NUMBER]    = $row["ATTENDSUSPEND07"];
            $data["ATTENDSUSPEND08"][NUMBER]    = $row["ATTENDSUSPEND08"];
            $data["ATTENDSUSPEND09"][NUMBER]    = $row["ATTENDSUSPEND09"];
            $data["ATTENDSUSPEND10"][NUMBER]    = $row["ATTENDSUSPEND10"];
            $data["ATTENDSUSPEND11"][NUMBER]    = $row["ATTENDSUSPEND11"];
            $data["TOTALSUM01"][NUMBER]         = $row["TOTALSUM01"];
            $data["TOTALSUM02"][NUMBER]         = $row["TOTALSUM02"];
            $data["TOTALSUM03"][NUMBER]         = $row["TOTALSUM03"];
            $data["TOTALSUM04"][NUMBER]         = $row["TOTALSUM04"];
            $data["TOTALSUM04_PERCENT"][NUMBER] = $row["TOTALSUM04_PERCENT"];
            $data["TOTALSUM05"][NUMBER]         = $row["TOTALSUM05"];
            $data["TOTALSUM05_PERCENT"][NUMBER] = $row["TOTALSUM05_PERCENT"];
            $data["TOTALSUM06"][NUMBER]         = $row["TOTALSUM06"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION2_FIXED_DAT");
            $db2->query($query);

        }
        $result->free();

        //報告処理
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".CTRL_YEAR."' AND ";
        $where .= "     DOC_NUMBER = {$model->docNumber} AND ";
        $where .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSV
    function selectCsvQuery($model) {
        $query  = "";
        $query .= " WITH T_SCHREG (GRADE, HR_CLASS, SCHREGNO) AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "         AND T1.SEMESTER = '".$model->semester."' ";
        if ($model->field["GRADE"]) {
            $query .= "         AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= "         AND NOT EXISTS( ";
        $query .= "                        SELECT ";
        $query .= "                            'x' ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_TRANSFER_DAT E1 ";
        $query .= "                        WHERE ";
        $query .= "                                E1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                            AND E1.TRANSFERCD IN ('1', '2') ";
        $query .= "                            AND '".str_replace("/", "-", $model->field["ADDITION_DATE"])."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "                       ) ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     ) ";
        $query .= " , T_GRADE (GRADE, HR_CLASS, HR_NAME, TOTALSUM06) AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T3.HR_NAME, ";
        $query .= "         COUNT(*) ";
        $query .= "     FROM ";
        $query .= "         T_SCHREG T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "             ON  T3.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "             AND T3.SEMESTER = '".$model->semester."' ";
        $query .= "             AND T3.GRADE    = T1.GRADE ";
        $query .= "             AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     ) ";
        //登録テーブル
        $query .= " , T_ADDITION1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        if ($model->fixedData) {
            $query .= "         MEDEXAM_DISEASE_ADDITION2_FIXED_DAT ";
        } else {
            $query .= "         MEDEXAM_DISEASE_ADDITION2_DAT ";
        }
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND YEAR = '".$model->field["YEAR"]."' ";
        if ($model->fixedData) {
            $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "         AND ADDITION_DATE = '".str_replace("/", "-", $model->field["ADDITION_DATE"])."' ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     L1.ABSENCE01, ";
        $query .= "     L1.ABSENCE02, ";
        $query .= "     L1.ABSENCE03, ";
        $query .= "     L1.ABSENCE04, ";
        $query .= "     L1.ABSENCE05, ";
        $query .= "     L1.ABSENCE06, ";
        $query .= "     L1.ABSENCE07, ";
        $query .= "     L1.ABSENCE08, ";
        $query .= "     L1.ATTENDSUSPEND01, ";
        $query .= "     L1.ATTENDSUSPEND02, ";
        $query .= "     L1.ATTENDSUSPEND03, ";
        $query .= "     L1.ATTENDSUSPEND04, ";
        $query .= "     L1.ATTENDSUSPEND05, ";
        $query .= "     L1.ATTENDSUSPEND06, ";
        $query .= "     L1.ATTENDSUSPEND07, ";
        $query .= "     L1.ATTENDSUSPEND08, ";
        $query .= "     L1.ATTENDSUSPEND09, ";
        $query .= "     L1.ATTENDSUSPEND10, ";
        $query .= "     L1.ATTENDSUSPEND11, ";
        $query .= "     L1.TOTALSUM01, ";
        $query .= "     L1.TOTALSUM02, ";
        $query .= "     L1.TOTALSUM03, ";
        $query .= "     L1.TOTALSUM04, ";
        $query .= "     L1.TOTALSUM04_PERCENT, ";
        $query .= "     L1.TOTALSUM05, ";
        $query .= "     L1.TOTALSUM05_PERCENT, ";
        $query .= "     CASE WHEN L1.GRADE IS NOT NULL ";
        $query .= "          THEN L1.TOTALSUM06 ";
        $query .= "          ELSE T1.TOTALSUM06 ";
        $query .= "     END AS TOTALSUM06 ";
        $query .= " FROM ";
        $query .= "     T_GRADE T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.GRADE = T1.GRADE AND L1.HR_CLASS = T1.HR_CLASS ";
        } else {
            $query .= "     LEFT JOIN T_ADDITION1 L1 ON L1.GRADE = T1.GRADE AND L1.HR_CLASS = T1.HR_CLASS ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }
}
?>
