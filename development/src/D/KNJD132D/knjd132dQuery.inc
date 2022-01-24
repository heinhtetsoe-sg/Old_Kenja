<?php

require_once('for_php7.php');

class knjd132dQuery extends Query {
    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期制取得
    function getSemesterCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //名称取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1 ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query .= " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT  ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->exp_year."' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }

    //GRADE_CD取得
    function getGradeCd($model) {
        $query .= " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT  ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->exp_year."' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }

    //生活のようすの取得
    function getBehavior($model) {
        $query  = " SELECT ";   //1学期
        $query .= "     SEMESTER, ";
        $query .= "     L_CD, ";
        $query .= "     M_CD, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_LM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '1' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= " UNION ";
        $query .= " SELECT ";   //2学期
        $query .= "     SEMESTER, ";
        $query .= "     L_CD, ";
        $query .= "     M_CD, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_LM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '2' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= " UNION ";
        $query .= " SELECT ";   //3学期
        $query .= "     SEMESTER, ";
        $query .= "     L_CD, ";
        $query .= "     M_CD, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_LM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '3' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER, ";
        $query .= "     L_CD, ";
        $query .= "     M_CD ";
        return $query;
    }

    //項目数取得
    function getHreportBehaviorCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_BEHAVIOR_L_MST LMST ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "     AND GRADE       = '{$model->grade}' ";

        return $query;
    }

    //項目取得
    function getHreportBehavior($model, $gradeCnt) {
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
        $query .= "          AND LMST.GRADE       = MMST.GRADE ";
        $query .= "          AND LMST.L_CD        = MMST.L_CD ";
        $query .= " WHERE ";
        $query .= "         LMST.YEAR        = '{$model->exp_year}' ";
        $query .= "     AND LMST.SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "     AND LMST.GRADE       = '{$setGrade}' ";
        $query .= " ORDER BY ";
        $query .= "     LMST.L_CD, ";
        $query .= "     MMST.M_CD ";

        return $query;
    }

    //所見取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //INSERT -- HREPORTREMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd132dQuery::getHreportremarkDat($model));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->field["SEMESTER"]." ' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //INSERT -- HREPORT_BEHAVIOR_LM_DAT
    function &getInsertQuery2($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- HREPORT_BEHAVIOR_LM_DAT
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_BEHAVIOR_LM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);

        //追加 -- HREPORT_BEHAVIOR_LM_DAT
        foreach ($model->itemArrayL as $Lkey => $Mval) {

            foreach ($model->itemArrayM[$Lkey] as $Mkey => $Mval) {
                $lmKey = $Lkey."_".$Mkey."_".$model->field["SEMESTER"];
                if ($model->Properties["knjdBehaviorsd_UseText_P"] != "1" && $model->record["RECORD"][$lmKey] != "1") {
                    continue;
                } else if ($model->record["RECORD"][$lmKey] == "") {
                    continue;
                }

                $data = array();
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

/*===================================定型文選択===================================*/

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_G.GRADE AS VALUE, ";
        $query .= "     REGD_G.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "          SCHREG_REGD_GDAT REGD_G ";
        $query .= "     JOIN SCHREG_REGD_DAT REGD_D ON REGD_G.YEAR = REGD_D.YEAR ";
        $query .= " WHERE ";
        $query .= "         REGD_G.YEAR     = '".CTRL_YEAR."'  ";
        $query .= "     AND REGD_D.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD_G.GRADE    = '".$model->grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //定型文マスタ一覧取得
    function getHtrainRemarkTempDatIkkatsu($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE   = '".$model->grade."' ";
        if ($model->field["TEIKEI_CMD"] == "teikei1") {
            $query .= "     AND DATA_DIV    = '08' ";
        } else if ($model->field["TEIKEI_CMD"] == "teikei2") {
            $query .= "     AND DATA_DIV    = '09' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }

/*===================================一括更新画面===================================*/
    //学期名取得
    function getSemeName($semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //所見取得
    function getHreportremarkDatIkkatsu($model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     REMARK1, ";
        $query .= "     ATTENDREC_REMARK, ";
        $query .= "     COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->ikkatsuSeme."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //年組名取得
    function getHR_Name($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         GRADE    = '".$model->grade."' ";
        $query .= "     AND HR_CLASS = '".$model->hr_class."' ";
        $query .= "     AND YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //生徒一覧
    function GetStudent($model) {
        $query  = " SELECT ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$model->grade."' ";
        $query .= "     AND T1.HR_CLASS = '".$model->hr_class."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //レコードチェック
    function checkHrepExist($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->ikkatsuSeme."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //一括更新
    function IkkatsuUpdate($db, $model) {
        $db->autoCommit(false);
        $schNo = explode(",", $model->ikkatsu_data["selectdata"]);
        for ($i = 0; $i < get_count($schNo); $i++) {
            //レコードチェック（無ければインサート）
            $checkExist = $db->getOne(knjd132dQuery::checkHrepExist($model, $schNo[$i]));

            foreach ($model->ikkatsu_data["hreport"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            if ($checkExist == 0) {
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["SEMESTER"][TEXT] = $model->ikkatsuSeme;
                $data["SCHREGNO"][TEXT] = $schNo[$i];

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
            } else {

                $where  = " WHERE ";
                $where .= "         YEAR     = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER = '".$model->ikkatsuSeme."' ";
                $where .= "     AND SCHREGNO = '".$schNo[$i]."' ";

                $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
            }
        }
        $db->commit();
    }
}
?>
