<?php

require_once('for_php7.php');

class knjm700dQuery extends Query
{
    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= "     , SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->expYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧リスト取得
    function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SAD.SPECIAL_FLG ";
        $query .= "     , SAM.SPECIALCD ";
        $query .= "     , SAM.SPECIAL_SDATE ";
        $query .= "     , SAM.SPECIAL_EDATE ";
        $query .= "     , SAM.SPECIALACTIVITYNAME ";
        $query .= "     , SAM.SPECIALACTIVITYTIME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REG ";
        $query .= "     INNER JOIN SEMESTER_MST SEME ";
        $query .= "         ON SEME.YEAR = REG.YEAR ";
        $query .= "     INNER JOIN SPECIAL_ACTIVITY_MST SAM ";
        $query .= "         ON SAM.YEAR = REG.YEAR ";
        $query .= "     LEFT JOIN SPECIAL_ACTIVITY_DAT SAD ";
        $query .= "         ON SAD.YEAR = REG.YEAR ";
        $query .= "         AND SAD.SPECIALCD = SAM.SPECIALCD ";
        $query .= "         AND SAD.SCHREGNO = REG.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REG.YEAR = '".$model->expYear."' ";
        $query .= "     AND SEME.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND REG.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SAM.SPECIAL_SDATE BETWEEN SEME.SDATE AND SEME.EDATE  ";
        $query .= " ORDER BY ";
        $query .= "     SAM.SPECIAL_SDATE ";

        return $query;
    }

    // UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     SPECIAL_ACTIVITY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->expYear."' ";
        $query .= "     AND SPECIALCD IN ('".implode("','", $model->specialcd)."') ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $db->query($query);

        foreach ($model->checked as $checkedSpecialcd) {
            $data["YEAR"][TEXT]        = $model->expYear;
            $data["SPECIALCD"][TEXT]   = $checkedSpecialcd;
            $data["SCHREGNO"][TEXT]    = $model->schregno;
            $data["SPECIAL_FLG"][TEXT] = 1;
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "sysdate()";

            $db->query(Query::insertSQL($data, "SPECIAL_ACTIVITY_DAT"));
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
