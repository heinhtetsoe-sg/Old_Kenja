<?php

require_once('for_php7.php');


class knja270Model extends Model {
    var $field          = array();
    var $select_data    = array();
    var $opt_subclass   = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $date_gakki;   //学期（任意の日付指定時）
    var $date_gakki2;   //終了学期学期（任意の日付指定時）
    var $absent_cov; // 欠課数換算
    var $absent_cov_late; // 欠課数換算遅刻
    var $virus;
    var $koudome;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja270Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //学校名取得
        $query = knja270Query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期
                            "RUIKEI"            =>  VARS::post("RUIKEI"),           //'1:毎 2:累計' 選択ラジオ NO001
                            "ATTENDDATE"        =>  VARS::post("ATTENDDATE"),       //出欠集計日付 NO001
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //出力種別
                            "OUTDIV"            =>  VARS::post("OUTDIV"),           //欠席者のみチェックボックス
                            "DATE"              =>  VARS::post("DATE"),             //開始日付（任意）
                            "DATE2"             =>  VARS::post("DATE2"),            //終了日付（任意）
                            "GAKKI_SDATE"       =>  VARS::post("GAKKI_SDATE"),      //開始日付（学期）
                            "GAKKI_FDATE"       =>  VARS::post("GAKKI_FDATE"),      //終了日付（学期）
                            "ABBDATE"           =>  VARS::post("ABBDATE"),          //日付（欠席者）
                            "PERIODCD"          =>  VARS::post("PERIODCD"),         //校時（欠席者）
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );
        $this->cmd         = VARS::request("cmd");
        $tmp  = VARS::post("DATE_SEME");
        if(isset($tmp)) $this->date_gakki  = VARS::post("DATE_SEME");
        $tmp  = VARS::post("DATE_SEME2");
        if(isset($tmp)) $this->date_gakki2  = VARS::post("DATE_SEME2");

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->getPropertiesAll(); //使用するテーブルを取得
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->virus     = ($this->Properties["useVirus"] == 'true') ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
    }

    function clean()
    {
    }

    function getDownloadModel()
    {
        $kinndai = false;    //近大判定用フラグ

        $db = Query::dbCheckOut();

        $opt_year=array();
        $query = knja270Query::getSelectName();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_name[$row["NAMECD2"]] = $row["NAME1"];
        }

        //ヘッダの作成//
        if ($this->field["OUTPUT"] == 1)
        {
            $header = array("YEAR"          => "年度",
                            "SEMESTER"      => "学期",
                            "SCHREGNO"      => "学籍番号",
                            "GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "NAME_SHOW"     => "生徒氏名",
                            "ATTENDDATE"    => "出欠日付",
                            "DI_CD"         => "勤怠コード",
                            "PERIODCD"      => "校時",
                            "SUBCLASSNAME"  => "科目",
                            "NAME1"         => "出欠",
                            "DI_REMARK_CD"  => "備考コード",
                            "DI_REMARK"     => "備考",
                            "COUNTFLG"      => "集計フラグ",
                            "CHAIRNAME"     => "講座",
                            );
        }
        if ($this->field["OUTPUT"] == 2)
        {
            $header = array("YEAR"          => "年度",
                            "SEMESTER"      => "学期",
                            "SCHREGNO"      => "学籍番号",
                            "GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "NAME_SHOW"     => "生徒氏名",
                            "ATTENDDATE"    => "出欠日付",
                            "PERIODCD"      => "校時",
                            "CLASSNAME"     => "教科",
                            "NAME1"         => "出欠",
                            "DI_REMARK_CD"  => "備考コード",
                            "DI_REMARK"     => "備考",
                            );
        }
        if ($this->field["OUTPUT"] == 3)
        {
            if ($this->schoolName == 'tosajoshi') {
                $header = array("YEAR"          => "年度",
                                "SEMESTER"      => "学期",
                                "SCHREGNO"      => "学籍番号",
                                "GRADE"         => "学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "NAME_SHOW"     => "生徒氏名",
                                "LESSON"        => "授業日数",
                                "APPOINTED_DAY" => "集計日付",
                                "ABSENT"        => "公欠",
                                "SUSPEND"       => "$opt_name[2]",
                                "MOURNING"      => "忌引",
                                "OFFDAYS"       => "休学",
                                "ABROAD"        => "留学",
                                "SICK"          => "$opt_name[4]",
                                "NOTICE"        => "$opt_name[5]",
                                "NONOTICE"      => "$opt_name[6]",
                                "NOTICE_LATE"   => "欠課",
                                "LATE"          => "遅刻",
                                "EARLY"         => "早退",
                                "SUM_LE_KEKKA"  => "早退回数と欠課日数の合計"
                                );
            } else {
                $header = array("YEAR"          => "年度",
                                "SEMESTER"      => "学期",
                                "SCHREGNO"      => "学籍番号",
                                "GRADE"         => "学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "NAME_SHOW"     => "生徒氏名",
                                "LESSON"        => "授業日数",
                                "APPOINTED_DAY" => "集計日付",
                                "ABSENT"        => "公欠",
                                "SUSPEND"       => "$opt_name[2]",
                                "MOURNING"      => "忌引",
                                "OFFDAYS"       => "休学",
                                "ABROAD"        => "留学",
                                "SICK"          => "$opt_name[4]",
                                "NOTICE"        => "$opt_name[5]",
                                "NONOTICE"      => "$opt_name[6]",
                                "LATE"          => "遅刻",
                                "EARLY"         => "早退"
                                );
            }
        }

        if ($this->field["OUTPUT"] == 4)
        {
            $header = array("YEAR"           => "年度",
                            "SEMESTER"       => "学期",
                            "SCHREGNO"       => "学籍番号",
                            "GRADE"          => "学年",
                            "HR_CLASS"       => "組",
                            "ATTENDNO"       => "出席番号",
                            "NAME_SHOW"      => "生徒氏名",
                            "SUBCLASSCD"     => "科目コード",
                            "SUBCLASSNAME"   => "科目名",
                            "CREDITS"        => "単位数",
                            "LESSON"         => "時数",
                            "APPOINTED_DAY"  => "集計日付",
                            "ABSENT"         => "公欠",
                            "SUSPEND"        => "$opt_name[2]",
                            "MOURNING"       => "忌引",
                            "OFFDAYS"        => "休学",
                            "ABROAD"         => "留学",
                            "SICK"           => "$opt_name[4]",
                            "NOTICE"         => "$opt_name[5]",
                            "NONOTICE"       => "$opt_name[6]",
                            "NURSEOFF"       => "保健室欠課",
                            "LATE"           => "遅刻（欠課数換算前の数字）",
                            "EARLY"          => "早退（欠課数換算前の数字）",
                            "NOTICE_LATE"    => "欠課数",
                            );
        }
        if ($this->field["OUTPUT"] == "5") {
            $header = array("GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "CHAIRCD"       => "講座コード",
                            "NAME_SHOW"     => "生徒氏名",
                            "NAME1"         => "出欠"
                            );
        }

        $contents = "" ;
        if ($this->field["OUTPUT"] == "5") {
            $periName = $db->getOne(knja270Query::getPeriodcd($this, $this->field["PERIODCD"]));
            $contents .="日付：{$this->field["ABBDATE"]},,校時：{$periName}"."\n";
        }
        $contents .= implode($header, ",").",";

        if ($this->field["OUTPUT"] == "1") {
            $cnt_query = knja270Query::getStaffCount($this);
            $cnt = ($db->getOne($cnt_query)) ? $db->getOne($cnt_query) : "1";
            for ($i=1 ; $i<= $cnt ; $i++) {
                $contents .= "講座担当職員".$i.",";
            }
        }

        $contents .= "\n";

        //近大判定
        $db = Query::dbCheckOut();
        $row = $db->getRow(knja270Query::getSelectKindai(),DB_FETCHMODE_ASSOC);
        if (is_array($row)) {
            $kinndai = true;
        }
        Query::dbCheckIn($db);

        // 科目別のみ (※2019/12/13追加 学期別かつ土佐女子の場合も取得)
        if ($this->field["OUTPUT"] == 4 || ($this->schoolName == 'tosajoshi' && $this->field["OUTPUT"] == 3)) {
            // 遅刻何回で欠課とするかの指数取得
            $absent = knja270Query::getScAbsentCov($this->field["YEAR"]);
            $this->absent_cov = $absent["ABSENT_COV"];
            $this->absent_cov_late = $absent["ABSENT_COV_LATE"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //学校マスタ
        $knjSchoolMst = array();
        $query = knja270Query::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $knjSchoolMst[$key] = $val;
            }
        }
        $result->free();

        //学期マスタ取得
        $gakkimei = array();
        $query = knja270Query::getSemesterMst($this->field["YEAR"]);
        $result = $db->query($query);
        while ($semeRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gakkimei[$semeRow["SEMESTER"]] = $semeRow["SEMESTERNAME"];
        }
        $result->free();

        //出力データの作成//
        //SQL文発行
        if ($this->field["OUTPUT"] == 1)
            $query = knja270Query::selectCsvQuery($this);

