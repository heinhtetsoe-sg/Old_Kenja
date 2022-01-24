<?php

require_once('for_php7.php');

class knjp803Query extends Query {
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

    function getClassName($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

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
        $query .= "     AND T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //パターン取得
    function getCollectPatternCd($model) {
        $query  = " SELECT ";
        $query .= "     COLLECT_PATTERN_CD || ':' || COLLECT_PATTERN_NAME AS LABEL, ";
        $query .= "     COLLECT_PATTERN_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

/******リストtoリスト*******/
    //生徒取得(左側に表示する生徒)
    function getStudentLeft($model, $semester) {
        $query  = " SELECT ";
        $query .= "     HDAT.HR_NAME || ' ' || REGD.ATTENDNO || '番　' || SLIP.SLIP_NO || '　' || BASE.NAME_SHOW AS LABEL, ";
        $query .= "     REGD.SCHREGNO || '-' || SLIP.SLIP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
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
        $query .= "         SLIP.YEAR           = '".CTRL_YEAR."' ";
        $query .= "     AND SLIP.COLLECT_GRP_CD = '{$model->field["COLLECT_GRP_CD"]}' ";
        $query .= "     AND SLIP.CANCEL_DATE    IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //生徒取得(右側に表示する生徒)
    function getStudentRight($model, $semester) {
        $query  = " SELECT ";
        $query .= "     HDAT.HR_NAME || ' ' || REGD.ATTENDNO || '番　' || SLIP.SLIP_NO || '　' || BASE.NAME_SHOW AS LABEL, ";
        $query .= "     REGD.SCHREGNO || '-' || SLIP.SLIP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT SLIP ";
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
        $query .= "         SLIP.YEAR           = '".CTRL_YEAR."' ";
        $query .= "     AND SLIP.COLLECT_GRP_CD = '{$model->field["COLLECT_GRP_CD"]}' ";
        $query .= "     AND SLIP.CANCEL_DATE    IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

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
        $query .= "     AND YEAR               = '".CTRL_YEAR."' ";
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
        $monthRow = $db->getRow(knjp803Query::getCollectPatternMonth($model), DB_FETCHMODE_ASSOC);

        if ($schregSlipArr[0] !="") {
            foreach ($schregSlipArr as $schregNoSlipNo) {
                list($schregNo, $slipNo) = explode("-", $schregNoSlipNo);

                foreach ($model->monthArray as $month) {
                    $setYear      = ($monthRow["COLLECT_MONTH_{$month}"] < 4) ? CTRL_YEAR + 1: CTRL_YEAR;
                    $setMonth     = sprintf("%02d", $monthRow["COLLECT_MONTH_{$month}"]);
                    $setLimitDate = $setYear."-".$setMonth."-".$monthRow["DIRECT_DEBIT_DATE"];

                    $data = array();
                    $data["PAID_LIMIT_DATE"][DATE]  = $setLimitDate;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $where  = " WHERE SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
                    $where .= "   AND YEAR          = '".CTRL_YEAR."' ";
                    $where .= "   AND SCHREGNO      = '".$schregNo."' ";
                    $where .= "   AND SLIP_NO       = '".$slipNo."' ";
                    $where .= "   AND PLAN_YEAR     = '".$setYear."' ";
                    $where .= "   AND PLAN_MONTH    = '".sprintf("%02d", $month)."' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT", $where);
                    $db->query($query);
                }
            }
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
