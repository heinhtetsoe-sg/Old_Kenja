<?php

require_once('for_php7.php');

class knje365Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knje365Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("BRDIV"         =>  VARS::post("BRDIV"),
                             "SORT"          =>  VARS::post("SORT")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //学校判定
        $db = Query::dbCheckOut();
        $this->isSundaiKouhu = $db->getOne(knje365Query::getSchoolHantei('sundaikoufu'));
        Query::dbCheckIn($db);
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ（1行目：タイトル）
        $title = "生徒別大学進学資料";
        $contents .= $title. str_repeat(",", 10). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";

        //初期化
        $header = array();

        //ヘッダ（2行目、3行目）
        $header["HR_NAME"]      = array("年組",     "");
        $header["ATTENDNO"]     = array("出席番号", "");
        $header["SCHREGNO"]     = array("学籍番号", "");
        $header["NAME"]         = array("氏名",     "");
        if ($this->isSundaiKouhu > 0) {
            $header["SEX_SHOW"]     = array("性別",     "");
            $header["INOUTCD_SHOW"] = array("内外区分", "");
            $header["GRD_DIV_SHOW"] = array("浪人情報", "");
        }

        //学年名称取得
        $max_grade = "";
        $grade_array = array();
        $query = knje365Query::getGradeList();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->isSundaiKouhu == 0) $header["GRADE_".$row["GRADE"]."_AVE"]  = array($row["GRADE_NAME1"], "平均");
            if ($this->isSundaiKouhu == 0) $header["GRADE_".$row["GRADE"]."_RANK"] = array("", "順位");
            $grade_array[] = $row["GRADE"];
            if (!$max_grade) $max_grade = $row["GRADE_NAME1"];
        }
        $result->free();

        //科目一覧取得
        $sub_cnt = 0;
        $subclass_array = array();
        $query = knje365Query::getSubclassList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            if ($this->isSundaiKouhu == 0) $header["SUBCLASSCD_".$subclass] = array((($sub_cnt == 0) ? $max_grade."学年成績" : ""), $row["SUBCLASSABBV"]);
            $subclass_array[] = $subclass;
            $sub_cnt++;
        }
        $result->free();
        //科目データなし
        if (get_count($subclass_array) == 0) {
            if ($this->isSundaiKouhu == 0) $header["SUBCLASSCD_0"] = array($max_grade."学年成績",  "");
        }

        //センター試験科目一覧取得
        $centerTitle = "センター試験";
        $header["BUNRIDIV"]        = array("$centerTitle",     "文理区分");
        $header["BUNRINAME1"]      = array("",                 "文理名称");
        $mock_cnt = 0;
        $mocksub_array = array();
        $query = knje365Query::getMockCenterSubclassList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header["CENTER_SUBCLASS_CD_".$row["CENTER_SUBCLASS_CD"]] = array("", $row["SUBCLASS_ABBV"]);
            $mocksub_array[] = $row["CENTER_SUBCLASS_CD"];
            $mock_cnt++;
        }
        $result->free();
        //センター試験科目データなし
        if (get_count($mocksub_array) == 0) {
            $header["MOCK_CENTER_SUBCLASS_CD_0"] = array($centerTitle,  "");
        }
        $header["CENTER_TOTAL1"]      = array("",     "文総点（900）");
        $header["CENTER_TOTAL_PER1"]  = array("",     "％");
        $header["CENTER_TOTAL2"]      = array("",     "理総点（900）");
        $header["CENTER_TOTAL_PER2"]  = array("",     "％");
        $header["CENTER_TOTAL3"]      = array("",     "総点（800）");
        $header["CENTER_TOTAL_PER3"]  = array("",     "％");
        $header["CENTER_TOTAL4"]      = array("",     "文総点（500）");
        $header["CENTER_TOTAL_PER4"]  = array("",     "％");
        $header["CENTER_TOTAL5"]      = array("",     "理総点（500）");
        $header["CENTER_TOTAL_PER5"]  = array("",     "％");

        //ヘッダ（2行目、3行目）
        $header["PASS_SCHOOL_CD1"]      = array("国公立",   "大学コード");
        $header["PASS_SCHOOL_GROUP1"]   = array("",         "合格");
        $header["PASS_SCHOOL_CD2"]      = array("私立",     "大学コード");
        $header["PASS_SCHOOL_GROUP2"]   = array("",         "合格");
        $header["FAIL_SCHOOL_CD1"]      = array("国公立",   "大学コード");
        $header["FAIL_SCHOOL_GROUP1"]   = array("",         "不合格");
        $header["FAIL_SCHOOL_CD2"]      = array("私立",     "大学コード");
        $header["FAIL_SCHOOL_GROUP2"]   = array("",         "不合格");

        //ヘッダ（2行目、3行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            $header1[] = $val[0];
            $header2[] = $val[1];

            $header_array[] = $key;
        }
        $contents .= implode($header1, ",") ."\n";
        $contents .= implode($header2, ",") ."\n";

        //成績表示科目一覧取得（学年・コースごと）
        $course_subclass = array();
        $query = knje365Query::getSubclassList($this, 'list');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $course = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
            $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $course_subclass[$course][] = $subclass;
        }
        $result->free();

        //進路データ取得
        $aftGrdCrs = array();
        $query = knje365Query::getAftGradCourseDat();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]."_".$row["DECISION"]]["label"] .= $row["SCHOOL_GROUP1"];

            if (!strlen($aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]."_".$row["DECISION"]]["value"])) {
                $aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]."_".$row["DECISION"]]["value"] = $row["STAT_CD"];
            } else {
                $aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]."_".$row["DECISION"]]["value"] = "NULL";
            }
        }
        $result->free();

        //SQL文発行
        $data = false;
        $dataTemp = array();
        $query = knje365Query::getCsvQuery($this, $subclass_array, $grade_array, $mocksub_array);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            if ($dataTemp[$row["SCHREGNO"]] == "") {
                $dataTemp[$row["SCHREGNO"]]["SCHREGNO"]     = $row["SCHREGNO"];
                $dataTemp[$row["SCHREGNO"]]["HR_NAME"]      = $row["HR_NAME"];
                $dataTemp[$row["SCHREGNO"]]["ATTENDNO"]     = $row["ATTENDNO"];
                $dataTemp[$row["SCHREGNO"]]["NAME"]         = $row["NAME"];
                $dataTemp[$row["SCHREGNO"]]["SEX_SHOW"]     = $row["SEX_SHOW"];
                $dataTemp[$row["SCHREGNO"]]["INOUTCD_SHOW"] = $row["INOUTCD_SHOW"];
                $dataTemp[$row["SCHREGNO"]]["GRD_DIV_SHOW"] = $row["GRD_DIV_SHOW"];

                //学年ごとの学年末成績の平均点・順位
                foreach ($grade_array as $gkey) {
                    $dataTemp[$row["SCHREGNO"]]["GRADE_".$gkey."_AVE"] = $row["GRADE_".$gkey."_AVE"];
                    $dataTemp[$row["SCHREGNO"]]["GRADE_".$gkey."_RANK"] = $row["GRADE_".$gkey."_RANK"];
                }

                //文理区分
                $dataTemp[$row["SCHREGNO"]]["BUNRIDIV"]    = $row["BUNRIDIV"];
                $dataTemp[$row["SCHREGNO"]]["BUNRINAME1"]  = $row["BUNRINAME1"];

                //文総点（900）
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL1"]     = ($row["BUNRIDIV"] == "1") ? $row["CENTER_TOTAL1"] : "";
                //文総点％
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER1"] = ($row["BUNRIDIV"] == "1") ? culPer(900, $row["CENTER_TOTAL1"]) : "";
                //理総点（900）
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL2"]     = ($row["BUNRIDIV"] == "2") ? $row["CENTER_TOTAL2"] : "";
                //理総点％
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER2"] = ($row["BUNRIDIV"] == "2") ? culPer(900, $row["CENTER_TOTAL2"]) : "";
                //総点（800）
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL3"]     = $row["CENTER_TOTAL3"];
                //総点％
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER3"] = culPer(800, $row["CENTER_TOTAL3"]);
                //文総点（500）
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL4"]     = ($row["BUNRIDIV"] == "1") ? $row["CENTER_TOTAL4"] : "";
                //文総点％
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER4"] = ($row["BUNRIDIV"] == "1") ? culPer(500, $row["CENTER_TOTAL4"]) : "";
                //理総点（500）
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL5"]     = ($row["BUNRIDIV"] == "2") ? $row["CENTER_TOTAL5"] : "";
                //理総点％
                $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER5"] = ($row["BUNRIDIV"] == "2") ? culPer(500, $row["CENTER_TOTAL5"]) : "";

                //国公立（合格）
                $dataTemp[$row["SCHREGNO"]]["PASS_SCHOOL_CD1"]      = ($aftGrdCrs[$row["SCHREGNO"]]["1_1"]["value"] == "NULL") ? "" : $aftGrdCrs[$row["SCHREGNO"]]["1_1"]["value"];
                $dataTemp[$row["SCHREGNO"]]["PASS_SCHOOL_GROUP1"]   = $aftGrdCrs[$row["SCHREGNO"]]["1_1"]["label"];
                //私立（合格）
                $dataTemp[$row["SCHREGNO"]]["PASS_SCHOOL_CD2"]      = ($aftGrdCrs[$row["SCHREGNO"]]["2_1"]["value"] == "NULL") ? "" : $aftGrdCrs[$row["SCHREGNO"]]["2_1"]["value"];
                $dataTemp[$row["SCHREGNO"]]["PASS_SCHOOL_GROUP2"]   = $aftGrdCrs[$row["SCHREGNO"]]["2_1"]["label"];
                //国公立（不合格）
                $dataTemp[$row["SCHREGNO"]]["FAIL_SCHOOL_CD1"]      = ($aftGrdCrs[$row["SCHREGNO"]]["1_2"]["value"] == "NULL") ? "" : $aftGrdCrs[$row["SCHREGNO"]]["1_2"]["value"];
                $dataTemp[$row["SCHREGNO"]]["FAIL_SCHOOL_GROUP1"]   = $aftGrdCrs[$row["SCHREGNO"]]["1_2"]["label"];
                //私立（不合格）
                $dataTemp[$row["SCHREGNO"]]["FAIL_SCHOOL_CD2"]      = ($aftGrdCrs[$row["SCHREGNO"]]["2_2"]["value"] == "NULL") ? "" : $aftGrdCrs[$row["SCHREGNO"]]["2_2"]["value"];
                $dataTemp[$row["SCHREGNO"]]["FAIL_SCHOOL_GROUP2"]   = $aftGrdCrs[$row["SCHREGNO"]]["2_2"]["label"];
            }

            if ($row["CLASSCD"] == "99") {
                $dataTemp[$row["SCHREGNO"]]["CENTER_SUBCLASS_CD_".$row["SUBCLASSCD"]] = $row["SCORE"];
            } else {
                $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                $course = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
                if (is_array($course_subclass[$course]) && in_array($subclass, $course_subclass[$course])) {
                    $dataTemp[$row["SCHREGNO"]]["SUBCLASSCD_".$subclass] = $row["SCORE"];
                } else {
                    $dataTemp[$row["SCHREGNO"]]["SUBCLASSCD_".$subclass] = '';
                }
            }
        }
        $result->free();

        $data = false;
        foreach ($dataTemp as $key => $val) {
            //ヘッダー順で出力
            $sep = "";
            foreach ($header_array as $hkey) {
                //データセット
                $contents .= $sep.$dataTemp[$key][$hkey];
                $sep = ",";
            }

            $contents .= "\n";
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = "生徒別大学進学資料";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
//総点％算出
function culPer($perfect, $total) {
    if ($total != "") {
        $kekka = round(((int)$total/(int)$perfect)*100, 1);
    } else {
        $kekka = "";
    }
    return $kekka;
}
?>