//        if ($this->field["OUTPUT"] == 2)
//            $query = knja270Query::selectCsvQuery1($this);

        if ($this->field["OUTPUT"] == 3) {
            $query = knja270Query::selectCsvQuery2($this, $knjSchoolMst);
        }

        if ($this->field["OUTPUT"] == 4 && $this->field["RUIKEI"] == 1)
            $query = knja270Query::selectCsvQuery3($this, $knjSchoolMst);
//NO001-->
        if ($this->field["OUTPUT"] == 4 && $this->field["RUIKEI"] == 2) {
            //初期値
            $attend_sdate = "";
            $attend_seme = "";
            $attend_month = array();
            $query = knja270Query::getAttendDate($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
//NO002Add-->
                if ($kinndai) {//近大高校の場合
                    $spday = preg_split("/-/",$tmp_attend_sdate);
                    if ($spday[2] == "01") {
                        if ($spday[1] == "12") {
                            $spday[1] = "01";
                            $spday[0]++;
                        } else {
                            $spday[1]++;
                        }
                        $tmp_attend_sdate = $spday[0]."-".sprintf("%02d",$spday[1])."-".sprintf("%02d",$spday[2]);
                    }
                }
//NO002Add<--
                if (str_replace("/","-",$this->field["ATTENDDATE"]) < $tmp_attend_sdate) break;
                $attend_month[] = $row["MONTH"];
                $attend_sdate = $tmp_attend_sdate;
                $attend_seme = $row["SEMESTER"];
            }
            $result->free();
            if ($attend_sdate == "") {
                $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".$this->field["YEAR"]."' AND SEMESTER='1'";
                $attend_sdate = $db->getOne($query2);//学期開始日
            } else {
                $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
                $attend_sdate = $db->getOne($query2);//次の日
            }
            $query = knja270Query::selectCsvQuery4($this, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst);
//echo $query;
        }
        if ($this->field["OUTPUT"] == "5")
            $query = knja270Query::selectCsvQuery5($this);
