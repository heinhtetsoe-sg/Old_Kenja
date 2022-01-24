<?php

require_once('for_php7.php');


class knjp731Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    function getHrClassAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     L1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR     = L1.YEAR ";
            $query .= "                                  AND T1.SEMESTER = L1.SEMESTER ";
            $query .= "                                  AND T1.GRADE    = L1.GRADE ";
            $query .= "                                  AND T1.HR_CLASS = L1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR     = G1.YEAR ";
            $query .= "                                  AND T1.GRADE    = G1.GRADE ";
            $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON L_AUTH.SCHREGNO    = T1.SCHREGNO ";
            $query .= "                                             AND L_AUTH.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                                             AND L_AUTH.SCHOOL_KIND = G1.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR        = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER    = '".$model->field["SEMESTER"] ."' ";
            $query .= "     AND G1.SCHOOL_KIND = '".$model->schoolKind."' ";
            if ($model->field["UPDATE_DIV"] == "1") {
                $query .= "     AND L_AUTH.PASSNO IS NULL ";
            } else if ($model->field["UPDATE_DIV"] == "2") {
                $query .= "     AND L_AUTH.PASSNO IS NULL ";
                $query .= "     AND L_AUTH.RENBAN IS NULL ";
            } else if ($model->field["UPDATE_DIV"] == "3" && $model->field["OUTPUT_DATA"] == "1") {
                $query .= "     AND L_AUTH.PASSNO IS NOT NULL ";
            } else if ($model->field["UPDATE_DIV"] == "3" && $model->field["OUTPUT_DATA"] == "2") {
                $query .= "     AND L_AUTH.PASSNO IS NULL ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     L1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
            $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
            $query .= "          AND T1.GRADE = L1.GRADE ";
            $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR     = G1.YEAR ";
            $query .= "                                  AND T1.GRADE    = G1.GRADE ";
            $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON L_AUTH.SCHREGNO    = T1.SCHREGNO ";
            $query .= "                                             AND L_AUTH.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                                             AND L_AUTH.SCHOOL_KIND = G1.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR        = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER    = '".$model->field["SEMESTER"] ."' ";
            $query .= "     AND G1.SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "     AND (L1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR L1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR L1.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "          OR L1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR L1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR L1.SUBTR_CD3 = '" .STAFFCD ."') ";
            if ($model->field["UPDATE_DIV"] == "1") {
                $query .= "     AND L_AUTH.PASSNO IS NULL ";
            } else if ($model->field["UPDATE_DIV"] == "2") {
                $query .= "     AND L_AUTH.PASSNO IS NULL ";
                $query .= "     AND L_AUTH.RENBAN IS NULL ";
            } else if ($model->field["UPDATE_DIV"] == "3" && $model->field["OUTPUT_DATA"] == "1") {
                $query .= "     AND L_AUTH.PASSNO IS NOT NULL ";
            } else if ($model->field["UPDATE_DIV"] == "3" && $model->field["OUTPUT_DATA"] == "2") {
                $query .= "     AND L_AUTH.PASSNO IS NULL ";
            }
        }

        return $query;
    }

    // 学期
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒データ
    function getSchno($model, $gradeHr = "") {
        $query  = " SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW,T2.GRADE,T2.HR_CLASS,T3.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON L_AUTH.SCHREGNO    = T1.SCHREGNO ";
        $query .= "                                             AND L_AUTH.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                                             AND L_AUTH.SCHOOL_KIND = '{$model->schoolKind}', ";
        $query .= "     SCHREG_REGD_DAT T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($gradeHr) {
            $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$gradeHr."' ";
        } else {
            $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND T2.YEAR = T3.YEAR AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T2.GRADE = T3.GRADE AND T2.HR_CLASS = T3.HR_CLASS ";
        if ($model->field["UPDATE_DIV"] == "1") {
            $query .= "     AND L_AUTH.PASSNO IS NULL ";
        } else if ($model->field["UPDATE_DIV"] == "2") {
            $query .= "     AND L_AUTH.PASSNO IS NULL ";
            $query .= "     AND L_AUTH.RENBAN IS NULL ";
        } else if ($model->field["UPDATE_DIV"] == "3" && $model->field["OUTPUT_DATA"] == "1") {
            $query .= "     AND L_AUTH.PASSNO IS NOT NULL ";
        } else if ($model->field["UPDATE_DIV"] == "3" && $model->field["OUTPUT_DATA"] == "2") {
            $query .= "     AND L_AUTH.PASSNO IS NULL ";
        }
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //Z010取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //連番クリア
    function deleteSql($model, $schregno = "") {
        if ($schregno) {
            $query  = " DELETE FROM REDUCTION_AUTHORIZE_DAT ";
            $query .= " WHERE SCHREGNO    = '".$schregno."' ";
            $query .= "   AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "   AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "   AND PASSNO     IS NULL ";
            $query .= "     ";
        } else {
            $query  = " DELETE FROM REDUCTION_AUTHORIZE_DAT ";
            $query .= " WHERE SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "   AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "   AND PASSNO     IS NULL";
        }
        return $query;
    }

    //MAX連番
    function getMaxRenban($model, $div) {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(RENBAN), '0000') AS RENBAN ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        if ($div == 1) {
            $query .= "     AND SUBSTR(PASSNO, 1, 2) = '".substr(CTRL_YEAR, 2)."' ";
        } else {
            $query .= "     AND SUBSTR(PASSNO, 1, 2) = '".substr(CTRL_YEAR, 2)."' ";
            $query .= "     OR (PASSNO IS NULL AND RENBAN IS NOT NULL) ";
        }

        return $query;
    }

    //認定番号テーブル更新
    function reductionAuthorizeDatIns($model, $schregno, $setRendban) {
        $data = array();
        $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["RENBAN"][TEXT]       = sprintf('%04d', $setRendban);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_AUTHORIZE_DAT");

        return $query;
    }

    //ＣＳＶ出力
    function selectCsvData($model, $schregno) {
        $query  = " SELECT ";
        if ($this->field["UPDATE_DIV"] == 3) {
            $query .= "     L_AUTH.RENBAN, ";
        } else {
            $query .= "     '' AS RENBAN, ";
        }
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     ADDR.ADDR, ";
        $query .= "     case when CHECK.INTENTION_YES_FLG = '1' then '' ";
        $query .= "          when CHECK.INTENTION_NO_FLG  = '1' then '1' ";
        $query .= "     end AS INTENTION_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             L1.SCHREGNO, ";
        $query .= "             VALUE(L1.ZIPCD, '') || VALUE(L1.ADDR1, '') || VALUE(L1.ADDR2, '') AS ADDR ";
        $query .= "         FROM ";
        $query .= "             SCHREG_ADDRESS_DAT L1, ";
        $query .= "             (SELECT ";
        $query .= "                  E1.SCHREGNO, ";
        $query .= "                  MAX(E1.ISSUEDATE) AS ISSUEDATE ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_ADDRESS_DAT E1 ";
        $query .= "              WHERE ";
        $query .= "                  E1.SCHREGNO = '".$schregno."' ";
        $query .= "              GROUP BY ";
        $query .= "                  E1.SCHREGNO ";
        $query .= "             ) L2 ";
        $query .= "         WHERE ";
        $query .= "             L1.SCHREGNO = '".$schregno."' ";
        $query .= "             AND L1.SCHREGNO = L2.SCHREGNO ";
        $query .= "             AND L1.ISSUEDATE = L2.ISSUEDATE ";
        $query .= "     ) ADDR ON BASE.SCHREGNO = ADDR.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                   AND REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON L_AUTH.SCHREGNO    = BASE.SCHREGNO ";
        $query .= "                                             AND L_AUTH.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                                             AND L_AUTH.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_APPLICANT_CHECK_DAT CHECK ON CHECK.SCHOOLCD    = L_AUTH.SCHOOLCD ";
        $query .= "                                                          AND CHECK.SCHOOL_KIND = L_AUTH.SCHOOL_KIND ";
        $query .= "                                                          AND CHECK.YEAR        = REGD.YEAR ";
        $query .= "                                                          AND CHECK.MONTH       = '{$model->month}' ";
        $query .= "                                                          AND CHECK.SCHREGNO    = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '".$schregno."' ";

        return $query;
    }
}
?>
