<?php

require_once('for_php7.php');

class knjh171Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjh171Model() {
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

        $contents  = "";
        $contents .= "●：責任者,,○：副責任者,,［ ］：集合場所\n\n";

        //DB接続
        $db = Query::dbCheckOut();

        $data = false;
        $query = knjh171Query::getGoHomeGroup();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= $row["GO_HOME_GROUP_NAME"] .",,,,,,,,". $row["MEETING_PLACE"] ."\n";

            $cnt = 0;
            $query1 = knjh171Query::selectCsvQuery($row["GO_HOME_GROUP_NO"]);
            $result1 = $db->query($query1);
            while($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)){
                $row1 = str_replace(",",".",$row1);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($row1["IS_GRD"] == "1") {
                    continue;
                }

                if($row1["RESPONSIBILITY"] == "1") {
                    $row1["RESPONSIBILITY"] = '●';
                } else if($row1["RESPONSIBILITY"] == "2"){
                    $row1["RESPONSIBILITY"] = '○';
                } else {
                    $row1["RESPONSIBILITY"] = "";
                }

                $cnt++;
                if($cnt % 3 == 0){
                    $contents .= implode($row1, ",") ."\n";
                } else {
                    $contents .= implode($row1, ",") .",";
                }
                $data = true;
            }
            $result1->free();
            $contents .= ($cnt % 3 == 0) ? "" : "\n";
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="災害対策避難地域別グループ一覧";
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
