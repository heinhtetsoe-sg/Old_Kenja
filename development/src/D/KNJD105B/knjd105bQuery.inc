<?php

require_once('for_php7.php');

class knjd105bQuery extends Query {

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

    //学期
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //テスト種別取得
    function getTest($model) 
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model)
    {

        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     REMARK1 AS TOTALSTUDYTIME ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '".$model->field["TESTCD"]."' AND ";
        $query .= "     REMARK_DIV = '2' ";

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
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '".$fields["TESTCD"]."' AND ";
        $query .= "     REMARK_DIV = '2' ";

        $num  = $db->getOne($query);

        if($num > "0" ){    //データあり

            $data["REMARK1"][TEXT]          = $fields["TOTALSTUDYTIME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where = " WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".$fields["SEMESTER"]."' AND SCHREGNO = '".$schregno."' AND ";
            $where.= "       TESTKINDCD || TESTITEMCD = '".$fields["TESTCD"]."' AND REMARK_DIV = '2' ";
            $db->query(Query::updateSQL($data, "HEXAM_RECORD_REMARK_DAT", $where));

        } else {  //データ無し

            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = $fields["SEMESTER"];
            $data["TESTKINDCD"][TEXT]          = substr($fields["TESTCD"],0,2);
            $data["TESTITEMCD"][TEXT]          = substr($fields["TESTCD"],2);
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["REMARK_DIV"][TEXT]          = "2";
            $data["REMARK1"][TEXT]             = $fields["TOTALSTUDYTIME"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            $db->query(Query::insertSQL($data, "HEXAM_RECORD_REMARK_DAT"));

        }

        Query::dbCheckIn($db);
        return ;
    }

}
?>
