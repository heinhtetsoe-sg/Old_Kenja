<?php

require_once('for_php7.php');

class knjl348kModel extends Model {
    var $examcourseall = array();
    var $examcourse;
    var $cmd;
    var $examyear;
    var $testdiv;
    var $output;

    function knjl348kModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->examcourse   = VARS::post("EXAMCOURSE");
        $this->testdiv   = VARS::post("TESTDIV");
        $this->output   = VARS::post("OUTPUT");
    }

    //実行
    function getDownloadModel()
    {
        //実行---2:SS(標準偏差値)相関データ
        if ($this->output == 2) {
        $header = array("EXAMNO2"       => "受験番号",
                        "DESIREDIV2"    => "志望区分",
                        "FUZOKU2"       => "附属",
                        "SEX2"          => "性別",
                        "NAME2"         => "氏名",
                        "JUDGEMENT2"    => "合否",
                        "ITUGI2"        => "五ツ木偏差値",
                        "MOGI2"         => "各種模擬偏差値",
                        "SCORE2_1"      => "国語",
                        "SCORE2_2"      => "算数",
                        "SCORE2_3"      => "社会",
                        "SCORE2_4"      => "理科",
                        "KEI2_4"        => "４計",
                        "KEI2_SHAKAI"   => "社計",
                        "KEI2_RIKA"     => "理計",
                        "KEI2_A"        => "Ａ計"
                        );
        $contents  = implode($header, ",")."\n";

        //実行---1:前後期重複受験者名簿
        } else {
        $header1 = array("EXAMNO2"       => "後期",
                        "DESIREDIV2"    => "",
                        "FUZOKU2"       => "",
                        "SEX2"          => "",
                        "NAME2"         => "",
                        "JUDGEMENT2"    => "",
                        "SCORE2_1"      => "",
                        "SCORE2_2"      => "",
                        "SCORE2_3"      => "",
                        "SCORE2_4"      => "",
                        "KEI2_4"        => "",
                        "KEI2_SHAKAI"   => "",
                        "KEI2_RIKA"     => "",
                        "KEI2_A"        => "",
                        "EXAMNO1"       => "前期",
                        "DESIREDIV1"    => "",
                        "JUDGEMENT1"    => "",
                        "SCORE1_1"      => "",
                        "SCORE1_2"      => "",
                        "SCORE1_3"      => "",
                        "SCORE1_4"      => "",
                        "KEI1_4"        => "",
                        "KEI1_SHAKAI"   => "",
                        "KEI1_RIKA"     => "",
                        "KEI1_A"        => " "
                        );
        $header2 = array("EXAMNO2"       => "受験番号",
                        "DESIREDIV2"    => "志望区分",
                        "FUZOKU2"       => "附属",
                        "SEX2"          => "性別",
                        "NAME2"         => "氏名",
                        "JUDGEMENT2"    => "合否",
                        "SCORE2_1"      => "国語",
                        "SCORE2_2"      => "算数",
                        "SCORE2_3"      => "社会",
                        "SCORE2_4"      => "理科",
                        "KEI2_4"        => "４計",
                        "KEI2_SHAKAI"   => "社計",
                        "KEI2_RIKA"     => "理計",
                        "KEI2_A"        => "Ａ計",
                        "EXAMNO1"       => "受験番号",
                        "DESIREDIV1"    => "志望区分",
                        "JUDGEMENT1"    => "合否",
                        "SCORE1_1"      => "国語",
                        "SCORE1_2"      => "算数",
                        "SCORE1_3"      => "社会",
                        "SCORE1_4"      => "理科",
                        "KEI1_4"        => "４計",
                        "KEI1_SHAKAI"   => "社計",
                        "KEI1_RIKA"     => "理計",
                        "KEI1_A"        => "Ａ計"
                        );
        $contents  = implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";
        }//if

        $data = false;

        $db = Query::dbCheckOut();
        //SQL文発行
        if ($this->output == 1) $query = knjl348kQuery::selectCvsQuery1($this);
        if ($this->output == 2) $query = knjl348kQuery::selectCvsQuery2($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $row["KEI2_4"]      = (0 < $row["KEI2_4"]) ? $row["KEI2_4"] : "";
            $row["KEI2_SHAKAI"] = (0 < $row["KEI2_SHAKAI"]) ? $row["KEI2_SHAKAI"] : "";
            $row["KEI2_RIKA"]   = (0 < $row["KEI2_RIKA"]) ? $row["KEI2_RIKA"] : "";
            $row["KEI2_A"]      = (0 < $row["KEI2_A"]) ? $row["KEI2_A"] : "";
        //実行---1:前後期重複受験者名簿
        if ($this->output == 1) {
            $row["KEI1_4"]      = (0 < $row["KEI1_4"]) ? $row["KEI1_4"] : "";
            $row["KEI1_SHAKAI"] = (0 < $row["KEI1_SHAKAI"]) ? $row["KEI1_SHAKAI"] : "";
            $row["KEI1_RIKA"]   = (0 < $row["KEI1_RIKA"]) ? $row["KEI1_RIKA"] : "";
            $row["KEI1_A"]      = (0 < $row["KEI1_A"]) ? $row["KEI1_A"] : "";
        }

            $contents .= implode($row, ",")."\n";
            $data = true;
        }
        $result->free();

        if ($this->output == 2) 
            $testname = $db->getOne(knjl348kQuery::getTestdivName($this));
        Query::dbCheckIn($db);

        if($data){
            if ($this->output == 2) 
                $filename = $this->examyear."_SS(標準偏差値)相関データ(".$testname.").csv";
            if ($this->output == 1) 
                $filename = $this->examyear."_前後期重複受験者名簿.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
