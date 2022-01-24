<?php

require_once('for_php7.php');

class knjs520Query extends Query {

    //１レコード取得
    function getRow($model, $db)
    {
        $query  = "SELECT * FROM HOLIDAY_BASE_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST   L1 ON  L1.NAMECD2  = T1.SCHOOL_DIV ";
        $query .= "                   AND L1.NAMECD1  = 'A023' ";
        $query .= " WHERE T1.YEAR       = '".$model->field["YEAR"]."'";
        $query .= " AND   T1.SCHOOL_DIV = '".$model->field["SCHOOL_DIV"]."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }
    
    function getHolidayBaseMst($year,$schooldiv)    
    {
        $query  = "SELECT ";
        $query .= "     SCHOOL_DIV, ";
        $query .= "     YEAR, ";
        $query .= "     LEGAL_HOLIDAY_FRG, ";
        $query .= "     FIRST_SATURDAY_FRG, ";
        $query .= "     SECOND_SATURDAY_FRG, ";
        $query .= "     THIRD_SATURDAY_FRG, ";
        $query .= "     FOUR_SATURDAY_FRG, ";
        $query .= "     FIVE_SATURDAY_FRG, ";
        $query .= "     BEFORE_SPRING_VACATION_FRG, ";
        $query .= "     BEFORE_SPRING_VACATION_SDATE, ";
        $query .= "     BEFORE_SPRING_VACATION_EDATE, ";
        $query .= "     SUMMER_VACATION_FRG, ";
        $query .= "     SUMMER_VACATION_SDATE, ";
        $query .= "     SUMMER_VACATION_EDATE, ";
        $query .= "     AUTUMN_VACATION_FRG, ";
        $query .= "     AUTUMN_VACATION_SDATE, ";
        $query .= "     AUTUMN_VACATION_EDATE, ";
        $query .= "     WINTER_VACATION_FRG, ";
        $query .= "     WINTER_VACATION_SDATE, ";
        $query .= "     WINTER_VACATION_EDATE, ";
        $query .= "     AFTER_SPRING_VACATION_FRG, ";
        $query .= "     AFTER_SPRING_VACATION_SDATE, ";
        $query .= "     AFTER_SPRING_VACATION_EDATE ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_BASE_MST ";        
        $query .= " WHERE YEAR       = '".$year."' ";
        $query .= " AND   SCHOOL_DIV = '".$schooldiv."' " ;
        
        return $query;
    }
    
    //年度取得
    function selectYearQuery() {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_BASE_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";
        return $query;

    }
    
    //学校区分
    function getSchooldiv() {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }
    
    //学期制の取得
    function getSemesterdiv($year) {
        return "SELECT SEMESTERDIV FROM SCHOOL_MST WHERE YEAR = '".$year."'";
    }
    
    //HOLIDAY_BASE_MSTデータ確認
    function getCountdata() {
        return "SELECT COUNT(*) FROM HOLIDAY_BASE_MST ";
    }
    
    //EVENT_MSTデータ確認
    function getEventcount() {
        return "SELECT COUNT(*) FROM EVENT_MST ";
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FROM EVENT_MST ";

        return $query;
        
    }
    
    //PUBLIC_HOLIDAY_MST必要データを取得
    function getAllPublicHoliday($year) {
            $query  = " SELECT  ";
            $query .= "   * ";
            $query .= " FROM  ";
            $query .= "   PUBLIC_HOLIDAY_MST  ";
            $query .= " WHERE  ";
            $query .= "   YEAR = '".$year."' ";
            return $query;
    }
    
    //INSERT    
    function &getInsertQuery($model) {
    
        $db = Query::dbCheckOut();
        $db->query("DELETE FROM HOLIDAY_BASE_MST WHERE SCHOOL_DIV = '".$model->field["SCHOOL_DIV"]."' AND YEAR = '".$model->field["YEAR"]."'");
        
        $data["SCHOOL_DIV"][TEXT]                       = $model->field["SCHOOL_DIV"];
        $data["YEAR"][TEXT]                             = $model->field["YEAR"];
        $data["LEGAL_HOLIDAY_FRG"][TEXT]                = strlen($model->field["LEGAL_HOLIDAY_FRG"]) ? "1" : "";
        $data["FIRST_SATURDAY_FRG"][TEXT]               = strlen($model->field["FIRST_SATURDAY_FRG"]) ? "1" : "";
        $data["SECOND_SATURDAY_FRG"][TEXT]              = strlen($model->field["SECOND_SATURDAY_FRG"]) ? "1" : "";
        $data["THIRD_SATURDAY_FRG"][TEXT]               = strlen($model->field["THIRD_SATURDAY_FRG"]) ? "1" : "";
        $data["FOUR_SATURDAY_FRG"][TEXT]                = strlen($model->field["FOUR_SATURDAY_FRG"]) ? "1" : "";
        $data["FIVE_SATURDAY_FRG"][TEXT]                = strlen($model->field["FIVE_SATURDAY_FRG"]) ? "1" : "";
        $data["BEFORE_SPRING_VACATION_FRG"][TEXT]       = ($model->field["BEFORE_SPRING_VACATION_SDATE"] != "") ? "1" : "";
        $data["BEFORE_SPRING_VACATION_SDATE"][FUNC]     = ($model->field["BEFORE_SPRING_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["BEFORE_SPRING_VACATION_SDATE"])."')" : "NULL";
        $data["BEFORE_SPRING_VACATION_EDATE"][FUNC]     = ($model->field["BEFORE_SPRING_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["BEFORE_SPRING_VACATION_EDATE"])."')" : "NULL";
        $data["SUMMER_VACATION_FRG"][TEXT]              = ($model->field["SUMMER_VACATION_SDATE"] != "") ? "1" : "";
        $data["SUMMER_VACATION_SDATE"][FUNC]            = ($model->field["SUMMER_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["SUMMER_VACATION_SDATE"])."')" : "NULL";
        $data["SUMMER_VACATION_EDATE"][FUNC]            = ($model->field["SUMMER_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["SUMMER_VACATION_EDATE"])."')" : "NULL";
        $data["AUTUMN_VACATION_FRG"][TEXT]              = ($model->field["AUTUMN_VACATION_SDATE"] != "") ? "1" : "";
        $data["AUTUMN_VACATION_SDATE"][FUNC]            = ($model->field["AUTUMN_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AUTUMN_VACATION_SDATE"])."')" : "NULL";
        $data["AUTUMN_VACATION_EDATE"][FUNC]            = ($model->field["AUTUMN_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AUTUMN_VACATION_EDATE"])."')" : "NULL";
        $data["WINTER_VACATION_FRG"][TEXT]              = ($model->field["WINTER_VACATION_SDATE"] != "") ? "1" : "";
        $data["WINTER_VACATION_SDATE"][FUNC]            = ($model->field["WINTER_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["WINTER_VACATION_SDATE"])."')" : "NULL";
        $data["WINTER_VACATION_EDATE"][FUNC]            = ($model->field["WINTER_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["WINTER_VACATION_EDATE"])."')" : "NULL";
        $data["AFTER_SPRING_VACATION_FRG"][TEXT]        = ($model->field["AFTER_SPRING_VACATION_SDATE"] != "") ? "1" : "";
        $data["AFTER_SPRING_VACATION_SDATE"][FUNC]      = ($model->field["AFTER_SPRING_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AFTER_SPRING_VACATION_SDATE"])."')" : "NULL";
        $data["AFTER_SPRING_VACATION_EDATE"][FUNC]      = ($model->field["AFTER_SPRING_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AFTER_SPRING_VACATION_EDATE"])."')" : "NULL";
        $data["REGISTERCD"][TEXT]                       = STAFFCD;
        $data["UPDATED"][FUNC]                          = "sysdate()";

        $query = Query::insertSQL($data, "HOLIDAY_BASE_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    
    //対象年度の行事予定を削除する
    function getEventDelQuery($year) {
        $query  = " DELETE FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '1' ";
        $query .= "     AND GRADE = '00' ";
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$year."' ";

        return $query;
    }

    //対象年度の行事予定を登録する（休暇）
    function &getInsert1Query($setDate, $year) {
    
        $data["DATA_DIV"][TEXT]        = '1';
        $data["GRADE"][TEXT]           = '00';
        $data["EXECUTEDATE"][DATE]     = $setDate;
        $data["HOLIDAY_FRG"][TEXT]     = '1';
        $data["REMARK1"][TEXT]         = '';
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "EVENT_MST");
        return $query;
    }
    
    //対象年度の行事予定を登録する(日付、曜日指定)
    function &getInsert2Query($setday, $year) {
    
        $db = Query::dbCheckOut();
        //同一日の休暇データがDBに存在するかチェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '".$setday["EXECUTEDATE"]."' ";
        
        $flag = $db->getOne($query);
        if ($flag = 0) {
            $data["DATA_DIV"][TEXT]        = '1';
            $data["GRADE"][TEXT]           = '00';
            $data["EXECUTEDATE"][DATE]     = $setday["EXECUTEDATE"];
            $data["HOLIDAY_FRG"][TEXT]     = '1';
            $data["REMARK1"][TEXT]         = $setday["REMARK1"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "EVENT_MST");
            
        } else {
            $data["DATA_DIV"][TEXT]        = '1';
            $data["GRADE"][TEXT]           = '00';
            $data["EXECUTEDATE"][DATE]     = $setday["EXECUTEDATE"];
            $data["HOLIDAY_FRG"][TEXT]     = '1';
            $data["REMARK1"][TEXT]         = $setday["REMARK1"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::updateSQL($data, "EVENT_MST");
        }
        Query::dbCheckIn($db);
        return $query;
    }
    
}
?>
