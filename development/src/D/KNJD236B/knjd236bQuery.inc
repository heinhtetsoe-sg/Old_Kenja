<?php

require_once('for_php7.php');

class knjd236bQuery extends Query {

    //生徒情報取得
    function getStudentInfo($schregno)
    {
        $query  = " SELECT ";
        $query .= "     T2.HR_NAME || T1.ATTENDNO || '番' AS GHA ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query ;
    }

    //学期取得
    function getSemesterMst() 
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRecordRemark($model)
    {

        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '9900' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     REMARK_DIV = '3' ";

        return $query;
    }


    //更新処理
    function &getInsertQuery($fields, $schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$fields["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '9900' AND ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     REMARK_DIV = '3' ";

        $num = $db->getOne($query);

        $data["REMARK1"][TEXT]          = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($num > "0") {
            $where = " WHERE ";
            $where.= "     YEAR = '".CTRL_YEAR."' AND ";
            $where.= "     SEMESTER = '".$fields["SEMESTER"]."' AND ";
            $where.= "     TESTKINDCD || TESTITEMCD = '9900' AND ";
            $where.= "     SCHREGNO = '".$schregno."' AND ";
            $where.= "     REMARK_DIV = '3' ";

            $db->query(Query::updateSQL($data, "HEXAM_RECORD_REMARK_DAT", $where));

        } else {  //データ無し

            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $fields["SEMESTER"];
            $data["TESTKINDCD"][TEXT]   = "99";
            $data["TESTITEMCD"][TEXT]   = "00";
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["REMARK_DIV"][TEXT]   = "3";

            $db->query(Query::insertSQL($data, "HEXAM_RECORD_REMARK_DAT"));

        }

        Query::dbCheckIn($db);
        return ;
    }

}
?>
