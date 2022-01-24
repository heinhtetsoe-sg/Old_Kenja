<?php

require_once('for_php7.php');

class knjs530Model extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;          //セキュリティー用スタッフコード
    var $sec_competence;   //権限保持
    var $weekArray = array();
    var $updDateArray = array();
    

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
                                     
        $this->field = array("DATA_DIV"                 => VARS::post("DATA_DIV"),
                             "SCHOOL_KIND"              => VARS::post("SCHOOL_KIND"),
                             "GRADE"                    => VARS::post("GRADE"),
                             "HR_CLASS_DIV"             => VARS::post("HR_CLASS_DIV"),
                             "COURSE_MAJOR"             => VARS::post("COURSE_MAJOR"),
                             "EXECUTEDATE"              => VARS::post("EXECUTEDATE"),
                             "HOLIDAY_FLG"              => VARS::post("HOLIDAY_FLG"),
                             "REMARK1"                  => VARS::post("REMARK1"),
                             "REMARK2"                  => VARS::post("REMARK2"),
                             "UPDATED"                  => VARS::post("UPDATED"),
                             
                             "YEAR"                     => VARS::post("YEAR"),
                             "MONTH"                    => VARS::post("MONTH"),
                             "OUTPUT"                   => VARS::post("OUTPUT"),    //初期化対象

                             //コピー処理用
                             "COPY_GRADE"               => VARS::post("COPY_GRADE"),
                             "COPY_MAJOR"               => VARS::post("COPY_MAJOR"),
                             "COPY_SCHOOL_KIND"         => VARS::post("COPY_SCHOOL_KIND"),
                             "COPY_EVENT_CNT"           => VARS::post("COPY_EVENT_CNT"),
                             "COPY_HR_CLASS"            => VARS::post("COPY_HR_CLASS"),
                             
                             "HR_CLASS"                 => VARS::post("HR_CLASS")
                             );
        
        
        //日付の初期値をセット
        $day = 1;
        $year = $this->field["MONTH"] > 3 ? $this->field["YEAR"] : $this->field["YEAR"] + 1;
        $last_day = date("t", mktime(0, 0, 0, $this->field["MONTH"], 1, $year));
        
        //日付のカウントアップ（対象月の最終日まで）
        $addCnt = 0;
        //曜日のカウントアップ
        $youbiCnt = 0;
        $this->updfield = array();
        for ($i = 0; $i < $last_day; $i++) {
            $setDay = date("Y-m-d", mktime( 0, 0, 0, $this->field["MONTH"], $day + $addCnt, $year));
            $this->updfield[$setDay]["REMARK1"] = VARS::post("REMARK1_".$setDay);
            $this->updfield[$setDay]["REMARK2"] = VARS::post("REMARK2_".$setDay);
            $this->updfield[$setDay]["HOLIDAY_FLG"] = VARS::post("HOLIDAY_FLG_".$setDay);
            $addCnt++;
        }
        if ($this->cmd == "") {
            $this->field = array();
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    
    /***************************************
    更新メイン処理
    ****************************************/
    function &getInsertModel() {
    
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //更新処理
        $this->insert($db);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");

        return;
    }
    
    /*****************
    更新用の各処理
    ******************/
    //データの更新（日付をkeyにした配列、日付ごとに更新する）
    function insert($db) {
        foreach ($this->updfield as $key => $val) {
            knjs530Query::getInsertQuery($this, $key, $val, $db);
        }
    }
    
    /***************************************
    初期化メイン処理
    ****************************************/
    
    //行事予定登録処理
    function &getInsert2Model() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $messege_flg = false;
        //データ区分が1:学校の場合
        if ($this->field["DATA_DIV"] == "1") {
            //取得マスタチェック
            if ($this->cmd == 'shokitiyear') {
                $query = knjs530Query::getCountPublicHolidayHolidayBase($this, "1");
            } else {
                $query = knjs530Query::getCountPublicHolidayHolidayBase($this, "2");
            }
            $getCountMst = $db->getOne($query);
            if ($getCountMst > 0) {
                //対象データ削除処理
                $this->dateDelete($db);
                //休暇日登録
                $this->dateInput1($db);
                //指定休日登録
                $this->dateInput2($db);
                //土日登録
                $this->dateInput3($db);
                $messege_flg = true;
            }
        //データ区分が2:教務の場合
        } else {
            //学校データをカウント
            if ($this->cmd == 'shokitiyear') {
                $query = knjs530Query::getEventSchoolcount($this);
            } else {
                $query = knjs530Query::getEventSchoolcountMonth($this);
            }
            $getSchoolevent = $db->getOne($query);
            //学校データがある場合
            if ($getSchoolevent > 0) {
                //対象データ削除処理
                $this->dateDelete($db);
                //学校データセット処理
                $this->dateCopy($db);
                $messege_flg = true;
            //学校データがない場合
            } else {
                //取得マスタチェック
                if ($this->cmd == 'shokitiyear') {
                    $query = knjs530Query::getCountPublicHolidayHolidayBase($this, "1");
                } else {
                    $query = knjs530Query::getCountPublicHolidayHolidayBase($this, "2");
                }
                $getCountMst = $db->getOne($query);
                if ($getCountMst > 0) {
                    //対象データ削除処理
                    $this->dateDelete($db);
                    //休暇日登録
                    $this->dateInput1($db);
                    //指定休日登録
                    $this->dateInput2($db);
                    //土日登録
                    $this->dateInput3($db);
                    $messege_flg = true;
                }
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        
        if ($messege_flg) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG303", "公休日マスタ、基本休日設定を登録して下さい。");
        }

        return;
    }
    
    /*****************
    初期化用の各処理
    ******************/
    
    //行事予定登録処理用休業
    function dateDelete($db) {
        //EVENT_MSTを削除
        if ($this->cmd == 'shokitiyear') {
            $query = knjs530Query::getEventDelQuery($this);
        } else {
            $query = knjs530Query::getEventDelQueryMonth($this);
        }    
        $db->query($query);
    }
        
    //教務初期化処理
    function dateCopy($db) {
        //学校のEVENT_MSTを取得し、教務のEVENT_MSTへInsertする
        if ($this->cmd == 'shokitiyear') {
            $query = knjs530Query::getEventSchoolQuery($this);
        } else {
            $query = knjs530Query::getEventSchoolQueryMonth($this);
        }
        $result = $db->query($query);
        while ($setSchoolData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjs530Query::getEventCopyQuery($setSchoolData, $this);
            $db->query($query);
        }
        $result->free();
        return;
    }

    //行事予定登録処理用休業
    function dateInput1($db) {
        
        //各休業期間の開始日、終了日を取得
        $query = knjs530Query::getHolidayBaseMst($this);
        $result = $db->query($query);
        while ($setHolidayBaseData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        
            //春休み（始業式前）
            if ($setHolidayBaseData["BEFORE_SPRING_VACATION_FLG"] != "") {
                $this->eventVacationSet($db, $setHolidayBaseData["BEFORE_SPRING_VACATION_SDATE"], $setHolidayBaseData["BEFORE_SPRING_VACATION_EDATE"]);
            }
            //夏休み
            if ($setHolidayBaseData["SUMMER_VACATION_FLG"] != "") {
                $this->eventVacationSet($db, $setHolidayBaseData["SUMMER_VACATION_SDATE"], $setHolidayBaseData["SUMMER_VACATION_EDATE"]);
            }
            //秋休み
            if ($setHolidayBaseData["AUTUMN_VACATION_FLG"] != "") {
                $this->eventVacationSet($db, $setHolidayBaseData["AUTUMN_VACATION_SDATE"], $setHolidayBaseData["AUTUMN_VACATION_EDATE"]);
            }
            //冬休み
            if ($setHolidayBaseData["WINTER_VACATION_FLG"] != "") {
                $this->eventVacationSet($db, $setHolidayBaseData["WINTER_VACATION_SDATE"], $setHolidayBaseData["WINTER_VACATION_EDATE"]);
            }
            //春休み（終了式後）
            if ($setHolidayBaseData["AFTER_SPRING_VACATION_FLG"] != "") {
                $this->eventVacationSet($db, $setHolidayBaseData["AFTER_SPRING_VACATION_SDATE"], $setHolidayBaseData["AFTER_SPRING_VACATION_EDATE"]);
            }
        }
        $result->free();
        return;
    }
    
    //休暇登録用の共通関数
    function eventVacationSet($db, $sDate, $eDate) {

        $setDate = $sDate;
        $endDate = $eDate;
        $datecnt = 0;
        //日付のカウントアップ
        list($year, $month, $day) = preg_split("/-/", $setDate);
        list($endyear, $endmonth, $endday) = preg_split("/-/", $endDate);
        
        //開始日付セット用（休暇開始日が指定月と異なるとき使用）
        $set1Date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], 1, $year));
        //終了日付セット用（休暇終了日が指定月と異なるとき使用）※フィールドの月の最終日を取得し、セットする
        $endsetday = date("t", mktime(0, 0, 0, $this->field["MONTH"], $day, $year));
        $endsetDate = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], $endsetday, $year));
        //年またがりの開始日、終了日セット用
        $overYearset1Date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], 1, $year+1));
        $overYearendsetDate = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], $endsetday, $year+1));
        //カウントアップ用
        list($setyear, $setmonth, $setday) = preg_split("/-/", $overYearset1Date);
        
        //初期化対象が対象年度全ての場合
        if ($this->cmd == 'shokitiyear') {
            while ($setDate < $endDate) {
                $setDate = date("Y-m-d", mktime(0, 0, 0, $month, $day + $datecnt, $year));
                $query = knjs530Query::getInsert1Query($setDate, $this);
                $db->query($query);

                $datecnt++;
            }
        //初期化対象が指定月の場合
        } else {
            //1.休業の開始月と指定月が一致している場合
            if ($month == $this->field["MONTH"]) {
                //1-1.休業の終了月と指定月が一致している時（月をまたがっていない）
                if ($endmonth == $this->field["MONTH"]) {
                    //$datecnt = countHoliday($setDate, $endDate, $month, $day, $year, $datecnt, $db, $this);
                    while ($setDate < $endDate) {
                        $setDate = date("Y-m-d", mktime(0, 0, 0, $month, $day + $datecnt, $year));
                        $query = knjs530Query::getInsert1Query($setDate, $this);
                        $db->query($query);

                        $datecnt++;
                    }
                    
                //1-2.休業の終了月と指定月が一致していない時（月をまたがっている）
                } else if ($endmonth > $this->field["MONTH"] && $endyear == $this->field["YEAR"]) {
                    //指定月の最終日をセットし、カウント対象を指定月の終了日に修正して、更新する
                    //$datecnt = countHoliday($setDate, $endsetDate, $month, $day, $year, $datecnt, $db, $this);
                    while ($setDate < $endsetDate) {
                        $setDate = date("Y-m-d", mktime(0, 0, 0, $month, $day + $datecnt, $year));
                        $query = knjs530Query::getInsert1Query($setDate, $this);
                        $db->query($query);

                        $datecnt++;
                    }
                    
                //1-3.休業の終了月と指定月が一致していない時（年をまたがっている）
                } else if ($endmonth < $this->field["MONTH"] && $endyear != $this->field["YEAR"]) {
                    while ($setDate < $endsetDate) {
                        $setDate = date("Y-m-d", mktime(0, 0, 0, $month, $day + $datecnt, $year));
                        $query = knjs530Query::getInsert1Query($setDate, $this);
                        $db->query($query);

                        $datecnt++;
                    }
                }
                
            //2.休業の開始月と指定月が一致していない場合  (休業の開始月が指定月より前で終了月が指定つきよりも同じもしくは後の場合が処理の対象となる)
            } else if ($month < $this->field["MONTH"] && $endmonth >= $this->field["MONTH"]) {
                //2-1.休業の終了月と指定月が一致している時（月をまたがっていない）
                //指定月の最初の日をセットし、カウント対象の開始日を指定月の最初の日に修正して、更新する
                if ($endmonth == $this->field["MONTH"]) {
                    while ($set1Date < $endDate) {
                        $set1Date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], 1 + $datecnt, $year));
                        $query = knjs530Query::getInsert1Query($set1Date, $this);
                        $db->query($query);

                        $datecnt++;
                    }
                //2-2.休業の終了月と指定月が一致していない時（月をまたがっている）
                } else if ($endmonth > $this->field["MONTH"]) {
                    while ($set1Date < $endsetDate) {
                        $set1Date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], 1 + $datecnt, $year));
                        $query = knjs530Query::getInsert1Query($set1Date, $this);
                        $db->query($query);

                        $datecnt++;
                    }
                }
                
            //3.休業の開始月と指定月が一致していない場合でかつ開始年と指定年が異なる（年をまたがっているとき）
            } else if ($month > $this->field["MONTH"] && $endyear != $year) {
                //3-1.終了月が指定月と一致
                if ($endmonth == $this->field["MONTH"]) {
                    while ($overYearset1Date < $endDate) {
                        $overYearset1Date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], 1 + $datecnt, $setyear));
                        $query = knjs530Query::getInsert1Query($overYearset1Date, $this);
                        $db->query($query);

                        $datecnt++;
                    }
                //3-2.休業の終了月と指定月が一致していない時（月をまたがっている）
                } else if ($endmonth > $this->field["MONTH"]) {
                    while ($overYearset1Date < $overYearendsetDate) {
                        $overYearset1Date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], 1 + $datecnt, $setyear));
                        $query = knjs530Query::getInsert1Query($overYearset1Date, $this);
                        $db->query($query);

                        $datecnt++;
                    }
                }
            //4.休業の開始月および終了月が指定月と一致していない場合でかつ年が異なる（年をまたがっているとき）
            } else if ($month < $this->field["MONTH"] && $endyear != $year) {
                //4-1.休業の終了月と指定月が一致していない（年をまたがっている）
                while ($overYearset1Date < $overYearendsetDate) {
                    $overYearset1Date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], 1 + $datecnt, $setyear));
                    $query = knjs530Query::getInsert1Query($overYearset1Date, $this);
                    $db->query($query);

                    $datecnt++;
                }
            }
        }
    }
    
    //休業期間をカウントアップする共通関数
    /*function countHoliday($sDate, $eDate, $sMonth, $sDay, $sYear, $scnt, $db, $this) {
        while ($sDate < $eDate) {
            $sDate = date("Y-m-d", mktime(0, 0, 0, $sMonth, $sDay + $scnt, $sYear));
            $query = knjs530Query::getInsert1Query($sDate, $this);
            $db->query($query);

            $scnt++;
        }
        return $scnt;
    }*/
    
    //行事予定登録処理
    function dateInput2($db) {
        $data = array();
        //登録対象のデータをPUBLIC_HOLIDAY_MSTより取得
        if ($this->cmd == 'shokitiyear') {
            $query = knjs530Query::getAllPublicHoliday($this->field["YEAR"]);
        } else {
            $setmonth = sprintf("%02d",$this->field["MONTH"]);
            $query = knjs530Query::getAllPublicHolidayMonth($this->field["YEAR"], $setmonth);
        }
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
                if ($this->cmd == 'shokitiyear') {
                    $query = knjs530Query::getInsert2Query($db,$setday,$this, "1");
                } else {
                    $query = knjs530Query::getInsert2QueryMonth($db,$setday,$this);
                }
                $db->query($query);
            }
        }
        return;
    }

    //土日登録処理
    function dateInput3($db) {
    
        //指定年度、校種の土日の情報を取得
        $holidayData = array();
        $query = knjs530Query::getHolidayBaseMst($this);
        $holidayData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        //カウント月の設定
        $start_month = "";
        $end_month = "";
        if ($this->cmd == 'shokitiyear') {
            $start_month = "1";
            $end_month = "12";
        } else {
            $start_month = intval(ltrim($this->field["MONTH"],'0'));
            $end_month = intval(ltrim($this->field["MONTH"],'0'));
        }
        
        $year = $this->field["YEAR"];
        $holiday_sunday = array();
        $holiday_saturday = array();
        //月のカウント
        for ($month = $start_month; $month <= $end_month; $month++) {
            //週のカウント
            for ($week = 1; $week <= 5; $week++) {
                //日曜日//名前, 月, 週, 曜日（0～6）の順
                if ($holidayData["LEGAL_HOLIDAY_FLG"] != "" ) { 
                    $holiday_sunday[$month][$week] = array(array($month."月の第".$week."日曜日", $month, $week, 0));
                }
                //土曜日//名前, 月, 週, 曜日（0～6）の順
                if (($holidayData["FIRST_SATURDAY_FLG"] != "" && $week == 1) || ($holidayData["SECOND_SATURDAY_FLG"] != "" && $week == 2) || 
                    ($holidayData["THIRD_SATURDAY_FLG"] != "" && $week == 3) || ($holidayData["FOUR_SATURDAY_FLG"] != "" && $week == 4) || ($holidayData["FIVE_SATURDAY_FLG"] != "" && $week == 5)) {
                    $holiday_saturday[$month][$week] = array(array($month."月の第".$week."土曜日", $month, $week, 6));
                }
            }
        }
        //月のカウント
        for ($month = $start_month; $month <= $end_month; $month++) {
            //週のカウント
            for ($week = 1; $week <= 5; $week++) {
                //日曜日をセットする
                if ($holidayData["LEGAL_HOLIDAY_FLG"] != "" ) { 
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

                        $query = knjs530Query::getInsert2Query($db,$date,$this, "2");
                        $db->query($query);
                    }
                }
                //土曜日をセットする
                if (($holidayData["FIRST_SATURDAY_FLG"] != "" && $week == 1) || ($holidayData["SECOND_SATURDAY_FLG"] != "" && $week == 2) || 
                    ($holidayData["THIRD_SATURDAY_FLG"] != "" && $week == 3) || ($holidayData["FOUR_SATURDAY_FLG"] != "" && $week == 4) || ($holidayData["FIVE_SATURDAY_FLG"] != "" && $week == 5) ) {
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
                        
                        $query = knjs530Query::getInsert2Query($db,$date,$this, "2");
                        $db->query($query);
                    }
                }
            }
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

    /****************/
    /*  コピー処理  */
    /****************/
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象データ削除処理
        $this->copyDelete($db);
        //データセット処理
        $this->copyInsert($db);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        return;
    }

    function copyInsert($db) {
        //コピー元のデータを取得
        if ($this->cmd == 'copy_year') {
            $query = knjs530Query::getCopyEventMotoQuery($this);
        } else {
            $query = knjs530Query::getCopyEventMotoQuery($this, "month");
        }
        $result = $db->query($query);
        while ($setSchoolData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //コピー先へInsertする
            $query = knjs530Query::copyEventInsertQuery($setSchoolData, $this);
            $db->query($query);
        }
        $result->free();
        return;
    }

    function copyDelete($db) {
        //EVENT_MSTを削除
        if ($this->cmd == 'copy_year') {
            $query = knjs530Query::getCopyEventDelQuery($this);
        } else {
            $query = knjs530Query::getCopyEventDelQuery($this, "month");
        }    
        $db->query($query);
    }
}
?>
