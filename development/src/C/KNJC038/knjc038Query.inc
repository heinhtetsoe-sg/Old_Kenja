<?php

require_once('for_php7.php');

class knjc038query extends Query
{

    //勤怠コード取得
    public function getDiCd($kintai, $year)
    {
        $query  = " SELECT ";
        $query .= "         DI_CD || ' ' || DI_NAME1 AS LABEL, ";
        $query .= "         DI_CD AS VALUE, ";
        $query .= "         DI_NAME1 ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND REP_DI_CD IN ({$kintai}) ";
        $query .= " ORDER BY ";
        $query .= "     INT(DI_CD) ";

        return $query;
    }

    //今年度取得
    public function getThisYear()
    {
        $query .= " SELECT ";
        $query .= "     MAX(CTRL_YEAR) ";
        $query .= " FROM ";
        $query .= "     CONTROL_MST ";

        return $query;
    }

    //データの追加(出欠データ)
    public function insertQueryCsv1(&$db, $data, $year, $schedule)
    {
        $staffcd = STAFFCD;
        $day = str_replace('/', '-', $data["DATE"]);

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$year}' ";
        $query .= "     AND SCHREGNO   = '{$schedule[0]}' ";
        $query .= "     AND ATTENDDATE = '{$schedule[1]}' ";
        $query .= "     AND PERIODCD   = '{$schedule[2]}' ";

        $db->query($query);

        $query  = " INSERT INTO ";
        $query .= "     ATTEND_DAT( ";
        $query .= "         SCHREGNO, ";
        $query .= "         ATTENDDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         DI_CD, ";
        $query .= "         DI_REMARK_CD, ";
        $query .= "         DI_REMARK, ";
        $query .= "         YEAR, ";
        $query .= "         REGISTERCD, ";
        $query .= "         UPDATED ";
        $query .= "         )";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE AS ATTENDDATE, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     '{$data["DI_CD"]}' AS DI_CD, ";
        $query .= "     '{$data["DI_REMARK_CD"]}' AS DI_REMARK_CD, ";
        $query .= "     '{$data["DI_REMARK"]}' AS DI_REMARK, ";
        $query .= "     T1.YEAR, ";
        $query .= "     '{$staffcd}' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = T2.YEAR AND ";
        $query .= "     T1.YEAR        = '{$year}' AND ";
        $query .= "     T1.SEMESTER    = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD     = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD     = '{$schedule[3]}' AND ";
        $query .= "     T2.EXECUTEDATE = '{$schedule[1]}' AND ";
        $query .= "     T2.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE AND ";
        $query .= "     T1.SCHREGNO    = '{$schedule[0]}' AND ";
        $query .= "     T2.PERIODCD    = '{$schedule[2]}' ";

