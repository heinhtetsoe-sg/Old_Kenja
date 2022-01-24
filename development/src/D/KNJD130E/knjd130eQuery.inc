<?php

require_once('for_php7.php');

class knjd130eQuery extends Query {
    //１レコード取得
    function getTrainRow($schregno, $db) {

        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     SPECIALACTREMARK, ";
        $query .= "     COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."'";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = '9';
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["TOTALSTUDYTIME"][TEXT]   = $fields["TOTALSTUDYTIME"];
        $data["SPECIALACTREMARK"][TEXT] = $fields["SPECIALACTREMARK"];
        $data["COMMUNICATION"][TEXT]    = $fields["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM HREPORTREMARK_DAT ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '9'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

    //INSERT(一括更新)
    function &getInsertQuery2($field, $selectdata) {
        $db = Query::dbCheckOut();

        $schregArray = explode(",", $selectdata);
        foreach ($schregArray as $schregno) {
            $query = knjd130eQuery::checkExists($schregno);
            $cnt = $db->getOne($query);

            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = 9;
            $data["SCHREGNO"][TEXT]            = $schregno;
            if ($field["RCHECK1"] == "on") {
                $data["TOTALSTUDYTIME"][TEXT]   = $field["TOTALSTUDYTIME"];
            }
            if ($field["RCHECK2"] == "on") {
                $data["SPECIALACTREMARK"][TEXT]  = $field["SPECIALACTREMARK"];
            }
            if ($field["RCHECK3"] == "on") {
                $data["COMMUNICATION"][TEXT]  = $field["COMMUNICATION"];
            }

            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            if ($cnt > 0) {
                $where  = " WHERE YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "       SEMESTER = '9' AND ";
                $where .= "       SCHREGNO = '{$schregno}' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT",$where);
            } else {
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return $result;
    }

    //レコードがあるかのチェック(インサートにするかアップデートにするか)
    function checkExists($schregno) {
         $query .= " SELECT ";
         $query .= "     COUNT(*) ";
         $query .= " FROM ";
         $query .= "     HREPORTREMARK_DAT ";
         $query .= " WHERE ";
         $query .= "     YEAR = '".CTRL_YEAR."' AND ";
         $query .= "     SEMESTER = '9' AND ";
         $query .= "     SCHREGNO = '{$schregno}' ";

         return $query;
    }

/******************************************************************************************/
/******************************************************************************************/
/*******                    ***************************************************************/
/******* 一括更新の生徒一覧 ***************************************************************/
/*******                    ***************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDNO || ' ' || T1.SCHREGNO || ' ' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->grade_hrclass}' ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }
}
?>
