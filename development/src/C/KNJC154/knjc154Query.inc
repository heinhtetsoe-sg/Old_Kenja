<?php

require_once('for_php7.php');

class knjc154Query extends Query {

    function getNameMst($namecd1, $orderby) {
        $query  = " SELECT ";
        $query .= "    NAMECD2 AS VALUE,  ";
        $query .= "    NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($orderby) {
            $query .=  $orderby;
        } else {
            $query .= " NAMECD2 ";
        }
        return $query;
    }

    //１レコード取得
    function getTrainRow($model, $schregno) {
        $query  = "  SELECT ";
        $query .= "      REMARK1 ";
        $query .= "  FROM ";
        $query .= "      HEXAM_MONTH_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      MONTH = '".$model->field["MONTH"]."' AND ";
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
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HEXAM_MONTH_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH    = '".$field["MONTH"]."' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

   //レコードがあるかのチェック(インサートにするかアップデートにするか)
   function checkExists($field, $schregno) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HEXAM_MONTH_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH = '".$field["MONTH"]."' AND ";
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
        $data2["MONTH"][TEXT]               = $fields["MONTH"];
        $data2["SCHREGNO"][TEXT]            = $schregno;
        $data2["REMARK1"][TEXT]             = $fields["REMARK1"];
        $data2["REGISTERCD"][TEXT]          = STAFFCD;
        $data2["UPDATED"][NUMBER]           = "sysdate()";

        $query =Query::insertSQL($data2, "HEXAM_MONTH_REMARK_DAT");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($field, $schregno) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HEXAM_MONTH_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND MONTH = '".$field["MONTH"]."' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
   }

}
?>
