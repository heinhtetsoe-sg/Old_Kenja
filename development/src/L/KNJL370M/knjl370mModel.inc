<?php

require_once('for_php7.php');

class knjl370mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $test_year;

    function knjl370mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }
    function init() {
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "ORDER_DIV"    =>  VARS::post("ORDER_DIV"),    //出力順
                             "DATE"         =>  VARS::post("DATE"),         //対象日
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    function getDownloadModel() {

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $data = false;

        $headArray = array("受験番号", "順位", "総点");
        $kamokuArray = array();
        $query = knjl370mQuery::getKamokuSql($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);

            $kamokuArray[] = array("TESTSUBCLASSCD" => $row["TESTSUBCLASSCD"],
                                   "TESTPAPERCD" => $row["TESTPAPERCD"],
                                   "TESTPAPERNAME" => $row["TESTPAPERNAME"]);
            $headArray[] = $row["TESTPAPERNAME"];
        }
        $result->free();
        $headArray[] = "区分";
        $headArray[] = "学校名";
        $headArray[] = "地区";
        $headArray[] = "学校所在地";
        $headArray[] = "Z-1";
        $headArray[] = "Z-2";
        $headArray[] = "Z-国語";
        $headArray[] = "Z-算数";
        $headArray[] = "Z-理科";
        $headArray[] = "Z-社会";
        $headArray[] = "Z1-合計";
        $headArray[] = "Z2-合計";

        $contents .= implode(",", $headArray)."\n";

        $query = knjl370mQuery::getStudentSql($this);
        $result = $db->query($query);
        $studentArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);

            $studentArray[] = array("EXAMNO"                    => $row["EXAMNO"],
                                    "TOTAL_RANK4"               => $row["TOTAL_RANK4"],
                                    "TOTAL4"                    => $row["TOTAL4"],
                                    "NATPUBPRI_NAME"            => $row["NATPUBPRI_NAME"],
                                    "FS_NAME"                   => $row["FS_NAME"],
                                    "AREA_DIV_NAME"             => $row["AREA_DIV_NAME"],
                                    "AREA_NAME"                 => $row["AREA_NAME"],
                                    "JUDGE_DEVIATION_RANK"      => $row["JUDGE_DEVIATION_RANK"],
                                    "LINK_JUDGE_DEVIATION_RANK" => $row["LINK_JUDGE_DEVIATION_RANK"],
                                    "JUDGE_DEVIATION"           => $row["JUDGE_DEVIATION"],
                                    "LINK_JUDGE_DEVIATION"      => $row["LINK_JUDGE_DEVIATION"]
                                    );
            $data = true;
        }
        $result->free();

        foreach ($studentArray as $key => $val) {
            $stdArray = array();
            $examno = ($val["EXAMNO"]) ? (int)$val["EXAMNO"] : "";
            $contents .= $examno.",";
            $contents .= $val["TOTAL_RANK4"].",";
            $contents .= $val["TOTAL4"].",";

            foreach ($kamokuArray as $keyKamoku => $valKamoku) {
                $studentArray = array();

                $query = knjl370mQuery::getScoreDataSql($this, $val["EXAMNO"], $valKamoku["TESTSUBCLASSCD"], $valKamoku["TESTPAPERCD"]);
                $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $scoreData = str_replace(",", ".", $scoreData);
                $contents .= $scoreData["SCORE"].",";
                if ($valKamoku["TESTPAPERCD"] == "9") {
                    $stdArray[] = $scoreData["STD_SCORE"];
                }
            }

            $contents .= $val["NATPUBPRI_NAME"].",";
            $contents .= $val["FS_NAME"].",";
            $contents .= $val["AREA_DIV_NAME"].",";
            $contents .= $val["AREA_NAME"].",";
            $contents .= $val["JUDGE_DEVIATION_RANK"].",";
            $contents .= $val["LINK_JUDGE_DEVIATION_RANK"].",";
            $contents .= implode(",", $stdArray).",";
            $contents .= $val["JUDGE_DEVIATION"].",";
            $contents .= $val["LINK_JUDGE_DEVIATION"]."\n";
            $data = true;
        }

        //平均、偏差値等
        $avgData = "";
        $devData = "";
        $query = knjl370mQuery::getTotalAvgDev($this, "9");
        $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $scoreData = str_replace(",", ".", $scoreData);
        $avgData .= "平均,,";
        $avgData .= $scoreData["AVG_SCORE"].",";
        $devData .= "標準偏差,,";
        $devData .= $scoreData["STD_SCORE"].",";
        foreach ($kamokuArray as $keyKamoku => $valKamoku) {
            if ($valKamoku["TESTPAPERCD"] == "9") {
                $query = knjl370mQuery::getTotalAvgDev($this, $valKamoku["TESTSUBCLASSCD"]);
                $scoreData = array();
                $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $avgData .= $scoreData["AVG_SCORE"].",";
                $devData .= $scoreData["STD_SCORE"].",";
            } else {
                $avgData .= ",";
                $devData .= ",";
            }
        }
        $contents .= $avgData."\n";
        $contents .= $devData."\n";

        //人数
        $query = knjl370mQuery::getTotalJuken($this, "1");
        $jukenCnt = $db->getOne($query);
        $contents .= "受験者数,".$jukenCnt."\n";
        $query = knjl370mQuery::getTotalJuken($this, "2");
        $kessekiCnt = $db->getOne($query);
        $contents .= "欠席者,".$kessekiCnt."\n";

        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", $this->test_year."年度入試成績一覧");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
