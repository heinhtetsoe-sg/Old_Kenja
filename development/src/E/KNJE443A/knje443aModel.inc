<?php

require_once('for_php7.php');

class knje443aModel extends Model {
    var $field = array();
    var $cmd;

    function knje443aModel() {}
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
        $contents = "";
        if ($this->field["DATA_DIV"] == "1") {
            $contents .= $this->getContents($db, "ALL");
        } else {
            foreach ($this->edboard_schoolcd as $key => $val) {
                $query = knje443aQuery::getEdboardSchoolName($val);
                $this->printSchoolName = $db->getOne($query);
                $contents .= $this->getContents($db, $val);
                $contents .= "\n\n\n\n";
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($this->header_data) {
            $title = CTRL_YEAR."年度_大学進学者数";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSV出力
    function getContents($db, $schoolCd) {

        $contents = "";
        if ($schoolCd != "ALL") {
            $contents .= $schoolCd."　".$this->printSchoolName;
            $contents .= "\n";
        }

        //ヘッダ（1行目：タイトル）
        $title = "大学進学者数";
        $sakuseibi  = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $contents .= '年度：'.CTRL_YEAR.'年,,,'.$title.str_repeat(",", 6).'作成日：'.$sakuseibi."\n";

        //初期化
        $header = array();

        //ヘッダ（2行目）
        $header["COURSENAME"]           = "課程";
        $header["MAJORNAME"]            = "学科";
        $header["SCHOOL_GROUP_NAME"]    = "大学系列";
        $header["SCHOOL_NAME"]          = "大学名";
        $header["GOUKAKU_G"]            = "現役合格者数";
        $header["GOUKAKU_K"]            = "既卒合格者数";
        $header["GOUKAKU_T"]            = "合格者数計";
        $header["NAITEI_G"]             = "現役進学者数";
        $header["NAITEI_K"]             = "既卒進学者数";
        $header["NAITEI_T"]             = "進学者数計";

        //ヘッダ（2行目）セット
        $headerList = $header_array = array();
        foreach ($header as $key => $val) {
            $headerList[]   = $val;
            $header_array[] = $key;
        }
        $contents .= implode($headerList, ",") ."\n";

        //SQL文発行
        $dataTemp = array();
        $query = knje443aQuery::getCsvQuery($this, $schoolCd);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $sep = "";
            foreach ($header_array as $hkey) {
                //データセット
                $contents .= $sep.$row[$hkey];
                $sep = ",";
            }
            $contents .= "\n";
            $this->header_data = true;
        }
        $result->free();

        return $contents."\n\n\n";
    }
}
?>
