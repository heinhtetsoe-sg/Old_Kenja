<?php

require_once('for_php7.php');

class knjs530Query extends Query {

    //年度取得
    function selectYearQuery() {
        $query  = " SELECT ";
        $query .= "     FISCALYEAR(EXECUTEDATE) AS LABEL, ";
        $query .= "     FISCALYEAR(EXECUTEDATE) AS VALUE ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " GROUP BY ";
        $query .= "     FISCALYEAR(EXECUTEDATE) ";
        $query .= " ORDER BY ";
        $query .= "     'FISCALYEAR(EXECUTEDATE)' DESC ";
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
    
    //学年取得
    function getGrade($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->field["YEAR"]."'";
        if ($flg == "") {
            $query .= " AND     SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."'";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";
        return $query;
    }

    //学科取得
    function getCourdeMajor($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE, ";
        $query .= "     MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得(法的)
    function getHrClass($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$model->field["YEAR"]."'";
        $query .= " AND SEMESTER  = '".CTRL_SEMESTER."'";
        if (!$flg) {
            $query .= " AND GRADE  = '".$model->field["GRADE"]."'";
        } else {
            $query .= " AND GRADE  = '".$model->field["COPY_GRADE"]."'";
        }
        $query .= " ORDER BY ";
        $query .= "     HR_CLASS ";
        return $query;
    }

    //年組取得(複式)
    function getFiClass($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$model->field["YEAR"]."'";
        $query .= " AND SEMESTER  = '".CTRL_SEMESTER."'";
        if (!$flg) {
            $query .= " AND GRADE  = '".$model->field["GRADE"]."'";
        } else {
            $query .= " AND GRADE  = '".$model->field["COPY_GRADE"]."'";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //EVENT_MSTデータ確認
    function getEventcount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND GRADE                       = '".$model->field["GRADE"]."' ";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COURSE_MAJOR"]."' ";
        $query .= " AND FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' ";
        $query .= " AND HR_CLASS                    = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
        
    }
    
    //EVENT_MSTデータ確認　※指定月のみ
    function getEventcountMonth($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND GRADE                       = '".$model->field["GRADE"]."' ";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COURSE_MAJOR"]."' ";
        $query .= " AND FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' ";
        $query .= " AND MONTH(EXECUTEDATE)          = ".$model->field["MONTH"]." ";
        $query .= " AND HR_CLASS                    = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
        
    }
    
    //学年データ存在取得
    function getGradeCount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->field["YEAR"]."'";
        $query .= " AND     SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."'";
        
        return $query;
    }
    
    //画面情報有無確認
    function countEventMst($model, $setday) 
    {
        $query  = "SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE DATA_DIV                  = '".$model->field["DATA_DIV"]."'";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " AND GRADE                       = '".$model->field["GRADE"]."'";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COURSE_MAJOR"]."'";
        $query .= " AND EXECUTEDATE                 = '".$setday."'";
        $query .= " AND HR_CLASS                    = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }
    
    //画面情報を取得
    function setEventMst($model, $setday) 
    {
        $query  = "SELECT ";
        $query .= "     DATA_DIV, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     HOLIDAY_FLG, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2 ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE DATA_DIV                  = '".$model->field["DATA_DIV"]."'";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " AND GRADE                       = '".$model->field["GRADE"]."'";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COURSE_MAJOR"]."'";
        $query .= " AND EXECUTEDATE                 = '".$setday."'";
        $query .= " AND HR_CLASS                    = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }
    
    /***************************************
    更新用
    ****************************************/
    //対象年度の行事予定を登録する
    function &getInsertQuery($model, $key, $val, $db) {

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);

        $query  = " DELETE FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV        = '".$model->field["DATA_DIV"]."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND GRADE       = '".$model->field["GRADE"]."' ";
        $query .= "     AND COURSECD    = '".$coursecd."' ";
        $query .= "     AND MAJORCD     = '".$majorcd."' ";
        $query .= "     AND EXECUTEDATE = '".$key."' ";
        $query .= "     AND HR_CLASS    = '".$model->field["HR_CLASS"]."' ";
        $query .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
        
        $db->query($query);
        
        if ($val["HOLIDAY_FLG"] || $val["REMARK1"] || $val["REMARK2"]) {
            $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
            $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["COURSECD"][TEXT]         = $coursecd;
            $data["MAJORCD"][TEXT]          = $majorcd;
            $data["EXECUTEDATE"][DATE]      = $key;
            $data["HR_CLASS"][TEXT]         = $model->field["HR_CLASS"];
            $data["HR_CLASS_DIV"][TEXT]     = $model->field["HR_CLASS_DIV"];
            $data["HOLIDAY_FLG"][TEXT]      = $val["HOLIDAY_FLG"];
            $data["REMARK1"][TEXT]          = $val["REMARK1"];
            $data["REMARK2"][TEXT]          = $val["REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
                        
            $query = Query::InsertSQL($data, "EVENT_MST");
            $db->query($query);
        }
        
        return;
    }
    
    /***************************************
    初期化用
    ****************************************/
    
    //対象年度の行事予定を削除する
    function getEventDelQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND GRADE                       = '".$model->field["GRADE"]."' ";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COURSE_MAJOR"]."'";
        $query .= " AND FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' ";
        $query .= " AND HR_CLASS                    = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }
    
    //対象年度の行事予定を削除する　※指定月のみ
    function getEventDelQueryMonth($model) {
        $query  = " DELETE FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND GRADE                       = '".$model->field["GRADE"]."' ";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COURSE_MAJOR"]."' ";
        $query .= " AND FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' ";
        $query .= " AND MONTH(EXECUTEDATE)          =  ".$model->field["MONTH"]." ";
        $query .= " AND HR_CLASS                    = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }
        
    //教務データ初期化時の学校のEVENT_MSTデータ確認
    function getEventSchoolcount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "       DATA_DIV    = '1' ";
        $query .= " AND   SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND   FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        $query .= " AND   HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
        
    }
    
    //教務データ初期化時の学校のEVENT_MSTデータ確認　※指定月のみ
    function getEventSchoolcountMonth($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "       DATA_DIV           = '1' ";
        $query .= " AND   SCHOOL_KIND        = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND   MONTH(EXECUTEDATE) = ".$model->field["MONTH"]." ";
        $query .= " AND   FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        $query .= " AND   HR_CLASS_DIV       = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
        
    }
    
    //学校の対象年度の行事予定を取得する（教務データ初期化用）
    function getEventSchoolQuery($model) {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '1' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND GRADE       = '00' ";
        $query .= "     AND COURSECD    = '0' ";
        $query .= "     AND MAJORCD     = '000' ";
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        $query .= "     AND HR_CLASS    = '000' ";
        $query .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }
    
    //学校の対象年度の行事予定を取得する（教務データ初期化用）　※指定月のみ
    function getEventSchoolQueryMonth($model) {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '1' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND GRADE       = '00' ";
        $query .= "     AND COURSECD    = '0' ";
        $query .= "     AND MAJORCD     = '000' ";
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        $query .= "     AND MONTH(EXECUTEDATE)      = ".$model->field["MONTH"]." ";
        $query .= "     AND HR_CLASS    = '000' ";
        $query .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }
    
    //学校の対象年度の行事予定を教務データへInsertする
    function &getEventCopyQuery($setSchoolData, $model) {
        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);

        $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
        $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["EXECUTEDATE"][DATE]      = $setSchoolData["EXECUTEDATE"];
        $data["HR_CLASS"][TEXT]         = $model->field["HR_CLASS"];
        $data["HR_CLASS_DIV"][TEXT]     = $model->field["HR_CLASS_DIV"];
        $data["HOLIDAY_FLG"][TEXT]      = $setSchoolData["HOLIDAY_FLG"];
        $data["REMARK1"][TEXT]          = $setSchoolData["REMARK1"];
        $data["REMARK2"][TEXT]          = $setSchoolData["REMARK2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "EVENT_MST");
        return $query;
    }
    
    //HOLIDAY_BASE_MST必要データを取得(休暇データ用)
    function getHolidayBaseMst($model) {
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND  YEAR   = '".$model->field["YEAR"]."' ";
        
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
    
    //PUBLIC_HOLIDAY_MST必要データを取得　※指定月のみ
    function getAllPublicHolidayMonth($year, $setmonth) {
        $query  = " SELECT  ";
        $query .= "   * ";
        $query .= " FROM  ";
        $query .= "   PUBLIC_HOLIDAY_MST  ";
        $query .= " WHERE  ";
        $query .= "   YEAR  = '".$year."' ";
        $query .= "   AND  HOLIDAY_MONTH = '".$setmonth."' ";
        return $query;
    }
    
    //PUBLIC_HOLIDAY_MST、HOLIDAY_BASE_MSTのカウント
    function getCountPublicHolidayHolidayBase($model, $flg) {
        $query  = " WITH COUNT_PUBLIC_H AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT1 ";
        $query .= " FROM ";
        $query .= "     PUBLIC_HOLIDAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        if ($flg === '2') {
            $query .= "   AND  HOLIDAY_MONTH = '".$model->field["MONTH"]."' ";
        }
        $query .= " ), COUNT_HOLIDAY_B AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT2 ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND  YEAR   = '".$model->field["YEAR"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CNT1 + CNT2 AS CNT ";
        $query .= " FROM ";
        $query .= "     COUNT_PUBLIC_H, ";
        $query .= "     COUNT_HOLIDAY_B ";
        return $query;
    }
    
    //対象年度の行事予定を登録する（休暇）
    function &getInsert1Query($setDate, $model) {
        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);

        $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
        $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["EXECUTEDATE"][DATE]      = $setDate;
        $data["HR_CLASS"][TEXT]         = $model->field["HR_CLASS"];
        $data["HR_CLASS_DIV"][TEXT]     = $model->field["HR_CLASS_DIV"];
        $data["HOLIDAY_FLG"][TEXT]      = '1';
        $data["REMARK1"][TEXT]          = '';
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "EVENT_MST");
        return $query;
    }
        
    //対象年度の行事予定を登録する(日付、曜日指定)
    function &getInsert2Query($db, $setday, $model, $flg) {
        $setUpday = "";
        if ($flg === '1') {
            $setUpday = $setday["EXECUTEDATE"];
        } else {
            $setUpday = $setday;
        }
    
        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);

        //同一日の休暇データがDBに存在するかチェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV            = '".$model->field["DATA_DIV"]."' ";
        $query .= "     AND   SCHOOL_KIND   = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND   GRADE         = '".$model->field["GRADE"]."' ";
        $query .= "     AND   COURSECD      = '".$coursecd."' ";
        $query .= "     AND   MAJORCD       = '".$majorcd."' ";
        $query .= "     AND   EXECUTEDATE   = '".$setUpday."' ";
        $query .= "     AND   HR_CLASS      = '".$model->field["HR_CLASS"]."' ";
        $query .= "     AND   HR_CLASS_DIV  = '".$model->field["HR_CLASS_DIV"]."' ";
        
        $flag = $db->getOne($query);
        if ($flag == '0') {
            $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
            $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["COURSECD"][TEXT]         = $coursecd;
            $data["MAJORCD"][TEXT]          = $majorcd;
            $data["EXECUTEDATE"][DATE]      = $setUpday;
            $data["HR_CLASS"][TEXT]         = $model->field["HR_CLASS"];
            $data["HR_CLASS_DIV"][TEXT]     = $model->field["HR_CLASS_DIV"];
            $data["HOLIDAY_FLG"][TEXT]      = '1';
            if ($flg === '1') {
                $data["REMARK1"][TEXT]          = $setday["REMARK1"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "EVENT_MST");

        } else {
            $data["HOLIDAY_FLG"][TEXT]     = '1';
            if ($flg === '1') {
                $data["REMARK1"][TEXT]         = $setday["REMARK1"];
            }
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
            $where                         = " WHERE ";
            $where                        .= " DATA_DIV             = '".$model->field["DATA_DIV"]."' ";
            $where                        .= " AND  SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' ";
            $where                        .= " AND  GRADE           = '".$model->field["GRADE"]."' ";
            $where                        .= " AND  COURSECD        = '".$coursecd."' ";
            $where                        .= " AND  MAJORCD         = '".$majorcd."' ";
            $where                        .= " AND  EXECUTEDATE     = '".$setUpday."' ";
            $where                        .= " AND  HR_CLASS        = '".$model->field["HR_CLASS"]."' ";
            $where                        .= " AND  HR_CLASS_DIV    = '".$model->field["HR_CLASS_DIV"]."' ";

            $query = Query::updateSQL($data, "EVENT_MST", $where);
        }

        return $query;
    }
    
    //対象年度の行事予定を登録する(日付、曜日指定)
    function &getInsert2QueryMonth($db, $setday, $model) {
        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);

        //同一日の休暇データがDBに存在するかチェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= "     AND   SCHOOL_KIND           = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND   GRADE                 = '".$model->field["GRADE"]."' ";
        $query .= "     AND   COURSECD              = '".$coursecd."' ";
        $query .= "     AND   MAJORCD               = '".$majorcd."' ";
        $query .= "     AND   EXECUTEDATE           = '".$setday["EXECUTEDATE"]."' ";
        $query .= "     AND   MONTH(EXECUTEDATE)    =  ".$model->field["MONTH"]." ";
        $query .= "     AND   HR_CLASS              = '".$model->field["HR_CLASS"]."' ";
        $query .= "     AND   HR_CLASS_DIV          = '".$model->field["HR_CLASS_DIV"]."' ";
        
        $flag = $db->getOne($query);
        if ($flag == '0') {
            $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
            $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["COURSECD"][TEXT]         = $coursecd;
            $data["MAJORCD"][TEXT]          = $majorcd;
            $data["EXECUTEDATE"][DATE]      = $setday["EXECUTEDATE"];
            $data["HR_CLASS"][TEXT]         = $model->field["HR_CLASS"];
            $data["HR_CLASS_DIV"][TEXT]     = $model->field["HR_CLASS_DIV"];
            $data["HOLIDAY_FLG"][TEXT]      = '1';
            $data["REMARK1"][TEXT]          = $setday["REMARK1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "EVENT_MST");

        } else {
            $data["HOLIDAY_FLG"][TEXT]     = '1';
            $data["REMARK1"][TEXT]         = $setday["REMARK1"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
            $where                         = " WHERE ";
            $where                        .= " DATA_DIV         = '".$model->field["DATA_DIV"]."' ";
            $where                        .= " AND  SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $where                        .= " AND  GRADE       = '".$model->field["GRADE"]."' ";
            $where                        .= " AND  COURSECD    = '".$coursecd."' ";
            $where                        .= " AND  MAJORCD     = '".$majorcd."' ";
            $where                        .= " AND  EXECUTEDATE = '".$setday["EXECUTEDATE"]."' ";
            $where                        .= " AND  HR_CLASS    = '".$model->field["HR_CLASS"]."' ";
            $where                        .= " AND  HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";

            $query = Query::updateSQL($data, "EVENT_MST", $where);
        }

        return $query;
    }


    /********************/
    /*   コピー処理用   */
    /********************/

    //学校校種取得
    function getCopySchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["YEAR"]."'";
        $query .= " AND GRADE   = '".$model->field["COPY_GRADE"]."'";

        return $query;
    }

    //EVENT_MSTデータ確認
    function getCopyEventcount($model, $school_kind, $month="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= " AND SCHOOL_KIND                 = '".$school_kind."' ";
        $query .= " AND GRADE                       = '".$model->field["COPY_GRADE"]."' ";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COPY_MAJOR"]."' ";
        $query .= " AND FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' ";
        if ($month) {
            $query .= " AND MONTH(EXECUTEDATE)          =  ".$model->field["MONTH"]." ";
        }
        $query .= " AND   HR_CLASS              = '".$model->field["COPY_HR_CLASS"]."' ";
        $query .= " AND   HR_CLASS_DIV          = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
        
    }

    //コピー先のデータを削除する
    function getCopyEventDelQuery($model, $month="") {
        $query  = " DELETE FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["COPY_SCHOOL_KIND"]."' ";
        $query .= " AND GRADE                       = '".$model->field["COPY_GRADE"]."' ";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COPY_MAJOR"]."'";
        $query .= " AND FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' ";
        if ($month) {
            $query .= " AND MONTH(EXECUTEDATE)          =  ".$model->field["MONTH"]." ";
        }
        $query .= " AND   HR_CLASS                  = '".$model->field["COPY_HR_CLASS"]."' ";
        $query .= " AND   HR_CLASS_DIV              = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }

    //コピー元のデータを取得する
    function getCopyEventMotoQuery($model, $month="") {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV                    = '".$model->field["DATA_DIV"]."' ";
        $query .= " AND SCHOOL_KIND                 = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND GRADE                       = '".$model->field["GRADE"]."' ";
        $query .= " AND COURSECD || '-' || MAJORCD  = '".$model->field["COURSE_MAJOR"]."'";
        $query .= " AND FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' ";
        if ($month) {
            $query .= " AND MONTH(EXECUTEDATE)          =  ".$model->field["MONTH"]." ";
        }
        $query .= " AND HR_CLASS                    = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }

    //コピー先へInsertする
    function &copyEventInsertQuery($setSchoolData, $model) {
        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COPY_MAJOR"]);

        $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
        $data["SCHOOL_KIND"][TEXT]      = $model->field["COPY_SCHOOL_KIND"];
        $data["GRADE"][TEXT]            = $model->field["COPY_GRADE"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["EXECUTEDATE"][DATE]      = $setSchoolData["EXECUTEDATE"];
        $data["HR_CLASS"][TEXT]         = $model->field["COPY_HR_CLASS"];
        $data["HR_CLASS_DIV"][TEXT]     = $model->field["HR_CLASS_DIV"];
        $data["HOLIDAY_FLG"][TEXT]      = $setSchoolData["HOLIDAY_FLG"];
        $data["REMARK1"][TEXT]          = $setSchoolData["REMARK1"];
        $data["REMARK2"][TEXT]          = $setSchoolData["REMARK2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "EVENT_MST");
        return $query;
    }
}
?>
