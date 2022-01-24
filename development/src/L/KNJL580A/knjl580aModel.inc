<?php

require_once('for_php7.php');

class knjl580aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $passCourse;
    var $sort;
    var $ObjYear;
    var $CONST_SELALL;
    var $CONST_FAILER;

    function knjl580aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->passCourse);
            unset($this->examno);
            unset($this->sort);
        }

        $this->CONST_SELALL = "99999";
        $this->CONST_FAILER = "99998";
        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->passCourse       = VARS::post("PASS_COURSE");
        $this->sort             = VARS::post("SORT");
    }

    function &getMainModel() {}
    
    //CSV出力処理
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        
        $passCourseName = "全て";
        if($this->passCourse == $this->CONST_FAILER) "不合格";
        if($this->passCourse != $this->CONST_SELALL && $this->passCourse != $this->CONST_FAILER) $passCourseName = $db->getOne(knjl580aQuery::getPassCourseCd($this, $this->passCourse));
        $sortName = ($this->sort == "1") ? "　＜受験番号順＞" : "　＜合計得点順＞";

        //ヘッダ（1行目：タイトル 2行目：サブタイトル）
        $title = $this->ObjYear."年度　合格者・不合格者一覧";
        $subTitle = "合格コース：".$passCourseName.$sortName;
        $contents .= $title."\n";
        $contents .= $subTitle."\n";

        //初期化
        $header = array();

        //ヘッダ
        $header["RECEPTNO"]         = array("", "受験番号");
        $header["NAME"]             = array("", "氏名");
        $header["FINSCHOOL_ABBV"]   = array("", "出身学校");
        $header["TOTAL5"]           = array("", "5教科合計");
        $subclslist = $this->convertListtoOneLine($this->expList($db, knjl580aQuery::getNameMst($this->ObjYear, "L008", "", "ABBV2"), "ABBV2"),false);
        $header["CONFIDENTIAL_RPT"] = array("", $subclslist);
        $subclscdlist = array();
        $subclscdlist = $this->expList($db, knjl580aQuery::getNameMst($this->ObjYear, "L008", "", "INT(NAMECD2) AS NAMECD2"), "NAMECD2");
        $header["TOTAL4"]           = array("", "合計");
        $header["TOTAL_RANK4"]      = array("", "順位");
        $header["PROMISEPAPER"]     = array("", "確約書");
        $header["NOT_STANDARD"]     = array("", "基準外");
        $header["HOPE_NAME"]        = array("", "志望区分");
        $header["RESULT"]           = array("", "判定結果");
        $header["SHOUGAKU"]         = array("", "奨学生");
        $header["TOKUTAIKANYUU"]    = array("", "勧誘");

        //ヘッダ（3行目、4行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            $header2[] = $val[1];

            $header_array[] = $key;
        }
        $contents .= implode($header2, ",") ."\n";

        //SQL文発行
        $dataTemp = array();
        $query = knjl580aQuery::getCsvQuery($this, $subclscdlist);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $confarry = array();
            foreach ($subclscdlist as $val) {
                $confarry[] = $row["CONFIDENTIAL_RPT0".$val] ? $row["CONFIDENTIAL_RPT0".$val] : "0";
            }

            $keyStr = $row["RECEPTNO"];
            $dataTemp[$keyStr]["RECEPTNO"]         = $row["RECEPTNO"];
            $dataTemp[$keyStr]["NAME"]             = $row["NAME"];
            $dataTemp[$keyStr]["FINSCHOOL_ABBV"]   = $row["FINSCHOOL_ABBV"];
            $dataTemp[$keyStr]["TOTAL5"]           = $row["TOTAL5"];
            $dataTemp[$keyStr]["CONFIDENTIAL_RPT"] = $this->convertListtoOneLine($confarry, true);
            $dataTemp[$keyStr]["TOTAL4"]           = $row["TOTAL4"];
            $dataTemp[$keyStr]["TOTAL_RANK4"]      = $row["TOTAL_RANK4"];
            $dataTemp[$keyStr]["PROMISEPAPER"]     = $row["PROMISEPAPER"];
            $dataTemp[$keyStr]["NOT_STANDARD"]     = $row["NOT_STANDARD"];
            $dataTemp[$keyStr]["HOPE_NAME"]        = $row["HOPE_NAME"];
            $dataTemp[$keyStr]["RESULT"]           = $row["RESULT"];
            $dataTemp[$keyStr]["SHOUGAKU"]         = $row["SHOUGAKU"];
            $dataTemp[$keyStr]["TOKUTAIKANYUU"]    = $row["TOKUTAIKANYUU"];
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
