<?php

require_once('for_php7.php');

class knjg048Query extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //日誌取得
    function getDiary($model) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }

    //天気取得
    function getWeather($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //行事取得
    function getEventMst($model) {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND  = '".SCHOOLKIND."' ";
        $query .= "     AND DATA_DIV     = '1' ";
        $query .= "     AND GRADE        = '00' ";
        $query .= "     AND COURSECD     = '0' ";
        $query .= "     AND MAJORCD      = '000' ";
        $query .= "     AND EXECUTEDATE  = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= "     AND HR_CLASS     = '000' ";
        $query .= "     AND HR_CLASS_DIV = '".$model->hr_class_div."' ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data = array();
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][DATE]                     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][DATE]                  = $model->field["SCHKIND"];
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][DATE]                     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][DATE]                  = SCHOOLKIND;
        }
        $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
        $data["WEATHER"][TEXT]                      = $model->field["WEATHER"];
        $data["WEATHER2"][TEXT]                     = $model->field["WEATHER2"];
        $data["NEWS"][TEXT]                         = $model->field["NEWS"];
        $data["STAFFNEWS"][TEXT]                    = $model->field["STAFFNEWS"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>