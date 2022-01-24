<?php

require_once('for_php7.php');

class knjh172Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjh172Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {

        $this->field = array("DBNAME"   =>  VARS::post("DBNAME"));  //DB変更に備えて

        $this->cmd = VARS::request("cmd");
    }

    //ＣＳＶ出力
    function getDownloadModel() {

        //DB接続
        $db = Query::dbCheckOut();

        $contents  = "";
        $header = "避難Ｇ,出席番号,生徒氏名,保護者氏名,郵便番号,保護者住所,急用連絡先電話番号";

        $data = false;
        $query = knjh172Query::getGradeHrclass();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= ",,,,,,". $row["HR_NAME"] ."\n";
            $contents .= $header ."\n";
            $subcontents  = "";

            $query1 = knjh172Query::selectCsvQuery($row["GRADE"], $row["HR_CLASS"]);
            $result1 = $db->query($query1);
            while($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)){
                $row1 = str_replace(",",".",$row1);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($row1["IS_GRD"] == "1") {
                    continue;
                }

                //保護者と住所が異なる生徒を格納
                if($row1["GUARD_ADDR1"] != $row1["ADDR1"] || $row1["GUARD_ADDR2"] != $row1["ADDR2"]){
                    $subcontents .= ",,,". $row1["NAME"] .",". $row1["ZIPCD"] .",". $row1["ADDR1"] .",". $row1["EMERGENCYTELNO"] ."\n";
                    $subcontents .= ",,,,,". $row1["ADDR2"] .",\n";
                }

                $contents .= $row1["GO_HOME_GROUP_NO"] .",". $row1["ATTENDNO"] .",". $row1["NAME"] .",". $row1["GUARD_NAME"] .",";
                $contents .= $row1["GUARD_ZIPCD"] .",". $row1["GUARD_ADDR1"] .",". $row1["GUARD_TELNO"] ."\n" ;
                $contents .= ",,,,,". $row1["GUARD_ADDR2"] .",\n";

                $data = true;
            }
            $result1->free();

            if($subcontents != ""){
                $contents .= "\n\n\n";
                $contents .= "*,保護者と住所を異にする生徒の住所\n";
                $contents .= $subcontents;
            }

            $contents .= "\n";
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="生徒名簿（避難グループ付き）";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
