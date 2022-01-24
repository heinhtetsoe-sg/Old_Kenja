<?php

require_once('for_php7.php');

class knjc020gQuery extends Query
{

    //講座
    public function getChair($model)
    {
        $query .= " SELECT ";
        $query .= "     t1.CHAIRCD AS VALUE, ";
        $query .= "     t1.CHAIRCD || ':' || t1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT t1 ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " LEFT JOIN CHAIR_STF_DAT t2 ";
            $query .= " ON t1.YEAR = t2.YEAR ";
            $query .= " AND t1.SEMESTER = t2.SEMESTER ";
            $query .= " AND t1.CHAIRCD = t2.CHAIRCD  ";
        }
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '{$model->year}' ";
        $query .= "    AND t1.SEMESTER = '{$model->semester}' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND t2.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     t1.CHAIRCD ";

        return $query;
    }

    //講座
    public function selectQuery($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.HR_CLASS ASC, ATTENDNO ASC) AS SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     RIGHT(T1.ATTENDNO, 2) AS ATTENDNO, ";
        $query .= "     CASE WHEN T2.SEX = '1' THEN '*' ELSE '' END AS SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T3.CHAIRCD, ";
        $query .= "     T3.APPDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " INNER JOIN CHAIR_STD_DAT T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND ('".str_replace('/', '-', $model->startDate)."' BETWEEN T3.APPDATE AND T3.APPENDDATE ";
        $query .= "     OR '".str_replace('/', '-', $model->endDate)."' BETWEEN T3.APPDATE AND T3.APPENDDATE) ";
        $query .= "     AND T3.CHAIRCD = '{$model->chairCd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //登録データ
    public function selectQuery2($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T7.NAME1 AS PERIODNAME, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     CASE WHEN T3.ATTENDDATE IS NULL THEN '' WHEN T4.DI_CD IS NULL THEN 'SHUSSEKI' ELSE T4.DI_CD END AS DI_CD1, ";
        $query .= "     CASE WHEN T5.ATTENDDATE IS NULL THEN '' WHEN T6.DI_CD IS NULL THEN 'SHUSSEKI' ELSE T6.DI_CD END AS DI_CD2, ";
        $query .= "     T4.REMARK1 AS KESSEKI1, ";
        $query .= "     T6.REMARK1 AS KESSEKI2, ";
        $query .= "     CASE WHEN T3.ATTENDDATE IS NULL THEN '' WHEN T4.DI_CD IS NULL THEN '出席' ELSE T4.DI_MARK END AS DI_MARK1, ";
        $query .= "     CASE WHEN T5.ATTENDDATE IS NULL THEN '' WHEN T6.DI_CD IS NULL THEN '出席' ELSE T6.DI_MARK END AS DI_MARK2, ";
        $query .= "     T5.DI_REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= " INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "     ON T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " LEFT JOIN ATTEND_CHAIR_STF_DAT T3 ";
        $query .= "     ON T1.EXECUTEDATE = T3.ATTENDDATE ";
        $query .= "     AND T2.SCHREGNO =T3.SCHREGNO ";
        $query .= "     AND T1.PERIODCD = T3.PERIODCD ";
        $query .= "     AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= " LEFT JOIN ATTEND_DI_CD_DAT T4 ";
        $query .= "     ON T3.DI_CD = T4.DI_CD ";
        $query .= "     AND T1.YEAR = T4.YEAR ";
        $query .= " LEFT JOIN ATTEND_DAT T5 ";
        $query .= "     ON T1.EXECUTEDATE = T5.ATTENDDATE ";
        $query .= "     AND T2.SCHREGNO =T5.SCHREGNO ";
        $query .= "     AND T1.PERIODCD = T5.PERIODCD ";
        $query .= "     AND T1.CHAIRCD = T5.CHAIRCD ";
        $query .= "     AND VALUE(T5.TMP_FLG,'0') !='1'  ";
        $query .= " LEFT JOIN ATTEND_DI_CD_DAT T6 ";
        $query .= "     ON T5.DI_CD = T6.DI_CD ";
        $query .= "     AND T1.YEAR = T6.YEAR ";
        $query .= " LEFT JOIN V_NAME_MST T7 ";
        $query .= "     ON T1.YEAR = T7.YEAR ";
        $query .= "     AND T1.PERIODCD = T7.NAMECD2 ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.EXECUTEDATE BETWEEN '".str_replace('/', '-', $model->startDate)."' AND '".str_replace('/', '-', $model->endDate)."' ";
        $query .= "     AND T1.CHAIRCD = '{$model->chairCd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD ";

        return $query;
    }

    //校時
    public function getPeriod($model, $namecd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
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

    public function selectAttendChairStfDat($model, $row)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='{$row['SCHREGNO']}' ";
        $query .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $query .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";

        return $query;
    }

    public function updateAttendChairStfDat($db, $model, $row)
    {
        $data["DI_CD"][TEXT]        = $row['NEW_DI_CD'];
        $data["DI_REMARK_CD"][TEXT] = null;
        $data["DI_REMARK"][TEXT]    = null;
        $data["YEAR"][TEXT]         = $model->year;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "     SCHREGNO ='{$row['SCHREGNO']}' ";
        $where .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $where .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $where .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";

        $db->query(Query::updateSQL($data, "ATTEND_CHAIR_STF_DAT", $where));
    }

    public function insertAttendChairStfDat($db, $model, $row)
    {
        $data["SCHREGNO"][TEXT]     = $row['SCHREGNO'];
        $data["ATTENDDATE"][TEXT]   = $row['EXECUTEDATE'];
        $data["PERIODCD"][TEXT]     = $row['PERIODCD'];
        $data["CHAIRCD"][TEXT]      = $row['CHAIRCD'];
        $data["DI_CD"][TEXT]        = $row['NEW_DI_CD'];
        $data["DI_REMARK_CD"][TEXT] = null;
        $data["DI_REMARK"][TEXT]    = null;
        $data["YEAR"][TEXT]         = $model->year;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $db->query(Query::insertSQL($data, "ATTEND_CHAIR_STF_DAT"));
    }

    public function deleteAttendChairStfDat($db, $model, $row)
    {
        $query .= " DELETE FROM ";
        $query .= "     ATTEND_CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='{$row['SCHREGNO']}' ";
        $query .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $query .= "     AND CHAIRCD = '{$row['CHAIRCD']}'; ";

        $db->query($query);
    }

    public function selectAttendDatCheck1($model, $row)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='{$row['SCHREGNO']}' ";
        $query .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $query .= "     AND TMP_FLG = '1'; ";

        return $query;
    }

    public function selectAttendDatCheck2($model, $row)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='{$row['SCHREGNO']}' ";
        $query .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";
        $query .= "     AND VALUE(TMP_FLG, '0') = '0'; ";

        return $query;
    }

    public function updateAttendDat($db, $model, $row)
    {
        $data["CHAIRCD"][TEXT]      = $row['CHAIRCD'];
        $data["DI_CD"][TEXT]        = $row['NEW_DI_CD'];
        $data["DI_REMARK_CD"][TEXT] = null;
        $data["DI_REMARK"][TEXT]    = null;
        $data["YEAR"][TEXT]         = $model->year;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $data["TMP_FLG"][TEXT]      = '1';

        $where .= " WHERE ";
        $where .= "     SCHREGNO ='{$row['SCHREGNO']}' ";
        $where .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $where .= "     AND PERIODCD = '{$row['PERIODCD']}' ";

        $db->query(Query::updateSQL($data, "ATTEND_DAT", $where));
    }

    public function insertAttendDat($db, $model, $row)
    {
        $data["SCHREGNO"][TEXT]     = $row['SCHREGNO'];
        $data["ATTENDDATE"][TEXT]   = $row['EXECUTEDATE'];
        $data["PERIODCD"][TEXT]     = $row['PERIODCD'];
        $data["CHAIRCD"][TEXT]      = $row['CHAIRCD'];
        $data["DI_CD"][TEXT]        = $row['NEW_DI_CD'];
        $data["DI_REMARK_CD"][TEXT] = null;
        $data["DI_REMARK"][TEXT]    = null;
        $data["YEAR"][TEXT]         = $model->year;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $data["TMP_FLG"][TEXT]      = '1';

        $db->query(Query::insertSQL($data, "ATTEND_DAT"));
    }

    public function deleteAttendDat($db, $model, $row)
    {
        $query .= " DELETE FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='{$row['SCHREGNO']}' ";
        $query .= "     AND ATTENDDATE = '{$row['EXECUTEDATE']}' ";
        $query .= "     AND PERIODCD = '{$row['PERIODCD']}' ";

        $db->query($query);
    }
}
