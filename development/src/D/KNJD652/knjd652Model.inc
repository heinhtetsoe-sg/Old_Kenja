<?php

require_once('for_php7.php');

class knjd652Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
    var $selectSemeName;
    var $selectTestName;
    var $virus;
    var $koudome;
    var $rankTableName;
    var $useSlumpD048;

    /**
     * メンバ変数の初期化
     */
    function knjd652Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }
    function init() {
        $this->field = array("YEAR"                     =>  VARS::post("YEAR"),                     //年度
                             "SEMESTER"                 =>  VARS::post("SEMESTER"),                 //学期
                             "GRADE"                    =>  VARS::post("GRADE"),                    //学年
                             "SUB_TESTKINDCD"           =>  VARS::post("SUB_TESTKINDCD"),           //テスト
                             "OUTPUT4"                  =>  VARS::post("OUTPUT4"),                  //チェックボックス選択データ
                             "OUTPUT5"                  =>  VARS::post("OUTPUT5"),                  //チェックボックス選択データ
                             "OUTPUT_SOUGOU"            =>  VARS::post("OUTPUT_SOUGOU"),            //チェックボックス選択データ
                             "OUTPUT_TOKUBETSU"         =>  VARS::post("OUTPUT_TOKUBETSU"),         //チェックボックス選択データ
                             "SCORE_FLG"                =>  VARS::post("SCORE_FLG"),                //評価ラジオボタン
                             "OUTPUT_RANK"              =>  VARS::post("OUTPUT_RANK"),              //総合順位出力ラジオボタン
                             "OUTPUT_KIJUN"             =>  VARS::post("OUTPUT_KIJUN"),             //順位の基準点ラジオボタン
                             "SUBCLASS_MAX"             =>  VARS::post("SUBCLASS_MAX"),             //科目数選択
                             "FORM_SELECT"              =>  VARS::post("FORM_SELECT"),              //フォーム選択
                             "REMARK_SELECT"            =>  VARS::post("REMARK_SELECT"),            //備考欄
                             "TYUI_TYOUKA"              =>  VARS::post("TYUI_TYOUKA"),              //注意・超過ラジオボタン
                             "DATE_DIV"                 =>  VARS::post("DATE_DIV"),                 //出欠集計範囲（累計・学期）ラジオボタン
                             "DATE"                     =>  VARS::post("DATE"),                     //出欠集計終了日付
                             "SDATE"                    =>  VARS::post("SDATE"),                    //出欠集計開始日付
                             "KETTEN"                   =>  VARS::post("KETTEN"),                   //欠点
                             "SCHOOL_KIND"              =>  VARS::post("SCHOOL_KIND"),              //学校区分
                             "DBNAME"                   =>  VARS::post("DBNAME")                    //DB変更に備えて
                     );

        $this->field["TESTKINDCD"] = substr($this->field["SUB_TESTKINDCD"], 0, 4);
        $this->field["KIRIKAE_CD"] = substr($this->field["SUB_TESTKINDCD"], -1);

        $this->field["REMARK"] = (VARS::post("SUBCLASS_MAX") == "2") ? VARS::post("REMARK_SELECT") : "1" ;

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
        $this->selectSemeName = VARS::post("selectSemeName");
        $this->selectTestName = VARS::post("selectTestName");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->virus = ($this->Properties["useVirus"] == "true") ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;

        //序列テーブル
        if (($this->field["TESTKINDCD"] == "9900" && $this->field["SCORE_FLG"] == "1") ||
            ($this->field["TESTKINDCD"] != "9900" && $this->field["SCORE_FLG"] == "2")) {
            $this->rankTableName = "RECORD_RANK_DAT";
        } else {
            $this->rankTableName = "RECORD_RANK_DAT";
        }

        //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
        $rtnRow = knjd652Query::getNameMstD048();
        $this->useSlumpD048 = $rtnRow["NAMESPARE1"];
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //変数定義
        $comma = ",";   //カンマ
        $lf = "\n";     //改行
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

        $rankName = '';
        if ($this->field["OUTPUT_RANK"] == "2") $rankName = "学年順位";
        if ($this->field["OUTPUT_RANK"] == "3") $rankName = "コース順位";
        if ($this->field["OUTPUT_RANK"] == "4") $rankName = "クラス順位";

        $header2 = array("SCORE"        => "総合点",
                         "AVG"          => "平均点",
                         "GRADE_RANK"   => $rankName,
                         "KETTEN_CNT"   => "不振科目数"
                         );
        $subHeader = array("YEAR"         => "出欠集計範囲：" .$this->field["SDATE"] ." ～ " .$this->field["DATE"],
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
        $query  = knjd652Query::getSubClass($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->arrSubclass[] = $row["VALUE"];
            $headerContents     .= $row["SUBCLASSNAME"] .$comma ."欠課時数" .$comma;
            $subHeaderContents  .= $row["VALUE"] .$comma .$comma;
        }
        $result->free();
        $headerContents .= implode($header2, $comma) .$lf;
        $subHeaderContents .= $lf;
/////////////////////////////////////////////////////////////////////
        //処理月(各学期の期間の月のみをコンボにセット
        $allMonth = array();
        $result = $db->query(knjd652Query::getSemesterMonth(CTRL_YEAR));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            for ($i = 4; $i < 16; $i++)
            {
                $mon = ($i<13) ? $i : ($i-12);

                if ($mon < 4) {
                    $year = (int) CTRL_YEAR + 1;
                } else {
                    $year = (int) CTRL_YEAR;
                }

                $setDate = "";
                $sDate = $year ."-" .sprintf("%02d",$mon)."-01";
                $semeMon = $row["SEMESTER"] .sprintf("%02d",$mon);

                //年と月を合わせて比較する
                if ((int)($year.sprintf("%02d",$mon)) >= (int)strftime("%Y%m",strtotime($row["SDATE"]))
                && ((int)$year.sprintf("%02d",$mon)) <= (int)strftime("%Y%m",strtotime($row["EDATE"])))
                {
                    //月が学期の開始月または終了月かチェック
                    //開始月の場合は開始日以降その月末日まで集計
                    if ($mon == (int)strftime("%m",strtotime($row["SDATE"]))) {

                        $setDate = $row["SDATE"];

                    //終了月の場合はその月の１日から終了日まで集計
                    } else if ($mon == (int)strftime("%m",strtotime($row["EDATE"]))) {

                        $setDate = $sDate;

                    //それ以外はその月の１日から月末日まで集計
                    } else {

                        $setDate = $sDate;
                    }

                    //学期と月の開始日
                    $allMonth[$semeMon] = $setDate;
                }
            }
        }
/////////////////////////////////////////////////////////////////////
        //締め日など
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd652Query::getAttendDate($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeMon = $row["SEMESTER"] .$row["MONTH"];
            $simedate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            //出欠集計開始日付以上且つ、出欠集計終了日付以下
            if (str_replace("/", "-", $this->field["SDATE"]) <= $allMonth[$semeMon] &&
                str_replace("/", "-", $this->field["DATE"]) >= $simedate) {
                $attend_sdate = $simedate;
                $attend_seme = $row["SEMESTER"];
                $attend_month[] = $semeMon;
            }
        }
        $result->free();
        if ($attend_sdate == "") {
            //出欠集計開始日付
            $attend_sdate = str_replace("/", "-", $this->field["SDATE"]);
        } else {
            //次の日
            $query = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query);
        }
        //学校マスタ
        $knjSchoolMst = array();
        $query = knjd652Query::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $knjSchoolMst[$key] = $val;
            }
        }
        $result->free();
        //メイン（成績・出欠）
        $isKettenCntText = false;
        if (($this->field["TESTKINDCD"] == '9900' || $this->field["TESTKINDCD"] == '9901') && $this->useSlumpD048 != '1') {
            $isKettenCntText = true;
        }
        $dataContents = "";
        $data = false; //CSV出力フラグ
        $query = knjd652Query::getCsvQuery($this, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace($comma, ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["YEAR"]     = CTRL_YEAR;
            $row["SEMESTER"] = $this->selectSemeName;
            $row["TESTNAME"] = $this->selectTestName;
            $kettenCnt = 0;
            for ($i = 0; $i < get_count($this->arrSubclass); $i++) {
                if ($row["DI" .$i] == 0) {
                    $row["DI" .$i] = "";
                }
                if ($isKettenCntText) {
                    if (strlen($row["SCORE" .$i]) && $row["SCORE" .$i] <= $this->field["KETTEN"]) {
                        $kettenCnt++;
                    }
                }
            }
            if (!$isKettenCntText) {
                $query = knjd652Query::get_ketten_count($this, $semester, $row["SCHREGNO"]);
                $kettenCnt = $db->getOne($query);
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
}
?>