//NO001<--

        $result = $db->query($query);
        $data = false;

        $row_tmp = "";

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if ($this->field["OUTPUT"] == 1) {
                if ($row_tmp != $row["SCHREGNO"].$row["ATTENDDATE"].$row["PERIODCD"]) {
                    if ($row_tmp) $contents .= "\n";
                    $contents .= $this->control["年度"].",";
                    $contents .= $gakkimei[$this->date_gakki].",";
                    $contents .= implode($row, ",");
                } else {
                    $contents .= ",".$row["STAFFNAME"];
                }
                $row_tmp = $row["SCHREGNO"].$row["ATTENDDATE"].$row["PERIODCD"];
            }

            if ($this->field["OUTPUT"] == 2) {
                $contents .= $this->control["年度"].",";
                $contents .= $gakkimei[$this->date_gakki].",";
            }
            if ($this->field["OUTPUT"] == 4 || $this->field["OUTPUT"] == 3) {//NO004Modify
                $contents .= $this->field["YEAR"].",";
                $contents .= $gakkimei[$this->field["GAKKI"]].",";
            }

            if ($this->field["OUTPUT"] == 3 || ($this->field["OUTPUT"] == 4 && $this->field["RUIKEI"] == 1)) {//NO003Add
                if ($kinndai) {
                    $spday = preg_split("/\//",$row["MAXDAY"]);
                    if ($spday[2] == "01") {
                        if ($spday[1] == "12") {
                            $spday[1] = "01";
                            $spday[0]++;
                        } else {
                            $spday[1]++;
                        }
                    }
                    $row["MAXDAY"] = $spday[0]."/".sprintf("%02d", $spday[1])."/".sprintf("%02d", $spday[2]);
                }
            }//NO003Add

            if ($this->field["OUTPUT"] != 1) $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["OUTPUT"] == 1) $couse = "出欠データ（通常授業）";
            if ($this->field["OUTPUT"] == 2) $couse = "出欠データ（０校時）";
            if ($this->field["OUTPUT"] == 3) $couse = "出欠データの集計（学期別）";
            if ($this->field["OUTPUT"] == 4) $couse = "出欠データの集計（科目別）";
            if ($this->field["OUTPUT"] == 5) $couse = "出欠データ（欠席者）";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
