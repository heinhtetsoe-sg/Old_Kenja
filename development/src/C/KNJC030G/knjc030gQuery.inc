<?php
class knjc030gQuery extends Query
{
    //組名
    public function getHrName($model)
    {
        $query .= " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= "     AND HR_CLASS = '{$model->hrClass}' ";

        return $query;
    }

    //登録データ
    public function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T3.CHAIRABBV, ";
        $query .= "     T5.STAFFNAME_SHOW, ";
        $query .= "     CASE WHEN T6.ATTENDDATE IS NULL THEN '' WHEN T6.DI_CD IS NULL THEN 'SHUSSEKI' ELSE T6.DI_CD END AS DI_CD, ";
        $query .= "     CASE WHEN T7.ATTENDDATE IS NULL THEN '' WHEN T7.DI_CD IS NULL THEN 'SHUSSEKI' ELSE T7.DI_CD END AS DI_CD2, ";
        $query .= "     T9.REMARK1 AS KESSEKI1, ";
        $query .= "     T8.REMARK1 AS KESSEKI2, ";
        $query .= "     CASE WHEN T7.ATTENDDATE IS NULL THEN '' WHEN T7.DI_CD IS NULL THEN '出席' ELSE T8.DI_MARK END AS DI_CD2_NAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= " INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "     ON T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " LEFT JOIN CHAIR_DAT T3 ";
        $query .= "     ON T1.CHAIRCD = T3.CHAIRCD ";
        $query .= "     AND T2.YEAR = T3.YEAR ";
        $query .= "     AND T2.SEMESTER = T3.SEMESTER ";
        $query .= " LEFT JOIN CHAIR_STF_DAT T4 ";
        $query .= "     ON T1.CHAIRCD = T4.CHAIRCD ";
        $query .= "     AND T2.YEAR = T4.YEAR ";
        $query .= "     AND T2.SEMESTER = T4.SEMESTER ";
        $query .= " LEFT JOIN STAFF_MST T5 ";
        $query .= "     ON T4.STAFFCD = T5.STAFFCD ";
        $query .= " LEFT JOIN ATTEND_DAT T6 ";
        $query .= "     ON T1.EXECUTEDATE = T6.ATTENDDATE ";
        $query .= "     AND T2.SCHREGNO =T6.SCHREGNO ";
        $query .= "     AND T1.PERIODCD = T6.PERIODCD ";
        $query .= "     AND VALUE(T6.TMP_FLG,'0') != '1' ";
        $query .= " LEFT JOIN ATTEND_CHAIR_STF_DAT T7 ";
        $query .= "     ON T1.EXECUTEDATE = T7.ATTENDDATE ";
        $query .= "     AND T2.SCHREGNO =T7.SCHREGNO ";
        $query .= "     AND T1.PERIODCD = T7.PERIODCD ";
        $query .= " LEFT JOIN ATTEND_DI_CD_DAT T8 ";
        $query .= "     ON T7.DI_CD = T8.DI_CD ";
        $query .= "     AND T1.YEAR = T8.YEAR ";
        $query .= " LEFT JOIN ATTEND_DI_CD_DAT T9 ";
        $query .= "     ON T6.DI_CD = T9.DI_CD ";
        $query .= "     AND T1.YEAR = T9.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE BETWEEN '".str_replace('/', '-', $model->startDate)."' AND '".str_replace('/', '-', $model->endDate)."' ";
        $query .= "     AND T2.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T2.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T2.SEMESTER = '{$model->exp_semester}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }
    //登録データ
    public function selectQuery2($model, $selectdate)
    {
        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     CASE WHEN T6.ATTENDDATE IS NULL THEN '0' ELSE '1' END AS DI_CD_FLAG, ";
        $query .= "     CASE WHEN T6.ATTENDDATE IS NULL THEN '' ELSE T6.DI_REMARK END AS DI_REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= " INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "     ON T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " LEFT JOIN ATTEND_DAT T6 ";
        $query .= "     ON T1.EXECUTEDATE = T6.ATTENDDATE ";
        $query .= "     AND T2.SCHREGNO =T6.SCHREGNO ";
        $query .= "     AND T1.PERIODCD = T6.PERIODCD ";
        $query .= "     AND VALUE(T6.TMP_FLG,'0') != '1' ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = '{$selectdate}' ";
        $query .= "     AND T2.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T2.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T2.SEMESTER = '{$model->exp_semester}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }
    //登録データ
    public function getAttendDayDat($model, $selectdate)
    {
        $query .= " SELECT ";
        $query .= "     DI_REMARK ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND ATTENDDATE = '{$selectdate}' ";
        $query .= "     AND DI_CD = '0' ";

        return $query;
    }
    //校時
    public function getPeriod($model, $namecd)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= " INNER JOIN COURSE_MST T3 ";
        $query .= "     ON T2.COURSECD = T3.COURSECD ";
        $query .= "     AND T1.NAMECD2 >=T3.S_PERIODCD ";
        $query .= "     AND T1.NAMECD2 <= T3.E_PERIODCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.NAMECD1 = '{$namecd}' ";
        $query .= "     AND T2.SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND T2.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //欠時ステータス
    public function getDiCd($model)
    {
        $query .= " SELECT ";
        $query .= "     DI_MARK AS LABEL, ";
        $query .= "     DI_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     ORDER ";

        return $query;
    }

    public function selectAttendDat($model, $row)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='{$model->schregno}' ";
        $query .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $query .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";

        return $query;
    }

