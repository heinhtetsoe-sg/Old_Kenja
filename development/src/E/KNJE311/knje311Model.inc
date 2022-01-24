<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje311Model.inc 56587 2017-10-22 12:54:51Z maeshiro $

class knje311Model extends Model {

    var $field   = array();
    var $cmd;
    var $control = array();
    var $data_seq;        // SEQデータの保持
    var $data_senkou_fin; // 選考結果データの保持

    function knje311Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array(
            "YEAR"          =>  VARS::post("YEAR"),         // 年度
            "SCHOOL_SORT"   =>  VARS::post("SCHOOL_SORT")   // 分類
            );

        $this->cmd = VARS::request("cmd");
        $this->data_seq = VARS::post("DATA_SEQ");
        $this->data_senkou_fin = VARS::post("DATA_SENKOU_FIN");
    }

    function &getUpdateModel() {
        if (isset($this->data_seq) && isset($this->data_senkou_fin)) {
            if (knje311Query::getUpdateQuery($this->field["YEAR"], $this->data_seq, $this->data_senkou_fin)) {
                $this->setMessage("MSG201");
            } 
        }
    }

    function getDownloadModel() {
        // 受験先種別ｺｰﾄﾞを決める
        if ("04" < $this->field["SCHOOL_SORT"]) { // 会社
            $senkou_kind = 1;
            $bango = "求人番号";
        } else { // 学校
            $senkou_kind = 0;
            $bango = "学校コード";
        }

        // ヘッダ
        $header = array("受験方法", $bango, "進路先", "クラス", "番", "生徒番号", "生徒氏名", "欠席", "平均", "総合", "選考会", "選考結果");
        $contents = "";
        $contents .= implode($header, ",") ."\n";

        // DB接続
        $db = Query::dbCheckOut();

        // SQL文発行
        $data = false;
        $query = knje311Query::sqlList($this->field["YEAR"], $senkou_kind, $this->field["SCHOOL_SORT"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nm_school_sort = $row["NM_SCHOOL_SORT"]; // 分類
            // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $contents .= $row["NM_JUKEN_HOWTO"] .",";
            $contents .= $row["SHINRO_NO"] .",";
            $contents .= $row["STAT_NAME"] .",";
            $contents .= $row["HR_NAME"] .",";
            $contents .= $row["ATTENDNO"] .",";
            $contents .= $row["SCHREGNO"] .",";
            $contents .= $row["NAME_SHOW"] .",";
            $contents .= $row["ATTEND_SUM"] .",";
            $contents .= $row["AVG"] .",";
            $contents .= $row["SEISEKI"] .",";
            $contents .= $row["NM_SENKOU_KAI"] .",";
            $contents .= $row["SENKOU_FIN"] .$row["NM_SENKOU_FIN"] ."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse="校内選考結果一括入力_" .$this->field["YEAR"] ."年度_" .$nm_school_sort;
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
