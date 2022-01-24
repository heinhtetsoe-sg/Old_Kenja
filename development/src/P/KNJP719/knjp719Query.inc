<?php

require_once('for_php7.php');

class knjp719Query extends Query {
/**********/
/** 画面 **/
/**********/
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
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

    //入金グループ取得
    function getCollectGrp($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COLLECT_GRP_CD AS VALUE, ";
        $query .= "     T1.COLLECT_GRP_CD || ':' || T2.COLLECT_GRP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_DAT T1 ";
        $query .= "     INNER JOIN COLLECT_GRP_MST T2 ";
        $query .= "          ON T2.SCHOOLCD       = T1.SCHOOLCD ";
        $query .= "         AND T2.SCHOOL_KIND    = T1.SCHOOL_KIND ";
        $query .= "         AND T2.YEAR           = T1.YEAR ";
        $query .= "         AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //パターン取得
    function getCollectPatternCd($model, $disabled="") {
        $query  = " SELECT ";
        $query .= "     COLLECT_PATTERN_CD || ':' || COLLECT_PATTERN_NAME AS LABEL, ";
        $query .= "     COLLECT_PATTERN_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '{$model->year}' ";
        if ($disabled && $model->field["LIST_COLLECT_PATTERN_CD"] != "") {
            $query .= "     AND COLLECT_PATTERN_CD <> '".$model->field["LIST_COLLECT_PATTERN_CD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

/******リストtoリスト*******/
    //生徒取得(右側に表示する生徒)
    function getStudentRight($model, $semester) {
        $query  = " SELECT ";
        $query .= "     HDAT.HR_NAMEABBV || ' ' || REGD.ATTENDNO || '番　' AS LABEL1, ";
        $query .= "     SLIP.COLLECT_PATTERN_CD, ";
        $query .= "     CASE WHEN SLIP.COLLECT_PATTERN_CD = '00' THEN 'パターンなし' ";
        $query .= "          ELSE DEFT.COLLECT_PATTERN_NAME ";
        $query .= "     END AS COLLECT_PATTERN_NAME, ";
        $query .= "     BASE.NAME_SHOW, ";
        $query .= "     REGD.SCHREGNO || '-' || SLIP.SLIP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= " LEFT JOIN ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST DEFT ON DEFT.SCHOOLCD           = SLIP.SCHOOLCD ";
        $query .= "                                      AND DEFT.SCHOOL_KIND        = SLIP.SCHOOL_KIND ";
        $query .= "                                      AND DEFT.YEAR               = SLIP.YEAR ";
        $query .= "                                      AND DEFT.COLLECT_PATTERN_CD = SLIP.COLLECT_PATTERN_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = SLIP.SCHREGNO ";
        $query .= "                         AND REGD.YEAR     = SLIP.YEAR ";
        $query .= "                         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                          AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         SLIP.YEAR           = '{$model->year}' ";
        $query .= "     AND SLIP.COLLECT_GRP_CD = '{$model->field["COLLECT_GRP_CD"]}' ";
        $query .= "     AND SLIP.CANCEL_DATE    IS NULL ";
        $query .= "     AND SLIP.SCHOOL_KIND = '".$model->schoolKind."' ";
        if ($model->field["LIST_COLLECT_PATTERN_CD"] != "") {
            $query .= "     AND SLIP.COLLECT_PATTERN_CD = '{$model->field["LIST_COLLECT_PATTERN_CD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }


/******リストtoリスト*******/
    //新入生取得(右側に表示する新入生)
    function getStudentRightFresh($model, $semester) {
        $query  = " SELECT ";
        $query .= "     FMD.SCHREGNO || '　' || FMD.GRADE || '-' || FMD.HR_CLASS || '-' || FMD.ATTENDNO || '番　' AS LABEL1, ";
        $query .= "     SLIP.COLLECT_PATTERN_CD, ";
        $query .= "     CASE WHEN SLIP.COLLECT_PATTERN_CD = '00' THEN 'パターンなし' ";
        $query .= "          ELSE DEFT.COLLECT_PATTERN_NAME ";
        $query .= "     END AS COLLECT_PATTERN_NAME, ";
        $query .= "     FMD.NAME AS NAME_SHOW, ";
        $query .= "     FMD.SCHREGNO || '-' || SLIP.SLIP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
        $query .= " LEFT JOIN ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST DEFT ON DEFT.SCHOOLCD           = SLIP.SCHOOLCD ";
        $query .= "                                      AND DEFT.SCHOOL_KIND        = SLIP.SCHOOL_KIND ";
        $query .= "                                      AND DEFT.YEAR               = SLIP.YEAR ";
        $query .= "                                      AND DEFT.COLLECT_PATTERN_CD = SLIP.COLLECT_PATTERN_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     FRESHMAN_DAT FMD  ON FMD.SCHREGNO  = SLIP.SCHREGNO ";
        $query .= "                      AND FMD.ENTERYEAR = SLIP.YEAR ";
        $query .= " WHERE ";
        $query .= "         SLIP.YEAR           = '{$model->year}' ";
        $query .= "     AND SLIP.COLLECT_GRP_CD = '{$model->field["COLLECT_GRP_CD"]}' ";
        $query .= "     AND SLIP.CANCEL_DATE    IS NULL ";
        $query .= "     AND SLIP.SCHOOL_KIND = '".$model->schoolKind."' ";
        if ($model->field["LIST_COLLECT_PATTERN_CD"] != "") {
            $query .= "     AND SLIP.COLLECT_PATTERN_CD = '{$model->field["LIST_COLLECT_PATTERN_CD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     FMD.GRADE, ";
        $query .= "     FMD.HR_CLASS, ";
        $query .= "     FMD.ATTENDNO ";

        return $query;
    }

/**************/
/** 更新処理 **/
/**************/
    //パターン取得
    function getCollectPatternMonth($model) {
        $query  = " SELECT ";
        foreach ($model->monthArray as $month) {
            $query .= "     COLLECT_MONTH_{$month}, ";
        }
        $query .= "     DIRECT_DEBIT_DATE ";
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD           = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND        = '".$model->schoolKind."' ";
        $query .= "     AND YEAR               = '{$model->year}' ";
        $query .= "     AND COLLECT_PATTERN_CD = '".$model->field["COLLECT_PATTERN_CD"]."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生徒一覧を配列に格納
        $schregSlipArr = explode (",", $model->selectdata);

        //入金パターン情報を取得
        $monthRow = $db->getRow(knjp719Query::getCollectPatternMonth($model), DB_FETCHMODE_ASSOC);

        //銀行休業日取得
        $holidayArray = array();
        $result = $db->query(knjp719Query::getBankHolidayDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayArray[] = $row["EXECUTEDATE"];
        }
        $result->free();

        //入金日は、土日祝日の場合は翌営業日
        $setLimitArray = array();
        foreach ($model->monthArray as $arrayMonth) {
            $paid_limit_date = NULL;
            if (strlen($monthRow["COLLECT_MONTH_".$arrayMonth])) {
                $month = intval($monthRow["COLLECT_MONTH_".$arrayMonth], 10);
                $year = ($month <= 3) ? $model->year + 1 : $model->year;
                $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year));
                $day = ($monthRow["DIRECT_DEBIT_DATE"] > $lastday) ? $lastday : $monthRow["DIRECT_DEBIT_DATE"];

                $date = $year."-".sprintf("%02d", $month)."-".sprintf("%02d", $day);

                //土日祝日の場合は翌営業日
                $paid_limit_date = $date;
                for ($d = 0; $d < 100; $d++) {
                    $paid_limit_date = date("Y-m-d", strtotime("$date $d day"));
                    //土(6)・日(0)・祝日以外
                    if (!in_array(date('w', strtotime($paid_limit_date)), array(0,6)) && !in_array($paid_limit_date, $holidayArray)) {
                        break;
                    }
                }
            }
            $setLimitArray[$arrayMonth] = $paid_limit_date;
        }

        if ($schregSlipArr[0] !="") {
            foreach ($schregSlipArr as $schregNoSlipNo) {
                list($schregNo, $slipNo) = explode("-", $schregNoSlipNo);

                foreach ($model->monthArray as $month) {

                    $setYear      = ($month < 4) ? $model->year + 1 : $model->year;

                    $data = array();
                    $data["PAID_LIMIT_DATE"][DATE]  = $setLimitArray[$month];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $where  = " WHERE SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
                    $where .= "   AND YEAR          = '{$model->year}' ";
                    $where .= "   AND SCHREGNO      = '".$schregNo."' ";
                    $where .= "   AND SLIP_NO       = '".$slipNo."' ";
                    $where .= "   AND PLAN_YEAR     = '".$setYear."' ";
                    $where .= "   AND PLAN_MONTH    = '".sprintf("%02d", $month)."' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT", $where);
                    $db->query($query);
                }

                //COLLECT_SLIP_DAT
                $data = array();
                $data["COLLECT_PATTERN_CD"][TEXT]   = $model->field["COLLECT_PATTERN_CD"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $where  = " WHERE SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
                $where .= "   AND YEAR          = '{$model->year}' ";
                $where .= "   AND SLIP_NO       = '".$slipNo."' ";

                $query = Query::updateSQL($data, "COLLECT_SLIP_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //銀行休業日取得
    function getBankHolidayDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BANK_HOLIDAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' ";

        return $query;
    }
}
?>
