<?php

require_once('for_php7.php');

class knjc172Query extends Query {
    //年組コンボ
    function getHrClass() {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //更新対象生徒（右）取得
    function getSchregSchoolRefusal($model)  {
        $query  = " SELECT DISTINCT ";
        $query .= "     RFS.SCHREGNO ";
        $query .= "     , REGD.ATTENDNO ";
        $query .= "     , BASE.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOOL_REFUSAL_DAT RFS ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "         ON REGD.YEAR = RFS.YEAR ";
        $query .= "         AND REGD.SCHREGNO = RFS.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = RFS.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     RFS.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";

        return $query;
    }

    //生徒一覧（右）取得
    function getStudent($model, $leftSchregnoList) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.SCHREGNO";
        $query .= "     , REGD.ATTENDNO ";
        $query .= "     , BASE.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND REGD.SCHREGNO NOT IN ('".implode("','", $leftSchregnoList)."') ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }

    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //指定年組の生徒を削除
        $query  = " DELETE FROM SCHREG_SCHOOL_REFUSAL_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."') ";
        $db->query($query);

        if ($model->selected[0] != "") {
            foreach ($model->selected as $schregno) {
                //INSERT
                $data = array();
                $data["YEAR"][TEXT]               = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]           = $schregno;
                $data["REGISTERCD"][TEXT]         = STAFFCD;
                $data["UPDATED"][NUMBER]          = "SYSDATE()";

                $query = Query::insertSQL($data, "SCHREG_SCHOOL_REFUSAL_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
