<?php

require_once('for_php7.php');

class knjp733query extends Query {

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

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getCourse($model) {
        $query  = " SELECT ";
        $query .= "     REGD.COURSECD || ':' || REGD.MAJORCD || ':' || REGD.COURSECODE AS VALUE, ";
        $query .= "     MAJOR.MAJORNAME || CCODE.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN MAJOR_MST MAJOR ON REGD.COURSECD = MAJOR.COURSECD ";
        $query .= "          AND REGD.MAJORCD = MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["GRADE"] != '00') {
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECD || ':' || REGD.MAJORCD || ':' || REGD.COURSECODE, ";
        $query .= "     MAJOR.MAJORNAME || CCODE.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履歴一覧
    function getListRireki($model) {
        $query  = " SELECT ";
        $query .= "     OFFSET_HIST.SCHOOLCD, ";
        $query .= "     OFFSET_HIST.SCHOOL_KIND, ";
        $query .= "     OFFSET_HIST.YEAR, ";
        $query .= "     OFFSET_HIST.PROCESS_DATE, ";
        $query .= "     OFFSET_HIST.REDUCTION_KIND, ";
        $query .= "     OFFSET_HIST.OFFSET_REFUND_DIV, ";
        $query .= "     OFFSET_HIST.MONTH_FROM, ";
        $query .= "     OFFSET_HIST.MONTH_TO, ";
        $query .= "     OFFSET_HIST.REGISTERCD, ";
        $query .= "     CASE WHEN SELECT_GRADE = '00' THEN '全て' ELSE GDAT.GRADE_NAME1 END AS GRADE_NAME1, ";
        $query .= "     CASE WHEN SELECT_COURSECD || SELECT_MAJORCD || SELECT_COURSECODE = '00000000' ";
        $query .= "          THEN '全て' ";
        $query .= "          ELSE MAJOR.MAJORNAME || CCODE.COURSECODENAME ";
        $query .= "     END AS CMC_NAME, ";
        $query .= "     DATE(OFFSET_HIST.UPDATED) AS EXE_DATE, ";
        $query .= "     TIME(OFFSET_HIST.UPDATED) AS EXE_TIME ";
        $query .= " FROM ";
        $query .= "     REDUCTION_OFFSET_REFUND_HIST_DAT OFFSET_HIST ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON OFFSET_HIST.YEAR = GDAT.YEAR ";
        $query .= "          AND OFFSET_HIST.SELECT_GRADE = GDAT.GRADE ";
        $query .= "     LEFT JOIN MAJOR_MST MAJOR ON OFFSET_HIST.SELECT_COURSECD = MAJOR.COURSECD ";
        $query .= "          AND OFFSET_HIST.SELECT_MAJORCD = MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST CCODE ON OFFSET_HIST.SELECT_COURSECODE = CCODE.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         OFFSET_HIST.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND OFFSET_HIST.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND OFFSET_HIST.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     OFFSET_HIST.UPDATED DESC ";

        return $query;
    }

    //キャンセル伝票取得
    function getCancelSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND CANCEL_DATE IS NOT NULL ";
        if ($model->field["REDUCTION_KIND"] == "5" || $model->field["REDUCTION_KIND"] == "6") {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     SLIP_D.SLIP_NO ";
            $query .= " FROM ";
            $query .= "     COLLECT_SLIP_DAT SLIP_D ";
            $query .= "     INNER JOIN COLLECT_SLIP_M_DAT SLIP_MD ";
            $query .= "            ON SLIP_MD.SCHOOLCD      = SLIP_D.SCHOOLCD ";
            $query .= "           AND SLIP_MD.SCHOOL_KIND   = SLIP_D.SCHOOL_KIND ";
            $query .= "           AND SLIP_MD.YEAR          = SLIP_D.YEAR ";
            $query .= "           AND SLIP_MD.SLIP_NO       = SLIP_D.SLIP_NO ";
            $query .= "     INNER JOIN COLLECT_M_MST COL_M ";
            $query .= "            ON COL_M.SCHOOLCD        = SLIP_MD.SCHOOLCD ";
            $query .= "           AND COL_M.SCHOOL_KIND     = SLIP_MD.SCHOOL_KIND ";
            $query .= "           AND COL_M.YEAR            = SLIP_MD.YEAR ";
            $query .= "           AND COL_M.COLLECT_L_CD    = SLIP_MD.COLLECT_L_CD ";
            $query .= "           AND COL_M.COLLECT_M_CD    = SLIP_MD.COLLECT_M_CD ";
            $query .= "           AND VALUE(COL_M.GAKUNOKIN_DIV, '') != '2' ";
            $query .= "           AND (VALUE(COL_M.REDUCTION_DIV, '') IN ('1','2') OR VALUE(COL_M.IS_REDUCTION_SCHOOL, '') = '1') ";
            $query .= " WHERE ";
            $query .= "         SLIP_D.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SLIP_D.SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "     AND SLIP_D.YEAR        = '".CTRL_YEAR."' ";
        } else {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     SLIP_D.SLIP_NO ";
            $query .= " FROM ";
            $query .= "     COLLECT_SLIP_DAT SLIP_D ";
            $query .= "     INNER JOIN COLLECT_SLIP_M_DAT SLIP_MD ";
            $query .= "            ON SLIP_MD.SCHOOLCD      = SLIP_D.SCHOOLCD ";
            $query .= "           AND SLIP_MD.SCHOOL_KIND   = SLIP_D.SCHOOL_KIND ";
            $query .= "           AND SLIP_MD.YEAR          = SLIP_D.YEAR ";
            $query .= "           AND SLIP_MD.SLIP_NO       = SLIP_D.SLIP_NO ";
            $query .= "     INNER JOIN COLLECT_M_MST COL_M ";
            $query .= "            ON COL_M.SCHOOLCD        = SLIP_MD.SCHOOLCD ";
            $query .= "           AND COL_M.SCHOOL_KIND     = SLIP_MD.SCHOOL_KIND ";
            $query .= "           AND COL_M.YEAR            = SLIP_MD.YEAR ";
            $query .= "           AND COL_M.COLLECT_L_CD    = SLIP_MD.COLLECT_L_CD ";
            $query .= "           AND COL_M.COLLECT_M_CD    = SLIP_MD.COLLECT_M_CD ";
            $query .= "           AND VALUE(COL_M.GAKUNOKIN_DIV, '') = '2' ";
            $query .= "           AND (VALUE(COL_M.REDUCTION_DIV, '') = '3' OR VALUE(COL_M.IS_REDUCTION_SCHOOL, '') = '1') ";
            $query .= " WHERE ";
            $query .= "         SLIP_D.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SLIP_D.SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "     AND SLIP_D.YEAR        = '".CTRL_YEAR."' ";
        }
        return $query;
    }

