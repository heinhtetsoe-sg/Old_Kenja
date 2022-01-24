<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjc163Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;

    function knjc163Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SEMESTER"    =>  VARS::post("SEMESTER"),    //学期
                             "SDATE"       =>  VARS::post("SDATE"),       //対象日(開始)
                             "EDATE"       =>  VARS::post("EDATE"),       //対象日(終了)
                             "DATE"        =>  VARS::post("DATE"),        //学籍処理日
                             "OUTPUT"      =>  VARS::post("OUTPUT"),        //欠席日数・指定した割合
                             "KESSEKI_NISSU" =>  VARS::post("KESSEKI_NISSU"),        //欠席日数・指定した割合
                             "DBNAME"      =>  VARS::post("DBNAME"),      //DB変更に備えて
                             "TYUI_TYOUKA" =>  VARS::post("TYUI_TYOUKA")  //注意・超過ラジオボタン
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
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
                        "SICK"         => "欠席",
                        "LATE"         => "遅刻",
                        "EARLY"        => "早退",
                        "SUS_MOUR"     => "出停・忌引等",
                        "ABSENT"       => "公欠"
                        );

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
        $query = knjc163Query::getNameMst($year, "Z010", "00");
        $z010 = $db->getOne($query);
        //出欠関連共通
        $attendSemAllMap = AttendAccumulate::getAttendSemesMap($db, $z010, $year);
        //学校マスタ
        $periodInState = AttendAccumulate::getPeiodValue($db, $definecode, $year, $SSEMESTER, $semester);
        $hasuuMap = AttendAccumulate::getHasuuMap($attendSemAllMap, str_replace("/", "-", $this->field["SDATE"]), str_replace("/", "-", $this->field["EDATE"]));
        $knjSchoolMst = knjc163Query::getSchoolMstMap($db, $year);
        $bunshi = 0;
        $bunbo = 0;
        if ("2" == $this->field["OUTPUT"]) {
            if ("1" == $this->field["TYUI_TYOUKA"]) {
                $bunshi = $knjSchoolMst["KESSEKI_WARN_BUNSI"] ? $knjSchoolMst["KESSEKI_WARN_BUNSI"] : 0;
                $bunbo = $knjSchoolMst["KESSEKI_WARN_BUNBO"] ? $knjSchoolMst["KESSEKI_WARN_BUNBO"] : 1;
            } else if ("2" == $this->field["TYUI_TYOUKA"]) {
                $bunshi = $knjSchoolMst["KESSEKI_OUT_BUNSI"] ? $knjSchoolMst["KESSEKI_OUT_BUNSI"] : 0;
                $bunbo = $knjSchoolMst["KESSEKI_OUT_BUNBO"] ? $knjSchoolMst["KESSEKI_OUT_BUNBO"] : 1;
            }
        }

        $subHeader1 = "出欠統計処理";
        if ("1" == $this->field["OUTPUT"]) {
            $subHeader1 .= "（欠席日数　".$this->field["KESSEKI_NISSU"]."日以上）";
        } else if ("2" == $this->field["OUTPUT"]) {
            $tyuiTyoukaStr = "";
            if ("1" == $this->field["TYUI_TYOUKA"]) {
                $tyuiTyoukaStr = "注意";
            } else if ("2" == $this->field["TYUI_TYOUKA"]) {
                $tyuiTyoukaStr = "超過";
            }
            $subHeader1 .= "（指定した割合　".$tyuiTyoukaStr."（".$bunshi." / ".$bunbo."））";
        }
        $subHeader2 = "出欠集計範囲：" .$this->field["SDATE"] ." ～ " .$this->field["EDATE"];

        //ヘッダ配列を出力変数にセット
        $headerContents     = implode($header, $comma) .$comma;
        $subHeaderContents  = $comma.$comma.$subHeader1 .$comma.$lf;
        $subHeaderContents .= $subHeader2 .$comma.$lf;
        $subHeaderContents .= $lf;

        $dataContents = "";
        $data = false; //CSV出力フラグ
        foreach (explode(",", $this->selectdata) as $gradeHrclass) {
            $grade = substr($gradeHrclass, 0, 2);
            $hrClass = substr($gradeHrclass, 2, 3);
            $hrName = $db->getOne(knjc163Query::getHrName($grade, $hrClass));
            $dataContents .= $hrName .$comma;
            $dataContents .= $lf;
            $dataContents .= $headerContents;
            $dataContents .= $lf;

            $attendSemes = array();
            //メイン（成績・出欠）
            $query = knjc163Query::attendSemes(
                                $definecode,
                                $year,
                                $defineSchoolCode,
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
                if ($row["SEMESTER"] == "9") {
                    $attendSemes[$row["SCHREGNO"]] = $row;
                }
            }
            $query = knjc163Query::getRegd($grade, $hrClass);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $att = $attendSemes[$row["SCHREGNO"]] ? $attendSemes[$row["SCHREGNO"]] : array();
                $limit = 0;
                if ($this->field["OUTPUT"] == "1") {
                    $limit = $this->field["KESSEKI_NISSU"];
                } else if ($this->field["OUTPUT"] == "2") {
                    $mlesson = $att["MLESSON"];
                    $limit = $mlesson * $bunshi / $bunbo;
                }
                if ($att["SICK"] < $limit) {
                    continue;
                }
                $dataContents .= intval($row["ATTENDNO"]) .$comma;
                $dataContents .= $row["NAME_SHOW"] .$comma;
                $dataContents .= $att["SICK"] .$comma;
                $dataContents .= $att["LATE"] .$comma;
                $dataContents .= $att["EARLY"] .$comma;
                $dataContents .= ((int)$att["SUSPEND"] + (int)$att["MOURNING"]) .$comma;
                $dataContents .= $att["ABSENT"] .$comma;
                $dataContents .= $lf;
                $data = true; //CSV出力フラグ
            }
            $result->free();
            $dataContents .= $lf;
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        $contents  = "";
        $contents .= $subHeaderContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", "出欠統計処理");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
