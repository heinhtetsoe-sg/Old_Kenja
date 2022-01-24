<?php

require_once('for_php7.php');

class knjs520Model extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;          //セキュリティー用スタッフコード
    var $sec_competence;   //権限保持

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        
        $this->field = array("YEAR"                             => VARS::post("YEAR"),
                             "SCHOOL_KIND"                      => VARS::post("SCHOOL_KIND"),
                             "LEGAL_HOLIDAY_FLG"                => VARS::post("LEGAL_HOLIDAY_FLG"),
                             "FIRST_SATURDAY_FLG"               => VARS::post("FIRST_SATURDAY_FLG"),
                             "SECOND_SATURDAY_FLG"              => VARS::post("SECOND_SATURDAY_FLG"),
                             "THIRD_SATURDAY_FLG"               => VARS::post("THIRD_SATURDAY_FLG"),
                             "FOUR_SATURDAY_FLG"                => VARS::post("FOUR_SATURDAY_FLG"),
                             "FIVE_SATURDAY_FLG"                => VARS::post("FIVE_SATURDAY_FLG"),
                             "BEFORE_SPRING_VACATION_FLG"       => VARS::post("BEFORE_SPRING_VACATION_FLG"),
                             "BEFORE_SPRING_VACATION_SDATE"     => VARS::post("BEFORE_SPRING_VACATION_SDATE"),
                             "BEFORE_SPRING_VACATION_EDATE"     => VARS::post("BEFORE_SPRING_VACATION_EDATE"),
                             "SUMMER_VACATION_FLG"              => VARS::post("SUMMER_VACATION_FLG"),
                             "SUMMER_VACATION_SDATE"            => VARS::post("SUMMER_VACATION_SDATE"),
                             "SUMMER_VACATION_EDATE"            => VARS::post("SUMMER_VACATION_EDATE"),
                             "AUTUMN_VACATION_FLG"              => VARS::post("AUTUMN_VACATION_FLG"),
                             "AUTUMN_VACATION_SDATE"            => VARS::post("AUTUMN_VACATION_SDATE"),
                             "AUTUMN_VACATION_EDATE"            => VARS::post("AUTUMN_VACATION_EDATE"),
                             "WINTER_VACATION_FLG"              => VARS::post("WINTER_VACATION_FLG"),
                             "WINTER_VACATION_SDATE"            => VARS::post("WINTER_VACATION_SDATE"),
                             "WINTER_VACATION_EDATE"            => VARS::post("WINTER_VACATION_EDATE"),
                             "AFTER_SPRING_VACATION_FLG"        => VARS::post("AFTER_SPRING_VACATION_FLG"),
                             "AFTER_SPRING_VACATION_SDATE"      => VARS::post("AFTER_SPRING_VACATION_SDATE"),
                             "AFTER_SPRING_VACATION_EDATE"      => VARS::post("AFTER_SPRING_VACATION_EDATE"),
                             "UPDATED"                          => VARS::post("UPDATED")
                             );
                             
        $this->field2 = array("DATA_DIV"                        => VARS::post("DATA_DIV"),
                             "GRADE"                            => VARS::post("GRADE"),
                             "EXECUTEDATE"                      => VARS::post("EXECUTEDATE"),
                             "HOLIDAY_FLG"                      => VARS::post("HOLIDAY_FLG"),
                             "REMARK1"                          => VARS::post("REMARK1"),
                             "REMARK2"                          => VARS::post("REMARK2"),
                             "UPDATED"                          => VARS::post("UPDATED")
                             );

        if ($this->cmd == "") {
            $this->field = array();
        }
        
        if ($this->cmd == "yotei") {
            $this->field2 = array();
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function validateInput() {
    
        //必須入力項目チェック
        //春休み始業式前
        //片方のみの入力かをチェック
        if (($this->field["BEFORE_SPRING_VACATION_SDATE"]!=""  &&  $this->field["BEFORE_SPRING_VACATION_EDATE"]=="") || ($this->field["BEFORE_SPRING_VACATION_SDATE"]==""  &&  $this->field["BEFORE_SPRING_VACATION_EDATE"]!="")) {
            $this->setWarning("MSG916","春休み（始業式前）の日付が正しくありません。");
            return false;
        //開始日、終了日前後チェック
        } elseif ($this->field["BEFORE_SPRING_VACATION_SDATE"] > $this->field["BEFORE_SPRING_VACATION_EDATE"]) {
            $this->setWarning("MSG916","春休み（始業式前）の日付が正しくありません。");
            return false;
        //年度内チェック
        } elseif ($this->field["BEFORE_SPRING_VACATION_SDATE"]!="" && (strtotime($this->field["BEFORE_SPRING_VACATION_SDATE"]) < strtotime($this->field["YEAR"]."/04/01")
            || strtotime($this->field["BEFORE_SPRING_VACATION_EDATE"]) > strtotime(($this->field["YEAR"]+1)."/03/31"))) {
            $this->setWarning("MSG916","春休み（始業式前）の日付が正しくありません。");
            return false;
        }
                
        //夏休み
        //片方のみの入力かをチェック
        if (($this->field["SUMMER_VACATION_SDATE"]!=""  &&  $this->field["SUMMER_VACATION_EDATE"]=="") || ($this->field["SUMMER_VACATION_SDATE"]==""  &&  $this->field["SUMMER_VACATION_EDATE"]!="")) {
            $this->setWarning("MSG916","夏休みの日付が正しくありません。");
            return false;
        //開始日、終了日前後チェック
        } elseif ($this->field["SUMMER_VACATION_SDATE"] > $this->field["SUMMER_VACATION_EDATE"]) {
            $this->setWarning("MSG916","夏休みの日付が正しくありません。");
            return false;
        //年度内チェック
        } elseif ($this->field["SUMMER_VACATION_SDATE"]!="" && (strtotime($this->field["SUMMER_VACATION_SDATE"]) < strtotime($this->field["YEAR"]."/04/01")
            || strtotime($this->field["SUMMER_VACATION_EDATE"]) > strtotime(($this->field["YEAR"]+1)."/03/31"))) {
            $this->setWarning("MSG916","夏休みの日付が正しくありません。");
            return false;
        //休業順番チェック
        } elseif ($this->field["SUMMER_VACATION_SDATE"]!="" && ($this->field["BEFORE_SPRING_VACATION_EDATE"] >= $this->field["SUMMER_VACATION_SDATE"])) {
            $this->setWarning("MSG916","夏休みの日付が正しくありません。");
            return false;
        }
                
        //秋休み
        //片方のみの入力かをチェック
        if (($this->field["AUTUMN_VACATION_SDATE"]!=""  &&  $this->field["AUTUMN_VACATION_EDATE"]=="") || ($this->field["AUTUMN_VACATION_SDATE"]==""  &&  $this->field["AUTUMN_VACATION_EDATE"]!="")) {
            $this->setWarning("MSG916","秋休みの日付が正しくありません。");
            return false;
        //開始日、終了日前後チェック
        } elseif ($this->field["AUTUMN_VACATION_SDATE"] > $this->field["AUTUMN_VACATION_EDATE"]) {
            $this->setWarning("MSG916","秋休みの日付が正しくありません。");
            return false;
        //年度内チェック
        } elseif ($this->field["AUTUMN_VACATION_SDATE"]!="" && (strtotime($this->field["AUTUMN_VACATION_SDATE"]) < strtotime($this->field["YEAR"]."/04/01")
            || strtotime($this->field["AUTUMN_VACATION_EDATE"]) > strtotime(($this->field["YEAR"]+1)."/03/31"))) {
            $this->setWarning("MSG916","秋休みの日付が正しくありません。");
            return false;
        //休業順番チェック
        } elseif ($this->field["AUTUMN_VACATION_SDATE"]!="" && ($this->field["BEFORE_SPRING_VACATION_EDATE"] >= $this->field["AUTUMN_VACATION_SDATE"])) {
            $this->setWarning("MSG916","秋休みの日付が正しくありません。");
            return false;
        } elseif ($this->field["AUTUMN_VACATION_SDATE"]!="" && ($this->field["SUMMER_VACATION_EDATE"] >= $this->field["AUTUMN_VACATION_SDATE"])) {
            $this->setWarning("MSG916","秋休みの日付が正しくありません。");
            return false;
        }
                
        //冬休み
        //片方のみの入力かをチェック
        if (($this->field["WINTER_VACATION_SDATE"]!=""  &&  $this->field["WINTER_VACATION_EDATE"]=="") || ($this->field["WINTER_VACATION_SDATE"]==""  &&  $this->field["WINTER_VACATION_EDATE"]!="")) {
            $this->setWarning("MSG916","冬休みの日付が正しくありません。");
            return false;
        //開始日、終了日前後チェック
        } elseif ($this->field["WINTER_VACATION_SDATE"] > $this->field["WINTER_VACATION_EDATE"]) {
            $this->setWarning("MSG916","冬休みの日付が正しくありません。");
            return false;
        //年度内チェック
        } elseif ($this->field["WINTER_VACATION_SDATE"]!="" && (strtotime($this->field["WINTER_VACATION_SDATE"]) < strtotime($this->field["YEAR"]."/04/01")
            || strtotime($this->field["WINTER_VACATION_EDATE"]) > strtotime(($this->field["YEAR"]+1)."/03/31"))) {
            $this->setWarning("MSG916","冬休みの日付が正しくありません。");
            return false;
        //休業順番チェック
        } elseif ($this->field["WINTER_VACATION_SDATE"]!="" && ($this->field["BEFORE_SPRING_VACATION_EDATE"] >= $this->field["WINTER_VACATION_SDATE"])) {
            $this->setWarning("MSG916","冬休みの日付が正しくありません。");
            return false;
        } elseif ($this->field["WINTER_VACATION_SDATE"]!="" && ($this->field["SUMMER_VACATION_EDATE"] >= $this->field["WINTER_VACATION_SDATE"])) {
            $this->setWarning("MSG916","冬休みの日付が正しくありません。");
            return false;
        } elseif ($this->field["WINTER_VACATION_SDATE"]!="" && ($this->field["AUTUMN_VACATION_EDATE"] >= $this->field["WINTER_VACATION_SDATE"])) {
            $this->setWarning("MSG916","冬休みの日付が正しくありません。");
            return false;
        } 
        
        //春休み終了式後
        //片方のみの入力かをチェック
        if (($this->field["AFTER_SPRING_VACATION_SDATE"]!=""  &&  $this->field["AFTER_SPRING_VACATION_EDATE"]=="") || ($this->field["AFTER_SPRING_VACATION_SDATE"]==""  &&  $this->field["AFTER_SPRING_VACATION_EDATE"]!="")) {
            $this->setWarning("MSG916","春休み（終了式後）の日付が正しくありません。");
            return false;
        //開始日、終了日前後チェック
        } elseif ($this->field["AFTER_SPRING_VACATION_SDATE"] > $this->field["AFTER_SPRING_VACATION_EDATE"]) {
            $this->setWarning("MSG916","春休み（終了式後）の日付が正しくありません。");
            return false;
        //年度内チェック
        } elseif ($this->field["AFTER_SPRING_VACATION_SDATE"]!="" && (strtotime($this->field["AFTER_SPRING_VACATION_SDATE"]) < strtotime($this->field["YEAR"]."/04/01")
            || strtotime($this->field["AFTER_SPRING_VACATION_EDATE"]) > strtotime(($this->field["YEAR"]+1)."/03/31"))) {
            $this->setWarning("MSG916","春休み（終了式後）の日付が正しくありません。");
            return false;
        //休業順番チェック
        } elseif ($this->field["AFTER_SPRING_VACATION_SDATE"]!="" && ($this->field["BEFORE_SPRING_VACATION_EDATE"] >= $this->field["AFTER_SPRING_VACATION_SDATE"])) {
            $this->setWarning("MSG916","春休み（終了式後）の日付が正しくありません。");
            return false;
        } elseif ($this->field["AFTER_SPRING_VACATION_SDATE"]!="" && ($this->field["SUMMER_VACATION_EDATE"] >= $this->field["AFTER_SPRING_VACATION_SDATE"])) {
            $this->setWarning("MSG916","春休み（終了式後）の日付が正しくありません。");
            return false;
        } elseif ($this->field["AFTER_SPRING_VACATION_SDATE"]!="" && ($this->field["AUTUMN_VACATION_EDATE"] >= $this->field["AFTER_SPRING_VACATION_SDATE"])) {
            $this->setWarning("MSG916","春休み（終了式後）の日付が正しくありません。");
            return false;
        } elseif ($this->field["AFTER_SPRING_VACATION_SDATE"]!="" && ($this->field["WINTER_VACATION_EDATE"] >= $this->field["AFTER_SPRING_VACATION_SDATE"])) {
            $this->setWarning("MSG916","春休み（終了式後）の日付が正しくありません。");
            return false;
        }
        
        return true;
    }
    
    /***************************************
    更新処理
    ****************************************/
    
    //行事予定登録処理用休業
    function dateDelete($db) {
        //EVENT_MSTを削除
        $query = knjs520Query::getEventcount($this, $this->field["SCHOOL_KIND"], $this->field["YEAR"]);
        $getEventcont = $db->getOne($query);
        if ($getEventcont != 0) {
            $query = knjs520Query::getEventDelQuery($this, $this->field["YEAR"],$this->field["SCHOOL_KIND"]);
            $db->query($query);
        }
    }

    //行事予定登録処理用休業
    function dateInput1($db) {

        //春休み（始業式前）
        if ($this->field["BEFORE_SPRING_VACATION_SDATE"] != "" && $this->field["BEFORE_SPRING_VACATION_EDATE"] != "") {
            $this->eventVacationSet($db, $this->field["BEFORE_SPRING_VACATION_SDATE"], $this->field["BEFORE_SPRING_VACATION_EDATE"]);
        }

        //夏休み
        if ($this->field["SUMMER_VACATION_SDATE"] != "" && $this->field["SUMMER_VACATION_EDATE"] != "") {
            $this->eventVacationSet($db, $this->field["SUMMER_VACATION_SDATE"], $this->field["SUMMER_VACATION_EDATE"]);
        }
        
        //秋休み
        if ($this->field["AUTUMN_VACATION_SDATE"] != "" && $this->field["AUTUMN_VACATION_EDATE"] != "") {
            $this->eventVacationSet($db, $this->field["AUTUMN_VACATION_SDATE"], $this->field["AUTUMN_VACATION_EDATE"]);
        }
        
        //冬休み
        if ($this->field["WINTER_VACATION_SDATE"] != "" && $this->field["WINTER_VACATION_EDATE"] != "") {
            $this->eventVacationSet($db, $this->field["WINTER_VACATION_SDATE"], $this->field["WINTER_VACATION_EDATE"]);
        }
        
        //春休み（終了式後）
        if ($this->field["AFTER_SPRING_VACATION_SDATE"] != "" && $this->field["AFTER_SPRING_VACATION_EDATE"] != "") {
            $this->eventVacationSet($db, $this->field["AFTER_SPRING_VACATION_SDATE"], $this->field["AFTER_SPRING_VACATION_EDATE"]);
        }
        
        return $result;
    }
    
    //休暇登録用の共通関数
    function eventVacationSet($db, $sDate, $eDate) {

        $setDate = $sDate;
        $endDate = $eDate;
        $datecnt = 0;
        //日付のカウントアップ
        list($year, $month, $day) = preg_split("/\//", $setDate);
        while ($setDate < $endDate) {
            $setDate = date("Y/m/d", mktime(0, 0, 0, $month, $day + $datecnt, $year));
            $query = knjs520Query::getInsert1Query($this, $setDate, $this->field["SCHOOL_KIND"]);
            $db->query($query);

            $datecnt++;
        }
    }

    //行事予定登録処理
    function dateInput2($db) {
        $data = array();
        //登録対象のデータをPUBLIC_HOLIDAY_MSTより取得
        $query = knjs520Query::getAllPublicHoliday($this->field["YEAR"]);
        $result = $db->query($query);
        $cnt = 0;
        while ($setdata = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        
            //月日を2桁変換、1月から3月の時は年を+1する
            $setdata["HOLIDAY_MONTH"] = sprintf("%02d",$setdata["HOLIDAY_MONTH"]);
            if ($setdata["HOLIDAY_MONTH"] == "01" || $setdata["HOLIDAY_MONTH"] == "02" || $setdata["HOLIDAY_MONTH"] == "03") {
                $setdata["YEAR"] = $setdata["YEAR"] + 1;
            }
            $setdata["HOLIDAY_DAY"] = sprintf("%02d",$setdata["HOLIDAY_DAY"]);
            
            //日付指定をする場合
            if ($setdata["HOLIDAY_DIV"] == "1") {                
                $data[$cnt]["EXECUTEDATE"] = $setdata["YEAR"]."-".$setdata["HOLIDAY_MONTH"]."-".$setdata["HOLIDAY_DAY"];
                $data[$cnt]["HOLIDAY_FLG"] = "1";
                $data[$cnt]["REMARK1"] = $setdata["HOLIDAY_NAME"];
                
            //曜日指定をする場合
            } else if ($setdata["HOLIDAY_DIV"] == "2") {
                //曜日コードをPHP用のコードに変換
                $setdata["HOLIDAY_WEEKDAY"] = $setdata["HOLIDAY_WEEKDAY"] - 1;
                
                $data[$cnt]["EXECUTEDATE"] = $this->getWhatDayOfWeek($setdata["YEAR"], $setdata["HOLIDAY_MONTH"], $setdata["HOLIDAY_WEEK_PERIOD"], $setdata["HOLIDAY_WEEKDAY"]);
                $data[$cnt]["HOLIDAY_FLG"] = "1";
                $data[$cnt]["REMARK1"] = $setdata["HOLIDAY_NAME"];
            }
            $cnt++;
        }
        
        $result->free();
        
        foreach ($data as $key => $setday) {
            if ($data != "") {
                $query = knjs520Query::getInsert2Query($db,$this,$setday,$this->field["SCHOOL_KIND"]);
                $db->query($query);
            }
        }
        return;
    }

    //土日登録処理
    function dateInput3($db) {
        $year = $this->field["YEAR"];
        $holiday_sunday = array();
        $holiday_saturday = array();
        //月のカウント
        for ($month = 1; $month <= 12; $month++) {
            //週のカウント
            for ($week = 1; $week <= 5; $week++) {
                //日曜日//名前, 月, 週, 曜日（0～6）の順
                if ($this->field["LEGAL_HOLIDAY_FLG"] != "" ) { 
                    $holiday_sunday[$month][$week] = array(array($month."月の第".$week."日曜日", $month, $week, 0));
                }
                //土曜日//名前, 月, 週, 曜日（0～6）の順
                if (($this->field["FIRST_SATURDAY_FLG"] != "" && $week == 1) || ($this->field["SECOND_SATURDAY_FLG"] != "" && $week == 2) || 
                    ($this->field["THIRD_SATURDAY_FLG"] != "" && $week == 3) || ($this->field["FOUR_SATURDAY_FLG"] != "" && $week == 4) || ($this->field["FIVE_SATURDAY_FLG"] != "" && $week == 5)) {
                    $holiday_saturday[$month][$week] = array(array($month."月の第".$week."土曜日", $month, $week, 6));
                }
            }
        }
        //月のカウント
        for ($month = 1; $month <= 12; $month++) {
            //週のカウント
            for ($week = 1; $week <= 5; $week++) {
                //日曜日をセットする
                if ($this->field["LEGAL_HOLIDAY_FLG"] != "" ) { 
                    foreach($holiday_sunday[$month][$week] as $value){
                        $name  = $value[0];
                        $month = $value[1];
                        $week  = $value[2];
                        $wday  = $value[3];
                        //1月～3月は年度+1する
                        if ($month == 1 || $month == 2 || $month == 3) {
                            $year = $this->field["YEAR"] + 1;
                        } else {
                            $year = $this->field["YEAR"];
                        }
                        //その月の始まりは何曜日か
                        $w = date('w', mktime(0, 0, 0, $month, 1, $year));
                        //指定された曜日の最初の日
                        $first = ($wday - $w >= 0) ? 1 + $wday - $w : 1 + $wday - $w + 7;
                        //日にちを算出
                        $day  = $first + ( 7 * ($week - 1) );
                        $date = date("Y-m-d", mktime(0, 0, 0, $month, $day, $year) );

                        $query = knjs520Query::getInsert3Query($db,$this,$date,$this->field["SCHOOL_KIND"]);
                        $db->query($query);
                    }
                }
                //土曜日をセットする
                if (($this->field["FIRST_SATURDAY_FLG"] != "" && $week == 1) || ($this->field["SECOND_SATURDAY_FLG"] != "" && $week == 2) || 
                    ($this->field["THIRD_SATURDAY_FLG"] != "" && $week == 3) || ($this->field["FOUR_SATURDAY_FLG"] != "" && $week == 4) || ($this->field["FIVE_SATURDAY_FLG"] != "" && $week == 5) ) {
                    foreach($holiday_saturday[$month][$week] as $value){
                        $name  = $value[0];
                        $month = $value[1];
                        $week  = $value[2];
                        $wday  = $value[3];
                        //1月～3月は年度+1する
                        if ($month == 1 || $month == 2 || $month == 3) {
                            $year = $this->field["YEAR"] + 1;
                        } else {
                            $year = $this->field["YEAR"];
                        }
                        //その月の始まりは何曜日か
                        $w = date('w', mktime(0, 0, 0, $month, 1, $year));
                        //指定された曜日の最初の日
                        $first = ($wday - $w >= 0) ? 1 + $wday - $w : 1 + $wday - $w + 7;
                        //日にちを算出
                        $day  = $first + ( 7 * ($week - 1) );
                        $date = date("Y-m-d", mktime(0, 0, 0, $month, $day, $year) );
                        
                        $query = knjs520Query::getInsert3Query($db,$this,$date,$this->field["SCHOOL_KIND"]);
                        $db->query($query);
                    }
                }
            }
        }
        return;
    }

    //BANK_HOLIDAY_DAT登録処理
    function InputBank($db) {
        //PUBLIC_HOLIDAY_MSTからHOLIDAY_KINDが"1"のデータを取得
        $pubHoliArr = array();
        $query = knjs520Query::getPublicHolidayKind($this->field["YEAR"], $this);
        $result = $db->query($query);
        $cnt = 0;
        while ($setdata = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //月日を2桁変換、1月から3月の時は年を+1する
            $setdata["HOLIDAY_MONTH"] = sprintf("%02d",$setdata["HOLIDAY_MONTH"]);
            if ($setdata["HOLIDAY_MONTH"] == "01" || $setdata["HOLIDAY_MONTH"] == "02" || $setdata["HOLIDAY_MONTH"] == "03") {
                $setdata["YEAR"] = $setdata["YEAR"] + 1;
            }
            $setdata["HOLIDAY_DAY"] = sprintf("%02d",$setdata["HOLIDAY_DAY"]);

            //日付指定をする場合
            if ($setdata["HOLIDAY_DIV"] == "1") {
                $pubHoliArr[$cnt]["EXECUTEDATE"]  = $setdata["YEAR"]."-".$setdata["HOLIDAY_MONTH"]."-".$setdata["HOLIDAY_DAY"];
                $pubHoliArr[$cnt]["HOLIDAY_NAME"] = $setdata["HOLIDAY_NAME"];

            //曜日指定をする場合
            } else if ($setdata["HOLIDAY_DIV"] == "2") {
                //曜日コードをPHP用のコードに変換
                $setdata["HOLIDAY_WEEKDAY"] = $setdata["HOLIDAY_WEEKDAY"] - 1;

                $pubHoliArr[$cnt]["EXECUTEDATE"]  = $this->getWhatDayOfWeek($setdata["YEAR"], $setdata["HOLIDAY_MONTH"], $setdata["HOLIDAY_WEEK_PERIOD"], $setdata["HOLIDAY_WEEKDAY"]);
                $pubHoliArr[$cnt]["HOLIDAY_NAME"] = $setdata["HOLIDAY_NAME"];
            }
            $cnt++;
        }

        //テーブルの有無チェック
        $query = knjs520Query::checkTableExist("BANK_HOLIDAY_DAT");
        $table_cnt = $db->getOne($query);

        if ($table_cnt > 0) {
            $query = knjs520Query::delInsBank($db, $this, $pubHoliArr, $this->field["SCHOOL_KIND"]);
        }
        return;
    }

    /**
     * 任意の年月の第n曜日の日付を求める関数
     * $year 年
     * $month 月
     * $number 何番目の曜日か、第1曜日なら1。第3曜日なら3
     * $dayOfWeek 求めたい曜日。0～6までの数字で曜日の日～土を指定する
     */
    function getWhatDayOfWeek($year, $month, $number, $dayOfWeek) {
        //指定した年月の1日の曜日を取得
        $firstDayOfWeek = date("w", mktime(0, 0, 0, $month, 1, $year));
        $day = $dayOfWeek - $firstDayOfWeek + 1;
        //1週間を足す
        if($day <= 0) {
            $day += 7;
        }
        $weekselect = mktime(0, 0, 0, $month, $day, $year);
        //n曜日まで1週間を足し込み
        $weekselect += (86400 * 7 * ($number - 1));
        return date("Y-m-d", $weekselect);
    }
    
    //編集完了用のモデルを返します
    function &getInsertModel() {
    
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if ($this->validateInput()) {
            $this->insert($db);
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }

        return;
    }
    
    //INSERT・UPDATE時の内部メソッド
    function &insert($db) {
        $query = knjs520Query::getInsertQuery($this,$db);
        $db->query($query);

        return;
    }

    /***************************************
    予定登録処理
    ****************************************/
    //行事予定登録処理
    function &getInsert2Model() {
    
        //画面の更新
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        if ($this->validateInput()) {
            //入力変換処理
            $this->dateDelete($db);
            //休暇日登録
            $this->dateInput1($db);
            //指定休日登録
            $this->dateInput2($db);
            //土日登録
            $this->dateInput3($db);
            //HOLIDAY_BASE_MST更新
            $this->insert($db);
            //BANK_HOLIDAY_DAT更新
            $this->InputBank($db);
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
            
        $this->setMessage("MSG201");

        return;
    }
}
?>
