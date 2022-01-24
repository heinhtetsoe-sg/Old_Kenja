<?php

require_once('for_php7.php');


class knje370jModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knje370jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("NOT_PRINT"    =>  VARS::post("NOT_PRINT"),    //課程学科を除く
                             "HR_KAIPAGE"   =>  VARS::post("HR_KAIPAGE"),   //クラス毎に改ページ
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );
        $this->notPrint = "3008112";

        $this->cmd         = VARS::request("cmd");
        $this->selectdata  = VARS::post("selectdata");
    }

    function getDownloadModel() {
        return $this->printSingaku();
    }

    function printSingaku() {
        //変数
        $senkouKind = "0";
        $contents = "";

        //ヘッダ
        $header = array("SCHREGNO"          => "学籍番号",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "組",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "NAME_KANA"         => "氏名かな",
                        "SEX"               => "性別",
                        "GAKKOU_RITSU"      => "学校立",
                        "PREF_NAME"         => "所在地",
                        "STAT_CD"           => "大学コード",
                        "STAT_NAME"         => "学校名",
                        "BUNAME"            => "学科",
                        "HOWTOEXAM_NAME"    => "受験方式",
                        "DECISION_NAME"     => "合否",
                        "PLANSTAT_NAME"     => "進路先",
                        "EXAMNO"            => "受験番号",
                        "REMARK"            => "備考"
                        );
        //DB接続
        $db = Query::dbCheckOut();
        //タイトル
        //SQL文発行
        $data = false;
        $query = knje370jQuery::getPrintSingaku($this, CTRL_YEAR, CTRL_SEMESTER, $senkouKind, $this->selectdata);
        $result = $db->query($query);
        $befHrClass = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($this->field["HR_KAIPAGE"] == "1" && $befHrClass != $row["GRADE"].$row["HR_CLASS"] && $data) {
                $contents .= "\n";
                $contents .= "\n";
            }
            if (($this->field["HR_KAIPAGE"] == "1" && $befHrClass != $row["GRADE"].$row["HR_CLASS"]) || !$data) {
                $contents .= implode($header, ",") ."\n";
            }
            foreach ($header as $field => $name) {
                if ($field != "REMARK") {
                    $contents .= $row[$field] .",";
                }
            }
            $contents .= "\n";
            $data = true;
            $befHrClass = $row["GRADE"].$row["HR_CLASS"];
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($data) {
            $nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 10) ."_";
            $title = $nendo ."進路状況一覧";
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
