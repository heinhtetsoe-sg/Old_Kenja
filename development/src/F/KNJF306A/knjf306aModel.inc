<?php

require_once('for_php7.php');

class knjf306aModel extends Model {
    var $field = array();
    var $cmd;

    function knjf306aModel() {}
    function init() {

        $this->field["YEAR"] = VARS::post("YEAR");
        $this->field["DATA_DIV"] = VARS::post("DATA_DIV");

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //CSV出力
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
        if ($this->field["DATA_DIV"] == "1") {
            $contents .= $this->getContents($db, "ALL");
        } else {
            foreach ($this->edboard_schoolcd as $key => $val) {
                $query = knjf306aQuery::getEdboardSchoolName($val);
                $this->printSchoolName = $db->getOne($query);
                $contents .= $this->getContents($db, $val);
                $contents .= "\n\n\n\n";
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($this->header_data) {
            $couse = "肥満度傾向児及び痩身傾向児出現率";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //CSV出力
    function getContents($db, $schoolCd) {
        $contents  = "";    //クリア

        if ($schoolCd != "ALL") {
            $contents .= $schoolCd."　".$this->printSchoolName;
            $contents .= "\n";
        }

        $contents .= $this->shcoolName."\n";
        $contents .= "肥満傾向児出現率,,,,,,,,出力日：".CTRL_DATE."\n";

        $header = array();
        $header[] = "区分";
        $header[] = "";

        $header2 = array();
        $header2[] = "";
        $header2[] = "";

        //年齢カウント分のヘッダーをセット
        $query = knjf306aQuery::getAge($this, $schoolCd);
        $age = $db->query($query);
        $this->age = array();
        while ($row = $age->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header[] = $row["AGE"]."歳";
            $header[] = "";
            $header[] = "";
            $header2[] = "受験者数";
            $header2[] = "該当者数";
            $header2[] = "出現率（％）";
            $this->age[] = $row["AGE"]; //年齢を配列にセット
        }

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        //肥満傾向児出現率
        $query  = knjf306aQuery::selectCsvQuery($this, "001", $schoolCd);
        $sum1 = $db->getRow(knjf306aQuery::selectCsvQuery2($this, "001", $schoolCd), DB_FETCHMODE_ASSOC);
        $sumFlg1 = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $this->header_data = true;
            $sumFlg1 = true;
        }
        $result->free();
        if ($sumFlg1) {
            $contents .= implode($sum1, ",") ."\n";
        }

        //痩身傾向児出現率
        $contents .= "\n痩身傾向児出現率\n";
        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";
        $query = knjf306aQuery::selectCsvQuery($this, "002", $schoolCd);
        $sum2 = $db->getRow(knjf306aQuery::selectCsvQuery2($this, "002", $schoolCd), DB_FETCHMODE_ASSOC);
        $sumFlg2 = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $this->header_data = true;
            $sumFlg2 = true;
        }
        $result->free();
        if ($sumFlg2) {
            $contents .= implode($sum2, ",") ."\n";
        }
        return $contents."\n\n\n";
    }
}
?>
