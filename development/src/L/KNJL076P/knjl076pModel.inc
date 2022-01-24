<?php

require_once('for_php7.php');

class knjl076pModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $ObjYear;

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->field);
        }

        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "JUDGEDIV"         =>  VARS::post("JUDGEDIV"),         //合否
                             "SORT"             =>  VARS::post("SORT"),             //出力順
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        /**************/
        /*  ヘッダー  */
        /**************/
        $header = array();
        $header["EXAMNO"]           = "受験番号";
        $header["JUDGEDIV"]         = "合否";
        $header["JUDGEDIV_NAME"]    = "合否名称";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        $query = knjl076pQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度名称
        $AppName = $db->getOne(knjl076pQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        $namecd1 = ($this->field["APPLICANTDIV"] == "2") ? "L004" : "L024";
        $TestdivName = $db->getOne(knjl076pQuery::getNameMst($this->ObjYear, $namecd1, $this->field["TESTDIV"]));
        //合否
        $JudgedivName = "合否";
        if ($this->field["JUDGEDIV"] != "ALL") $JudgedivName = $db->getOne(knjl076pQuery::getNameMst($this->ObjYear, "L013", $this->field["JUDGEDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$TestdivName.$JudgedivName.'データ';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
