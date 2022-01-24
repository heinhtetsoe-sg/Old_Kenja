<?php

require_once('for_php7.php');

class knjd130fQuery extends Query {
    //学期コンボ取得
    function getSemesterQuery() {
        $query  = "SELECT SEMESTER, SEMESTERNAME ";
        $query .= "  FROM SEMESTER_MST ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";
        $query .= "ORDER BY SEMESTER ";

        return $query;
    }

    //１レコード取得
    function getRow($schregno, $semester) {
        $db = Query::dbCheckOut();

        //出欠備考
        $query  = " SELECT schregno, COMMUNICATION ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '".$semester."' ";
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
        return $row;
    }

    //まとめ出欠備考取得
    function getAttendSemesRemarkDat($model) {
        $query  = "  SELECT ";
        $query .= "      MONTH, ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno) {
        $db = Query::dbCheckOut();

        //出欠備考
        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = $fields["SEMESTER"];//NO001
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        $data = array();
        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno, $semester) {
        $db = Query::dbCheckOut();

        //出欠備考
        $query  = " DELETE FROM hreportremark_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '".$semester."'";//NO001
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);
        
        Query::dbCheckIn($db);
        return $result;
   }
}
?>