    /* 実行 */
    function executeReduction($model, $grade, $cmc) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //キャンセル伝票取得
        $setCancelSlipNoIn = $sep = "";
        $query = knjp733Query::getCancelSlipNo($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setCancelSlipNoIn .= $sep.$row["SLIP_NO"];
            $sep = "', '";
        }
        $result->free();

        //テーブルセット
        $setTable = "";
        switch ($model->field["REDUCTION_KIND"]) {
            case "1":
            case "2":
                $setTable = "REDUCTION_COUNTRY_PLAN_DAT";
                break;
            case "3":
                $setTable = "REDUCTION_PLAN_DAT";
                break;
            case "4":
                $setTable = "REDUCTION_SCHOOL_PLAN_DAT";
                break;
            case "5":
                $setTable = "REDUCTION_PLAN_DAT";
                break;
            case "6":
                $setTable = "REDUCTION_SCHOOL_PLAN_DAT";
                break;
            default:
                $setTable = "";
                break;
        }

        //更新対象月をセット
        $setMonthArr = array();
        $monthFrom = (0 < $model->field["MONTH_FROM"] && $model->field["MONTH_FROM"] < 4) ? $model->field["MONTH_FROM"] + 12: $model->field["MONTH_FROM"];
        $monthTo   = (0 < $model->field["MONTH_TO"] && $model->field["MONTH_TO"] < 4)     ? $model->field["MONTH_TO"] + 12  : $model->field["MONTH_TO"];
        for ($i = $monthFrom; $i <= $monthTo; $i++) {
            $setmonth = ($i > 12) ? $i - 12: $i;
            $setMonthArr[] = sprintf("%02d", $setmonth);
        }

