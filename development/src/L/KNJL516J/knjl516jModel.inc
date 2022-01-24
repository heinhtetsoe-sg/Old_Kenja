<?php

require_once('for_php7.php');

class knjl516jModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        /**************/
        /*  ヘッダー  */
        /**************/
        $header = array();
        $header["EXAMNO"]       = "受験番号";
        $header["ENTEXAMYEAR"]  = "入試年度";
        $header["PASS_STATUS"]  = "合格ステータス";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        //SQL文発行
        $query = knjl516jQuery::selectMainQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //学校種別名称
        $AppName = $db->getOne(knjl516jQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試種別名称
        $TestdivName = $db->getOne(knjl516jQuery::getTestDiv($this->ObjYear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$TestdivName.' 合格者';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
