<?php

require_once('for_php7.php');


class knjc231Query extends Query {
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
        $query .= "     NAME1 ";

        return $query;
    }

    //委員会一覧取得
    function selectCommitteeQuery($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOL_KIND || '-' || T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD AS VALUE, ";
            } else {
                $query .= "     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD AS VALUE, ";
            }
            $query .= "     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD || ':' || T1.COMMITTEENAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     COMMITTEE_MST T1 ";
            $query .= "     INNER JOIN COMMITTEE_YDAT I1 ON I1.YEAR          = '".CTRL_YEAR."' ";
            $query .= "                                 AND T1.COMMITTEE_FLG = I1.COMMITTEE_FLG ";
            $query .= "                                 AND T1.COMMITTEECD   = I1.COMMITTEECD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                                 AND I1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "                                 AND I1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                                 AND I1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "                                 AND I1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " WHERE   T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " WHERE   T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " GROUP BY ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOL_KIND, ";
            }
            $query .= "     T1.COMMITTEE_FLG, ";
            $query .= "     T1.COMMITTEECD, ";
            $query .= "     T1.COMMITTEENAME ";
            $query .= " ORDER BY ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOL_KIND DESC, ";
            }
            $query .= "     T1.COMMITTEE_FLG, ";
            $query .= "     T1.COMMITTEECD ";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOL_KIND || '-' || T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD AS VALUE, ";
            } else {
                $query .= "     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD AS VALUE, ";
            }
            $query .= "     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD || ':' || T1.COMMITTEENAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     COMMITTEE_MST T1 ";
            $query .= "     INNER JOIN COMMITTEE_YDAT I1 ON I1.YEAR          = '".CTRL_YEAR."' ";
            $query .= "                                 AND T1.COMMITTEE_FLG = I1.COMMITTEE_FLG ";
            $query .= "                                 AND T1.COMMITTEECD   = I1.COMMITTEECD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                                 AND I1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "                                 AND I1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                                 AND I1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "                                 AND I1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "     INNER JOIN COMMITTEE_ADVISER_DAT I2 ON I2.YEAR          = '".CTRL_YEAR."' ";
            $query .= "                                        AND T1.COMMITTEE_FLG = I2.COMMITTEE_FLG ";
            $query .= "                                        AND T1.COMMITTEECD   = I2.COMMITTEECD ";
            $query .= "                                        AND I2.ADVISER       = '" .STAFFCD ."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "           AND I2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "           AND I2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "           AND I2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "           AND I2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " WHERE   T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " WHERE   T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " GROUP BY ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOL_KIND, ";
            }
            $query .= "     T1.COMMITTEE_FLG, ";
            $query .= "     T1.COMMITTEECD, ";
            $query .= "     T1.COMMITTEENAME ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //生徒一覧取得
    function selectStudentQuery($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     L2.GRADE, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     L3.HR_NAME || ' ' || L2.ATTENDNO || ' ' || L1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L2 ON L2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND L2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT L3 ON L2.YEAR = L3.YEAR ";
        $query .= "           AND L2.SEMESTER = L3.SEMESTER ";
        $query .= "           AND L2.GRADE = L3.GRADE ";
        $query .= "           AND L2.HR_CLASS = L3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER IN ('".CTRL_SEMESTER."', '9') ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND || '-' || T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD = '".$model->field["COMMITTEE"]."' ";
        } else {
            $query .= "     AND T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD = '".$model->field["COMMITTEE"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     L2.GRADE, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO ";
        return $query;
    }

    //校時
    function getPeriodcd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '　' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //勤怠
    function getDicd()
    {
        $query  = " SELECT ";
        $query .= "     DI_CD AS VALUE, ";
        $query .= "     DI_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND DI_CD = '1' ";
        $query .= " ORDER BY ";
        $query .= "     DI_CD ";

        return $query;
    }

    //対象理由取得
    function getDiRemarkcd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C900' AND ";
        $query .= "     NAMECD2 BETWEEN '200' AND '299' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //受付番号Max値の取得
    function getMaxSeqno()
    {
        $query  = "SELECT VALUE(MAX(SEQNO),0) + 1 AS SEQNO ";
        $query .= "FROM   ATTEND_PETITION_HDAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //追加
    function &getInsertQuery($db, $model, $schregNo, $seq)
    {
        $ctrl_date = CTRL_DATE;

        /*** 出欠届けヘッダデータ ***/

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQNO"][NUMBER]          = $seq;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["FIRSTREGISTER"][TEXT]    = STAFFCD;
        $data["FIRSTDATE"][NUMBER]      = "'{$ctrl_date}-' || SUBSTR(CHAR(SYSDATE()),12)";
        $data["FROMDATE"][TEXT]         = str_replace("/","-",$model->field["FROMDATE"]);
        $data["TODATE"][TEXT]           = str_replace("/","-",$model->field["TODATE"]);
        $data["FROMPERIOD"][TEXT]       = $model->field["FROMPERIOD"];
        $data["TOPERIOD"][TEXT]         = $model->field["TOPERIOD"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];

        for($i = 1; $i <= 10; $i++){
            $cd = $i - 1;
            $data["DI_REMARK_CD".$i][TEXT] = ($model->selectdata[$cd]) ? sprintf("%03d",$model->selectdata[$cd]) : "";
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "'{$ctrl_date}-' || SUBSTR(CHAR(SYSDATE()),12)";

        $query = Query::insertSQL($data, "ATTEND_PETITION_HDAT");
        $db->query($query);

        /*** 出欠届けデータ ***/

        knjc231Query::getInsertAttendPDat($model, $db, $schregNo, $seq);
        return true;
    }

    //出欠届けデータの追加
    function getInsertAttendPDat($model, $db, $schregNo, $seq)
    {
        $attenddate = str_replace("/","-",$model->field["FROMDATE"]);//初期値：開始日付
        //開始日付から終了日付までのループ
        for ($i = 1; $attenddate <= str_replace("/","-",$model->field["TODATE"]); $i++) {
            //開始校時から終了校時までのループ
            for ($ip = 0; $ip < get_count($model->arr_period); $ip++) {
                //開始校時＜終了校時の場合
                if ($model->field["FROMDATE"] < $model->field["TODATE"]) {
                    if ($attenddate == str_replace("/","-",$model->field["FROMDATE"])) {
                        if (($model->field["FROMPERIOD"] <= $model->arr_period[$ip]) && 
                            ($model->arr_period[$ip] <= $model->e_period)) {
                            knjc231Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip], $schregNo, $seq);
                        }
                    } else if ($attenddate == str_replace("/","-",$model->field["TODATE"])) {
                        if (($model->s_period <= $model->arr_period[$ip]) && 
                            ($model->arr_period[$ip] <= $model->field["TOPERIOD"])) {
                            knjc231Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip], $schregNo, $seq);
                        }
                    } else {
                        if (($model->s_period <= $model->arr_period[$ip]) && 
                            ($model->arr_period[$ip] <= $model->e_period)) {
                            knjc231Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip], $schregNo, $seq);
                        }
                    }
                //開始校時＝終了校時の場合
                } else {
                    if (($model->field["FROMPERIOD"] <= $model->arr_period[$ip]) && 
                        ($model->arr_period[$ip] <= $model->field["TOPERIOD"])) {
                            knjc231Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip], $schregNo, $seq);
                    }
                }
            }
            $query2 = "VALUES Add_days(date('".str_replace("/","-",$model->field["FROMDATE"])."'), ".$i.")";
            $attenddate = $db->getOne($query2); //次の日
        }

        return true;
    }

    //追加(出欠届けデータ)
    function getInsertAttendPDatQuery($model, $db, $attenddate, $period, $schregNo, $seq)
    {
        $di_remark = ($model->selectdata[0] && !$model->field["SONOTA"]) ? knjc231Query::getNameMst($db, "C900", sprintf("%03d",$model->selectdata[0])) : $model->field["SONOTA"];
        $di_remark_cd = ($model->selectdata[0] && !$model->field["SONOTA"]) ? substr($model->selectdata[0], 0, 3) : "";

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQNO"][NUMBER]          = $seq;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["ATTENDDATE"][TEXT]       = $attenddate;
        $data["PERIODCD"][TEXT]         = $period;
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK_CD"][TEXT]     = $di_remark_cd;
        $data["DI_REMARK"][TEXT]        = substr($di_remark, 0, 30);
        $data["INPUT_FLG"][TEXT]        = ($model->selectdata[0] && !$model->field["SONOTA"]) ? "0" : "1";
        $data["EXECUTED"][TEXT]         = "0";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "ATTEND_PETITION_DAT");
        $db->query($query);

        return true;
    }

    //名称取得
    function getNameMst($db, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        $name1 = $db->getOne($query);
        return $name1;
    }
}
?>
