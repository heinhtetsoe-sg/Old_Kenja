<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjc164Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;

    function knjc164Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SEMESTER"    =>  VARS::post("SEMESTER"),    //学期
                             "SDATE"       =>  VARS::post("SDATE"),       //対象日(開始)
                             "EDATE"       =>  VARS::post("EDATE"),       //対象日(終了)
                             "DATE"        =>  VARS::post("DATE"),        //学籍処理日
                             "OUTPUT"      =>  VARS::post("OUTPUT"),        //欠席日数・指定した割合
                             "KEKKA_JISU" =>  VARS::post("KEKKA_JISU"),        //欠席日数・指定した割合
                             "DBNAME"      =>  VARS::post("DBNAME"),      //DB変更に備えて
                             "TYUI_TYOUKA" =>  VARS::post("TYUI_TYOUKA"), //注意・超過ラジオボタン
                             "SCHKIND"     =>  VARS::post("SCHKIND")      //校種
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}

    function getDownloadModel() {
        //変数定義
        $comma = ",";   //カンマ
        $lf = "\n";      //改行

        //DB接続
        $db = Query::dbCheckOut();
        //ヘッダ配列
        $header = array("ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "氏名",
                        "SUBCLASSNAME" => "科目名",
                        "LESSON"       => "総実施時数",
                        "SICK2"        => "欠課時数"
                        );

        //ヘッダ配列を出力変数にセット
        $headerContents     = implode($header, $comma) .$comma;

        $definecode = array();
        $definecode["schoolmark"]        = $this->Properties["schoolmark"];
        $definecode["useschchrcountflg"] = $this->Properties["useschchrcountflg"];
        $definecode["usefromtoperiod"]   = $this->Properties["usefromtoperiod"];
        $definecode["useabsencehigh"]    = $this->Properties["useabsencehigh"];

        //固定
        $SSEMESTER = "1";
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;
        //近大判定用
        $query = knjc164Query::getNameMst($year, "Z010", "00");
        $z010 = $db->getOne($query);
        //出欠関連共通
        $attendSemAllMap = AttendAccumulate::getAttendSemesMap($db, $z010, $year);
        //学校マスタ
        $periodInState = AttendAccumulate::getPeiodValue($db, $definecode, $year, $SSEMESTER, $semester);
        $hasuuMap = AttendAccumulate::getHasuuMap($attendSemAllMap, str_replace("/", "-", $this->field["SDATE"]), str_replace("/", "-", $this->field["EDATE"]));
        $knjSchoolMst = knjc164Query::getSchoolMstMap($this, $db, $year);
        $bunshi = 0;
        $bunbo = 1;
        if ("1" != $this->field["OUTPUT"]) {
            $bunshi = $knjSchoolMst["RISYU_BUNSI"] ? $knjSchoolMst["RISYU_BUNSI"] : 0;
            $bunbo = $knjSchoolMst["RISYU_BUNBO"] ? $knjSchoolMst["RISYU_BUNBO"] : 1;
        }

        $subHeader1 = "欠課時数統計資料　";
        if ("1" == $this->field["OUTPUT"]) {
            $subHeader1 .= "（欠課時数が　".$this->field["KEKKA_JISU"]."時数 超過している科目）";
        } else if ("2" == $this->field["OUTPUT"]) {
            $subHeader1 .= "（授業時数から　".$bunshi." / ".$bunbo." 超過している科目）";
        } else if ("3" == $this->field["OUTPUT"]) {
            $subHeader1 .= "（予定時数から　".$bunshi." / ".$bunbo." 超過している科目）";
        }
        $subHeader2 = "出欠集計範囲：" .$this->field["SDATE"] ." ～ " .$this->field["EDATE"];

        //ヘッダ配列を出力変数にセット
        $headerContents     = implode($header, $comma) .$comma;
        $subHeaderContents  = $comma.$comma.$subHeader1 .$comma.$lf;
        $subHeaderContents .= $subHeader2 .$comma.$lf;
        $subHeaderContents .= $lf;

        $subclassname = array();
        $result = $db->query("SELECT * FROM SUBCLASS_MST");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ("1" == $this->Properties["useCurriculumcd"]) {
                $subclassname[$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
            } else {
                $subclassname[$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
            }
        }
        $result->free();

        $creditMst = array();
        if ($this->field["OUTPUT"] == "3") {
            $result = $db->query("SELECT * FROM CREDIT_MST WHERE YEAR = '".CTRL_YEAR."' ");
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ("1" == $this->Properties["useCurriculumcd"]) {
                    $creditMst[$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["GRADE"]."-".$row["COURSECODE"]."-".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]] = $row;
                } else {
                    $creditMst[$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["GRADE"]."-".$row["COURSECODE"]."-".$row["SUBCLASSCD"]] = $row;
                }
            }
            $result->free();
        }

        $dataContents = "";
        $data = false; //CSV出力フラグ
        $staffInfo = $db->getRow(knjc164Query::getStaffInfo(), DB_FETCHMODE_ASSOC); //英語・日本語切替処理用の職員情報
        foreach (explode(",", $this->selectdata) as $gradeHrclass) {
            $grade = substr($gradeHrclass, 0, 2);
            $hrClass = substr($gradeHrclass, 2, 3);
            $hrName = $db->getOne(knjc164Query::getHrName($grade, $hrClass));
            $dataContents .= $hrName .$comma;
            $dataContents .= $lf;
            $dataContents .= $headerContents;
            $dataContents .= $lf;

            $attendSubclass = array();
            //メイン（成績・出欠）
            $query = knjc164Query::attendSubclass(
                                $definecode,
                                $year,
                                $knjSchoolMst,
                                $periodInState,
                                str_replace("/", "-", $this->field["SDATE"]),
                                str_replace("/", "-", $this->field["EDATE"]),
                                $hasuuMap,
                                $grade,
                                $hrClass,
                                $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["SEMESTER"] == "9" && $row["CLASSCD"] <= "90") {
                    if (!$attendSubclass[$row["SCHREGNO"]]) {
                        $attendSubclass[$row["SCHREGNO"]] = array();
                    }
                    $attendSubclass[$row["SCHREGNO"]][$row["SUBCLASSCD"]] = $row;
                }
            }
            $query = knjc164Query::getRegd($grade, $hrClass);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schSubatt = $attendSubclass[$row["SCHREGNO"]] ? $attendSubclass[$row["SCHREGNO"]] : array();
                foreach ($schSubatt as $subclasscd => $subatt) {
                    if ($subclassname[$subclasscd] == '') {
                        continue;
                    }
                    $limit = 0;
                    if ($this->field["OUTPUT"] == "1") {
                        $limit = $this->field["KEKKA_JISU"];
                    } else if ($this->field["OUTPUT"] == "2") {
                        $limit = 1.0 * $subatt["MLESSON"] * $bunshi / $bunbo;
                    } else if ($this->field["OUTPUT"] == "3") {
                        $credits = $creditMst[$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["GRADE"]."-".$row["COURSECODE"]."-".$subclasscd]["CREDITS"];
                        if ($credits) {
                            $limit = 1.0 * ($credits * $knjSchoolMst["JITU_SYUSU"]) * $bunshi / $bunbo;
                        } else {
                            $limit = 9999;
                        }
                    }
                    if ($subatt["SICK2"] <= $limit) {
                        continue;
                    }
                    $dataContents .= intval($row["ATTENDNO"]) .$comma;
                    $nameShow = $staffInfo["FIELD5"] == "1" ? $row["NAME_ENG"] : $row["NAME_SHOW"]; //英語・日本語切替
                    $dataContents .= $nameShow .$comma;
                    $dataContents .= $subclassname[$subclasscd] .$comma;
                    $dataContents .= $subatt["MLESSON"] .$comma;
                    $dataContents .= $subatt["SICK2"] .$comma;
                    $dataContents .= $lf;
                    $data = true; //CSV出力フラグ
                }
            }
            $result->free();
            $dataContents .= $lf;
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        $contents  = "";
        $contents .= $subHeaderContents.$lf;
        $contents .= $dataContents;

        if ($data) {
            $filename = sprintf("%s.csv", "欠課時数統計資料");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
