<?php

require_once('for_php7.php');
class knjd_behavior_lmQuery extends Query {

    //生徒情報
    function getSchInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        if ($model->call_prg == "KNJD137L") {
            $query .= "     AND SEMESTER = '9' ";
        } else {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //項目取得
    function getHreportBehaviorCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }

    //項目取得
    function getHreportBehavior($model, $gradeCnt)
    {
        $setGrade = $gradeCnt > 0 ? $model->grade : "00";
        $query  = " SELECT ";
        $query .= "     LMST.L_CD, ";
        $query .= "     LMST.L_NAME, ";
        $query .= "     MMST.M_CD, ";
        $query .= "     MMST.M_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST LMST ";
        $query .= "     LEFT JOIN HREPORT_BEHAVIOR_M_MST MMST ON LMST.YEAR = MMST.YEAR ";
        $query .= "          AND LMST.SCHOOL_KIND = MMST.SCHOOL_KIND ";
        $query .= "          AND LMST.GRADE = MMST.GRADE ";
        $query .= "          AND LMST.L_CD = MMST.L_CD ";
        $query .= " WHERE ";
        $query .= "     LMST.YEAR = '{$model->exp_year}' ";
        $query .= "     AND LMST.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND LMST.GRADE = '{$setGrade}' ";
        $query .= " ORDER BY ";
        $query .= "     LMST.L_CD, ";
        $query .= "     MMST.M_CD ";

        return $query;
    }

    //項目取得
    function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1 ";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     L_CD, ";
        $query .= "     M_CD, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_LM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     L_CD, ";
        $query .= "     M_CD ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //HREPORT_BEHAVIOR_LM_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_BEHAVIOR_LM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);

        //HREPORT_BEHAVIOR_LM_DAT追加
        foreach ($model->itemArrayL as $Lkey => $Mval) {

            foreach ($model->itemArrayM[$Lkey] as $Mkey => $Mval) {
                $lmKey = $Lkey."_".$Mkey;
                // if ($model->Properties["knjdBehaviorsd_UseText_P"] != "1" && $model->record["RECORD"][$lmKey] != "1") {
                if ($model->getPro["knjdBehaviorsd_UseText_P"] != "1" && $model->record["RECORD"][$lmKey] != "1") {
                    continue;
                } else if ($model->record["RECORD"][$lmKey] == "") {
                    continue;
                }

                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["L_CD"][TEXT]         = sprintf("%02d", $Lkey);
                $data["M_CD"][TEXT]         = sprintf("%02d", $Mkey);
                $data["RECORD"][TEXT]       = $model->record["RECORD"][$lmKey];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "HREPORT_BEHAVIOR_LM_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
