<?php

require_once('for_php7.php');

class knjz466Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOLARSHIP_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SCHOLARSHIP ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $scholarship_cd) {
       $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOLARSHIP_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOLARSHIP = '".$scholarship_cd."'";

       $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
       Query::dbCheckIn($db);
       return $Row;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHOLARSHIP"][TEXT]      = sprintf("%02d",$fields["SCHOLARSHIP"]);
        $data["SCHOLARSHIP_NAME"][TEXT] = $fields["SCHOLARSHIP_NAME"];
        $data["SCHOLARSHIP_ABBV"][TEXT] = $fields["SCHOLARSHIP_ABBV"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCHOLARSHIP_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOLARSHIP_NAME"][TEXT] = $fields["SCHOLARSHIP_NAME"];
        $data["SCHOLARSHIP_ABBV"][TEXT] = $fields["SCHOLARSHIP_ABBV"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $where .= "     AND SCHOLARSHIP = '".sprintf("%02d",$fields["SCHOLARSHIP"])."' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR."' ";

        $query = Query::updateSQL($data, "SCHOLARSHIP_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHOLARSHIP_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND SCHOLARSHIP = '".sprintf("%02d",$fields["SCHOLARSHIP"])."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック(SCHOLARSHIP_MST)
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM SCHOLARSHIP_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND YEAR        = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO SCHOLARSHIP_MST ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             SCHOLARSHIP, ";
        $query .= "             SCHOLARSHIP_NAME, ";
        $query .= "             SCHOLARSHIP_ABBV, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         SCHOLARSHIP, ";
        $query .= "         SCHOLARSHIP_NAME, ";
        $query .= "         SCHOLARSHIP_ABBV, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         SCHOLARSHIP_MST ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "         AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "         AND YEAR        = '".(CTRL_YEAR - 1)."'";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
