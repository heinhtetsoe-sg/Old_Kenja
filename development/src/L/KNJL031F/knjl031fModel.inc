<?php

require_once('for_php7.php');

class knjl031fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl031fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TESTDIV0"         =>  VARS::post("TESTDIV0"),         //入試回数
                             "EXAMCOURSE"       =>  VARS::post("EXAMCOURSE")        //志望区分
                     );

        $this->cmd = VARS::request("cmd");
    }

    //ＣＳＶ出力
    function getDownloadModel() {
		//ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]          = "入試年度";
        $header["APPLICANTDIV"]         = "入試制度コード";
        $header["APPLICANTDIV_NAME"]    = "入試制度";
        $header["TESTDIV"]              = "入試区分コード";
        $header["TESTDIV_NAME"]         = "入試区分";
        if ($this->field["APPLICANTDIV"] == "2") {
            $header["TESTDIV0_NAME"]        = "入試回数";
            $header["EXAMCOURSE_NAME"]      = "志望区分";
        }
        if ($this->field["APPLICANTDIV"] == "1") {
            $header["EXAM_TYPE_NAME"]       = "受験型";
        }
        $header["RECEPTNO"]             = "受験番号";
        $header["NAME"]                 = "氏名";
        $header["NAME_KANA"]            = "ふりがな";

        $contents  = implode($header, ",") ."\n";

        //DB接続
        $db = Query::dbCheckOut();

		//出力データ作成
        $data_flg = false;
        $query = knjl031fQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";

            $data_flg = true;
        }
        $result->free();

        //入試制度名称取得
        $query = knjl031fQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]);
        $app_name = $db->getOne($query);

        //DB切断
        Query::dbCheckIn($db);

        if ($data_flg) {
	        $couse = "受験者データ（".$app_name."）";
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
