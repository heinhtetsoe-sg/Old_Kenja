<?php

require_once('for_php7.php');

class knjf152Query extends Query {

    //年度取得
   function getYear()
   {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE,";
        $query .= "     YEAR AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
    
    //学期リスト
    function getList($year) {
        $query  = " SELECT  ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER,  ";
        $query .= "     SEMESTERNAME,  ";
        $query .= "     YEAR(SDATE) AS SYEAR, ";
        $query .= "     YEAR(EDATE) AS EYEAR, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "         THEN MONTH(SDATE) + 12 ";
        $query .= "         ELSE MONTH(SDATE) ";
        $query .= "     END AS SMONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "         THEN MONTH(EDATE) + 12 ";
        $query .= "         ELSE MONTH(EDATE) ";
        $query .= "     END AS EMONTH, ";
        $query .= "     DAY(SDATE) AS SDAY, ";
        $query .= "     DAY(EDATE) AS EDAY ";
        $query .= " FROM  ";
        $query .= "     SEMESTER_MST  ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '{$year}'  ";
        $query .= "     AND SEMESTER = '9'  ";
        $query .= " ORDER BY  ";
        $query .= "     SEMESTER  ";

        return $query;
    }

    //月の表示文字をDBから取得
    function getMonthName($year, $month) {
        $query .= " SELECT DISTINCT  ";
        $query .= "     NAME1 ";
        $query .= " FROM  ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE  ";
        $query .= "     YEAR  = '{$year}'  ";
        $query .= "     AND NAMECD1 = 'Z005'  ";
        $query .= "     AND NAMECD2 = '{$month}' ";

        return $query;
    }

    //授業日数の取得
    function getLesson($year, $month) {
        $query  = " SELECT ";
        $query .= "     LESSON ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_LESSON_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' ";
        $query .= " AND MONTH    = '{$month}' ";

        return $query;
    }

    //削除処理
    function getDeleteQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     NURSEOFF_LESSON_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year."' ";

        return $query;
    }

    //更新処理
    function getInsertQuery($model, $month, $lesson) {

        $data = array();
        $data["YEAR"][TEXT]           = $model->year;
        $data["MONTH"][TEXT]          = $month;
        $data["LESSON"][TEXT]         = $lesson;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $query = Query::insertSQL($data, "NURSEOFF_LESSON_DAT");
        return $query;
    }
}
?>