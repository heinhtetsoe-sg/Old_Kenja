<?php

require_once('for_php7.php');

class knjm502Query extends Query {
    //１レコード取得
    function getTrainRow($model, $schregno) {
        $query  = "  SELECT ";
        $query .= "      COMMUNICATION ";
        $query .= "  FROM ";
        $query .= "      HREPORTREMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "      SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //一括更新の生徒一覧
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

    function getCommunication($field, $schregno) {
        $query .= " SELECT ";
        $query .= "     COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

   //レコードがあるかのチェック(インサートにするかアップデートにするか)
   function checkExists($field, $schregno) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
   }

    /********/
    /* 更新 */
    /********/
    //INSERT
    function &getInsertQuery($fields, $schregno) {
        $db = Query::dbCheckOut();

        //通信欄
        $data2["YEAR"][TEXT]                = CTRL_YEAR;
        $data2["SEMESTER"][TEXT]            = $fields["SEMESTER"];
        $data2["SCHREGNO"][TEXT]            = $schregno;
        $data2["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
        $data2["REGISTERCD"][TEXT]          = STAFFCD;
        $data2["UPDATED"][NUMBER]           = "sysdate()";

        $query =Query::insertSQL($data2, "hreportremark_dat");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($field, $schregno) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
   }

    //INSERT
    function &getInsertQuery2($field, $selectdata) {
        $db = Query::dbCheckOut();

        $schregArray = explode(",", $selectdata);
        foreach ($schregArray as $schregno) {
            $query = knjm502Query::checkExists($field, $schregno);
            $cnt = $db->getOne($query);

            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = $field["SEMESTER"];
            $data["SCHREGNO"][TEXT]            = $schregno;
            if ($field["RCHECK1"] == "on") {
                $data["COMMUNICATION"][TEXT]   = $field["COMMUNICATION"];
            }
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            if ($cnt > 0) {
                $where  = " WHERE YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "       SEMESTER = '".$field["SEMESTER"]."' AND ";
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
}
?>
