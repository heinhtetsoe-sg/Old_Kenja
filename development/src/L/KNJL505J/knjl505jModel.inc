<?php

require_once('for_php7.php');

class knjl505jModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;

    function knjl505jModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別
                             "EXAM_TYPE"        =>  VARS::post("EXAM_TYPE"),        //入試方式
                             "RECEPTNO_FROM"    =>  VARS::post("RECEPTNO_FROM"),    //受験番号FROM
                             "RECEPTNO_TO"      =>  VARS::post("RECEPTNO_TO"),      //受験番号TO
                             "OUTPUT_SORT"      =>  VARS::post("OUTPUT_SORT"),      //表示順
                             "OUTPUT"           =>  VARS::post("OUTPUT")            //帳票種類
                              );
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //入試種別配列
        $maxCnt = 0;
        $this->testdivArray = array();
        $query = knjl505jQuery::getTestDivArray($this->examyear, $this->field["APPLICANTDIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($maxCnt < 9) {
                $this->testdivArray[$row["TESTDIV"]] = $row["TESTDIV_ABBV"];
            }
            $maxCnt++;
        }
        $result->free();

        /**************/
        /*  ヘッダー  */
        /**************/
        $header = array();
        $header["RECEPTNO"]         = "受験番号";
        if ($this->field["APPLICANTDIV"] == "1") {
            $header["ORDERNO"]          = "整理番号";
        }
        if ($this->field["APPLICANTDIV"] == "2") {
            $header["EXAMTYPE"]         = "入試方式";
        }
        $header["INTERVIEW"]        = "面接";
        $header["ATTEND"]           = "出欠";
        if ($this->field["APPLICANTDIV"] == "1") {
            $header["EXAMTYPE"]         = "入試方式";
        }
        foreach ($this->testdivArray as $testdiv => $testdivAbbv) {
            $header["KEKKA".$testdiv]   = $testdivAbbv;
        }
        if ($this->field["APPLICANTDIV"] == "2") {
            $header["NAITEI"]           = "内定";
        }
        $header["CHK_A"]            = "A";
        $header["CHK_F"]            = "F";
        $header["CHK_T"]            = "T";
        $header["CHK_B"]            = "B";
        $header["CHK_J"]            = "J";
        if ($this->field["APPLICANTDIV"] == "2") {
            $header["NAISINTEN"]        = "内申";
            $header["KESSEKI"]          = "欠席";
        }
        $header["NAME"]             = "氏名";
        $header["NAME_KANA"]        = "氏名カナ";
        $header["REMARK"]           = "備考";
        $header["FINSCHOOL_NAME"]   = "出身校名";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //SQL文発行
        $query = knjl505jQuery::selectMainQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

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
        //学校種別名称
        $AppName = $db->getOne(knjl505jQuery::getNameMst($this->examyear, "L003", $this->field["APPLICANTDIV"]));
        //入試種別名称
        $TestdivName = $db->getOne(knjl505jQuery::getTestDiv($this->examyear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->examyear.'年度 '.$AppName.' '.$TestdivName.' 入試基礎資料';

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
