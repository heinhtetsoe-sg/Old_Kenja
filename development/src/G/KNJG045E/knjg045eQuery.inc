<?php

require_once('for_php7.php');

class knjg045eQuery extends Query {
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
    function getSchChrStfDiary($model) {
        $query  = " SELECT ";
        $query .= "       T1.DIARY_DATE ";
        $query .= "     , T1.WEATHER ";
        $query .= "     , T1.WEATHER2 ";
        $query .= "     , T1.TEMPERATURE ";
        $query .= "     , T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_STF_DIARY_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
        $query .= "     AND T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";

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

    //講座取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     CHR.PERIODCD ";
        $query .= "     , CHR.CHAIRCD ";
        $query .= "     , CHAIR_D.CHAIRNAME ";
        $query .= "     , DETAIL.REMARK1 ";
        $query .= "     , NAME.NAME1 AS PERIODNAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT CHR ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR_D ";
        $query .= "         ON CHAIR_D.YEAR = CHR.YEAR ";
        $query .= "         AND CHAIR_D.SEMESTER = CHR.SEMESTER ";
        $query .= "         AND CHAIR_D.CHAIRCD = CHR.CHAIRCD ";
        $query .= "     LEFT JOIN SCH_STF_DAT STF ";
        $query .= "         ON STF.CHAIRCD = CHR.CHAIRCD ";
        $query .= "         AND STF.EXECUTEDATE = CHR.EXECUTEDATE ";
        $query .= "         AND STF.PERIODCD = CHR.PERIODCD ";
        $query .= "     LEFT JOIN CHAIR_STF_DAT CHAIR_S ";
        $query .= "         ON CHAIR_S.YEAR = CHR.YEAR ";
        $query .= "         AND CHAIR_S.SEMESTER = CHR.SEMESTER ";
        $query .= "         AND CHAIR_S.CHAIRCD = CHR.CHAIRCD ";
        $query .= "         AND CHAIR_S.CHARGEDIV = '1' ";
        $query .= "     LEFT JOIN SCH_CHR_STF_DIARY_DETAIL_DAT DETAIL ";
        $query .= "         ON  DETAIL.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "         AND DETAIL.SCHOOL_KIND = CHAIR_D.SCHOOL_KIND ";
        $query .= "         AND DETAIL.STAFFCD = '".STAFFCD."' ";
        $query .= "         AND DETAIL.DIARY_DATE = CHR.EXECUTEDATE ";
        $query .= "         AND DETAIL.PERIODCD = CHR.PERIODCD ";
        $query .= "         AND DETAIL.CHAIRCD = CHR.CHAIRCD ";
        $query .= "     LEFT JOIN V_NAME_MST NAME ";
        $query .= "         ON  NAME.YEAR = CHR.YEAR ";
        $query .= "         AND NAME.NAMECD1 = 'B001' ";
        $query .= "         AND NAME.NAMECD2 = CHR.PERIODCD ";
        $query .= " WHERE ";
        $query .= "     CHR.EXECUTEDATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= "     AND CHAIR_D.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= "     AND ( ";
        $query .= "         STF.STAFFCD = '".STAFFCD."' ";
        $query .= "         OR ( ";
        $query .= "             STF.STAFFCD IS NULL ";
        $query .= "             AND CHAIR_S.STAFFCD = '".STAFFCD."' ";
        $query .= "         ) ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     CHR.PERIODCD ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]                  = $model->field["SCHKIND"];
        $data["STAFFCD"][TEXT]                      = STAFFCD;
        $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
        $data["WEATHER"][TEXT]                      = $model->field["WEATHER"];
        $data["WEATHER2"][TEXT]                     = $model->field["WEATHER2"];
        $data["TEMPERATURE"][NUMBER]                = $model->field["TEMPERATURE"];
        $data["REMARK"][TEXT]                       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        $query = Query::insertSQL($data, "SCH_CHR_STF_DIARY_DAT");
        $db->query($query);

        for ($i = 0; $i < $model->field["DATA_MAX_CNT"]; $i++) {
            $data = array();
            $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]                  = $model->field["SCHKIND"];
            $data["STAFFCD"][TEXT]                      = STAFFCD;
            $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
            $data["PERIODCD"][TEXT]                     = $model->data[$i]["PERIODCD"];
            $data["CHAIRCD"][TEXT]                      = $model->data[$i]["CHAIRCD"];
            $data["REMARK1"][TEXT]                      = $model->data[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]                   = STAFFCD;
            $data["UPDATED"][FUNC]                      = "SYSDATE()";
            $query = Query::insertSQL($data, "SCH_CHR_STF_DIARY_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);

        return;
    }

    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE ";
        $query .= " FROM SCH_CHR_STF_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= " AND STAFFCD     = '".STAFFCD."' ";
        $query .= " AND DIARY_DATE  = '".str_replace('/', '-', $model->diaryDate)."' ";

        $db->query($query);

        $query  = "DELETE ";
        $query .= " FROM SCH_CHR_STF_DIARY_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= " AND STAFFCD     = '".STAFFCD."' ";
        $query .= " AND DIARY_DATE  = '".str_replace('/', '-', $model->diaryDate)."' ";

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);

        return;
    }
}
?>