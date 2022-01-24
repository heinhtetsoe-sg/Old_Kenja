<?php

require_once('for_php7.php');

class knjd131kQuery extends Query {
    //学年チェック
    function checkGrade($model) {
        $query  = "SELECT ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = 'A034' ";
        $query .= "AND NAME1 <> 'M' ";
        $query .= "AND '".$model->grade."' BETWEEN NAME2 AND NAME3 ";

        return $query;
    }

    //学年取得
    function getGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    L1.GRADE AS VALUE, ";
        $query .= "    L1.GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                 AND L1.GRADE = T1.GRADE, ";
        $query .= "    NAME_MST N1 ";
        $query .= "WHERE ";
        //$query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "AND N1.NAMECD1 = 'A034' ";
        $query .= "AND N1.NAME1 <> 'M' ";
        $query .= "AND T1.GRADE BETWEEN N1.NAME2 AND N1.NAME3 ";
        return $query;
    }

    //学年に対応した年度を取得
    function getGradeYear($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.YEAR ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "AND T1.GRADE    = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model, $schregno, $getYear) {
        $query  = "  SELECT ";
        $query .= "      SCHREGNO, ";
        $query .= "      REMARK4 AS DIV4_REMARK ";
        $query .= "  FROM ";
        $query .= "      HREPORTREMARK_DETAIL_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$getYear."' AND ";
        $query .= "      SEMESTER = '9' AND ";
        $query .= "      DIV = '04' AND ";
        $query .= "      CODE = '01' AND ";
        $query .= "      SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //データチェック
    function getDataCount($semester, $schregno, $div)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    HREPORTREMARK_DETAIL_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER = '".$semester."' ";
        $query .= "AND SCHREGNO = '".$schregno."' ";
        $query .= "AND DIV = '".$div."' ";
        $query .= "AND CODE = '01' ";

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    //INSERT
    function &getInsertQuery($fields, $schregno) {
        $db = Query::dbCheckOut();

        //総合的な学習の時間
        $div1_data = $db->getOne(knjd131kQuery::getDataCount("9", $schregno, "04"));
        $data = array();
        $data["REMARK4"][TEXT]             = $fields["DIV4_REMARK"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";
        if ($div1_data == 0) {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = 9;
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["DIV"][TEXT]                 = "04";
            $data["CODE"][TEXT]                = "01";
            
            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $where  = "WHERE ";
            $where .= "    YEAR = '".CTRL_YEAR."' ";
            $where .= "AND SEMESTER = '9' ";
            $where .= "AND SCHREGNO = '".$schregno."' ";
            $where .= "AND DIV  = '04' ";
            $where .= "AND CODE = '01' ";
            
            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }


        Query::dbCheckIn($db);
        return ;
    }
   
}
?>
