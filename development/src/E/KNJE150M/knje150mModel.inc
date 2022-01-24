<?php

require_once('for_php7.php');


class knje150mModel extends Model {
    var $field = array();
    var $cmd;

    function knje150mModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->grade = VARS::post("GRADE");
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $data = false;

        $headArray = array("組", "番号", "調査書番号", "氏名");

        $headKyouka = array();
        $headKotei = array();
        foreach ($headArray as $val) {
            $headKyouka[] = "";
            $headKotei[] = "";
        }
        $befKyouka = "@";

        //観点マスタ
        $query = knje150mQuery::getJviewMst();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $headKyouka[] = $befKyouka != $row["CLASSNAME"] ? $row["CLASSNAME"] : "";
            $headKotei[] = $befKyouka != $row["CLASSNAME"] ? "評価の観点" : "";
            $headArray[] = $row["VIEWNAME"];
            $befKyouka = $row["CLASSNAME"];
        }
        $result->free();

        $contents  = ",,,,必修教科"."\n";
        $contents .= implode(",", $headKyouka)."\n";
        $contents .= implode(",", $headKotei)."\n";
        $contents .= implode(",", $headArray)."\n";

        //観点データ
        $schregData = array();
        $befSchreg = "@";
        $schregCnt = 0;
        $query = knje150mQuery::getJviewCsv($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if ($befSchreg != $row["SCHREGNO"]) {
                $schregCnt++;
            }

            $schKey = $row["HR_CLASS"].$row["ATTENDNO"];

            $schregData[$schKey]["HR_NAME"] = $row["HR_NAME"];
            $schregData[$schKey]["ATTENDNO"] = $row["ATTENDNO"];
            $schregData[$schKey]["RENBAN"] = $schregCnt;
            $schregData[$schKey]["NAME"] = $row["NAME"];
            $schregData[$schKey]["STATUS"][] = $row["STATUS"];
            $befSchreg = $row["SCHREGNO"];
        }
        $result->free();

        foreach ($schregData as $key => $val) {
            $contents .= $val["HR_NAME"].",";
            $contents .= $val["ATTENDNO"].",";
            $contents .= $val["RENBAN"].",";
            $contents .= $val["NAME"].",";
            $contents .= implode(",", $val["STATUS"])."\n";
            $data = true;
        }

        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", "観点の記録");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
