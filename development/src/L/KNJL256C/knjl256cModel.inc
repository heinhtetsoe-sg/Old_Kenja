<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl256cModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $isGojo;

    /**
     * メンバ変数の初期化
     */
    function knjl256cModel()
    {
    }

    function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("PRE_TESTDIV" =>  VARS::post("PRE_TESTDIV"),   //プレテスト区分
                             "APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                            );

        $this->cmd = VARS::request("cmd");

        //学校コードを取得
        $schoolCode = knjl256cQuery::getNameMstZ010();
        $this->isGojo = $schoolCode == "30290053001" ? true : false;
    }
    function getDownloadModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjl256cQuery::getTestSubclass($this);
        $result = $db->query($query);
        $this->testSubclass = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->testSubclass[] = $row;
        }

        $header["PRE_RECEPTNO"]     = "受験番号";
        $header["PRE_EXAM_TYPE"]    = "受験型";
        $header["NAME"]             = "氏名";
        $header["SEX"]              = "性別";
        $header["PREF_NAME"]        = "県別";
        $header["FINSCHOOL_NAME"]   = "出身学校";
        foreach ($this->testSubclass as $key => $val) {
            $header["SUBCLASS{$val["SUBCLASSCD"]}"] = $val["SUBCLASSNAME"];
        }
        if ($this->isGojo) {
            $header["TOTAL2"]           = "国算理合計";
            $header["AVARAGE2"]         = "国算理平均";
            $header["TOTAL_RANK2"]      = "国算理順位";
            $header["TOTAL4"]           = "４教科合計";
            $header["AVARAGE4"]         = "４教科平均";
            $header["DIV_RANK4"]        = "４教科順位";
            $header["TOTAL3"]           = "国算英合計";
            $header["AVARAGE3"]         = "国算英平均";
            $header["DIV_RANK3"]        = "国算英順位";
        } else {
            $header["TOTAL2"]           = "２教科合計";
            $header["AVARAGE2"]         = "２教科平均";
            $header["TOTAL_RANK2"]      = "２教科順位";
            $header["TOTAL4"]           = "３教科合計";
            $header["AVARAGE4"]         = "３教科平均";
            $header["DIV_RANK4"]        = "３教科順位";
        }
        $header["PRISCHOOL_NAME"]   = "塾名";

        //入試制度
        $query = knjl256cQuery::getApctDiv($this);
        $applicantName = $db->getOne($query);

        //年度取得
        $nxtyr_date = (substr(CTRL_DATE,0,4)+1).(substr(CTRL_DATE,4));
        $year = common::DateConv1(str_replace("-","/",$nxtyr_date),10);

        //日付・時間取得
        $date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);

        //プレテスト区分の名称を取得
        $query = knjl256cQuery::getPreTestdiv($this, true);
        $preTestdivName = $db->getOne($query);

        $contents  = ",,{$year}　{$applicantName}入試プレテスト　成績一覧（".$preTestdivName."）\n";
        $contents .= ",,,,,,,,,{$date}\n";
        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        $query = knjl256cQuery::getCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);

            $setCsv["PRE_RECEPTNO"]     = $row["PRE_RECEPTNO"];
            $setCsv["PRE_EXAM_TYPE"]    = $row["PRE_EXAM_TYPE_NAME"];
            $setCsv["NAME"]             = $row["NAME"];
            $setCsv["SEX"]              = $row["SEX_NAME"];
            $setCsv["PREF_NAME"]        = $row["FINSCHOOL_PREF"];
            $setCsv["FINSCHOOL_NAME"]   = $row["FINSCHOOL_NAME"];
            foreach ($this->testSubclass as $key => $val) {
                $setCsv["SCORE{$val["SUBCLASSCD"]}"] = $row["ATTEND_FLG{$val["SUBCLASSCD"]}"] == "0" ? "*" : $row["SCORE{$val["SUBCLASSCD"]}"];
            }
            $setCsv["TOTAL2"]           = $row["TOTAL2"] == "" ? "*" : $row["TOTAL2"];
            $setCsv["AVARAGE2"]         = $row["TOTAL2"] == "" ? "*" : $row["AVARAGE2"];
            $setCsv["TOTAL_RANK2"]      = $row["TOTAL2"] == "" ? "*" : $row["TOTAL_RANK2"];
            if ($this->isGojo) {
                $setCsv["TOTAL4"]           = $row["PRE_EXAM_TYPE"] == "2" ? ($row["TOTAL4"] == "" ? "*" : $row["TOTAL4"]) : "";
                $setCsv["AVARAGE4"]         = $row["PRE_EXAM_TYPE"] == "2" ? ($row["TOTAL4"] == "" ? "*" : $row["AVARAGE4"]) : "";
                $setCsv["DIV_RANK4"]        = $row["PRE_EXAM_TYPE"] == "2" ? ($row["TOTAL4"] == "" ? "*" : $row["DIV_RANK4"]) : "";
                $setCsv["TOTAL3"]           = $row["PRE_EXAM_TYPE"] == "2" ? ($row["TOTAL3"] == "" ? "*" : $row["TOTAL3"]) : "";
                $setCsv["AVARAGE3"]         = $row["PRE_EXAM_TYPE"] == "2" ? ($row["TOTAL3"] == "" ? "*" : $row["AVARAGE3"]) : "";
                $setCsv["DIV_RANK3"]        = $row["PRE_EXAM_TYPE"] == "2" ? ($row["TOTAL3"] == "" ? "*" : $row["DIV_RANK3"]) : "";
            } else {
                $setCsv["TOTAL4"]           = $row["PRE_EXAM_TYPE"] == "1" ? ($row["TOTAL4"] == "" ? "*" : $row["TOTAL4"]) : "";
                $setCsv["AVARAGE4"]         = $row["PRE_EXAM_TYPE"] == "1" ? ($row["TOTAL4"] == "" ? "*" : $row["AVARAGE4"]) : "";
                $setCsv["DIV_RANK4"]        = $row["PRE_EXAM_TYPE"] == "1" ? ($row["TOTAL4"] == "" ? "*" : $row["DIV_RANK4"]) : "";
            }
            $setCsv["PRISCHOOL_NAME"]   = $row["PRISCHOOL_NAME"];

            $contents .= implode($setCsv, ",") ."\n";
            $data = true;
        }

        $result->free();

        Query::dbCheckIn($db);

        if ($data){
            $couse="成績一覧（".$preTestdivName."）";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
