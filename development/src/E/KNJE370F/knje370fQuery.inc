<?php

require_once('for_php7.php');

class knje370fQuery extends Query {

    //校種取得
    function getSchoolkind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //１レコード取得
    function getSchChurchRemarkDat($schregno) {
        $query = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "    SCHREG_CHURCH_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    function &getInsertUpdateQuery($model, $schregno) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knje370fQuery::getSchChurchRemarkDat($schregno));
        if(get_count($check) == 0) {
            $data["SCHREGNO"][TEXT]             = $schregno;

            $data["CHURCH_NAME"][TEXT]          = $model->field["CHURCH_NAME"];
            $data["BAPTISM_DAY"][TEXT]          = str_replace("/", "-", $model->field["BAPTISM_DAY"]);
            $data["HOUSHI_TOU"][TEXT]           = $model->field["HOUSHI_TOU"];
            $data["REMARK"][TEXT]               = $model->field["REMARK"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $db->query(Query::insertSQL($data, "SCHREG_CHURCH_REMARK_DAT"));
        } else {
            $data["CHURCH_NAME"][TEXT]          = $model->field["CHURCH_NAME"];
            $data["BAPTISM_DAY"][TEXT]          = str_replace("/", "-", $model->field["BAPTISM_DAY"]);
            $data["HOUSHI_TOU"][TEXT]           = $model->field["HOUSHI_TOU"];
            $data["REMARK"][TEXT]               = $model->field["REMARK"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$schregno."' ";
            $db->query(Query::updateSQL($data, "SCHREG_CHURCH_REMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
