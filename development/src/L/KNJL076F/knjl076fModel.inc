<?php

require_once('for_php7.php');

class knjl076fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl076fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TESTDIV0"         =>  VARS::post("TESTDIV0"),         //入試回数
                             "PRINT"            =>  VARS::post("PRINT")             //入金額・入金日印刷
                     );

        $this->cmd = VARS::request("cmd");
    }

    function getDownloadModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        $header = array();
        $header["RECEPTNO"]         = "受験番号";
        $header["BIRTHDAY"]         = "生年月日";
        $header["JUDGE_STATUS"]     = "合否ステータス";

        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        if ($this->field["APPLICANTDIV"] == "1") {
            $query = knjl076fQuery::getCsvQueryJ($this);
        } else {
            $query = knjl076fQuery::getCsvQueryH($this);
        }
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);

            $setCsv = array();
            $setCsv["RECEPTNO"]         = $row["RECEPTNO"];
            $setCsv["BIRTHDAY"]         = str_replace("-", "", $row["BIRTHDAY"]);

            //中学
            if ($this->field["APPLICANTDIV"] == "1") {
                if ($this->field["TESTDIV"] == "5") {
                    if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "S") {
                        $setCsv["JUDGE_STATUS"]     = 25;
                    } else if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "D") {
                        $setCsv["JUDGE_STATUS"]     = 26;
                    } else if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "B") {
                        $setCsv["JUDGE_STATUS"]     = 27;
                    } else {
                        $setCsv["JUDGE_STATUS"]     = 28;
                    }
                } else {
                    if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "S") {
                        $setCsv["JUDGE_STATUS"]     = 12;
                    } else if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "D") {
                        $setCsv["JUDGE_STATUS"]     = 13;
                    } else if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "B") {
                        $setCsv["JUDGE_STATUS"]     = 14;
                    } else if ($row["JUDGEDIV"] == "1") {
                        $setCsv["JUDGE_STATUS"]     = 2;
                    } else if ($row["JUDGEDIV"] == "2") {
                        $setCsv["JUDGE_STATUS"]     = 3;
                    } else {
                        $setCsv["JUDGE_STATUS"]     = 8;
                    }
                }

            //高校
            } else {
                if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "T100") {
                    $setCsv["JUDGE_STATUS"]     = 12;
                } else if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "T3") {
                    $setCsv["JUDGE_STATUS"]     = 13;
                } else if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "T1") {
                    $setCsv["JUDGE_STATUS"]     = 14;
                } else if ($row["JUDGEDIV"] == "1" && $row["TOKUTAI"] == "T0") {
                    $setCsv["JUDGE_STATUS"]     = 15;
                } else if ($row["JUDGEDIV"] == "1" && $row["KIBOU"] != $row["GOKAKU"]) {
                    $setCsv["JUDGE_STATUS"]     = 16;
                } else if ($row["JUDGEDIV"] == "1") {
                    $setCsv["JUDGE_STATUS"]     = 2;
                } else if ($row["JUDGEDIV"] == "2") {
                    $setCsv["JUDGE_STATUS"]     = 9;
                } else {
                    $setCsv["JUDGE_STATUS"]     = 8;
                }
            }

            $contents .= implode($setCsv, ",") ."\n";
            $data = true;
        }

        $result->free();

        Query::dbCheckIn($db);

        if ($data){
            $couse="合否データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
