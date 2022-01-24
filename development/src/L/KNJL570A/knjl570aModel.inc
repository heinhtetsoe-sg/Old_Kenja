<?php

require_once('for_php7.php');

class knjl570aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $desirediv;
    var $sort;
    var $ObjYear;
    var $CONST_SELALL;

    function knjl570aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->desirediv);
            unset($this->examno);
            unset($this->sort);
        }

        $this->CONST_SELALL = "99999";
        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = VARS::post("TESTDIV");
        $this->desirediv        = VARS::post("DESIREDIV");
        $this->sort             = VARS::post("SORT");
    }

    function &getMainModel() {}
    
    //CSV出力処理
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        
        $testDivName = "全て";
        if($this->testdiv != $this->CONST_SELALL) $testDivName   = $db->getOne(knjl570aQuery::getNameMst($this->ObjYear, "L004", $this->testdiv));
        $desireDivName = "全て";
        if($this->desirediv != $this->CONST_SELALL) $desireDivName = $db->getOne(knjl570aQuery::getHopeCourseCd($this, $this->desirediv));
        $sortName = ($this->sort == "1") ? "　＜受験番号順＞" : "　＜合計得点順＞";

        //ヘッダ（1行目：タイトル 2行目：サブタイトル）
        $title = $this->ObjYear."年度　合否判定会議資料";
        $subTitle = "入試区分：".$testDivName."　志望区分：".$desireDivName.$sortName;
        $contents .= $title."\n";
        $contents .= $subTitle."\n";

        //初期化
        $header = array();

        //ヘッダ
        $header["RECEPTNO"]            = array("", "受験番号");
        $header["NAME"]                = array("", "氏名");
        $header["FINSCHOOL_ABBV"] = array("", "出身中学");
        $header["TOTAL5"]              = array("", "5科");
        $header["TOTAL_ALL"]           = array("", "9科");
        $subclslist = $this->convertListtoOneLine($this->expList($db, knjl570aQuery::getNameMst($this->ObjYear, "L008"), "ABBV2"),false);
        $header["CONFIDENTIAL_RPT"]    = array("", $subclslist);
        $subclsnamelist = array();
        $subclsnamelist = $this->expIdList($db, knjl570aQuery::getNameMstIdName($this, "L009"), "NAMECD2", "NAME1");
        $subclscdlist = $this->expList($db, knjl570aQuery::getTestSubjectId($this), "TESTSUBCLASSCD");
        $header["SCORE_1"]             = array("", $subclscdlist[0] == "" ? "" : $subclsnamelist[$subclscdlist[0]]."(偏差値)");
        $header["SCORE_2"]             = array("", $subclscdlist[1] == "" ? "" : $subclsnamelist[$subclscdlist[1]]."(偏差値)");
        $header["SCORE_3"]             = array("", $subclscdlist[2] == "" ? "" : $subclsnamelist[$subclscdlist[2]]."(偏差値)");
        $header["TOTAL4"]              = array("", "合計");
        $header["TOTAL_RANK4"]         = array("", "順位");
        $header["PROMISEPAPER"]        = array("", "確約者");
        $header["NOT_STANDARD"]        = array("", "基準外");
        $header["HOPE_NAME"]           = array("", "希望");
        $header["RESULT"]              = array("", "判定結果");
        $header["HEIGAN_KOURITU1"]     = array("", "公立併願1");
        $header["HEIGAN_KOURITU2"]     = array("", "公立併願2");
        $header["HEIGAN_SIRITU1"]      = array("", "私立併願1");
        $header["HEIGAN_SIRITU2"]      = array("", "私立併願2");

        //ヘッダ（3行目、4行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            $header2[] = $val[1];

            $header_array[] = $key;
        }
        $contents .= implode($header2, ",") ."\n";

        //SQL文発行
        $dataTemp = array();
        $query = knjl570aQuery::getCsvQuery($this, $subclscdlist);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $confarry = array();
            $confarry[] = $row["CONFIDENTIAL_RPT01"] ? $row["CONFIDENTIAL_RPT01"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT02"] ? $row["CONFIDENTIAL_RPT02"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT03"] ? $row["CONFIDENTIAL_RPT03"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT04"] ? $row["CONFIDENTIAL_RPT04"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT05"] ? $row["CONFIDENTIAL_RPT05"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT06"] ? $row["CONFIDENTIAL_RPT06"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT07"] ? $row["CONFIDENTIAL_RPT07"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT08"] ? $row["CONFIDENTIAL_RPT08"] : "0";
            $confarry[] = $row["CONFIDENTIAL_RPT09"] ? $row["CONFIDENTIAL_RPT09"] : "0";

            $keyStr = $row["RECEPTNO"];
            $dataTemp[$keyStr]["RECEPTNO"]         = $row["RECEPTNO"];
            $dataTemp[$keyStr]["NAME"]             = $row["NAME"];
            $dataTemp[$keyStr]["FINSCHOOL_ABBV"]   = $row["FINSCHOOL_ABBV"];
            $dataTemp[$keyStr]["TOTAL5"]           = $row["TOTAL5"];
            $dataTemp[$keyStr]["TOTAL_ALL"]        = $row["TOTAL_ALL"];
            $dataTemp[$keyStr]["CONFIDENTIAL_RPT"] = $this->convertListtoOneLine($confarry, true);
            $dataTemp[$keyStr]["SCORE_1"]          = $row["SCORE_1"]."(".$row["SCORE_DEV1"].")";
            $dataTemp[$keyStr]["SCORE_2"]          = $row["SCORE_2"]."(".$row["SCORE_DEV2"].")";
            $dataTemp[$keyStr]["SCORE_3"]          = $row["SCORE_3"]."(".$row["SCORE_DEV3"].")";
            $dataTemp[$keyStr]["TOTAL4"]           = $row["TOTAL4"];
            $dataTemp[$keyStr]["TOTAL_RANK4"]      = $row["TOTAL_RANK4"];
            $dataTemp[$keyStr]["PROMISEPAPER"]     = $row["PROMISEPAPER"];
            $dataTemp[$keyStr]["NOT_STANDARD"]     = $row["NOT_STANDARD"];
            $dataTemp[$keyStr]["HOPE_NAME"]        = $row["HOPE_NAME"];
            $dataTemp[$keyStr]["RESULT"]           = $row["RESULT"];
            $dataTemp[$keyStr]["HEIGAN_KOURITU1"]  = $row["HEIGAN_KOURITU1"];
            $dataTemp[$keyStr]["HEIGAN_KOURITU2"]  = $row["HEIGAN_KOURITU2"];
            $dataTemp[$keyStr]["HEIGAN_SIRITU1"]   = $row["HEIGAN_SIRITU1"];
            $dataTemp[$keyStr]["HEIGAN_SIRITU2"]   = $row["HEIGAN_SIRITU2"];
            $dataTemp[$keyStr]["DESIREDIV_NAME"]   = $row["DESIREDIV_NAME"];
            $dataTemp[$keyStr]["EXAMNO"]           = $row["EXAMNO"];
            $dataTemp[$keyStr]["NAME"]             = $row["NAME"];
            $dataTemp[$keyStr]["FINSCHOOL_NAME"]   = $row["FINSCHOOL_NAME"];
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
            $title = $this->ObjYear."年度　合否判定会議資料";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
    function expList($db, $query, $getidxStr) {
        $retList = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $retList[] = $row[$getidxStr];
        }
        return $retList;
    }
    function expIdList($db, $query, $getCdStr, $getIdxStr) {
        $retList = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $retList[$row[$getCdStr]] = $row[$getIdxStr];
        }
        return $retList;
    }
    function convertListtoOneLine($datList, $valSetFlg) {
        $retStr = "";
        for ($cnt = 0;$cnt < get_count($datList);$cnt++) {
            if ($cnt == 0) {
                $retStr .= "(";
            } else {
                if ($valSetFlg) {
                    $retStr .= " ";
                } else {
                    $retStr .= "|";
                }
            }
            if ($valSetFlg) {
                $retStr .= ($datList[$cnt] > 9 ? "" : " ").$datList[$cnt];
            } else {
                $retStr .= $datList[$cnt];
            }
            if ($cnt == get_count($datList)-1) {
                $retStr .= ")";
            }
        }
        return $retStr;
    }
}
?>
