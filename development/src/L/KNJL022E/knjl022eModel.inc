<?php

require_once('for_php7.php');

class knjl022eModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"      =>  VARS::post("TESTDIV")
                             );

        $this->cmd = VARS::request("cmd");

    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $header = array();

        $header["ENTEXAMYEAR"]      = "入試年度";
        $header["EXAMNO"]           = "受験番号";
        $header["APPLICANTDIV"]     = "入試制度";
        $header["TESTDIV"]          = "入試区分";
        $header["TESTDIV1"]         = "受験区分";
        $header["NAME"]             = "氏名";
        $header["NAME_KANA"]        = "ふりがな";
        $header["BIRTHDAY"]         = "生年月日";
        $header["FINSCHOOL_NAME"]   = "出身中学校";
        $header["SEQ034_R1"]        = "資格内容";
        $header["SEQ034_R2"]        = "資格";
        $header["SEQ034_R3"]        = "実技有無";
        $header["SEQ034_R4"]        = "判定";

        //クリア
        $contents  = "";
        //ヘッダ出力
        $contents .= implode($header, ",")."\n";
        $seq034R2 = array('1' => '実績', '2' => '推薦');
        $seq034R3 = array('1' => '未設定', '2' => '有り', '3' => '無し');
        $seq034R4 = array('1' => '未設定', '2' => '可', '3' => '否');

        $query = knjl022eQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["SEQ034_R2"] = $seq034R2[$row["SEQ034_R2"]];
            $row["SEQ034_R3"] = $seq034R3[$row["SEQ034_R3"]];
            $row["SEQ034_R4"] = $seq034R4[$row["SEQ034_R4"]];
            $row["BIRTHDAY"] = str_replace('-', '/', $row["BIRTHDAY"]);
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $array = $header;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($array as $field => $label) {
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
        //入試制度
        $applicantdivName = $db->getOne(knjl022eQuery::getNameMstNamecd2($this, "L003", $this->field["APPLICANTDIV"]));
        //入試区分
        $TestdivName = $db->getOne(knjl022eQuery::getNameMstNamecd2($this, "L004", $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度 '.$applicantdivName.$TestdivName.'データ';

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