        $db->query($query);
    }

    //データの削除(出欠データ)
    public function deleteQueryCsv1(&$db, $data, $year)
    {
        $attenddate = str_replace('/', '-', $data["DATE"]);

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHREGNO    = '".$data["SCHREGNO"]."' AND ";
        $query .= "     ATTENDDATE  = '".$attenddate."' AND ";
        $query .= "     PERIODCD    = '".$data["PERIODCD"]."' AND ";
        $query .= "     DI_CD       = '".$data["DI_CD"]."' ";

        $db->query($query);
    }

    //時間割データ取得
    public function getSchChrDat1($data, $year)
    {
        $staffcd = STAFFCD;
        $day = str_replace('/', '-', $data["DATE"]);

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = T2.YEAR AND ";
        $query .= "     T1.YEAR        = '{$year}' AND ";
        $query .= "     T1.SEMESTER    = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD     = T2.CHAIRCD AND ";
        $query .= "     T2.EXECUTEDATE = '{$day}' AND ";
        $query .= "     T1.SCHREGNO    = '{$data["SCHREGNO"]}' AND ";
        $query .= "     T2.PERIODCD    = '{$data["PERIODCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //データの追加(出欠データ)
    public function insertQueryCsv2(&$db, $data, $year, $schedule)
    {
        $staffcd = STAFFCD;

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "     AND SCHREGNO    = '{$schedule[0]}' ";
        $query .= "     AND ATTENDDATE  = '{$schedule[1]}' ";
        $query .= "     AND PERIODCD    = '{$schedule[2]}' ";

        $db->query($query);

        $query  = " INSERT INTO ";
        $query .= "     ATTEND_DAT( ";
        $query .= "         SCHREGNO, ";
        $query .= "         ATTENDDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         DI_CD, ";
        $query .= "         DI_REMARK_CD, ";
        $query .= "         DI_REMARK, ";
        $query .= "         YEAR, ";
        $query .= "         REGISTERCD, ";
        $query .= "         UPDATED ";
        $query .= "         )";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE AS ATTENDDATE, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     '{$data["DI_CD"]}' AS DI_CD, ";
        $query .= "     '{$data["DI_REMARK_CD"]}' AS DI_REMARK_CD, ";
        $query .= "     '{$data["DI_REMARK"]}' AS DI_REMARK, ";
        $query .= "     T1.YEAR, ";
        $query .= "     '{$staffcd}' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '{$year}' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD  = '{$schedule[3]}' AND ";
        $query .= "     T2.EXECUTEDATE = '{$schedule[1]}' AND ";
        $query .= "     T2.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE AND ";
        $query .= "     T2.PERIODCD = '{$schedule[2]}' AND ";
        $query .= "     T1.SCHREGNO = '{$schedule[0]}' ";

        $db->query($query);
    }

    //データの削除(出欠データ)
    public function deleteQueryCsv2(&$db, $data, $year)
    {
        $sdate = str_replace('/', '-', $data["SDATE"]);
        $edate = str_replace('/', '-', $data["EDATE"]);

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHREGNO    = '".$data["SCHREGNO"]."' AND ";
        $query .= "     DI_CD       = '".$data["DI_CD"]."' AND ";
        $query .= "     ATTENDDATE BETWEEN '".$sdate."' AND '".$edate."' ";

        $db->query($query);
    }

    //時間割データ取得
    public function getSchChrDat2($data, $year)
    {
        $staffcd = STAFFCD;
        $sday = str_replace('/', '-', $data["SDATE"]);
        $eday = str_replace('/', '-', $data["EDATE"]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '{$year}' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     T2.EXECUTEDATE BETWEEN '{$sday}' AND '{$eday}' AND ";
        $query .= "     T2.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE AND ";
        $query .= "     T1.SCHREGNO = '{$data["SCHREGNO"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //生徒有無
    public function checkSchreg($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //その日その校時のその生徒が受講した講座コードを取得してくる(取れなければ引数が不正)
    public function getChaircd($data, $model)
    {
        $day = str_replace('/', '-', $data["DATE"]);

        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCH_CHR_DAT L1 ON  L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "                    AND L1.EXECUTEDATE = '{$day}' ";
        $query .= "                    AND L1.PERIODCD = '{$data["PERIODCD"]}' ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$data["SCHREGNO"]}' ";
        $query .= "     AND L1.EXECUTEDATE BETWEEN APPDATE AND APPENDDATE ";

        return $query;
    }

    //今年度の開始日を取得してくる
    public function getYearDate($year)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER = '9' ";

        return $query;
    }

    //出欠データ存在チェック
    public function checkAttendDat($model, $data)
    {
        $attenddate = str_replace('/', '-', $data["DATE"]);
        $sdate = str_replace('/', '-', $data["SDATE"]);
        $edate = str_replace('/', '-', $data["EDATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SCHREGNO    = '".$data["SCHREGNO"]."' AND ";
        $query .= "     DI_CD       = '".$data["DI_CD"]."' AND ";
        if ($model->field["HANI_DIV"] == "1") {
            $query .= "     PERIODCD    = '".$data["PERIODCD"]."' AND ";
            $query .= "     ATTENDDATE  = '".$attenddate."' ";
        } else {
            $query .= "     ATTENDDATE  BETWEEN '".$sdate."' AND '".$edate."' ";
        }

        return $query;
    }

    //備考コード存在チェック
    public function checkDiRemarkCd($model, $di_remark_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["YEAR"]."' AND ";
        $query .= "     NAMECD1 = 'C901' AND ";
        $query .= "     NAMECD2 = '{$di_remark_cd}' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = "KNJC038";
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }
}
