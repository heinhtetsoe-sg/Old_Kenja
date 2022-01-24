<?php

require_once('for_php7.php');

class knje070hQuery extends Query
{

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      TR_CD3     = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年コンボ
    public function getGradename($model)
    {

        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND GRADE IN ( SELECT ";
            $query .= "       GRADE ";
            $query .= "   FROM ";
            $query .= "       SCHREG_REGD_HDAT ";
            $query .= "   WHERE ";
            $query .= "       YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "       SEMESTER    = '".CTRL_SEMESTER."' AND ";
            $query .= "       (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "        TR_CD2     = '".STAFFCD."' OR ";
            $query .= "        TR_CD3     = '".STAFFCD."' OR ";
            $query .= "        SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "        SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "        SUBTR_CD3  = '".STAFFCD."') ";
            $query .= " ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //生徒一覧
    public function getList($model, $selectdata = array())
    {
        if ($model->field["OUTPUT"] == "1") {
            $query  = " SELECT ";
            $query .= "     T2.GRADE || '-' || T2.HR_CLASS || '-' || T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
            $query .= "     T2.SCHREGNO, ";
            $query .= "     T3.HR_NAME, ";
            $query .= "     T2.ATTENDNO, ";
            $query .= "     T1.NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR     = T2.YEAR ";
            $query .= "                                  AND T3.SEMESTER = T2.SEMESTER ";
            $query .= "                                  AND T3.GRADE    = T2.GRADE ";
            $query .= "                                  AND T3.HR_CLASS = T2.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
            if ($selectdata) {
                $query .= "     T1.SCHREGNO IN ('".implode("','", $selectdata)."') ";
            } else {
                $query .= "     T2.GRADE || T2.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } elseif ($model->field["OUTPUT"] == "2") {
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
            if ($selectdata) {
                $query .= "     AND GRADE || HR_CLASS IN ('".implode("','", $selectdata)."') ";
            } else {
                $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (TR_CD1     = '".STAFFCD."' OR ";
                $query .= "      TR_CD2     = '".STAFFCD."' OR ";
                $query .= "      TR_CD3     = '".STAFFCD."' OR ";
                $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "      SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }
}
