<?php

require_once('for_php7.php');

class knjl302eModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $testscoreprint;
    var $perhallcdprint;

    function knjl302eModel(){}

    function init() {

        $this->cmd            = VARS::request("cmd");

        $this->examyear       = CTRL_YEAR + 1;
        $this->applicantdiv   = VARS::post("APPLICANTDIV");
        $this->testscoreprint = VARS::post("TESTSCORE_PRINT");
        $this->testdiv        = VARS::post("TESTDIV");
        $this->perhallcdprint = VARS::post("PERHALLCD_PRINT");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $header = array();

        $header["NO_"]             = "No.";
        $header["EXAMNO"]          = "受験番号";
        $header["EXAMHALL_NAME"]   = "室";
        $header["NAME"]            = "氏名";
        $header["NAME_KANA"]       = "フリガナ";
        $header["BIRTHDAY"]        = "生年月日";
        $header["FINSCHOOL_NAME"]  = "中学校";
        $header["CONF_AVG5"]       = "評定値";
        $header["DDIVNAME"]        = "受験コース";
        $header["TDIVNAME"]        = "受験区分";
        $header["EXAMHALLGROUPCD"] = "グループ";
        $header["INTERVIEW1"]      = "面接点1";
        $header["INTERVIEW2"]      = "面接点2";
        $header["CIRCLECNT"]       = "○の数";
        $header["ABSENCE_DAYS"]    = "３年次欠席16以上";
        $header["REMARK"]          = "備考";
        $header["QUALIFYDETAIL"]   = "資格内容";
        $header["QUALIFY"]         = "資格";
        $header["SKILL"]           = "実技";
        $header["JUDGE"]           = "判定";

        //クリア
        $contents  = "";
        //ヘッダ出力
        $contents .= implode($header, ",")."\n";
        //$seq034R2 = array('1' => '実績', '2' => '推薦');
        //$seq034R3 = array('1' => '未設定', '2' => '有り', '3' => '無し');
        //$seq034R4 = array('1' => '未設定', '2' => '可', '3' => '否');

        $query = knjl302eQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //$row["SEQ034_R2"] = $seq034R2[$row["SEQ034_R2"]];
            //$row["SEQ034_R3"] = $seq034R3[$row["SEQ034_R3"]];
            //$row["SEQ034_R4"] = $seq034R4[$row["SEQ034_R4"]];
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
        $applicantdivName = $db->getOne(knjl302eQuery::getNameMstNamecd2($this, "L003", $this->applicantdiv));
        //入試区分
        $TestdivName = $db->getOne(knjl302eQuery::getNameMstNamecd2($this, "L004", $this->testdiv));
        //ファイル名
        $title = $this->examyear.'年度 '.$applicantdivName.$TestdivName.'データ';

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
