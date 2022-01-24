<?php

require_once('for_php7.php');

class knjs520Query extends Query {

    function getHolidayBaseMst($year,$schoolkind)    
    {
        $query  = "SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     LEGAL_HOLIDAY_FLG, ";
        $query .= "     FIRST_SATURDAY_FLG, ";
        $query .= "     SECOND_SATURDAY_FLG, ";
        $query .= "     THIRD_SATURDAY_FLG, ";
        $query .= "     FOUR_SATURDAY_FLG, ";
        $query .= "     FIVE_SATURDAY_FLG, ";
        $query .= "     BEFORE_SPRING_VACATION_FLG, ";
        $query .= "     BEFORE_SPRING_VACATION_SDATE, ";
        $query .= "     BEFORE_SPRING_VACATION_EDATE, ";
        $query .= "     SUMMER_VACATION_FLG, ";
        $query .= "     SUMMER_VACATION_SDATE, ";
        $query .= "     SUMMER_VACATION_EDATE, ";
        $query .= "     AUTUMN_VACATION_FLG, ";
        $query .= "     AUTUMN_VACATION_SDATE, ";
        $query .= "     AUTUMN_VACATION_EDATE, ";
        $query .= "     WINTER_VACATION_FLG, ";
        $query .= "     WINTER_VACATION_SDATE, ";
        $query .= "     WINTER_VACATION_EDATE, ";
        $query .= "     AFTER_SPRING_VACATION_FLG, ";
        $query .= "     AFTER_SPRING_VACATION_SDATE, ";
        $query .= "     AFTER_SPRING_VACATION_EDATE ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_BASE_MST ";        
        $query .= " WHERE YEAR       = '".$year."' ";
        $query .= " AND   SCHOOL_KIND = '".$schoolkind."' " ;
        
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

    //名称マスタ
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAME1 AS LABEL";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2";
        return $query;
    }
    
    //学校区分
    function getSchoolkind($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAME1 || ':' || T1.ABBV1 AS LABEL, ";
        $query .= "     T1.NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAME1 IN (SELECT ";
                $query .= "                             REGD_G.SCHOOL_KIND ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.NAME1 IN (SELECT ";
            $query .= "                             REGD_G.SCHOOL_KIND ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";
        return $query;
    }
    
    //学期制の取得
    function getSemesterdiv($year) {
        return "SELECT SEMESTERDIV FROM SCHOOL_MST WHERE YEAR = '".$year."'";
    }
    
    //HOLIDAY_BASE_MSTデータ確認
    function getCountdata($schoolkind, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_BASE_MST ";
        $query .= " WHERE ";
        $query .= "       SCHOOL_KIND = '".$schoolkind."' ";
        $query .= " AND   YEAR        = '".$year."' ";
        
        return $query;
    }
    
