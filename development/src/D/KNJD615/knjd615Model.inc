<?php

require_once('for_php7.php');

class knjd615Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
    var $selectSemeName;
    var $selectTestName;
    var $virus;
    var $koudome;
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd615Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "KETTEN"           =>  VARS::post("KETTEN"),           //欠点
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //チェックボックス選択データ
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),          //チェックボックス選択データ
                             "OUTPUT_COURSE"    =>  VARS::post("OUTPUT_COURSE"),    //チェックボックス選択データ
                             "OUTPUT_SOUGOU"    =>  VARS::post("OUTPUT_SOUGOU"),    //チェックボックス選択データ
                             "OUTPUT_RANK"      =>  VARS::post("OUTPUT_RANK"),      //総合順位出力ラジオボタン
                             "OUTPUT_KIJUN"     =>  VARS::post("OUTPUT_KIJUN"),     //順位の基準点ラジオボタン
                             "SUBCLASS_MAX"     =>  VARS::post("SUBCLASS_MAX"),     //科目数選択
                             "FORM_SELECT"      =>  VARS::post("FORM_SELECT"),      //フォーム選択
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "SDATE"            =>  VARS::post("SDATE"),            //集計開始日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
        $this->selectSemeName = VARS::post("selectSemeName");
        $this->selectTestName = VARS::post("selectTestName");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->virus = ($this->Properties["useVirus"] == "true") ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
    }

    function getDownloadModel() {
        //変数定義
        $comma = ",";   //カンマ
        $lf = "\n";      //改行
        //DB接続
        $db = Query::dbCheckOut();
        //ヘッダ配列
        $header = array("YEAR"         => "年度",
                        "SEMESTER"     => "学期",
                        "SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名",
                        "TESTITEMNAME" => "テスト種別名"
                        );
        $rankName = ($this->field["OUTPUT_RANK"] == "2") ? "学年順位" : "コース順位";
        $header2 = array("SCORE"        => "総合点",
                         "AVG"          => "平均点",
                         "CLASS_RANK"   => "学級順位",
                         "GRADE_RANK"   => $rankName,
                         "KETTEN_CNT"   => "欠点科目数"
                         );
        $subHeader = array("YEAR"         => "出欠集計日付：" .$this->field["DATE"],
                           "SEMESTER"     => "",
                           "SCHREGNO"     => "",
                           "GRADE"        => "",
                           "HR_CLASS"     => "",
                           "ATTENDNO"     => "",
                           "NAME_SHOW"    => "",
                           "TESTITEMNAME" => ""
                           );
        //ヘッダ配列を出力変数にセット
        $headerContents     = implode($header, $comma) .$comma;
        $subHeaderContents  = implode($subHeader, $comma) .$comma;
        //科目コード・科目名の取得
        $this->arrSubclass = array();
        $query  = knjd615Query::getSubClass($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->arrSubclass[] = $row["VALUE"];
            $headerContents     .= $row["SUBCLASSNAME"] .$comma ."欠課時数" .$comma;
            $subHeaderContents  .= $row["VALUE"] .$comma .$comma;
        }
        $result->free();
        $headerContents .= implode($header2, $comma) .$lf;
        $subHeaderContents .= $lf;
        //締め日など
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd615Query::getAttendDate($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (str_replace("/", "-", $this->field["DATE"]) < $tmp_attend_sdate) break;
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
            $attend_month[] = $row["MONTH"];
        }
        $result->free();
        if ($attend_sdate == "") {
            //1学期開始日
            $query = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER = '1'";
            $attend_sdate = $db->getOne($query);
        } else {
            //次の日
            $query = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query);
        }
        //学校マスタ
        $knjSchoolMst = array();
        $query = knjd615Query::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $knjSchoolMst[$key] = $val;
            }
        }
        $result->free();
        //メイン（成績・出欠）
        $dataContents = "";
        $data = false; //CSV出力フラグ
        $query = knjd615Query::getCsvQuery($this, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace($comma, ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["YEAR"]     = CTRL_YEAR;
            $row["SEMESTER"] = $this->selectSemeName;
            $row["TESTNAME"] = $this->selectTestName;
            $kettenCnt = 0;

            $kettenCnt = $this->count_ketten($row, $db);

            for ($i = 0; $i < get_count($this->arrSubclass); $i++) {
                if ($row["DI" .$i] == 0) $row["DI" .$i] = "";
            }

            if (0 < $kettenCnt) $row["KETTEN"] = $kettenCnt;
            $dataContents .= implode($row, $comma) .$lf;
            $data = true; //CSV出力フラグ
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        $contents  = "";
        $contents .= $headerContents;
        $contents .= $subHeaderContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", "成績一覧表");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //欠点にするかしないかを数える
    function count_ketten($row, $db) {
        $kettenCnt = 0;
        if (($this->Properties["checkKettenDiv"] != '1' && $this->Properties["checkKettenDiv"] != '2') || ($this->Properties["checkKettenDiv"] == '1' && $this->field["SEMESTER"] == '9')) {

            $kettenLine = ($this->field["TESTKINDCD"] == "9909") ? 2 : (int)$this->field["KETTEN"];
            for ($i = 0; $i < get_count($this->arrSubclass); $i++) {
                if ($row["SCORE" .$i] != "" && $row["SCORE" .$i] < $kettenLine) {
                    $kettenCnt++;
                }
            }
        } elseif ($this->Properties["checkKettenDiv"] == '1') {

            for ($i = 0; $i < get_count($this->arrSubclass); $i++) {
                $query = knjd615Query::get_count_record_slump_dat($this, $row, $this->arrSubclass[$i]);
                if (0 < $db->getOne($query)) {
                    $kettenCnt++;
                }
            }
        } elseif ($this->Properties["checkKettenDiv"] == '2') {

            for ($i = 0; $i < get_count($this->arrSubclass); $i++) {
                $query = knjd615Query::get_perfect_record_dat($this, $row, $this->arrSubclass[$i]);
                if ($row["SCORE" .$i] != "" && $row["SCORE" .$i] < $db->getOne($query)) {
                    $kettenCnt++;
                }
            }
        }

        return $kettenCnt;
    }
}
?>
