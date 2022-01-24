<?php

require_once('for_php7.php');


class knjf150_2Query extends Query
{

    //生徒
    public function getSchregInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     S1.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO=T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ";
        $query .= "          AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "          AND T1.GRADE = S2.GRADE ";
        $query .= "          AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        return $query;
    }

    //直近データ
    public function getList($model)
    {
        $query  = "SELECT SEQNO, DATE(UPDATED) AS UPDATED1, TIME(UPDATED) AS UPDATED2 ";
        $query .= "FROM   ATTEND_PETITION_HDAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $query .= "  AND  DI_CD IN ('".implode("','", $model->diCd)."') ";
        $query .= "ORDER BY UPDATED DESC ";
        return $query;
    }

    //１レコード取得
    public function getListRow($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEQNO,";
        $query .= "     T1.FROMDATE, ";
        $query .= "     T1.FROMPERIOD, ";
        $query .= "     T1.TODATE, ";
        $query .= "     T1.TOPERIOD, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     L1.SONOTA ";
        $query .= " FROM ";
        $query .= "     ATTEND_PETITION_HDAT T1 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             LT1.SCHREGNO, ";
        $query .= "             MAX(DI_REMARK) AS SONOTA ";
        $query .= "         FROM ";
        $query .= "             ATTEND_PETITION_DAT LT1 ";
        $query .= "         WHERE ";
        $query .= "             LT1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND  LT1.SEQNO = ".$model->field["SEQNO"]." ";
        $query .= "             AND  LT1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $query .= "             AND  LT1.INPUT_FLG = '1' ";
        $query .= "         GROUP BY ";
        $query .= "             LT1.SCHREGNO ";
        $query .= "     ) L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEQNO = ".$model->field["SEQNO"]." ";
        $query .= "     AND T1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";

        return $query;
    }

    //校時
    public function getPeriodcd()
    {
        $query  = "SELECT namecd2, name1 ";
        $query .= "FROM   v_name_mst ";
        $query .= "WHERE  year='".CTRL_YEAR."' and namecd1='B001' ";
        $query .= "ORDER BY namecd2 ";
        return $query;
    }

    //勤怠
    public function getDicd($model)
    {
        $query  = " SELECT ";
        $query .= "     DI_CD AS VALUE, ";
        $query .= "     DI_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND DI_CD IN ('".implode("','", $model->diCd)."') ";
        $query .= " ORDER BY ";
        $query .= "     PETITION_ORDER, ";
        $query .= "     smallint(DI_CD) ";

        return $query;
    }

    //理由一覧取得
    public function getDiRemarkList($model, $namecd1, $di_remark)
    {
        $di_remark = implode($di_remark, "','");

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 NOT IN ('".$di_remark."') AND";
        $query .= "     NAMECD2 NOT LIKE '2__'";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //対象理由取得
    public function getDiRemarkcd1($model, $namecd1)
    {
        $query  = " ";
        for ($i = 1; $i <= 10; $i++) {
            $query .= " SELECT ";
            $query .= "     T1.DI_REMARK_CD".$i." AS VALUE, ";
            $query .= "     T1.DI_REMARK_CD".$i." || ' ' || T2.NAME1 AS LABEL, ";
            $query .= "     ".$i." AS SORT ";
            $query .= " FROM ";
            $query .= "     ATTEND_PETITION_HDAT T1 ";
            $query .= "     LEFT JOIN V_NAME_MST T2 ON ";
            $query .= "         T1.YEAR = T2.YEAR AND T2.NAMECD1 = '".$namecd1."' AND T1.DI_REMARK_CD".$i." = T2.NAMECD2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEQNO = ".$model->field["SEQNO"]." AND ";
            $query .= "     T1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
            if ($i <= 9) {
                $query .= " UNION ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //対象理由取得
    public function getDiRemarkcd2($model, $namecd1, $di_remark)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$di_remark."' ";

        return $query;
    }

    //受付番号Max値の取得
    public function getMaxSeqno()
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT VALUE(MAX(SEQNO),0) + 1 AS SEQNO ";
        $query .= "FROM   ATTEND_PETITION_HDAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $max_seqno = $db->getOne($query);
        Query::dbCheckIn($db);
        return $max_seqno;
    }

    //追加
    public function &getInsertQuery($model)
    {
        $ctrl_date = CTRL_DATE;
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*** 出欠届けヘッダデータ ***/

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQNO"][NUMBER]          = $model->field["SEQNO"];
        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        $data["FIRSTREGISTER"][TEXT]    = STAFFCD;
        $data["FIRSTDATE"][NUMBER]      = "'{$ctrl_date}-' || SUBSTR(CHAR(SYSDATE()),12)";
        $data["FROMDATE"][TEXT]         = str_replace("/", "-", $model->field["FROMDATE"]);
        $data["TODATE"][TEXT]           = str_replace("/", "-", $model->field["TODATE"]);
        $data["FROMPERIOD"][TEXT]       = $model->field["FROMPERIOD"];
        $data["TOPERIOD"][TEXT]         = $model->field["TOPERIOD"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];

        for ($i = 1; $i <= 10; $i++) {
            $cd = $i - 1;
            $data["DI_REMARK_CD".$i][TEXT] = ($model->selectdata[$cd]) ? sprintf("%03d", $model->selectdata[$cd]) : "";
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "'{$ctrl_date}-' || SUBSTR(CHAR(SYSDATE()),12)";

        $query = Query::insertSQL($data, "attend_petition_hdat");
        $db->query($query);

        /*** 出欠届けデータ ***/

        knjf150_2Query::getInsertAttendPDat($model, $db);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //更新
    public function &getUpdateQuery($model)
    {
        $ctrl_date = CTRL_DATE;
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*** 出欠届けヘッダデータ ***/

        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        $data["FROMDATE"][TEXT]         = str_replace("/", "-", $model->field["FROMDATE"]);
        $data["TODATE"][TEXT]           = str_replace("/", "-", $model->field["TODATE"]);
        $data["FROMPERIOD"][TEXT]       = $model->field["FROMPERIOD"];
        $data["TOPERIOD"][TEXT]         = $model->field["TOPERIOD"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];

        for ($i = 1; $i <= 10; $i++) {
            $cd = $i - 1;
            $data["DI_REMARK_CD".$i][TEXT] = ($model->selectdata[$cd]) ? sprintf("%03d", $model->selectdata[$cd]) : "";
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]       = "'{$ctrl_date}-' || SUBSTR(CHAR(SYSDATE()),12)";

        $where  = "WHERE  YEAR = '".CTRL_YEAR."' ";
        $where .= "  AND  SEQNO = ".$model->field["SEQNO"]." ";

        $query = Query::updateSQL($data, "attend_petition_hdat", $where);
        $db->query($query);

        /*** 出欠届けデータ ***/

        knjf150_2Query::getDeleteAttendPDat($model, $db);
        knjf150_2Query::getInsertAttendPDat($model, $db);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //削除
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM attend_petition_hdat ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  SEQNO = ".$model->field["SEQNO"]." ";
        $db->query($query);

        knjf150_2Query::getDeleteAttendPDat($model, $db);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //名称マスタ(備考)のMax値の取得
    public function getMaxNamecd2()
    {
        return "SELECT value(max(smallint(namecd2)),0) + 1 FROM name_mst WHERE namecd1='C999' ";
    }

    //出欠届けデータの追加
    public function getInsertAttendPDat($model, $db)
    {
        $attenddate = str_replace("/", "-", $model->field["FROMDATE"]);//初期値：開始日付
        //開始日付から終了日付までのループ
        for ($i = 1; $attenddate <= str_replace("/", "-", $model->field["TODATE"]); $i++) {
            //開始校時から終了校時までのループ
            for ($ip = 0; $ip < get_count($model->arr_period); $ip++) {
                //開始校時＜終了校時の場合
                if ($model->field["FROMDATE"] < $model->field["TODATE"]) {
                    if ($attenddate == str_replace("/", "-", $model->field["FROMDATE"])) {
                        if (($model->field["FROMPERIOD"] <= $model->arr_period[$ip]) &&
                            ($model->arr_period[$ip] <= $model->e_period)) {
                            knjf150_2Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                        }
                    } elseif ($attenddate == str_replace("/", "-", $model->field["TODATE"])) {
                        if (($model->s_period <= $model->arr_period[$ip]) &&
                            ($model->arr_period[$ip] <= $model->field["TOPERIOD"])) {
                            knjf150_2Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                        }
                    } else {
                        if (($model->s_period <= $model->arr_period[$ip]) &&
                            ($model->arr_period[$ip] <= $model->e_period)) {
                            knjf150_2Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                        }
                    }
                //開始校時＝終了校時の場合
                } else {
                    if (($model->field["FROMPERIOD"] <= $model->arr_period[$ip]) &&
                        ($model->arr_period[$ip] <= $model->field["TOPERIOD"])) {
                            knjf150_2Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                    }
                }
            }
            $query2 = "VALUES Add_days(date('".str_replace("/", "-", $model->field["FROMDATE"])."'), ".$i.")";
            $attenddate = $db->getOne($query2); //次の日
        }

        return true;
    }

    //削除(出欠届けデータ)
    public function getDeleteAttendPDat($model, $db)
    {
        $query  = "DELETE FROM attend_petition_dat ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  SEQNO = ".$model->field["SEQNO"]." ";
        $db->query($query);

        return true;
    }

    //追加(出欠届けデータ)
    public function getInsertAttendPDatQuery($model, $db, $attenddate, $period)
    {
        $di_remark    = ($model->selectdata[0] && !$model->field["SONOTA"]) ? knjf150_2Query::getNameMst($db, "C900", sprintf("%03d", $model->selectdata[0])) : $model->field["SONOTA"];
        $di_remark_cd = ($model->selectdata[0] && !$model->field["SONOTA"]) ? substr($model->selectdata[0], 0, 3) : "";

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQNO"][NUMBER]          = $model->field["SEQNO"];
        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        $data["ATTENDDATE"][TEXT]       = $attenddate;
        $data["PERIODCD"][TEXT]         = $period;
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK_CD"][TEXT]     = $di_remark_cd;
        $data["DI_REMARK"][TEXT]        = substr($di_remark, 0, 30);
        $data["INPUT_FLG"][TEXT]        = ($model->selectdata[0] && !$model->field["SONOTA"]) ? "0" : "1";
        $data["EXECUTED"][TEXT]         = "0";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "attend_petition_dat");
        $db->query($query);

        return true;
    }

    //名称取得
    public function getNameMst($db, $namecd1, $namecd2)
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

    //Z010取得
    public function getZ010name1()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
