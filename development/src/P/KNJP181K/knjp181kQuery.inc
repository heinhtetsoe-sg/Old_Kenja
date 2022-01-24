<?php

require_once('for_php7.php');


class knjp181kQuery extends Query {

    function getHrClassAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     L1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
            $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
            $query .= "          AND T1.GRADE = L1.GRADE ";
            $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
            $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON T1.SCHREGNO = L_AUTH.SCHREGNO ";
            $query .= "             AND L_AUTH.DATA_DIV = '1' ";
            $query .= "             AND L_AUTH.DATA_DIV_SUB = '1' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"] ."' ";
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
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     L1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
            $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
            $query .= "          AND T1.GRADE = L1.GRADE ";
            $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
            $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON T1.SCHREGNO = L_AUTH.SCHREGNO ";
            $query .= "             AND L_AUTH.DATA_DIV = '1' ";
            $query .= "             AND L_AUTH.DATA_DIV_SUB = '1' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"] ."' ";
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
    function getSchno($model, $gradeHr = "")
    {
        $query  = " SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW,T2.GRADE,T2.HR_CLASS,T3.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON T1.SCHREGNO = L_AUTH.SCHREGNO ";
        $query .= "             AND L_AUTH.DATA_DIV = '1' ";
        $query .= "             AND L_AUTH.DATA_DIV_SUB = '1', ";
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

    //連番クリア
    function deleteSql($model, $schregno = "")
    {
        if ($schregno) {
            $query = "DELETE FROM REDUCTION_AUTHORIZE_DAT WHERE SCHREGNO = '".$schregno."' AND DATA_DIV = '1' AND DATA_DIV_SUB = '1' AND PASSNO IS NULL";
        } else {
            $query = "DELETE FROM REDUCTION_AUTHORIZE_DAT WHERE DATA_DIV = '1' AND DATA_DIV_SUB = '1' AND PASSNO IS NULL";
        }
        return $query;
    }

    //MAX連番
    function getMaxRenban($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(RENBAN), '0000') AS RENBAN ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        if ($div == 1) {
            $query .= "     SUBSTR(PASSNO, 1, 2) = '".substr(CTRL_YEAR, 2)."' ";
        } else {
            $query .= "     SUBSTR(PASSNO, 1, 2) = '".substr(CTRL_YEAR, 2)."' ";
            $query .= "     OR (PASSNO IS NULL AND RENBAN IS NOT NULL) ";
        }

        return $query;
    }

    //認定番号テーブル更新
    function reductionAuthorizeDatIns($schregno, $setRendban)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DATA_DIV"][TEXT]     = "1";
        $data["DATA_DIV_SUB"][TEXT] = "1";
        $data["RENBAN"][TEXT]       = sprintf('%04d', $setRendban);
        $data["STATUS"][TEXT]       = "01";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_AUTHORIZE_DAT");

        return $query;
    }

    //ＣＳＶ出力
    function selectCsvData($model, $schregno)
    {
        $query  = " SELECT ";
        if ($this->field["UPDATE_DIV"] == 3) {
            $query .= "     L_AUTH.RENBAN, ";
        } else {
            $query .= "     '' AS RENBAN, ";
        }
        $query .= "     REPLACE(BASE.NAME, ' ', '　') AS NAME, ";
        $query .= "     '' AS NAME_MEI, ";
        $query .= "     REPLACE(BASE.NAME_KANA, ' ', '　') AS NAME_KANA, ";
        $query .= "     '' AS KANA_MEI, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     ADDR.ADDR, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     DUE11.MONEY_DUE AS MONEY_DUE11, ";
        $query .= "     DUE12.MONEY_DUE AS MONEY_DUE12, ";
        $query .= "     DUE13.MONEY_DUE AS MONEY_DUE13 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             L1.SCHREGNO, ";
        $query .= "             VALUE(L1.ADDR1, '') || VALUE(L1.ADDR2, '') AS ADDR ";
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
        $query .= "          AND REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT DUE11 ON DUE11.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND BASE.SCHREGNO = DUE11.SCHREGNO ";
        $query .= "          AND DUE11.EXPENSE_M_CD = '11' ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT DUE12 ON DUE12.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND BASE.SCHREGNO = DUE12.SCHREGNO ";
        $query .= "          AND DUE12.EXPENSE_M_CD = '12' ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT DUE13 ON DUE13.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND BASE.SCHREGNO = DUE13.SCHREGNO ";
        $query .= "          AND DUE13.EXPENSE_M_CD = '13' ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON BASE.SCHREGNO = L_AUTH.SCHREGNO ";
        $query .= "             AND L_AUTH.DATA_DIV = '1' ";
        $query .= "             AND L_AUTH.DATA_DIV_SUB = '1' ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '".$schregno."' ";

        return $query;
    }
}
?>
