<?php

require_once('for_php7.php');

class knjh141aModel extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;

    function knjh141aModel() {}

    function init() {
        $this->field = array("AREA"             =>  VARS::post("AREA"),         //エリア
                             "ROSEN"            =>  VARS::post("ROSEN"),        //路線
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
    
    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header = array();
        $header['SCHREGNO']       = '学籍番号';
        $header['MAJORNAME']      = '所属学科';
        $header['COURSECODENAME'] = '所属コース';
        $header['GRADE']          = '学年';
        $header['HR_CLASS']       = '組';
        $header['ATTENDNO']       = '出席番号';
        $header['NAME']           = '生徒氏名';
        $header['JOSYA_1']        = '乗車1';
        $header['ROSEN_1']        = '路線1';
        $header['GESYA_1']        = '下車1';
        $header['FLG_1']          = '通学手段1';
        $header['JOSYA_2']        = '乗車2';
        $header['ROSEN_2']        = '路線2';
        $header['GESYA_2']        = '下車2';
        $header['FLG_2']          = '通学手段2';
        $header['JOSYA_3']        = '乗車3';
        $header['ROSEN_3']        = '路線3';
        $header['GESYA_3']        = '下車3';
        $header['FLG_3']          = '通学手段3';
        $header['JOSYA_4']        = '乗車4';
        $header['ROSEN_4']        = '路線4';
        $header['GESYA_4']        = '下車4';
        $header['FLG_4']          = '通学手段4';
        $header['JOSYA_5']        = '乗車5';
        $header['ROSEN_5']        = '路線5';
        $header['GESYA_5']        = '下車5';
        $header['FLG_5']          = '通学手段5';
        $header['JOSYA_6']        = '乗車6';
        $header['ROSEN_6']        = '路線6';
        $header['GESYA_6']        = '下車6';
        $header['FLG_6']          = '通学手段6';
        $header['JOSYA_7']        = '乗車7';
        $header['ROSEN_7']        = '路線7';
        $header['GESYA_7']        = '下車7';
        $header['FLG_7']          = '通学手段7';

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjh141aQuery::selectQuery($this);
        $header_data = false;

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(","   , "." , $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r"  , "↓", $row);
            $row = str_replace("\n"  , "↓", $row);

            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "生徒別利用路線情報";

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
