<?php

require_once('for_php7.php');


class knjp182kQuery extends Query {

    function getYMdata() {
        $konNendo = CTRL_YEAR;
        $jiNendo = CTRL_YEAR + 1;
        $query .= " WITH YM_DATA(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('{$konNendo}/04/01', '{$konNendo}/04/01:1') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/05/01', '{$konNendo}/05/01:1') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/06/01', '{$konNendo}/06/01:1') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/07/01', '{$konNendo}/07/01:2') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/08/01', '{$konNendo}/08/01:2') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/09/01', '{$konNendo}/09/01:2') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/10/01', '{$konNendo}/10/01:2') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/11/01', '{$konNendo}/11/01:2') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$konNendo}/12/01', '{$konNendo}/12/01:2') ";
        $query .= "     UNION ";
        $query .= "     VALUES('{$jiNendo}/01/01', '{$jiNendo}/01/01:2') ";
        $query .= "     UNION ";                    
        $query .= "     VALUES('{$jiNendo}/02/01', '{$jiNendo}/02/01:2') ";
        $query .= "     UNION ";                    
        $query .= "     VALUES('{$jiNendo}/03/01', '{$jiNendo}/03/01:2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     YM_DATA ";
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

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
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"] ."' ";
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             REDUCTION_AUTHORIZE_DAT E1 ";
            $query .= "         WHERE ";
            $query .= "             E1.DATA_DIV = '2' ";
            $query .= "             AND E1.DATA_DIV_SUB = '".$model->field["KASAN_DIV"]."' ";
            if ($model->field["UPDATE_DIV"] == "1") {
                $query .= "             AND VALUE(E1.STATUS, '01') != '01' ";
            } else {
                $query .= "             AND E1.STATUS IS NOT NULL ";
            }
            $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "     ) ";
            $query .= "     AND EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             REDUCTION_COUNTRY_DAT E2 ";
            $query .= "         WHERE ";
            $query .= "             E2.REDUCTION_ADD_MONEY_".$model->field["KASAN_DIV"]." IS NOT NULL ";
            $query .= "             AND VALUE(E2.REDUC_ADD_FLG_".$model->field["KASAN_DIV"].", '0') = '0' ";
            $query .= "             AND T1.SCHREGNO = E2.SCHREGNO ";
            $query .= "     ) ";
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
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"] ."' ";
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             REDUCTION_AUTHORIZE_DAT E1 ";
            $query .= "         WHERE ";
            $query .= "             E1.DATA_DIV = '2' ";
            $query .= "             AND E1.DATA_DIV_SUB = '".$model->field["KASAN_DIV"]."' ";
            if ($model->field["UPDATE_DIV"] == "1") {
                $query .= "             AND VALUE(E1.STATUS, '01') != '01' ";
            } else {
                $query .= "             AND E1.STATUS IS NOT NULL ";
            }
            $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "     ) ";
            $query .= "     AND EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             REDUCTION_COUNTRY_DAT E2 ";
            $query .= "         WHERE ";
            $query .= "             E2.REDUCTION_ADD_MONEY_".$model->field["KASAN_DIV"]." IS NOT NULL ";
            $query .= "             AND VALUE(E2.REDUC_ADD_FLG_".$model->field["KASAN_DIV"].", '0') = '0' ";
            $query .= "             AND T1.SCHREGNO = E2.SCHREGNO ";
            $query .= "     ) ";
            $query .= "     AND (L1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR L1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR L1.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "          OR L1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR L1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR L1.SUBTR_CD3 = '" .STAFFCD ."') ";
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
        $query .= " FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2,SCHREG_REGD_HDAT T3 ";
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
        $query .= "     AND NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REDUCTION_AUTHORIZE_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.DATA_DIV = '2' ";
        $query .= "             AND E1.DATA_DIV_SUB = '".$model->field["KASAN_DIV"]."' ";
        if ($model->field["UPDATE_DIV"] == "1") {
            $query .= "             AND VALUE(E1.STATUS, '01') != '01' ";
        } else {
            $query .= "             AND E1.STATUS IS NOT NULL ";
        }
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REDUCTION_COUNTRY_DAT E2 ";
        $query .= "         WHERE ";
        $query .= "             E2.REDUCTION_ADD_MONEY_".$model->field["KASAN_DIV"]." IS NOT NULL ";
        $query .= "             AND VALUE(E2.REDUC_ADD_FLG_".$model->field["KASAN_DIV"].", '0') = '0' ";
        $query .= "             AND T1.SCHREGNO = E2.SCHREGNO ";
        $query .= "     ) ";
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //連番クリア
    function deleteSql($model, $schregno = "")
    {
        if ($schregno) {
            $query = "DELETE FROM REDUCTION_AUTHORIZE_DAT WHERE SCHREGNO = '".$schregno."' AND DATA_DIV = '2' AND DATA_DIV_SUB = '".$model->field["KASAN_DIV"]."'";
        } else {
            $query = "DELETE FROM REDUCTION_AUTHORIZE_DAT WHERE DATA_DIV = '2' AND DATA_DIV_SUB = '".$model->field["KASAN_DIV"]."' AND VALUE(STATUS, '01') = '01'";
        }
        return $query;
    }

    //認定番号
    function getPassNo($schregno)
    {
        $query  = " SELECT ";
        $query .= "     PASSNO ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //認定番号テーブル更新
    function reductionAuthorizeDatIns($schregno, $model, $passNo)
    {
        list($nen, $ken, $school, $ren) = preg_split("/-/", $passNo);
        $data = array();
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DATA_DIV"][TEXT]     = "2";
        $data["DATA_DIV_SUB"][TEXT] = $model->field["KASAN_DIV"];
        $data["PASSNO"][TEXT]       = $passNo;
        $data["RENBAN"][TEXT]       = $ren;
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
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     L1.PASSNO, ";
        $query .= "     '' AS S_YM, ";
        $query .= "     RED_C.REDUCTION_ADD_MONEY_".$model->field["KASAN_DIV"]." AS ADDMONEY, ";
        $query .= "     RED_C.REDUC_INCOME_".$model->field["KASAN_DIV"]." AS INCOME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "          AND REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L1 ON BASE.SCHREGNO = L1.SCHREGNO ";
        $query .= "          AND L1.DATA_DIV = '2' ";
        $query .= "          AND L1.DATA_DIV_SUB = '".$model->field["KASAN_DIV"]."' ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT RED_C ON RED_C.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND BASE.SCHREGNO = RED_C.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '".$schregno."' ";

        return $query;
    }
}
?>
