<?php

require_once('for_php7.php');

class knjd106bQuery extends Query {

    //生徒情報取得
    function getStudentInfo($schregno)
    {

        $query  = " SELECT ";
        $query .= "     T2.HR_NAME || T1.ATTENDNO || '番' AS GHA ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";

        return $query ;
    }

    //テスト種別取得
    function getTest($model) 
    {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
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
        $query .= "     HEXAM_MOCK_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND MOCKCD = '".$model->field["TESTCD"]."' ";
        $query .= "     AND REMARK_DIV = '2' ";

        return $query;
    }


    //更新処理
    function &getInsertQuery($fields, $schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HEXAM_MOCK_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND MOCKCD = '".$fields["TESTCD"]."' ";
        $query .= "     AND REMARK_DIV = '2' ";

        $num = $db->getOne($query);

        $data["REMARK1"][TEXT]          = $fields["TOTALSTUDYTIME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($num > "0") {
            $where = " WHERE ";
            $where.= "     YEAR = '".CTRL_YEAR."' ";
            $where.= "     AND SCHREGNO = '".$schregno."' ";
            $where.= "     AND MOCKCD = '".$fields["TESTCD"]."' ";
            $where.= "     AND REMARK_DIV = '2' ";

            $db->query(Query::updateSQL($data, "HEXAM_MOCK_REMARK_DAT", $where));

        } else {  //データ無し

            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["MOCKCD"][TEXT]       = $fields["TESTCD"];
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["REMARK_DIV"][TEXT]   = "2";

            $db->query(Query::insertSQL($data, "HEXAM_MOCK_REMARK_DAT"));

        }

        Query::dbCheckIn($db);
        return ;
    }

}
?>
