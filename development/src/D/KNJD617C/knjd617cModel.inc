<?php

require_once('for_php7.php');

class knjd617cModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
    var $selectSemeName;
    var $selectTestName;
    var $virus;
    var $koudome;
    var $semesterDetailS;
    var $semesterDetailE;
    var $rankTableName;

    /**
     * メンバ変数の初期化
     */
    function knjd617cModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init() {
        $this->field = array("YEAR"                     =>  VARS::post("YEAR"),                     //年度
                             "SEMESTER"                 =>  VARS::post("SEMESTER"),                 //学期
                             "GRADE"                    =>  VARS::post("GRADE"),                    //学年
                             "SUB_TESTKINDCD"           =>  VARS::post("SUB_TESTKINDCD"),           //テスト
                             "OUTPUT4"                  =>  VARS::post("OUTPUT4"),                  //チェックボックス選択データ
                             "OUTPUT5"                  =>  VARS::post("OUTPUT5"),                  //チェックボックス選択データ
                             "OUTPUT_COURSE"            =>  VARS::post("OUTPUT_COURSE"),            //チェックボックス選択データ
                             "OUTPUT_SOUGOU"            =>  VARS::post("OUTPUT_SOUGOU"),            //チェックボックス選択データ
                             "SEITO_BETU_JUGYOU_JISU"   =>  VARS::post("SEITO_BETU_JUGYOU_JISU"),   //チェックボックス生徒別授業時数
                             "KETTEN"                   =>  VARS::post("KETTEN"),                   //欠点テキストボックス
                             "SCORE_FLG"                =>  VARS::post("SCORE_FLG"),                //評価ラジオボタン
                             "OUTPUT_RANK"              =>  VARS::post("OUTPUT_RANK"),              //総合順位出力ラジオボタン
                             "OUTPUT_KIJUN"             =>  VARS::post("OUTPUT_KIJUN"),             //順位の基準点ラジオボタン
                             "SUBCLASS_MAX"             =>  VARS::post("SUBCLASS_MAX"),             //科目数選択
                             "OUTPUT_LESSON"            =>  VARS::post("OUTPUT_LESSON"),            //授業日数ラジオボタン
                             "FORM_SELECT"              =>  VARS::post("FORM_SELECT"),              //フォーム選択
                             "TYUI_TYOUKA"              =>  VARS::post("TYUI_TYOUKA"),              //注意・超過ラジオボタン
                             "DATE_DIV"                 =>  VARS::post("DATE_DIV"),                 //出欠集計範囲（累計・学期）ラジオボタン
                             "DATE"                     =>  VARS::post("DATE"),                     //出欠集計終了日付
                             "SDATE"                    =>  VARS::post("SDATE"),                    //出欠集計開始日付
                             "DBNAME"                   =>  VARS::post("DBNAME")                    //DB変更に備えて
                     );

        $this->field["TESTKINDCD"] = substr($this->field["SUB_TESTKINDCD"], 0, 4);
        $this->field["KIRIKAE_CD"] = substr($this->field["SUB_TESTKINDCD"], -1);

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
        $this->selectSemeName = VARS::post("selectSemeName");
        $this->selectTestName = VARS::post("selectTestName");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->virus = ($this->Properties["useVirus"] == "true") ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
        $this->getSemesterDetails();
        //序列テーブル
        if (($this->field["TESTKINDCD"] == "9900" && $this->field["SCORE_FLG"] == "1") || 
            ($this->field["TESTKINDCD"] != "9900" && $this->field["SCORE_FLG"] == "2")) {
            $this->rankTableName = "RECORD_RANK_V_DAT";
        } else {
            $this->rankTableName = "RECORD_RANK_DAT";
        }
//echo "序列テーブル：" .$this->rankTableName;
    }

    //各学校のテストコードと学期詳細コード
    function getSemesterDetails() {
        /**********************/
        /* 出欠集計開始日付用 */
        /**********************/
        /* アンダーバーの後ろは切替コード */
        //湖陵
        $this->semesterDetailS["220170"] = array("10101_2" => "1",
                                                 "19900_2" => "1",
                                                 "20101_2" => "3",
                                                 "29900_2" => "3",
                                                 "99900_1" => "1",
                                                 "99900_2" => "1"
                                            );
        //倉吉
        $this->semesterDetailS["220360"] = array("10101_1" => "1",
                                                 "19900_2" => "1",
                                                 "20101_1" => "3",
                                                 "29900_2" => "3",
                                                 "99900_2" => "1"
                                            );
        //米子
        $this->semesterDetailS["220250"] = array("10101_1" => "1",
                                                 "10201_1" => "1",
                                                 "10201_2" => "1",
                                                 "19900_2" => "1",
                                                 "20101_1" => "4",
                                                 "20201_1" => "4",
                                                 "20201_2" => "4",
                                                 "20202_1" => "4",
                                                 "99900_2" => "1"
                                            );
        //境
        $this->semesterDetailS["220410"] = array("10101_1" => "1",
                                                 "10201_1" => "1",
                                                 "10201_2" => "1",
                                                 "19900_2" => "1",
                                                 "20101_1" => "4",
                                                 "20201_1" => "4",
                                                 "20201_2" => "4",
                                                 "20202_1" => "4",
                                                 "99900_1" => "1",
                                                 "99900_2" => "1"
                                            );
        //中央育英
        $this->semesterDetailS["224030"] = array("10101_1" => "1",
                                                 "10201_1" => "1",
                                                 "10202_1" => "1",
                                                 "19900_2" => "1",
                                                 "20101_1" => "4",
                                                 "20201_1" => "4",
                                                 "20202_1" => "4",
                                                 "29900_2" => "4",
                                                 "99900_2" => "1"
                                            );

        /**********************/
        /* 出欠集計終了日付用 */
        /**********************/
        //湖陵
        $this->semesterDetailE["220170"] = array("10101" => "1",
                                                 "19900" => "2",
                                                 "20101" => "3",
                                                 "29900" => "4",
                                                 "99900" => "5"
                                            );
        //倉吉
        $this->semesterDetailE["220360"] = array("10101" => "1",
                                                 "19900" => "2",
                                                 "20101" => "3",
                                                 "29900" => "4",
                                                 "99900" => "5"
                                            );
        //米子
        $this->semesterDetailE["220250"] = array("10101" => "1",
                                                 "10201" => "2",
                                                 "19900" => "3",
                                                 "20101" => "4",
                                                 "20201" => "5",
                                                 "20202" => "6",
                                                 "99900" => "6"
                                            );
        //境
        $this->semesterDetailE["220410"] = array("10101" => "1",
                                                 "10201" => "2",
                                                 "19900" => "3",
                                                 "20101" => "4",
                                                 "20201" => "5",
                                                 "20202" => "6",
                                                 "99900" => "6"
                                            );
        //中央育英
        $this->semesterDetailE["224030"] = array("10101" => "1",
                                                 "10201" => "2",
                                                 "10202" => "3",
                                                 "19900" => "3",
                                                 "20101" => "4",
                                                 "20201" => "5",
                                                 "20202" => "6",
                                                 "29900" => "6",
                                                 "99900" => "6"
                                            );
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
        $query  = knjd617cQuery::getSubClass($this);
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
        $result = $db->query(knjd617cQuery::getSemesterMonth(CTRL_YEAR));
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
        $query = knjd617cQuery::getAttendDate($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeMon = $row["SEMESTER"] .$row["MONTH"];
            $simedate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
//            if (str_replace("/", "-", $this->field["DATE"]) < $simedate) break;
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
        $query = knjd617cQuery::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $knjSchoolMst[$key] = $val;
            }
        }
        $result->free();
        //メイン（成績・出欠）
        $kettenLine = (strlen($this->field["KETTEN"])) ? $this->field["KETTEN"] : 0;
        $dataContents = "";
        $data = false; //CSV出力フラグ
        $query = knjd617cQuery::getCsvQuery($this, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace($comma, ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["YEAR"]     = CTRL_YEAR;
            $row["SEMESTER"] = $this->selectSemeName;
            $row["TESTNAME"] = $this->selectTestName;
            $kettenCnt = 0;
            for ($i = 0; $i < get_count($this->arrSubclass); $i++) {
                if ($row["SCORE" .$i] != "" && $row["SCORE" .$i] <= $kettenLine) {
                    $kettenCnt++;
                }
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
}
?>
