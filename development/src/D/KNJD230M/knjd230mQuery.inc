<?php

require_once('for_php7.php');

class knjd230mQuery extends Query {
    //特別事情区分
    function getSpecialReasonDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'D019' ";
        $query .= "     AND NAMECD2 <> '08' ";
        $query .= "     AND NAMECD2 <> '09' ";

        return $query;
    }


    //年組取得(権限チェック)
    function getHrclass() {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     HR_NAME AS LABEL, ";
            $query .= "     GRADE || '_' || HR_CLASS AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE   YEAR     = '". CTRL_YEAR ."' ";
            $query .= "     AND SEMESTER = '". CTRL_SEMESTER ."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     HR_NAME AS LABEL, ";
            $query .= "     GRADE || '_' || HR_CLASS AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE   YEAR     = '". CTRL_YEAR ."' ";
            $query .= "     AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "     AND (TR_CD1  = '". STAFFCD ."' ";
            $query .= "       OR TR_CD2  = '". STAFFCD ."' ";
            $query .= "       OR TR_CD3  = '". STAFFCD ."') ";
        }

        return $query;
    }

    //リスト取得
    function getList($model) {
        list($grade, $hr_class) = explode("_", $model->field["HR_CLASS"]);

        $query  = " WITH SCHREG AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "     MAX(L1.NAME_SHOW) AS NAME_SHOW, ";
        $query .= "     MAX(T1.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND T1.GRADE = '{$grade}' ";
        $query .= "     AND T1.HR_CLASS = '{$hr_class}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.YEAR, ";
        $query .= "     L1.SPECIAL_REASON_DIV, ";
        $query .= "     L1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SPECIAL_REASON_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                           AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                           AND L1.SPECIAL_REASON_DIV = '{$model->field["SPECIAL_REASON_DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //アップデートかインサートかのチェックのためのクエリ
    function getSpecialReasonDat($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SPECIAL_REASON_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     SPECIAL_REASON_DIV = '{$model->field["SPECIAL_REASON_DIV"]}' ";

        return $query;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        list($grade, $hr_class) = explode("_", $model->field["HR_CLASS"]);

        $query  = " DELETE FROM ";
        $query .= "     SPECIAL_REASON_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SPECIAL_REASON_DIV = '{$model->field["SPECIAL_REASON_DIV"]}' AND ";
        $query .= "     SCHREGNO IN (SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "                     GRADE = '{$grade}' AND ";
        $query .= "                     HR_CLASS = '{$hr_class}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 ) ";

        $db->query($query);

        if (strlen($model->field["SCHREGNO"])) {
            $schregnoArray = explode(",", $model->field["SCHREGNO"]); //学籍番号がカンマ区切りでくるので、配列に入れてまわす
        } else {
            $schregnoArray = array();
        }

        foreach ($schregnoArray as $schregno) {
            $data["YEAR"][TEXT]               = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]           = $schregno;
            $data["SPECIAL_REASON_DIV"][TEXT] = $model->field["SPECIAL_REASON_DIV"];
            $data["REMARK"][TEXT]             = $model->field["REMARK_{$schregno}"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "SPECIAL_REASON_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
