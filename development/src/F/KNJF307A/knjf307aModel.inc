<?php

require_once('for_php7.php');

class knjf307aModel extends Model {
    var $field = array();
    var $cmd;

    function knjf307aModel() {}
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
                $query = knjf307aQuery::getEdboardSchoolName($val);
                $this->printSchoolName = $db->getOne($query);
                $contents .= $this->getContents($db, $val);
                $contents .= "\n\n\n\n";
            }
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($this->header_data) {
            $couse = "身体測定値平均値一覧";
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

        $contents .= "\n";
        $contents .= "身体測定値平均値一覧,,,,出力日：".CTRL_DATE."\n";

        $headerH = array();
        $headerH[] = "身長";

        $headerW = array();
        $headerW[] = "体重";

        $headerS = array();
        $headerS[] = "座高";

        $header2 = array();
        $header2[] = "区分";
        $header2[] = "";

        $header3 = array();
        $header3[] = "";
        $header3[] = "";

        //年齢カウント分のヘッダーをセット
        $query = knjf307aQuery::getAge($this, $schoolCd);
        $age = $db->query($query);
        $this->age = array();
        while ($row = $age->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header2[] = $row["AGE"]."歳";
            $header2[] = "";
            $header2[] = "";
            $header3[] = "受検者数";
            $header3[] = "総和";
            $header3[] = "平均";
            $this->age[] = $row["AGE"]; //年齢を配列にセット
        }

        //身体測定値平均値一覧
        $coursecd = array();
        $query = knjf307aQuery::getCoursecd();
        $result = $db->query($query);
        $coursecd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $coursecd[] .= $row["COURSECD"];
        }
        $query  = knjf307aQuery::selectCsvQuery($this, "003", $coursecd, $schoolCd);
        $seqFlg  = "";
        $sumFlg3 = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SEQ"] == "01") {
                if ($seqFlg != $row["SEQ"]) {
                    $contents .= implode($headerH, ",")."\n";
                    $contents .= implode($header2, ",")."\n";
                    $contents .= implode($header3, ",")."\n";
                }
                $dataTmp   = array();
                $dataTmp[] = $row["COURSENAME"];
                $dataTmp[] = $row["SEXNAME"];
                foreach ($this->age as $ageVal) {
                    $dataTmp[] = $row["VAL1_".$ageVal];
                    $dataTmp[] = $row["VAL2_".$ageVal];
                    $dataTmp[] = round($row["AVG_".$ageVal], 2);
                }
            } else if ($row["SEQ"] == "02") {
                if ($seqFlg != $row["SEQ"]) {
                    $contents .= "\n";
                    $contents .= implode($headerW, ",")."\n";
                    $contents .= implode($header2, ",")."\n";
                    $contents .= implode($header3, ",")."\n";
                }
                $dataTmp   = array();
                $dataTmp[] = $row["COURSENAME"];
                $dataTmp[] = $row["SEXNAME"];
                foreach ($this->age as $ageVal) {
                    $dataTmp[] = $row["VAL1_".$ageVal];
                    $dataTmp[] = $row["VAL2_".$ageVal];
                    $dataTmp[] = round($row["AVG_".$ageVal], 2);
                }
            } else if ($row["SEQ"] == "03") {
                if ($seqFlg != $row["SEQ"]) {
                    $contents .= "\n";
                    $contents .= implode($headerS, ",")."\n";
                    $contents .= implode($header2, ",")."\n";
                    $contents .= implode($header3, ",")."\n";
                }
                $dataTmp   = array();
                $dataTmp[] = $row["COURSENAME"];
                $dataTmp[] = $row["SEXNAME"];
                foreach ($this->age as $ageVal) {
                    $dataTmp[] = $row["VAL1_".$ageVal];
                    $dataTmp[] = $row["VAL2_".$ageVal];
                    $dataTmp[] = round($row["AVG_".$ageVal], 2);
                }
            }
            $dataTmp = str_replace(",",".",$dataTmp);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($dataTmp, ",") ."\n";
            $seqFlg    = $row["SEQ"];

            $this->header_data = true;
        }
        $result->free();
        return $contents."\n\n\n";
    }
}
?>
