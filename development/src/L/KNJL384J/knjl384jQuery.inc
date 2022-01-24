<?php

require_once('for_php7.php');
class knjl384jquery extends Query
{

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "";
        $query .= " SELECT  T1.YEAR,T1.EXAMNO,T2.NAME, ";
        $query .= "         T1.HR_CLASS,T1.ATTENDNO,T1.PRE_HR_CLASS,T1.PRE_ATTENDNO, ";
        $query .= "         T1.ATTENDFLG1,T1.ATTENDFLG2,T1.SCORE1,T1.SCORE2,T1.TOTAL_SCORE,T1.REMARK, ";
        $query .= "         T1.STATIONCD1,T1.STATIONCD2,T1.STATIONCD3,T1.STATIONCD4, ";
        $query .= "         T1.PRE_INFO1,T1.PRE_INFO2,T1.PRE_INFO3, ";
        $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM    PRE_SCHOOL_INFO_DAT T1 ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR=T1.YEAR AND T2.EXAMNO=T1.EXAMNO ";
        $query .= " WHERE   T1.YEAR = '".$model->examyear."' ";
        $query .= " ORDER BY T1.EXAMNO ";
        return $query;
    }

    //存在チェック(追加・更新・削除用)
    public function getExistCheck($data)
    {
        $query  = "SELECT COUNT(*) FROM PRE_SCHOOL_INFO_DAT ";
        $query .= "WHERE  YEAR      = '".$data["YEAR"]."' ";
        $query .= "AND    EXAMNO    = '".$data["EXAMNO"]."' ";
        return $query;
    }

    //削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM PRE_SCHOOL_INFO_DAT ";
        $query .= "WHERE  YEAR      = '".$data["YEAR"]."' ";
        $query .= "AND    EXAMNO    = '".$data["EXAMNO"]."' ";
        $result = $db->query($query);
    }

    //追加
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]              = $data["YEAR"];
        $data1["EXAMNO"][TEXT]            = $data["EXAMNO"];
        $data1["SEMESTER"][TEXT]          = "1";
        $data1["GRADE"][TEXT]             = "01";
        $data1["HR_CLASS"][TEXT]          = $data["HR_CLASS"];
        $data1["ATTENDNO"][TEXT]          = $data["ATTENDNO"];
        $data1["PRE_HR_CLASS"][TEXT]      = $data["PRE_HR_CLASS"];
        $data1["PRE_ATTENDNO"][TEXT]      = $data["PRE_ATTENDNO"];
        $data1["ATTENDFLG1"][TEXT]        = $data["ATTENDFLG1"];
        $data1["ATTENDFLG2"][TEXT]        = $data["ATTENDFLG2"];
        $data1["SCORE1"][NUMBER]          = $data["SCORE1"];
        $data1["SCORE2"][NUMBER]          = $data["SCORE2"];
        $data1["TOTAL_SCORE"][NUMBER]     = $data["TOTAL_SCORE"];
        $data1["REMARK"][TEXT]            = $data["REMARK"];
        $data1["STATIONCD1"][TEXT]        = $data["STATIONCD1"];
        $data1["STATIONCD2"][TEXT]        = $data["STATIONCD2"];
        $data1["STATIONCD3"][TEXT]        = $data["STATIONCD3"];
        $data1["STATIONCD4"][TEXT]        = $data["STATIONCD4"];
        $data1["PRE_INFO1"][TEXT]         = $data["PRE_INFO1"];
        $data1["PRE_INFO2"][TEXT]         = $data["PRE_INFO2"];
        $data1["PRE_INFO3"][TEXT]         = $data["PRE_INFO3"];
        $data1["REGISTERCD"][TEXT]        = $registercd;
        $data1["UPDATED"][NUMBER]         = "sysdate()";
        $query = Query::insertSQL($data1, "PRE_SCHOOL_INFO_DAT");
        $result = $db->query($query);
    }

    //更新
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]              = $data["YEAR"];
        $data1["EXAMNO"][TEXT]            = $data["EXAMNO"];
        $data1["SEMESTER"][TEXT]          = "1";
        $data1["GRADE"][TEXT]             = "01";
        $data1["HR_CLASS"][TEXT]          = $data["HR_CLASS"];
        $data1["ATTENDNO"][TEXT]          = $data["ATTENDNO"];
        $data1["PRE_HR_CLASS"][TEXT]      = $data["PRE_HR_CLASS"];
        $data1["PRE_ATTENDNO"][TEXT]      = $data["PRE_ATTENDNO"];
        $data1["ATTENDFLG1"][TEXT]        = $data["ATTENDFLG1"];
        $data1["ATTENDFLG2"][TEXT]        = $data["ATTENDFLG2"];
        $data1["SCORE1"][NUMBER]          = $data["SCORE1"];
        $data1["SCORE2"][NUMBER]          = $data["SCORE2"];
        $data1["TOTAL_SCORE"][NUMBER]     = $data["TOTAL_SCORE"];
        $data1["REMARK"][TEXT]            = $data["REMARK"];
        $data1["STATIONCD1"][TEXT]        = $data["STATIONCD1"];
        $data1["STATIONCD2"][TEXT]        = $data["STATIONCD2"];
        $data1["STATIONCD3"][TEXT]        = $data["STATIONCD3"];
        $data1["STATIONCD4"][TEXT]        = $data["STATIONCD4"];
        $data1["PRE_INFO1"][TEXT]         = $data["PRE_INFO1"];
        $data1["PRE_INFO2"][TEXT]         = $data["PRE_INFO2"];
        $data1["PRE_INFO3"][TEXT]         = $data["PRE_INFO3"];
        $data1["REGISTERCD"][TEXT]        = $registercd;
        $data1["UPDATED"][NUMBER]         = "sysdate()";
        $where  = " where YEAR = '".$data["YEAR"]."' ";
        $where .= " and EXAMNO = '".$data["EXAMNO"]."' ";
        $query  = Query::updateSQL($data1, "PRE_SCHOOL_INFO_DAT", $where);
        $result = $db->query($query);
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM w_csvmsg_dat ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]         = $record_no;
        $data1["MSGREMARK"][TEXT]        = $check_error;
        $query = Query::insertSQL($data1, "w_csvmsg_dat");
        $result = $db->query($query);
    }

    //CVS作成用のQUERY
    public function selectCsvQuery()
    {
        $query  = "select * from w_csvmsg_dat";
        return $query;
    }
}
