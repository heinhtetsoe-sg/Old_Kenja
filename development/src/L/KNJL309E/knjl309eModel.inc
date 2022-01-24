<?php

require_once('for_php7.php');

class knjl309eModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $testscoreprint;
    var $perhallcdprint;

    function knjl309eModel(){}

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
        $subclsttl = array();
        $query = knjl309eQuery::getNameMstNamecd3($this, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclsttl[] = $row["NAME1"];
        }

        $header = array();

        $header["EXAMHALL_NAME"]   = "室";
        $header["NO_"]             = "No.";
        $header["EXAMNO"]          = "受験番号";
        $header["NAME"]            = "氏名";
        $header["NAME_KANA"]       = "フリガナ";
        $header["BIRTHDAY"]        = "生年月日";
        $header["FINSCHOOL_NAME"]  = "中学校";
        $header["DEVI"]            = "評定値";
        $header["REUNION"]         = "同窓生推薦";
        $header["COURSE"]          = "受験コース";
        $header["TDIVNAME"]        = "受験区分";
        $header["EXAMHALLGROUPCD"] = "グループ";
        $header["INTERVIEW1"]      = "面接点1";
        $header["INTERVIEW2"]      = "面接点2";
        $header["CIRCLECNT"]       = "○の数";
        $header["ABSENCE_DAYS"]    = "3年次欠席16以上";
        $header["SCORE1"]          = get_count($subclsttl) > 0 ? $subclsttl[0] : "";
        $header["SCORE2"]          = get_count($subclsttl) > 1 ? $subclsttl[1] : "";
        $header["SCORE3"]          = get_count($subclsttl) > 2 ? $subclsttl[2] : "";
        $header["TOTAL4"]          = "合計";
        $header["REMARK"]          = "備考";

        //クリア
        $contents  = "";
        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        $query = knjl309eQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["BIRTHDAY"] = str_replace('-', '/', $row["BIRTHDAY"]);
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $array = $header;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($array as $field => $label) {
                if ($field == "COURSE"){
                    $contents .= $sep.$row["COURSE"];
                    if ($row["HEIGAN_ABBV"] != ""){
                        $contents .= "(".$row["HEIGAN_ABBV"].")";
                    }
                }else{
                    $contents .= $sep.$row[$field];
                }
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
        $applicantdivName = $db->getOne(knjl309eQuery::getNameMstNamecd2($this, "L003", $this->applicantdiv));
        //入試日程
        $TestdivName = $this->testdiv == "3" ? "Ｂ日程" : "Ａ日程";
        //ファイル名
        $nendo = common::DateConv1($this->ObjYear."/04/01",10);
        $title = $nendo.' '.$TestdivName.'受験者一覧表';

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
