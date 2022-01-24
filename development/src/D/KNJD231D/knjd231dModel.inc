<?php

require_once('for_php7.php');

class knjd231dModel extends Model {
    var $cmd;
    var $field = array();
    var $checked;

    function knjd231dModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            $this->field    = array();
            $this->checked  = array();
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "TESTCD"           =>  VARS::post("TESTCD"),           //テスト種別
                             "KETTEN"           =>  VARS::post("KETTEN"),           //欠点
                             "DATE"             =>  VARS::post("DATE"),             //出力日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        if ($this->cmd == "chgKetten") {
            $this->checked = VARS::post("CHECKED");     //チェック
        } else {
            $this->checked = array();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $contents  = "";    //クリア
        $header_data = false;   //出力フラグ

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ一覧
        $header = array();
        $header["SCHREGNO"]         = "学籍番号";
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "組";
        $header["NAME"]             = "氏名";
        $header["GUARD_NAME"]       = "保護者氏名";
        $header["SEMESTERNAME"]     = "学期";
        $header["TESTITEMNAME"]     = "種別";
        for ($i = 1; $i <= 10; $i++) {
            $header["SUBCLASSNAME".$i]    = "不合格科目名".$i;
            $header["SCORE".$i]           = "不合格科目成績".$i;
            $header["CREDITS".$i]         = "不合格科目単位数".$i;
        }

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //データ取得
        $setval = array();
        $schregno = "";
        $counter = 1;
        $query = knjd231dQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($schregno != $row["SCHREGNO"]) {
                $setval[$row["SCHREGNO"]] = $row;
                $counter = 1;
            } else {
                $counter++;
            }
            $setval[$row["SCHREGNO"]]["SUBCLASSNAME".$counter]  = $row["SUBCLASSNAME"];
            $setval[$row["SCHREGNO"]]["SCORE".$counter]         = $row["SCORE"];
            $setval[$row["SCHREGNO"]]["CREDITS".$counter]       = $row["CREDITS"];

            $schregno = $row["SCHREGNO"];
        }
        $result->free();

        Query::dbCheckIn($db);

        //データ出力
        foreach ($setval as $schregno => $row) {
            $row = str_replace(","   , "." , $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        }

        //ＣＳＶ出力
        if ($header_data) {
            $couse = CTRL_YEAR."年度　成績不振者リスト";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
