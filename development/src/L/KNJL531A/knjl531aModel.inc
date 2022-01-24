<?php

require_once('for_php7.php');

class knjl531aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $desirediv;
    var $sort;
    var $ObjYear;

    function knjl531aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->desirediv);
            unset($this->examno);
            unset($this->sort);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->desirediv        = strlen(VARS::post("DESIREDIV")) ? VARS::post("DESIREDIV") : VARS::post("HID_DESIREDIV");
        $this->sort             = strlen(VARS::post("SORT")) ? VARS::post("SORT") : VARS::post("HID_SORT");
    }

    function &getMainModel() {}
    
    //CSV出力処理
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        
        $testDivName   = $db->getOne(knjl531aQuery::getNameMst($this->ObjYear, "L004", $this->testdiv));
        $desireDivName = "全て";
        if($this->desirediv != "") $desireDivName = $db->getOne(knjl531aQuery::getHopeCourseName($this->ObjYear, $this->desirediv));
        $sortName = ($this->sort == "1") ? "　＜受験番号順＞" : "　＜名前順＞";

        //ヘッダ（1行目：タイトル 2行目：サブタイトル）
        $title = $this->ObjYear."年度　合格確約者一覧";
        $subTitle = "受験区分：".$testDivName."　志望区分：".$desireDivName.$sortName;
        $contents .= $title."\n";
        $contents .= $subTitle."\n";

        //初期化
        $header = array();

        //ヘッダ
        // $header["TESTDIV"]          = array("", "入試コード");
        $header["TESTDIV_NAME"]     = array("", "入試区分");
        // $header["DESIREDIV"]        = array("", "志望コード");
        $header["DESIREDIV_NAME"]   = array("", "志望区分");
        $header["EXAMNO"]           = array("", "受験番号");
        $header["NAME"]             = array("", "氏名");
        // $header["FS_CD"]            = array("", "学校コード");
        $header["FINSCHOOL_NAME"]   = array("", "出身中学校");

        //ヘッダ（3行目、4行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            //$header1[] = $val[0];
            $header2[] = $val[1];

            $header_array[] = $key;
        }
        //$contents .= implode($header1, ",") ."\n";
        $contents .= implode($header2, ",") ."\n";

        //SQL文発行
        $dataTemp = array();
        $query = knjl531aQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $keyStr = $row["EXAMNO"];
            if ($dataTemp[$keyStr]["EXAMNO"] == "") {
                // $dataTemp[$keyStr]["TESTDIV"]           = $row["TESTDIV"];
                $dataTemp[$keyStr]["TESTDIV_NAME"]      = $row["TESTDIV_NAME"];
                // $dataTemp[$keyStr]["DESIREDIV"]         = $row["DESIREDIV"];
                $dataTemp[$keyStr]["DESIREDIV_NAME"]    = $row["DESIREDIV_NAME"];
                $dataTemp[$keyStr]["EXAMNO"]            = $row["EXAMNO"];
                $dataTemp[$keyStr]["NAME"]              = $row["NAME"];
                // $dataTemp[$keyStr]["FS_CD"]             = $row["FS_CD"];
                $dataTemp[$keyStr]["FINSCHOOL_NAME"]    = $row["FINSCHOOL_NAME"];
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
            $title = $this->ObjYear."年度　合格確約者一覧";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
