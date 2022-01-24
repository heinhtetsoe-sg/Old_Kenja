<?php

require_once('for_php7.php');

class knje441aModel extends Model {
    var $field = array();
    var $cmd;

    function knje441aModel() {}
    function init() {

        $this->field["YEAR"] = VARS::post("YEAR");
        $this->field["DATA_DIV"] = VARS::post("DATA_DIV");

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $this->header_data = false;

        $this->edboard_schoolcd = array();
        if ($this->selectdata) {
            $selectdata = explode(',', $this->selectdata);
            for ($i = 0; $i < get_count($selectdata); $i++) {
                list ($this->flg[], $this->edboard_schoolcd[]) = explode('_', $selectdata[$i]);
            }
        }

        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        if ($this->field["DATA_DIV"] == "1") {
            $contents .= $this->getCollegeCsv($db, "ALL");
            $contents .= $this->getCollege2Csv($db, "ALL");
            $contents .= $this->getSenmonCsv($db, "ALL");
            $contents .= $this->getSenmon2Csv($db, "ALL");
        } else {
            foreach ($this->edboard_schoolcd as $key => $val) {
                $query = knje441aQuery::getEdboardSchoolName($val);
                $this->printSchoolName = $db->getOne($query);
                $contents .= $this->getCollegeCsv($db, $val);
                $contents .= $this->getCollege2Csv($db, $val);
                $contents .= $this->getSenmonCsv($db, $val);
                $contents .= $this->getSenmon2Csv($db, $val);
                $contents .= "\n\n\n\n";
            }
        }
        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($this->header_data) {
            $couse = CTRL_YEAR."大学短大等進学状況 ";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ＣＳＶ出力
    function getCollegeCsv($db, $schoolCd) {
        $contents  = "";    //クリア
        if ($schoolCd != "ALL") {
            $contents .= $schoolCd."　".$this->printSchoolName;
            $contents .= "\n";
        }

        $contents .= "(1) ".CTRL_YEAR."年度 大学短大等進学状況\n";

        $header = array();
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "大学(学部)";
        $header[] = "";
        $header[] = "";
        $header[] = "短大(本科)";
        $header[] = "";
        $header[] = "";
        $header[] = "大学・短大(通信制)";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";

        $header2 = array();
        $header2[] = "学科名";
        $header2[] = "性別";
        $header2[] = "卒業者数";
        $header2[] = "国立";
        $header2[] = "公立";
        $header2[] = "私立";
        $header2[] = "国立";
        $header2[] = "公立";
        $header2[] = "私立";
        $header2[] = "国立";
        $header2[] = "公立";
        $header2[] = "私立";
        $header2[] = "放送大学";
        $header2[] = "大学・短大(別科)";
        $header2[] = "専攻科";
        $header2[] = "合計";

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        //課程学科性別取得
        $table = "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT";
        $query = knje441aQuery::getCsvMajor($this, $table, $schoolCd);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $this->dataDiv = array();
        $this->dataDiv[] = array("SCHOOL_SORT" => "00", "SCHOOL_GROUP" => "00");
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "SCHOOL_GROUP" => "01");
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "SCHOOL_GROUP" => "02");
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "SCHOOL_GROUP" => "03");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "SCHOOL_GROUP" => "01");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "SCHOOL_GROUP" => "02");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "SCHOOL_GROUP" => "03");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "SCHOOL_GROUP" => "01");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "SCHOOL_GROUP" => "02");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "SCHOOL_GROUP" => "03");
        $this->dataDiv[] = array("SCHOOL_SORT" => "04", "SCHOOL_GROUP" => "00");
        $this->dataDiv[] = array("SCHOOL_SORT" => "05", "SCHOOL_GROUP" => "00");
        $this->dataDiv[] = array("SCHOOL_SORT" => "06", "SCHOOL_GROUP" => "00");

        $allData = array();
        foreach ($this->majorSex as $msKey => $msVal) {
            $totalCnt = 0;
            $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
            foreach ($this->dataDiv as $dataKey => $dataVal) {
                $query = knje441aQuery::getCollegeCnt($this, $msVal, $dataVal, $schoolCd);
                $setCnt = $db->getOne($query);
                $setCnt = $setCnt ? $setCnt : 0;
                $contents .= ",{$setCnt}";
                if ($dataVal["SCHOOL_SORT"] != "00") {
                    $totalCnt += $setCnt;
                }
                $allData[$dataVal["SCHOOL_SORT"].$dataVal["SCHOOL_GROUP"]] += $setCnt;
            }
            $contents .= ",{$totalCnt}\n";
            $allData["9900"] += $totalCnt;
            $this->header_data = true;
        }

        $contents .= "合計,";
        foreach ($allData as $key => $val) {
            $contents .= ",{$val}";
        }

        return $contents."\n\n\n";
    }

    //ＣＳＶ出力
    function getCollege2Csv($db, $schoolCd) {
        $contents  = "";    //クリア

        $this->dataDiv = array();
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "NAME" => "大学(学部)(系統別人数)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "NAME" => "短大(本科)(系統別人数)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "NAME" => "大学・短大(通信制)(系統別人数)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "04", "NAME" => "放送大学(系統別人数)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "05", "NAME" => "大学・短大(別科)(系統別人数)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "06", "NAME" => "専攻科(系統別人数)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "ALL", "NAME" => "合計(系統別人数)");

        $header = array();
        $header[] = "学科名";
        $header[] = "性別";

        //タイトル学部
        $query = knje441aQuery::getNameMst("E050");
        $this->titleE050 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleE050[] = $row;
            $header[] = $row["NAME1"];
        }
        $result->free();
        $header[] = "計";

        //課程学科性別取得
        $table = "AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT";
        $query = knje441aQuery::getCsvMajor($this, $table, $schoolCd);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $totalCntAll = array();
        $titleCnt = 2;
        foreach ($this->dataDiv as $dataKey => $dataVal) {
            $allData = array();
            $contents .= "({$titleCnt}) ".CTRL_YEAR."年度 {$dataVal["NAME"]}\n";
            $titleCnt++;

            $contents .= implode($header, ",")."\n";

            foreach ($this->majorSex as $msKey => $msVal) {
                $totalCnt = 0;
                $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
                foreach ($this->titleE050 as $e050Key => $e050Val) {
                    if ($dataVal["SCHOOL_SORT"] != "ALL") {
                        $query = knje441aQuery::getCollege2Cnt($this, $msVal, $dataVal, $e050Val, $schoolCd);
                        $setCnt = $db->getOne($query);
                        $setCnt = $setCnt ? $setCnt : 0;
                        $contents .= ",{$setCnt}";
                        $totalCnt += $setCnt;
                        $totalCntAll[$msVal["COURSECD"]][$msVal["MAJORCD"]][$msVal["SEX"]][$e050Val["NAMECD2"]] += $setCnt;
                        $allData[$e050Val["NAMECD2"]] += $setCnt;
                    } else {
                        $setCnt = $totalCntAll[$msVal["COURSECD"]][$msVal["MAJORCD"]][$msVal["SEX"]][$e050Val["NAMECD2"]];
                        $totalCnt += $setCnt;
                        $contents .= ",{$setCnt}";
                        $allData[$e050Val["NAMECD2"]] += $setCnt;
                    }
                }
                $contents .= ",{$totalCnt}\n";
                $allData["9999"] += $totalCnt;
                $this->header_data = true;
            }
            $contents .= "合計,";
            foreach ($allData as $key => $val) {
                $contents .= ",{$val}";
            }
            $contents .= "\n\n";
        }

        return $contents."\n\n";
    }

    //ＣＳＶ出力
    function getSenmonCsv($db, $schoolCd) {
        $contents  = "";    //クリア
        $contents .= "(1) ".CTRL_YEAR."年度 専修・各種学校等入学状況\n";

        $header = array();
        $header[] = "学科名";
        $header[] = "性別";
        $header[] = "専修学校";
        $header[] = "各種学校";
        $header[] = "a合計";
        $header[] = "b公共職業訓練施設等";
        $header[] = "a + b合計";

        $contents .= implode($header, ",")."\n";

        //課程学科性別取得
        $table = "AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT";
        $query = knje441aQuery::getCsvMajor($this, $table, $schoolCd);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $this->dataDiv = array();
        $this->dataDiv[] = array("SCHOOL_SORT" => "11");
        $this->dataDiv[] = array("SCHOOL_SORT" => "12");
        $this->dataDiv[] = array("SCHOOL_SORT" => "ALL1");
        $this->dataDiv[] = array("SCHOOL_SORT" => "13");
        $this->dataDiv[] = array("SCHOOL_SORT" => "ALL2");

        $allData = array();
        foreach ($this->majorSex as $msKey => $msVal) {
            $totalCnt = 0;
            $totalAllCnt = 0;
            $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
            foreach ($this->dataDiv as $dataKey => $dataVal) {
                if ($dataVal["SCHOOL_SORT"] != "ALL1" && $dataVal["SCHOOL_SORT"] != "ALL2") {
                    $query = knje441aQuery::getSenmonCnt($this, $msVal, $dataVal, $schoolCd);
                    $setCnt = $db->getOne($query);
                    $setCnt = $setCnt ? $setCnt : 0;
                    $contents .= ",{$setCnt}";
                    $totalAllCnt += $setCnt;
                    if ($dataVal["SCHOOL_SORT"] != "13") {
                        $totalCnt += $setCnt;
                    }
                    $allData[$dataVal["SCHOOL_SORT"]] += $setCnt;
                } else if ($dataVal["SCHOOL_SORT"] == "ALL1") {
                    $contents .= ",{$totalCnt}";
                    $allData[$dataVal["SCHOOL_SORT"]] += $totalCnt;
                } else if ($dataVal["SCHOOL_SORT"] == "ALL2") {
                    $contents .= ",{$totalAllCnt}";
                    $allData[$dataVal["SCHOOL_SORT"]] += $totalCnt;
                }
            }
            $contents .= "\n";
            $this->header_data = true;
        }

        $contents .= "合計,";
        foreach ($allData as $key => $val) {
            $contents .= ",{$val}";
        }

        return $contents."\n\n\n";
    }

    //ＣＳＶ出力
    function getSenmon2Csv($db, $schoolCd) {
        $contents  = "";    //クリア

        $contents .= "(2) ".CTRL_YEAR."年度 専修・各種学校等系統別人数\n";

        $header = array();
        $header[] = "学科名";
        $header[] = "性別";

        //タイトル学部
        $query = knje441aQuery::getNameMst("E051");
        $this->titleE051 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleE051[] = $row;
            $header[] = $row["NAME1"];
        }
        $result->free();
        $header[] = "合計";
        $contents .= implode($header, ",")."\n";

        //課程学科性別取得
        $table = "AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT";
        $query = knje441aQuery::getCsvMajor($this, $table, $schoolCd);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $allData = array();
        foreach ($this->majorSex as $msKey => $msVal) {
            $totalCnt = 0;
            $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
            foreach ($this->titleE051 as $e051Key => $e051Val) {
                $query = knje441aQuery::getSenmon2Cnt($this, $msVal, $e051Val, $schoolCd);
                $setCnt = $db->getOne($query);
                $setCnt = $setCnt ? $setCnt : 0;
                $contents .= ",{$setCnt}";
                $totalCnt += $setCnt;
                $allData[$e051Val["NAMECD2"]] += $setCnt;
            }
            $contents .= ",{$totalCnt}\n";
            $allData["9999"] += $totalCnt;
            $this->header_data = true;
        }

        $contents .= "合計,";
        foreach ($allData as $key => $val) {
            $contents .= ",{$val}";
        }

        return $contents."\n\n\n";
    }
}
?>
