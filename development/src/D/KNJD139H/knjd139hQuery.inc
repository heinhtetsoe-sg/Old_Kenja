<?php

require_once('for_php7.php');

class knjd139hQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getHreportremarkDat($schregno, $semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    function &getInsertUpdateQuery($model, $schregno, $semester) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd139hQuery::getHreportremarkDat($schregno, $semester));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SEMESTER"][TEXT]             = $semester;
            $data["SCHREGNO"][TEXT]             = $schregno;

            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER = '".$semester."' AND ";
            $where .= "     SCHREGNO = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
