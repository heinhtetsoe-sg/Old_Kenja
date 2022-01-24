<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl320rModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;
        
    /**
     * メンバ変数の初期化
     */
    function knjl320rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"        =>  VARS::post("TESTDIV"),        //入試区分
                             "OUTKEISYA"      =>  VARS::post("OUTKEISYA"),      //傾斜
                             "INC_KASAN"      =>  VARS::post("INC_KASAN"),      //加算
                             "OUTPUT"         =>  VARS::post("OUTPUT")          //出力順
                     );

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd         = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {

        $setNendo = common::DateConv1($this->ObjYear."/04/01", 10);
        $db = Query::dbCheckOut();

        //入試制度
        $applicantdivName = $db->getOne(knjl320rQuery::getNameMstName1($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分
        $namecd1 = $this->field["APPLICANTDIV"] == "2" ? "L024" : "L004";
        $TestdivName = $db->getOne(knjl320rQuery::getNameMstName1($this->ObjYear, $namecd1, $this->field["TESTDIV"]));

        $query = knjl320rQuery::getKamouk();
        $result = $db->query($query);
        $kamokuArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kamokuArray[$row["NAMECD2"]] = $row["NAME2"];
        }

        $col = array();

        $col[]["NO"]              = "No.";
        $col[]["RECEPTNO"]        = "受験番号";
        $col[]["NAME"]            = "氏名";
        $col[]["NAME_KANA"]       = "ひらがな";
        $col[]["SEX_NAME"]        = "性別";
        $col[]["BIRTHDAY"]        = "生年月日";
        $col[]["FINSCHOOL_NAME"]  = "小学校名";
        $col[]["PRISCHOOL_NAME"]  = "塾名";
        $col[]["SHDIV"]           = "専/併";
        if ($this->field["TESTDIV"] == "1") {
            $col[]["S1_1_SCORE"]      = $kamokuArray["1"];
            $col[]["S1_2_SCORE"]      = $kamokuArray["2"];
            $col[]["S1_4_SCORE"]      = $kamokuArray["4"];
            $col[]["S1_5_SCORE"]      = $kamokuArray["5"];
        } else {
            $col[]["S1_6_SCORE"]      = $kamokuArray["6"];
            $col[]["S1_7_SCORE"]      = $kamokuArray["7"];
            $col[]["DUMMY_SCORE1"]    = "";
            $col[]["DUMMY_SCORE2"]    = "";
        }
        $col[]["TOTAL_A"]         = "計";
        $col[]["KATEN1"]          = "加点1";
        $col[]["KATEN2"]          = "加点2";
        $col[]["TOTAL_B"]         = "合計";
        $col[]["TOTAL_RANK_B"]    = "順位(加点あり)";
        $col[]["INTERVIEW_VALUE"] = "面接";
        $col[]["JDIVNAME"]        = "合否";
        $col[]["NAIDAKU"]         = "内諾";
        $col[]["NOTICE"]          = "欠席日数";
        $col[]["OTHER_RECEPTNO"]  = "受験番号";
        $col[]["OTHER_JDIVNAME"]  = "合否";
        $col[]["REMARK1"]         = "備考";

        //クリア
        $contents = "";
        $contents .= ",,,,,".$setNendo." ".$applicantdivName."入試判定会議資料\n";
        $subTitle = $this->field["OUTPUT"] == "1" ? "-受験番号順-" : "-成績順-";
        $setKaten = $this->field["INC_KASAN"] == "on" ? " (加点あり) " : " (加点なし)";
        $setSubTitle = $this->field["OUTPUT"] == "2" ? $setKaten : "";
        $contents .= ",,,,,".$TestdivName.$subTitle.$setSubTitle."\n";
        $contents .= "\n";

        $titleSep = "";
        $dataCol = array();
        foreach ($col as $renBan => $fieldVal) {
            foreach ($fieldVal as $field => $val) {
                $contents .= $titleSep.$val;
                $titleSep = ",";
                $dataCol[$field] =  $val;
            }
        }
        $contents .= "\n";

        $query = knjl320rQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            //値をセット
            $sep = "";
            foreach ($dataCol as $field => $label) {
                if ($field == "BIRTHDAY") {
                    $row[$field] = str_replace("-", "/", $row[$field]);
                }
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $title = $this->ObjYear.'年度 '.$applicantdivName.$TestdivName.'入試判定会議資料';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
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
