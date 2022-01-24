<?php

require_once('for_php7.php');

class knjb0211Query extends Query {

    //年度
    function getYear($model) {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        
        return $query;
    }

    //全体の一覧の取得（左画面）
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     G1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_GROUP_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "              ON G1.YEAR     = T1.YEAR ";
        $query .= "             AND G1.GRADE    = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "             AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->selectYear."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.GROUP_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($cd, $model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_GROUP_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->selectYear."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND GROUP_CD = '".sprintf("%03d",$cd)."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     GROUP_CD ";

        return $query;
    }

    //学年
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .$model->selectYear. "' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種
    function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.GRADE = '" .$model->field["GRADE"]. "' ";

        return $query;
    }

    //ログイン年度データカウント
    function cntThisOrder($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_GROUP_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->selectYear."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);

        return $data_check;
    }
    
    //前年度データカウント
    function cntLastOrder($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_GROUP_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->selectYear - 1)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);

        return $data_check;
    }

    //前年度からコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COMP_CONDITION_GROUP_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             GROUP_CD, ";
        $query .= "             GRADE, ";
        $query .= "             HR_CLASS, ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD, ";
        $query .= "             COURSECODE, ";
        $query .= "             GROUP_NAME, ";
        $query .= "             GROUP_ABBV, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->selectYear."', ";
        $query .= "         GROUP_CD, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GROUP_NAME, ";
        $query .= "         GROUP_ABBV, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COMP_CONDITION_GROUP_MST T1 ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($model->selectYear - 1)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (  SELECT ";
                $query .= "                         G1.GRADE ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT G1 ";
                $query .= "                     WHERE ";
                $query .= "                         G1.YEAR         = T1.YEAR AND ";
                $query .= "                         G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                     ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND GRADE IN (  SELECT ";
            $query .= "                         G1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT G1 ";
            $query .= "                     WHERE ";
            $query .= "                         G1.YEAR         = T1.YEAR AND ";
            $query .= "                         G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //--- INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->selectYear;
        $data["GROUP_CD"][TEXT]     = sprintf("%03d", $fields["GROUP_CD"]);
        $data["GRADE"][TEXT]        = $fields["GRADE"];
        $data["HR_CLASS"][TEXT]     = "000";
        $data["COURSECD"][TEXT]     = "0";
        $data["MAJORCD"][TEXT]      = "000";
        $data["COURSECODE"][TEXT]   = "0000";
        $data["GROUP_NAME"][TEXT]   = $fields["GROUP_NAME"];
        $data["GROUP_ABBV"][TEXT]   = $fields["GROUP_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;        
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "COMP_CONDITION_GROUP_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {        
        $db = Query::dbCheckOut();

        $data["GROUP_NAME"][TEXT]   = $fields["GROUP_NAME"];
        $data["GROUP_ABBV"][TEXT]   = $fields["GROUP_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR    = '".$model->selectYear."' ";
        $where .= " AND   GROUP_CD = '" .sprintf("%03d",$fields["GROUP_CD"])."'";
        $where .= " AND   GRADE = '" .$fields["GRADE"]."'";
        $query  = Query::updateSQL($data, "COMP_CONDITION_GROUP_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM COMP_CONDITION_GROUP_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR =  '".$model->selectYear."' ";
        $query .= "     AND GROUP_CD = '" .sprintf("%03d", $fields["GROUP_CD"])."'";
        $query .= "     AND GRADE = '" .$fields["GRADE"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