        //実行履歴データ・追加
        foreach ($setMonthArr as $month) {
            $setYear = ($month < 04) ? CTRL_YEAR + 1: CTRL_YEAR;

            $data = array();
            //相殺フラグ
            if ($model->field["OFFSET_REFUND_DIV"] == "1") {
                //支援金（加算）
                if ($model->field["REDUCTION_KIND"] == "2") {
                    $data["ADD_OFFSET_FLG"][TEXT]    = "1";
                    $data["ADD_REFUND_FLG"][TEXT]    = "";

                } else {
                    $data["OFFSET_FLG"][TEXT]    = "1";
                    $data["REFUND_FLG"][TEXT]    = "";
                }

            //還付フラグ
            } else {
                //支援金（加算）
                if ($model->field["REDUCTION_KIND"] == "2") {
                    $data["ADD_OFFSET_FLG"][TEXT]    = "";
                    $data["ADD_REFUND_FLG"][TEXT]    = "1";
                } else {
                    $data["OFFSET_FLG"][TEXT]    = "";
                    $data["REFUND_FLG"][TEXT]    = "1";
                }
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "       SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND  = '".$model->schoolKind."' ";
            $where .= "   AND YEAR         = '".CTRL_YEAR."' ";
            $where .= "   AND SLIP_NO NOT IN ('".$setCancelSlipNoIn."') ";
            $where .= "   AND PLAN_YEAR    = '".$setYear."' ";
            $where .= "   AND PLAN_MONTH   = '".$month."' ";
            $where .= "   AND SCHREGNO IN ( ";
            $where .= "     SELECT ";
            $where .= "         REGD.SCHREGNO ";
            $where .= "     FROM ";
            $where .= "         SCHREG_REGD_DAT REGD ";
            $where .= "     WHERE ";
            $where .= "         REGD.YEAR = '".CTRL_YEAR."' ";
            $where .= "         AND REGD.GRADE = '{$grade}' ";
            $where .= "         AND REGD.COURSECD || ':' || REGD.MAJORCD || ':' || REGD.COURSECODE = '{$cmc}' ";
            $where .= "   ) ";

            $query = Query::updateSQL($data, $setTable, $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //REDUCTION_OFFSET_REFUND_HIST_DAT
    function getOffRefDat($model, $grade, $cmc) {
        list($courseCd, $majorCd, $courseCode) = explode(":", $cmc);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_OFFSET_REFUND_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD          = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND YEAR              = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND       = '".$model->schoolKind."' ";
        $query .= "     AND PROCESS_DATE      = '".str_replace("/", "-", $model->field["PROCESS_DATE"])."' ";
        $query .= "     AND REDUCTION_KIND    = '".$model->field["REDUCTION_KIND"]."' ";
        $query .= "     AND OFFSET_REFUND_DIV = '".$model->field["OFFSET_REFUND_DIV"]."' ";
        $query .= "     AND SELECT_GRADE      = '{$grade}' ";
        $query .= "     AND SELECT_COURSECD   = '{$courseCd}' ";
        $query .= "     AND SELECT_MAJORCD    = '{$majorCd}' ";
        $query .= "     AND SELECT_COURSECODE = '{$courseCode}' ";

        return $query;
    }

    /* 実行履歴 */
    function executeRireki($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjp733Query::getOffRefDat($model, $model->field["GRADE"], $model->field["CMC"]);
        $offRefDat = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //実行履歴データ・追加
        list($courseCd, $majorCd, $courseCode) = explode(":", $model->field["CMC"]);
        $data = array();
        if (is_array($offRefDat)) {
            $data["MONTH_FROM"][TEXT]           = $model->field["MONTH_FROM"];
            $data["MONTH_TO"][TEXT]             = $model->field["MONTH_TO"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "         SCHOOLCD       = '".sprintf("%012d", SCHOOLCD)."' ";
            $where .= "     AND YEAR           = '".CTRL_YEAR."' ";
            $where .= "     AND SCHOOL_KIND    = '".$model->schoolKind."' ";
            $where .= "     AND PROCESS_DATE   = '".str_replace("/", "-", $model->field["PROCESS_DATE"])."' ";
            $where .= "     AND REDUCTION_KIND = '".$model->field["REDUCTION_KIND"]."' ";
            $where .= "     AND OFFSET_REFUND_DIV = '".$model->field["OFFSET_REFUND_DIV"]."' ";
            $where .= "     AND SELECT_GRADE      = '{$model->field["GRADE"]}' ";
            $where .= "     AND SELECT_COURSECD   = '{$courseCd}' ";
            $where .= "     AND SELECT_MAJORCD    = '{$majorCd}' ";
            $where .= "     AND SELECT_COURSECODE = '{$courseCode}' ";

            $query = Query::updateSQL($data, "REDUCTION_OFFSET_REFUND_HIST_DAT", $where);
        } else {
            $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["PROCESS_DATE"][DATE]         = str_replace("/", "-", $model->field["PROCESS_DATE"]);
            $data["REDUCTION_KIND"][TEXT]       = $model->field["REDUCTION_KIND"];
            $data["OFFSET_REFUND_DIV"][TEXT]    = $model->field["OFFSET_REFUND_DIV"];
            $data["SELECT_GRADE"][TEXT]         = $model->field["GRADE"];
            $data["SELECT_COURSECD"][TEXT]      = $courseCd;
            $data["SELECT_MAJORCD"][TEXT]       = $majorCd;
            $data["SELECT_COURSECODE"][TEXT]    = $courseCode;
            $data["MONTH_FROM"][TEXT]           = $model->field["MONTH_FROM"];
            $data["MONTH_TO"][TEXT]             = $model->field["MONTH_TO"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "REDUCTION_OFFSET_REFUND_HIST_DAT");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
