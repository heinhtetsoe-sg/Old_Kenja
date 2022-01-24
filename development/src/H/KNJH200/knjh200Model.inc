<?php

require_once('for_php7.php');

class knjh200Model extends Model {
    var $field  = array();
    var $cmd;
    var $selectdata;
    var $selectdataText;

    function knjh200Model() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
            $this->selectdata = array();
            $this->selectdataText = array();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $contents = "";

        //ヘッダ（項目名の出力なし）
        $header = array();
        $header["SCHREGNO"]     = "利用者コード";
        $header["SCH_DIV"]      = "利用者区分";
        $header["DATE"]         = "登録日";
        $header["NAME"]         = "氏名（漢字）";
        $header["NAME_KANA"]    = "カナ";
        $header["SEX"]          = "性別";
        $header["HR_CLASS_CD"]  = "クラスコード";
        $header["ATTENDNO"]     = "出席番号";
        $header["LIBRARYCD"]    = "館コード";

        //DB接続
        $db = Query::dbCheckOut();

        //性別変換用
        $sexArray = array("1" => "1", "2" => "3");

        //SQL文発行
        $query = knjh200Query::getSelectQuery($this);
        $result = $db->query($query);
        $data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            //利用者区分
            $row["SCH_DIV"] = "00";

            //登録日
            $row["DATE"] = str_replace("-", "", CTRL_DATE);

            //半角カタカナに変換
            $row["NAME_KANA"] = mb_convert_kana($row["NAME_KANA"], "kh", "UTF-8");

            //性別
            $row["SEX"] = (strlen($sexArray[$row["SEX"]])) ? $sexArray[$row["SEX"]] : "";

            //クラスコード
            $row["HR_CLASS_CD"] = $row["GRADE"].sprintf("%02d", (int)$row["HR_CLASS"]);

            //館コード
            $row["LIBRARYCD"] = "A1";

            $sep = "";
            foreach ($header as $field => $val) {
                $contents .= $sep.'"'.$row[$field].'"';
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse = CTRL_YEAR."年度　図書館システム用データ";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
