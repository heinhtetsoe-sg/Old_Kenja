<?php

require_once('for_php7.php');

class knjd130bQuery extends Query {

    //学期コンボ取得
    function getSemesterQuery()
    {
        $query  = "SELECT SEMESTER, SEMESTERNAME ";
        $query .= "  FROM SEMESTER_MST ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";
        $query .= "ORDER BY SEMESTER ";

        return $query;
    }

    //１レコード取得
    function getRow($schregno, $semester)
    {
        $db = Query::dbCheckOut();

        //出欠備考
        $query  = " SELECT schregno, COMMUNICATION ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '".$semester."' ";//NO001
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        
        //備考
        $query  = " SELECT schregno, SPECIALACTREMARK ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '9' ";//NO001
        $query .= "    AND schregno = '".$schregno."'";

        $row1 = $db->getRow($query,DB_FETCHMODE_ASSOC);
        
        $row["SPECIALACTREMARK"] = $row1["SPECIALACTREMARK"];
        
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno)
    {
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

        $data_check  = "select COUNT(*) from hreportremark_dat ";
        $data_check .= "where year = '".CTRL_YEAR."' and semester = '9' and schregno = '".$schregno."' ";
        $num  = $db->getOne($data_check);

        //備考
        if($num > "0" ){    //データあり

            $data["SPECIALACTREMARK"][TEXT]    = $fields["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            $where = "where year = '".CTRL_YEAR."' and semester = '9' and schregno = '".$schregno."'";
            $db->query(Query::updateSQL($data, "hreportremark_dat", $where));

        } else {  //データ無し

            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = "9";//NO001
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["SPECIALACTREMARK"][TEXT]    = $fields["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            $db->query(Query::insertSQL($data, "hreportremark_dat"));
        }

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno, $semester)
   {
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
