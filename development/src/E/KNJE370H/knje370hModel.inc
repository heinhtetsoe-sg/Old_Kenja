<?php

require_once('for_php7.php');

class knje370hModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knje370hModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("OUTPUT"  =>  VARS::post("OUTPUT"));

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    // //CSV出力処理
    // function getDownloadModel() {
    //     //DB接続
    //     $db = Query::dbCheckOut();
    // 
    //     $contents = "";
    // 
    //     //ヘッダ（1行目：タイトル）
    //     $title = "合格大学　学部調";
    //     $contents .= $title. str_repeat(",", 10). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";
    // 
    //     //初期化
    //     $header = array();
    // 
    //     //ヘッダ（2行目、3行目）
    //     $header["COURSECODENAME"] = array("コース",   "");
    // 
    //     //学年名称取得
    //     $max_grade = "";
    //     $grade_array = array();
    //     $query = knje370hQuery::getGradeList();
    //     $result = $db->query($query);
    //     while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    //         $header["GRADE_".$row["GRADE"]] = array($row["GRADE_NAME1"], "平均");
    //         $grade_array[] = $row["GRADE"];
    //         if (!$max_grade) $max_grade = $row["GRADE_NAME1"];
    //     }
    //     $result->free();
    // 
    //     //科目一覧取得
    //     $sub_cnt = 0;
    //     $subclass_array = array();
    //     $query = knje370hQuery::getSubclassList($this);
    //     $result = $db->query($query);
    //     while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    //         $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
    //         $header["SUBCLASSCD_".$subclass] = array((($sub_cnt == 0) ? $max_grade."成績" : ""), $row["SUBCLASSABBV"]);
    //         $subclass_array[] = $subclass;
    //         $sub_cnt++;
    //     }
    //     $result->free();
    //     //科目データなし
    //     if (get_count($subclass_array) == 0) {
    //         $header["SUBCLASSCD_0"] = array($max_grade."成績",  "");
    //     }
    // 
    //     //センタータイトル
    //     $centerTitle = "センター試験";
    // 
    //     //ヘッダ（2行目、3行目）
    //     $header["BUNRIDIV"]           = array("$centerTitle",     "文理区分");
    //     $header["BUNRINAME1"]         = array("",                 "文理名称");
    //     $header["CENTER_TOTAL_PER1"]  = array("",                 "文％[900]");
    //     $header["CENTER_TOTAL_PER2"]  = array("",                 "理％[900]");
    //     $header["CENTER_TOTAL_PER3"]  = array("",                 "　％[800]");
    //     $header["CENTER_TOTAL_PER4"]  = array("",                 "文％[500]");
    //     $header["CENTER_TOTAL_PER5"]  = array("",                 "理％[500]");
    //     $header["SCHOOL_CD1"]         = array("合格大学",         "国公立大学コード");
    //     $header["SCHOOL_GROUP1"]      = array("",                 "国公立大学");
    //     $header["SCHOOL_CD2"]         = array("",                 "私立大学コード");
    //     $header["SCHOOL_GROUP2"]      = array("",                 "私立大学");
    //     $header["HR_NAME"]            = array("年組",             "");
    //     $header["ATTENDNO"]           = array("出席番号",         "");
    //     $header["SCHREGNO"]           = array("学籍番号",         "");
    //     $header["NAME"]               = array("氏名",             "");
    // 
    //     //ヘッダ（2行目、3行目）セット
    //     $header1 = $header2 = $header_array = array();
    //     foreach ($header as $key => $val) {
    //         $header1[] = $val[0];
    //         $header2[] = $val[1];
    // 
    //         $header_array[] = $key;
    //     }
    //     $contents .= implode($header1, ",") ."\n";
    //     $contents .= implode($header2, ",") ."\n";
    // 
    //     //成績表示科目一覧取得（学年・コースごと）
    //     $course_subclass = array();
    //     $query = knje370hQuery::getSubclassList($this, 'list');
    //     $result = $db->query($query);
    //     while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    //         $course = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
    //         $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
    //         $course_subclass[$course][] = $subclass;
    //     }
    //     $result->free();
    // 
    //     //進路データ取得
    //     $aftGrdCrs = array();
    //     $query = knje370hQuery::getAftGradCourseDat();
    //     $result = $db->query($query);
    //     while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    //         $aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]]["label"] .= $row["SCHOOL_GROUP1"];
    // 
    //         if (!strlen($aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]]["value"])) {
    //             $aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]]["value"] = $row["STAT_CD"];
    //         } else {
    //             $aftGrdCrs[$row["SCHREGNO"]][$row["GROUP"]]["value"] = "NULL";
    //         }
    //     }
    //     $result->free();
    // 
    //     //SQL文発行
    //     $dataTemp = array();
    //     $query = knje370hQuery::getCsvQuery($this, $subclass_array, $grade_array);
    //     $result = $db->query($query);
    //     while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    //         //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
    //         $row = str_replace(",",".",$row);
    // 
    //         if ($dataTemp[$row["SCHREGNO"]] == "") {
    //             $dataTemp[$row["SCHREGNO"]]["SCHREGNO"]          = $row["SCHREGNO"];
    //             $dataTemp[$row["SCHREGNO"]]["HR_NAME"]           = $row["HR_NAME"];
    //             $dataTemp[$row["SCHREGNO"]]["ATTENDNO"]          = $row["ATTENDNO"];
    //             $dataTemp[$row["SCHREGNO"]]["NAME"]              = $row["NAME"];
    //             $dataTemp[$row["SCHREGNO"]]["COURSECODENAME"]    = $row["COURSECODENAME"];
    //             $dataTemp[$row["SCHREGNO"]]["BUNRIDIV"]          = $row["BUNRIDIV"];
    //             $dataTemp[$row["SCHREGNO"]]["BUNRINAME1"]        = $row["BUNRINAME1"];
    //             $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER1"] = ($row["BUNRIDIV"] == "1") ? culPer(900, $row["CENTER_TOTAL1"]) : "";
    //             $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER2"] = ($row["BUNRIDIV"] == "2") ? culPer(900, $row["CENTER_TOTAL2"]) : "";
    //             $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER3"] = culPer(800, $row["CENTER_TOTAL3"]);
    //             $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER4"] = ($row["BUNRIDIV"] == "1") ? culPer(500, $row["CENTER_TOTAL4"]) : "";
    //             $dataTemp[$row["SCHREGNO"]]["CENTER_TOTAL_PER5"] = ($row["BUNRIDIV"] == "2") ? culPer(500, $row["CENTER_TOTAL5"]) : "";
    //             //合格大学（国公立）
    //             $dataTemp[$row["SCHREGNO"]]["SCHOOL_CD1"]       = ($aftGrdCrs[$row["SCHREGNO"]]["1"]["value"] == "NULL") ? "" : $aftGrdCrs[$row["SCHREGNO"]]["1"]["value"];
    //             $dataTemp[$row["SCHREGNO"]]["SCHOOL_GROUP1"]    = $aftGrdCrs[$row["SCHREGNO"]]["1"]["label"];
    //             //合格大学（私立）
    //             $dataTemp[$row["SCHREGNO"]]["SCHOOL_CD2"]       = ($aftGrdCrs[$row["SCHREGNO"]]["2"]["value"] == "NULL") ? "" : $aftGrdCrs[$row["SCHREGNO"]]["2"]["value"];
    //             $dataTemp[$row["SCHREGNO"]]["SCHOOL_GROUP2"]    = $aftGrdCrs[$row["SCHREGNO"]]["2"]["label"];
    //             //学年ごとの学年末成績の平均点
    //             foreach ($grade_array as $gkey) {
    //                 $dataTemp[$row["SCHREGNO"]]["GRADE_".$gkey] = $row["GRADE_".$gkey];
    //             }
    //         }
    // 
    //         $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
    //         $course = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
    //         if (is_array($course_subclass[$course]) && in_array($subclass, $course_subclass[$course])) {
    //             $dataTemp[$row["SCHREGNO"]]["SUBCLASSCD_".$subclass] = $row["SCORE"];
    //         } else {
    //             $dataTemp[$row["SCHREGNO"]]["SUBCLASSCD_".$subclass] = '';
    //         }
    //     }
    //     $result->free();
    // 
    //     $data = false;
    //     foreach ($dataTemp as $key => $val) {
    //         //ヘッダー順で出力
    //         $sep = "";
    //         foreach ($header_array as $hkey) {
    //             //データセット
    //             $contents .= $sep.$dataTemp[$key][$hkey];
    //             $sep = ",";
    //         }
    // 
    //         $contents .= "\n";
    //         $data = true;
    //     }
    // 
    //     //DB切断
    //     Query::dbCheckIn($db);
    // 
    //     //CSV出力
    //     if ($data) {
    //         $title = "合格大学　学部調";
    //         $filename = sprintf("%s.csv", $title);
    //         common::downloadContents($contents, $filename, "SJIS-win");
    //         return true;
    //     } else {
    //         $this->setWarning("MSG303");
    //         return false;
    //     }
    // }
}
// //総点％算出
// function culPer($perfect, $total) {
//     if ($total != "") {
//         $kekka = round(((int)$total/(int)$perfect)*100, 1);
//     } else {
//         $kekka = "";
//     }
//     return $kekka;
// }
?>
