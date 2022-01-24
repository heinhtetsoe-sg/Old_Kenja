<?php

require_once('for_php7.php');

class knje434aModel extends Model {
    var $field = array();
    var $cmd;

    function knje434aModel() {}
    function init() {

        $this->field["YEAR"] = VARS::post("YEAR");
        $this->field["DATA_DIV"] = VARS::post("DATA_DIV");

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $this->data = false;

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
            $contents .= $this->getCsv($db, "ALL");
        } else {
            foreach ($this->edboard_schoolcd as $key => $val) {
                $query = knje434aQuery::getEdboardSchoolName($val);
                $this->printSchoolName = $db->getOne($query);
                $contents .= $this->getCsv($db, $val);
                $contents .= "\n\n\n\n";
            }
        }
        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($this->data) {
            $title = CTRL_YEAR."年度_就職状況集計表";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSV出力
    function getCsv($db, $schoolCd) {
        $contents = "";

        if ($schoolCd != "ALL") {
            $contents .= $schoolCd."　".$this->printSchoolName;
            $contents .= "\n";
        }

        //ヘッダ（1行目：タイトル）
        $title = "就職状況集計表";
        $sakuseibi  = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $contents .= '年度：'.CTRL_YEAR.'年,,'.$title.str_repeat(",", 6).'作成日：'.$sakuseibi."\n";

        //初期化
        $header = array();

        //ヘッダ（2行目、3行目）
        $header["SEQ_NAME"]         = array("",         "就職地域");
        $header["PREF_NAME"]        = array("",         "就職都道府県");
        $header["COMPANY_NAME"]     = array("",         "事業所名");
        $header["GOUKAKU_M"]        = array("合格者",   "男");
        $header["GOUKAKU_F"]        = array("",         "女");
        $header["GOUKAKU_T"]        = array("",         "計");
        $header["NAITEI_M"]         = array("内定者",   "男");
        $header["NAITEI_F"]         = array("",         "女");
        $header["NAITEI_T"]         = array("",         "計");

        //ヘッダ（2行目、3行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            $header1[] = $val[0];
            $header2[] = $val[1];

            $header_array[] = $key;
        }

        //各行数取得
        $cntData = $db->getRow(knje434aQuery::getCsvQuery($this, $schoolCd, "cnt"), DB_FETCHMODE_ASSOC);

        //SQL文発行
        $dataTemp = array();
        $subtotal = $total = array();
        $seq = $seq_pref = "";
        $subcounter = $counter = 0;
        $query = knje434aQuery::getCsvQuery($this, $schoolCd);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            if ($seq != $row["SEQ"]) {
                //初期化
                $subtotal = array();
                $subcounter = 0;

                //ヘッダーセット
                $contents .= implode($header1, ",") ."\n";
                $contents .= implode($header2, ",") ."\n";
            }

            if ($seq == $row["SEQ"]) $row["SEQ_NAME"] = "";
            if ($seq_pref == $row["SEQ"].$row["PREF_CD"]) $row["PREF_NAME"] = "";

            $sep = "";
            foreach ($header_array as $hkey) {
                //データセット
                $contents .= $sep.$row[$hkey];
                $sep = ",";

                if (preg_match('/^GOUKAKU/', $hkey) || preg_match('/^NAITEI/', $hkey)) {
                    $subtotal[$hkey] += $row[$hkey];
                    $total[$hkey] += $row[$hkey];
                } else {
                    $subtotal[$hkey] = ($hkey == "COMPANY_NAME") ? '計' : "";
                    $total[$hkey] = ($hkey == "COMPANY_NAME") ? '総合計' : "";
                }
            }
            $contents .= "\n";

            $subcounter++;
            $counter++;

            //小計
            if ($subcounter == $cntData["SEQ".$row["SEQ"]]) {
                $sep = "";
                foreach ($header_array as $hkey) {
                    //データセット
                    $contents .= $sep.$subtotal[$hkey];
                    $sep = ",";
                }
                $contents .= "\n\n";
            }

            //総合計
            if ($counter == $cntData["TOTAL"]) {
                $sep = "";
                foreach ($header_array as $hkey) {
                    //データセット
                    $contents .= $sep.$total[$hkey];
                    $sep = ",";
                }
                $contents .= "\n";
            }

            $seq        = $row["SEQ"];
            $seq_pref   = $row["SEQ"].$row["PREF_CD"];
            $this->data = true;
        }
        $result->free();

        return $contents."\n\n\n";
    }
}
?>
