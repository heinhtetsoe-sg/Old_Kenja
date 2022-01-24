<?php

require_once('for_php7.php');

class knjl583aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $ObjYear;

    function knjl583aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");
    }

    function &getMainModel() {}
    
    //CSV出力処理
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ（1行目：タイトル 2行目：サブタイトル）
        $title = $this->ObjYear."年度　各種統計資料用データ出力";
        $contents .= $title."\n";

        //初期化
        $header = array();

        //ヘッダ
        $header["RECEPTNO"]         = array("", "受験番号");
        $header["NAME"]             = array("", "氏名");
        $header["NAME_KANA"]        = array("", "氏名かな");
        $header["HOPE_NAME"]        = array("", "受験区分");
        $subclsnamelist = array();
        $subclsnamelist = $this->expIdList($db, knjl583aQuery::getNameMstIdName($this, "L009"), "NAMECD2", "NAME1");
        $subclscdlist = $this->expList($db, knjl583aQuery::getTestSubjectId($this), "TESTSUBCLASSCD");
        $header["TOTAL4"]           = array("", "得点合計");
        $header["SCORE_1"]          = array("", $subclscdlist[0] == "" ? "" : $subclsnamelist[$subclscdlist[0]]."(得点)");
        $header["SCORE_2"]          = array("", $subclscdlist[1] == "" ? "" : $subclsnamelist[$subclscdlist[1]]."(得点)");
        $header["SCORE_3"]          = array("", $subclscdlist[2] == "" ? "" : $subclsnamelist[$subclscdlist[2]]."(得点)");
        $header["SCORE_DEV1"]          = array("", $subclscdlist[0] == "" ? "" : $subclsnamelist[$subclscdlist[0]]."(偏差値)");
        $header["SCORE_DEV2"]          = array("", $subclscdlist[1] == "" ? "" : $subclsnamelist[$subclscdlist[1]]."(偏差値)");
        $header["SCORE_DEV3"]          = array("", $subclscdlist[2] == "" ? "" : $subclsnamelist[$subclscdlist[2]]."(偏差値)");
        $subclslist = $this->expList($db, knjl583aQuery::getNameMst($this->ObjYear, "L008"), "ABBV2");
        for ($cnt = 0;$cnt < get_count($subclslist);$cnt++) {
            $idxStr = "CONFIDENTIAL_RPT".sprintf("%02d", $cnt+1);
            $header[$idxStr] = array("", "内申(".$subclslist[$cnt].")");
        }
        $header["TOTAL5"]           = array("", "5科内申合計");
        $header["TOTAL_ALL"]        = array("", "9科内申合計");
        $header["GOUHI"]            = array("", "合否");
        $header["GLABEL"]           = array("", "合否ラベル");
        $header["SHOUGAKU"]         = array("", "奨学生");
        $header["TOKUTAIKANYUU"]    = array("", "特進勧誘");
        $header["PROMISE"]          = array("", "確約");
        $header["LSUM_ADMFEE"]      = array("", "入学一時金");
        $header["ADMFEE"]           = array("", "入学金");
        $header["DECLINE"]          = array("", "入学辞退");

        //ヘッダ（3行目、4行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            $header2[] = $val[1];

            $header_array[] = $key;
        }
        $contents .= implode($header2, ",") ."\n";

        //SQL文発行
        $dataTemp = array();
        $query = knjl583aQuery::getCsvQuery($this, $subclscdlist);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            // $confarry = array();
            // $confarry[] = $row["CONFIDENTIAL_RPT01"] ? $row["CONFIDENTIAL_RPT01"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT02"] ? $row["CONFIDENTIAL_RPT02"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT03"] ? $row["CONFIDENTIAL_RPT03"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT04"] ? $row["CONFIDENTIAL_RPT04"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT05"] ? $row["CONFIDENTIAL_RPT05"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT06"] ? $row["CONFIDENTIAL_RPT06"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT07"] ? $row["CONFIDENTIAL_RPT07"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT08"] ? $row["CONFIDENTIAL_RPT08"] : "0";
            // $confarry[] = $row["CONFIDENTIAL_RPT09"] ? $row["CONFIDENTIAL_RPT09"] : "0";

            $keyStr = $row["RECEPTNO"];
            $dataTemp[$keyStr]["RECEPTNO"]         = $row["RECEPTNO"];
            $dataTemp[$keyStr]["NAME"]             = $row["NAME"];
            $dataTemp[$keyStr]["NAME_KANA"]        = $row["NAME_KANA"];
            $dataTemp[$keyStr]["TOTAL4"]           = $row["TOTAL4"];
            $dataTemp[$keyStr]["SCORE_1"]          = $row["SCORE_1"];
            $dataTemp[$keyStr]["SCORE_2"]          = $row["SCORE_2"];
            $dataTemp[$keyStr]["SCORE_3"]          = $row["SCORE_3"];
            $dataTemp[$keyStr]["SCORE_DEV1"]       = $row["SCORE_DEV1"];
            $dataTemp[$keyStr]["SCORE_DEV2"]       = $row["SCORE_DEV2"];
            $dataTemp[$keyStr]["SCORE_DEV3"]       = $row["SCORE_DEV3"];
            // $dataTemp[$keyStr]["CONFIDENTIAL_RPT"] = $this->convertListtoOneLine($confarry, true);
            for ($cnt = 0;$cnt < get_count($subclslist);$cnt++) {
                $idxStr = "CONFIDENTIAL_RPT".sprintf("%02d", $cnt+1);
                $dataTemp[$keyStr][$idxStr] = $row[$idxStr] ? $row[$idxStr] : "0";
            }

            $dataTemp[$keyStr]["TOTAL5"]           = $row["TOTAL5"];
            $dataTemp[$keyStr]["TOTAL_ALL"]        = $row["TOTAL_ALL"];
            $dataTemp[$keyStr]["GOUHI"]            = $row["GOUHI"];
            $dataTemp[$keyStr]["GLABEL"]           = $row["GLABEL"];
            $dataTemp[$keyStr]["SHOUGAKU"]         = $row["SHOUGAKU"];
            $dataTemp[$keyStr]["TOKUTAIKANYUU"]    = $row["TOKUTAIKANYUU"];
            $dataTemp[$keyStr]["PROMISE"]          = $row["PROMISE"];
            $dataTemp[$keyStr]["LSUM_ADMFEE"]      = $row["LSUM_ADMFEE"];
            $dataTemp[$keyStr]["ADMFEE"]           = $row["ADMFEE"];
            $dataTemp[$keyStr]["DECLINE"]          = $row["DECLINE"];
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
            //$title = $this->ObjYear."年度　各種統計資料用データ";
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
    function convertListtoOneLine($datList, $onlySEFlg) {
        $retStr = "";
        for ($cnt = 0;$cnt < get_count($datList);$cnt++) {
            if (!$onlySEFlg || ($onlySEFlg && $cnt == 0)) {
                $retStr .= "(";
            } else {
                $retStr .= "  ";
            }
            $retStr .= $datList[$cnt];
            if (!$onlySEFlg || ($onlySEFlg && $cnt == get_count($datList)-1)) {
                $retStr .= ")";
            }
        }
        return $retStr;
    }
}
?>