    //EVENT_MSTデータ確認
    function getEventcount($model, $schoolkind, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "       DATA_DIV                  = '1' ";
        $query .= " AND   SCHOOL_KIND               = '".$schoolkind."' ";
        $query .= " AND   GRADE                     = '00' ";
        $query .= " AND   COURSECD                  = '0' ";
        $query .= " AND   MAJORCD                   = '000' ";
        $query .= " AND   FISCALYEAR(EXECUTEDATE)   = '".$year."' ";
        $query .= " AND   HR_CLASS = '000' ";
        //FI複式クラスを使うプロパティ
        if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     AND   HR_CLASS_DIV      = '2' ";
        } else {
            $query .= "     AND   HR_CLASS_DIV      = '1' ";
        }

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
    function &getInsertQuery($model,$db) {
    
        //$db = Query::dbCheckOut();
        $db->query("DELETE FROM HOLIDAY_BASE_MST WHERE SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND YEAR = '".$model->field["YEAR"]."'");
        $data = array();
        $data["SCHOOL_KIND"][TEXT]                      = $model->field["SCHOOL_KIND"];
        $data["YEAR"][TEXT]                             = $model->field["YEAR"];
        $data["LEGAL_HOLIDAY_FLG"][TEXT]                = strlen($model->field["LEGAL_HOLIDAY_FLG"]) ? "1" : "";
        $data["FIRST_SATURDAY_FLG"][TEXT]               = strlen($model->field["FIRST_SATURDAY_FLG"]) ? "1" : "";
        $data["SECOND_SATURDAY_FLG"][TEXT]              = strlen($model->field["SECOND_SATURDAY_FLG"]) ? "1" : "";
        $data["THIRD_SATURDAY_FLG"][TEXT]               = strlen($model->field["THIRD_SATURDAY_FLG"]) ? "1" : "";
        $data["FOUR_SATURDAY_FLG"][TEXT]                = strlen($model->field["FOUR_SATURDAY_FLG"]) ? "1" : "";
        $data["FIVE_SATURDAY_FLG"][TEXT]                = strlen($model->field["FIVE_SATURDAY_FLG"]) ? "1" : "";
        $data["BEFORE_SPRING_VACATION_FLG"][TEXT]       = ($model->field["BEFORE_SPRING_VACATION_SDATE"] != "") ? "1" : "";
        $data["BEFORE_SPRING_VACATION_SDATE"][FUNC]     = ($model->field["BEFORE_SPRING_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["BEFORE_SPRING_VACATION_SDATE"])."')" : "NULL";
        $data["BEFORE_SPRING_VACATION_EDATE"][FUNC]     = ($model->field["BEFORE_SPRING_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["BEFORE_SPRING_VACATION_EDATE"])."')" : "NULL";
        $data["SUMMER_VACATION_FLG"][TEXT]              = ($model->field["SUMMER_VACATION_SDATE"] != "") ? "1" : "";
        $data["SUMMER_VACATION_SDATE"][FUNC]            = ($model->field["SUMMER_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["SUMMER_VACATION_SDATE"])."')" : "NULL";
        $data["SUMMER_VACATION_EDATE"][FUNC]            = ($model->field["SUMMER_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["SUMMER_VACATION_EDATE"])."')" : "NULL";
        $data["AUTUMN_VACATION_FLG"][TEXT]              = ($model->field["AUTUMN_VACATION_SDATE"] != "") ? "1" : "";
        $data["AUTUMN_VACATION_SDATE"][FUNC]            = ($model->field["AUTUMN_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AUTUMN_VACATION_SDATE"])."')" : "NULL";
        $data["AUTUMN_VACATION_EDATE"][FUNC]            = ($model->field["AUTUMN_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AUTUMN_VACATION_EDATE"])."')" : "NULL";
        $data["WINTER_VACATION_FLG"][TEXT]              = ($model->field["WINTER_VACATION_SDATE"] != "") ? "1" : "";
        $data["WINTER_VACATION_SDATE"][FUNC]            = ($model->field["WINTER_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["WINTER_VACATION_SDATE"])."')" : "NULL";
        $data["WINTER_VACATION_EDATE"][FUNC]            = ($model->field["WINTER_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["WINTER_VACATION_EDATE"])."')" : "NULL";
        $data["AFTER_SPRING_VACATION_FLG"][TEXT]        = ($model->field["AFTER_SPRING_VACATION_SDATE"] != "") ? "1" : "";
        $data["AFTER_SPRING_VACATION_SDATE"][FUNC]      = ($model->field["AFTER_SPRING_VACATION_SDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AFTER_SPRING_VACATION_SDATE"])."')" : "NULL";
        $data["AFTER_SPRING_VACATION_EDATE"][FUNC]      = ($model->field["AFTER_SPRING_VACATION_EDATE"] != "") ? "DATE('".str_replace("/","-",$model->field["AFTER_SPRING_VACATION_EDATE"])."')" : "NULL";
        $data["REGISTERCD"][TEXT]                       = STAFFCD;
        $data["UPDATED"][FUNC]                          = "sysdate()";

        $query = Query::insertSQL($data, "HOLIDAY_BASE_MST");
        //$db->query($query);
        //Query::dbCheckIn($db);
        return $query;
    }
    
    //対象年度の行事予定を削除する
    function getEventDelQuery($model, $year, $schoolkind) {
        $query  = " DELETE FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '1' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolkind."' ";
        $query .= "     AND GRADE = '00' ";
        $query .= "     AND COURSECD = '0' ";
        $query .= "     AND MAJORCD = '000' ";
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$year."' ";
        $query .= "     AND HR_CLASS = '000' ";
        //FI複式クラスを使うプロパティ
        if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     AND   HR_CLASS_DIV      = '2' ";
        } else {
            $query .= "     AND   HR_CLASS_DIV      = '1' ";
        }

        return $query;
    }

    //対象年度の行事予定を登録する（休暇）
    function &getInsert1Query($model, $setDate, $schoolkind) {
        
        $data = array();
        $data["DATA_DIV"][TEXT]        = '1';
        $data["SCHOOL_KIND"][TEXT]     = $schoolkind;
        $data["GRADE"][TEXT]           = '00';
        $data["COURSECD"][TEXT]        = '0';
        $data["MAJORCD"][TEXT]         = '000';
        $data["EXECUTEDATE"][DATE]     = $setDate;
        $data["HR_CLASS"][TEXT]        = '000';
        //FI複式クラスを使うプロパティ
        if ($model->Properties["useFi_Hrclass"] == '1') {
            $data["HR_CLASS_DIV"][TEXT] = '2';
        } else {
            $data["HR_CLASS_DIV"][TEXT] = '1';
        }
        $data["HOLIDAY_FLG"][TEXT]     = '1';
        $data["REMARK1"][TEXT]         = '';
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "EVENT_MST");
        return $query;
    }
    
    //対象年度の行事予定を登録する(日付、曜日指定)
    function &getInsert2Query($db, $model, $setday, $schoolkind) {
        //同一日の休暇データがDBに存在するかチェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV    = '1' ";
        $query .= "     AND   SCHOOL_KIND = '".$schoolkind."' ";
        $query .= "     AND   GRADE       = '00' ";
        $query .= "     AND   COURSECD    = '0' ";
        $query .= "     AND   MAJORCD     = '000' ";
        $query .= "     AND   EXECUTEDATE = '".$setday["EXECUTEDATE"]."' ";
        $query .= "     AND   HR_CLASS = '000' ";
        //FI複式クラスを使うプロパティ
        if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     AND   HR_CLASS_DIV      = '2' ";
        } else {
            $query .= "     AND   HR_CLASS_DIV      = '1' ";
        }
        
        $flag = $db->getOne($query);
        if ($flag == '0') {
            $data = array();
            $data["DATA_DIV"][TEXT]        = '1';
            $data["SCHOOL_KIND"][TEXT]     = $schoolkind;
            $data["GRADE"][TEXT]           = '00';
            $data["COURSECD"][TEXT]        = '0';
            $data["MAJORCD"][TEXT]         = '000';
            $data["EXECUTEDATE"][DATE]     = $setday["EXECUTEDATE"];
            $data["HR_CLASS"][TEXT]        = '000';
            //FI複式クラスを使うプロパティ
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $data["HR_CLASS_DIV"][TEXT] = '2';
            } else {
                $data["HR_CLASS_DIV"][TEXT] = '1';
            }
            $data["HOLIDAY_FLG"][TEXT]     = '1';
            $data["REMARK1"][TEXT]         = $setday["REMARK1"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "EVENT_MST");

        } else {
            $data = array();
            $data["HOLIDAY_FLG"][TEXT]     = '1';
            $data["REMARK1"][TEXT]         = $setday["REMARK1"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
            $where                         = " WHERE ";
            $where                        .= " DATA_DIV         = '1' ";
            $where                        .= " AND  SCHOOL_KIND = '".$schoolkind."' ";
            $where                        .= " AND  GRADE       = '00' ";
            $where                        .= " AND  COURSECD    = '0' ";
            $where                        .= " AND  MAJORCD     = '000' ";
            $where                        .= " AND  EXECUTEDATE = '".$setday["EXECUTEDATE"]."' ";
            $where                        .= " AND  HR_CLASS = '000' ";
            //FI複式クラスを使うプロパティ
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $where .= "     AND   HR_CLASS_DIV      = '2' ";
            } else {
                $where .= "     AND   HR_CLASS_DIV      = '1' ";
            }

            $query = Query::updateSQL($data, "EVENT_MST", $where);
        }
        
        return $query;
    }
    
    //対象年度の土曜、日曜を登録する(日付、曜日指定)
    function &getInsert3Query($db, $model, $date, $schoolkind) {
        //同一日の休暇データがDBに存在するかチェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV    = '1' ";
        $query .= "     AND   SCHOOL_KIND = '".$schoolkind."' ";
        $query .= "     AND   GRADE       = '00' ";
        $query .= "     AND   COURSECD    = '0' ";
        $query .= "     AND   MAJORCD     = '000' ";
        $query .= "     AND   EXECUTEDATE = '".$date."' ";
        $query .= "     AND   HR_CLASS = '000' ";
        //FI複式クラスを使うプロパティ
        if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     AND   HR_CLASS_DIV      = '2' ";
        } else {
            $query .= "     AND   HR_CLASS_DIV      = '1' ";
        }
        
        $flag = $db->getOne($query);
        if ($flag == '0') {
            $data = array();
            $data["DATA_DIV"][TEXT]        = '1';
            $data["SCHOOL_KIND"][TEXT]     = $schoolkind;
            $data["GRADE"][TEXT]           = '00';
            $data["COURSECD"][TEXT]        = '0';
            $data["MAJORCD"][TEXT]         = '000';
            $data["EXECUTEDATE"][DATE]     = $date;
            $data["HR_CLASS"][TEXT]        = '000';
            //FI複式クラスを使うプロパティ
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $data["HR_CLASS_DIV"][TEXT] = '2';
            } else {
                $data["HR_CLASS_DIV"][TEXT] = '1';
            }
            $data["HOLIDAY_FLG"][TEXT]     = '1';
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "EVENT_MST");

        } else {
            $data = array();
            $data["HOLIDAY_FLG"][TEXT]     = '1';
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
            $where                         = " WHERE ";
            $where                        .= " DATA_DIV         = '1' ";
            $where                        .= " AND  SCHOOL_KIND = '".$schoolkind."' ";
            $where                        .= " AND  GRADE       = '00' ";
            $where                        .= " AND  COURSECD    = '0' ";
            $where                        .= " AND  MAJORCD     = '000' ";
            $where                        .= " AND  EXECUTEDATE = '".$date."' ";
            $where                        .= " AND  HR_CLASS = '000' ";
            //FI複式クラスを使うプロパティ
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $where .= "     AND   HR_CLASS_DIV      = '2' ";
            } else {
                $where .= "     AND   HR_CLASS_DIV      = '1' ";
            }

            $query = Query::updateSQL($data, "EVENT_MST", $where);
        }

        return $query;
    }

    //PUBLIC_HOLIDAY_MSTからHOLIDAY_KINDが"1"のデータを取得
    function getPublicHolidayKind($year, $model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PUBLIC_HOLIDAY_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '{$year}' ";
        if ($model->Properties["useHOLIDAY_KIND"] == "1") {
            $query .= "     AND HOLIDAY_KIND = '1' ";
        }

        return $query;
    }

    //テーブル存在チェック
    function checkTableExist($tableName) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = '{$tableName}' ";

        return $query;
    }

    //BANK_HOLIDAY_DAT
    function &delInsBank($db, $model, $pubHoliArr, $schoolkind) {
        $query  = " DELETE FROM ";
        $query .= "     BANK_HOLIDAY_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolkind."' ";
        $db->query($query);

        $tmpDate = array();
        foreach ($pubHoliArr as $key => $arr) {
            if (in_array($arr["EXECUTEDATE"], $tmpDate)) continue;

            $data = array();
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $schoolkind;
            $data["EXECUTEDATE"][DATE]     = $arr["EXECUTEDATE"];
            $data["HOLIDAY_NAME"][TEXT]    = $arr["HOLIDAY_NAME"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "BANK_HOLIDAY_DAT");
            $db->query($query);

            $tmpDate[] = $arr["EXECUTEDATE"];
        }

        return;
    }
}
?>