    public function selectAttendDat2($model, $row)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";

        return $query;
    }

    public function updateAttendDat($db, $model, $row)
    {
        $data["DI_CD"][TEXT]        = $row['NEW_DI_CD'];
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TMP_FLG"][TEXT]      = '0';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "     SCHREGNO ='{$model->schregno}' ";
        $where .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $where .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $where .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";

        $db->query(Query::updateSQL($data, "ATTEND_DAT", $where));
    }

    public function insertAttendDat($db, $model, $row)
    {
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["ATTENDDATE"][TEXT]   = $row['EXECUTEDATE'];
        $data["PERIODCD"][TEXT]     = $row['PERIODCD'];
        $data["CHAIRCD"][TEXT]      = $row['CHAIRCD'];
        $data["DI_CD"][TEXT]        = $row['NEW_DI_CD'];
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TMP_FLG"][TEXT]      = '0';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $db->query(Query::insertSQL($data, "ATTEND_DAT"));
    }

    public function deleteAttendDat($db, $model, $row)
    {
        $query .= " DELETE FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='{$model->schregno}' ";
        $query .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $query .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";

        $db->query($query);
    }

    public function insertUpdateSchChrHrateDat($db, $model, $row, $executed)
    {
        $data["EXECUTED"][TEXT]     = $executed;
        $data["ATTESTOR "][TEXT]    = STAFFCD;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "     EXECUTEDATE = '{$row['EXECUTEDATE']}' ";
        $where .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $where .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";
        $where .= "     AND GRADE = '{$model->grade}'; ";
        $where .= "     AND HR_CLASS = '{$model->hrClass}'; ";

        if ($db->getOne("SELECT COUNT(*) FROM SCH_CHR_HRATE_DAT " . $where) > 0) {
            $query = Query::updateSQL($data, "SCH_CHR_HRATE_DAT", $where);
        } else {
            $data["EXECUTEDATE"][TEXT] = $row['EXECUTEDATE'];
            $data["PERIODCD"][TEXT]    = $row['PERIODCD'];
            $data["CHAIRCD"][TEXT]     = $row['CHAIRCD'];
            $data["GRADE"][TEXT]       = $model->grade;
            $data["HR_CLASS"][TEXT]    = $model->hrClass;

            $query = Query::insertSQL($data, "SCH_CHR_HRATE_DAT");
        }
        $db->query($query);
    }

    public function updateAttendDayDat($db, $model, $diRemark)
    {
        $data["DI_REMARK"][TEXT]    = $diRemark;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "     SCHREGNO ='{$model->schregno}' ";
        $where .= "     AND ATTENDDATE = '{$model->selectdate}' ";

        if ($db->getOne("SELECT COUNT(*) FROM ATTEND_DAY_DAT {$where}") > 0) {
            $db->query(Query::updateSQL($data, "ATTEND_DAY_DAT", $where));
        } else {
            $data["SCHREGNO"][TEXT]    = $model->schregno;
            $data["ATTENDDATE"][TEXT]  = $model->selectdate;
            $data["DI_CD"][TEXT]       = 0;
            $data["YEAR"][TEXT]        = $model->exp_year;

            $db->query(Query::insertSQL($data, "ATTEND_DAY_DAT"));
        }
    }
    public function updateAttendDat2($db, $model, $row, $diRemark)
    {
        $data["DI_REMARK"][TEXT]    = $diRemark;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO ='{$model->schregno}' ";
        $where .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $where .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $where .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";

        $db->query(Query::updateSQL($data, "ATTEND_DAT", $where));
    }
}
