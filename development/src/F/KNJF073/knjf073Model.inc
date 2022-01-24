<?php

require_once('for_php7.php');

class knjf073Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjf073Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array("DBNAME" =>  VARS::post("DBNAME")  //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $title = "歯科健康診断の結果一覧";
        $wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        $contents = $wrk_nendo ."," .$title .",,,,,,," ."作成日：" .$wrk_date ."\n";
        $contents .= "\n";

        //ヘッダの作成//
        $header = array("GRADE_NAME1"       => "学年",
                        "SEITO_SU"          => "生徒数",
                        "COUNT_STD_MUSHIBA" => "むし歯保有者数",
                        "COUNT_MI"          => "未処置歯総本数",
                        "COUNT_ZUMI"        => "処置歯総本数",
                        "COUNT_LOST"        => "喪失歯総本数",
                        "COUNT_STD_CO"      => "ＣＯ保有者数",
                        "COUNT_STD_GO"      => "ＧＯ保有者数",
                        "COUNT_STD_G"       => "Ｇ保有者数",
                        );

        $contents .= implode($header, ",") ."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成//
        //SQL文発行
        $query = knjf073Query::selectCsvQuery();
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= $row["GRADE_NAME1"] .",";
            $contents .= $row["SEITO_SU"] .",";
            $contents .= $row["COUNT_STD_MUSHIBA"] .",";
            $contents .= $row["COUNT_MI"] .",";
            $contents .= $row["COUNT_ZUMI"] .",";
            $contents .= $row["COUNT_LOST"] .",";
            $contents .= $row["COUNT_STD_CO"] .",";
            $contents .= $row["COUNT_STD_GO"] .",";
            $contents .= $row["COUNT_STD_G"] .",";
            $contents .= "\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="歯科健康診断の結果一覧";
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
