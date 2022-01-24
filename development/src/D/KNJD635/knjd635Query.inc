<?php

require_once('for_php7.php');

class knjd635Query extends Query {

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
    function getBehavior($schregno, $semester)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT schregno,code,record ";
        $query .= "   FROM BEHAVIOR_SEMES_DAT ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '".$semester."' ";
        $query .= "    AND schregno = '".$schregno."'";
		$query .= " ORDER BY CODE ";

        return $query;
    }


    //INSERT
    function &getInsertQuery($model, $schregno, $semester)
    {
        $db = Query::dbCheckOut();

        //行動の記録
        for($i=1; $i<11; $i++)
        {

            if ($model->record["RECORD"][$i] == "") continue;

            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $semester;
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["CODE"][TEXT]         = sprintf("%02d", $i);
            $data["RECORD"][TEXT]       = $model->record["RECORD"][$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "BEHAVIOR_SEMES_DAT"));
        }
        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno, $semester)
   {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '".$semester."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
