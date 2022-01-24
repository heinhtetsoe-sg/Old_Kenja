<?php

require_once('for_php7.php');

class knjd194Model extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolCode;

    function knjd194Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "SOTEN_HYOUKA"     =>  VARS::post("SOTEN_HYOUKA"),     //素点・評価
                             "JOUGEN_DIV"       =>  VARS::post("JOUGEN_DIV"),       //上限値基準
                             "OVER_DIV"         =>  VARS::post("OVER_DIV"),         //上限値選択
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "KETTEN"           =>  VARS::post("KETTEN"),           //欠点
                             "EDATE"            =>  VARS::post("EDATE"),            //出欠集計範囲・終了日付
                             "DATE_DIV"         =>  VARS::post("DATE_DIV")          //出欠集計範囲（累計・学期）ラジオボタン
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $nendo = CTRL_YEAR . "/04/01";
        $wrk_nendo = common::DateConv1($nendo,10);
        $wrk_gakki = $db->getOne(knjd194Query::getSemesterName($this->field["SEMESTER"]));
        $wrk_test  = $db->getOne(knjd194Query::getTestName($this));
        $title = "成績不振・欠時過多生徒一覧";

        //$contents = "{$wrk_nendo}　{$wrk_gakki}　{$wrk_test}　{$title}\n";

        //出力データの作成//
        //SQL文発行
        //初期値
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd194Query::getAttendDate($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (str_replace("/","-",$this->field["EDATE"]) < $tmp_attend_sdate) break;
            $attend_month[] = $row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        $maxSemester = '';
        $query = knjd194Query::getMaxSemester($this, str_replace("/","-",$this->field["EDATE"]));
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $maxSemester = $row["SEMESTER"];
        }
        $result->free();
        if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);   //次の日
        }
        //SCHOOL_MSTの情報を取得。
        $year = CTRL_YEAR;
        $knjSchoolMst = array();
        $query = knjd194Query::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $knjSchoolMst[$key] = $val;
            }
        }
        $result->free();
        $query = knjd194Query::selectCsvQuery2($this,$attend_seme,$attend_month,$attend_sdate,$knjSchoolMst);
        $result = $db->query($query);
        $data = false;
        $dataTemp = array();
        $tempCnt = array();
/******************************************************************************************/
/* 教科・科目 or 総合的な時間 *************************************************************/
/******************************************************************************************/
        //ヘッダの作成//
        $str = ($this->field["SOTEN_HYOUKA"] == "1") ? "素点" : "評価";
        $header = array("SCHREGNO"      => "学籍番号",
                        "GRADE"         => "学年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "NAME"          => "氏名",
                        "SUBCLASSCD"    => "科目コード",
                        "SUBCLASSNAME"  => "科目名",
                        "STAFFNAME"     => "教科担当者",
                        "CREDITS"       => "単位",
                        "SCORE"         => $str,
                        "KETUJISU"      => "欠時",
                        "NOTICE_LATE"   => "欠課",
                        "TIKOKU_SOUTAI" => "遅早",
                        "ABSENT"        => "公欠",
                        "MOURNING"      => "忌引",
                        "SUSPEND"       => "出停",
                        "FUSIN_FLG"     => "不振区分"
                        );
        $contents .= implode($header, ",") ."\n";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTemp["SCHREGNO"]       = $row["SCHREGNO"];
            $dataTemp["GRADE"]          = $row["GRADE"];
            $dataTemp["HR_CLASS"]       = $row["HR_CLASS"];
            $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
            $dataTemp["NAME"]           = $row["NAME"];
            $dataTemp["SUBCLASSCD"]     = $row["SUBCLASSCD"];
            $dataTemp["SUBCLASSNAME"]   = $row["SUBCLASSNAME"];
            $dataTemp["STAFFNAME"]      = $db->getOne(knjd194Query::getSubclassStf($this, CTRL_YEAR, $maxSemester, $row["SCHREGNO"], $row["SUBCLASSCD"]));
            $dataTemp["CREDITS"]        = $row["CREDITS"];
            $dataTemp["SCORE"]          = $row["SCORE"];
            $dataTemp["KETUJISU"]       = $row["KETUJISU"];
            $dataTemp["NOTICE_LATE"]    = $row["NOTICE_LATE"];
            $dataTemp["TIKOKU_SOUTAI"]  = $row["TIKOKU_SOUTAI"];
            $dataTemp["ABSENT"]         = $row["ABSENT"];
            $dataTemp["MOURNING"]       = $row["MOURNING"];
            $dataTemp["SUSPEND"]        = $row["SUSPEND"];

            $strFusin = "";
            // 成績・欠課共に不振
            if ($row["FUSIN_FLG"] == "1") {
                $strFusin  = "成/";
                $strFusin .= ($this->field["JOUGEN_DIV"] == '1') ? "履" : "修";
                $strFusin .= ($this->field["OVER_DIV"] == '1')   ? "注" : "超";
            }
            // 成績不振
            if ($row["FUSIN_FLG"] == "2") {
                $strFusin  = "成";
            }
            // 欠課不振
            if ($row["FUSIN_FLG"] == "3") {
                $strFusin  = ($this->field["JOUGEN_DIV"] == '1') ? "履" : "修";
                $strFusin .= ($this->field["OVER_DIV"] == '1')   ? "注" : "超";
            }
            $dataTemp["FUSIN_FLG"]      = $strFusin;

            $contents .= implode($dataTemp, ",") ."\n";
            $data = true;
        }
/******************************************************************************************/
/******************************************************************************************/
/******************************************************************************************/

        $result->free();
        Query::dbCheckIn($db);

        if ($data) {

            $couse = "{$wrk_nendo}　{$wrk_gakki}　{$wrk_test}　{$title}";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
