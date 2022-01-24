<?php

require_once('for_php7.php');

class knjl516hModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $year;

    function init() {
        $this->year = VARS::post("YEAR");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "DISTINCT_ID"      =>  VARS::post("DISTINCT_ID"),      //入試判別
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $header = array();
        $header["EXAMNO"]       = "受験番号";
        $header["ENTEXAMYEAR"]  = "入試年度";
        $header["PASS_STATUS"]  = "合格ステータス";

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if($this->field["HEADER"] == "on") $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //データ
        $query = knjl516hQuery::selectDataQuery($this);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"),"",str_replace(",",".",$row));
            $setArray = $header;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($setArray as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "合格者";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
