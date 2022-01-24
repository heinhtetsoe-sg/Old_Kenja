<?php

require_once('for_php7.php');

class knjl506jModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;

    public function knjl506jModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別
                             "EXAM_TYPE"        =>  VARS::post("EXAM_TYPE"),        //入試方式
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "SORT1"            =>  VARS::post("SORT1"),            //ソート(得点チェックリスト用)
                             "SORT2"            =>  VARS::post("SORT2"),            //ソート(成績一覧表用)
                             "SUBSORT"          =>  VARS::post("SUBSORT")           //ソート(成績一覧表用)成績順(小計・合計)
                              );
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        if ($this->field["OUTPUT"] == "2") {
            //入試種別配列
            $maxCnt = 0;
            $this->testdivArray = array();
            $query = knjl506jQuery::getTestDivArray($this->examyear, $this->field["APPLICANTDIV"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($maxCnt < 9) {
                    $this->testdivArray[$row["TESTDIV"]] = $row["TESTDIV_ABBV"];
                }
                $maxCnt++;
            }
            $result->free();

            //入試種別配列
            $this->testSubArray = array();
            $query = knjl506jQuery::getTestSubclassArray2($this->examyear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->field["EXAM_TYPE"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->testSubArray[$row["SUBCLASSCD"]] = $row["NAME1"];
            }
            $result->free();
        }

        /**************/
        /*  ヘッダー  */
        /**************/

        $header = array();
        if ($this->field["OUTPUT"] == "1") {
            $header["SUBNAME"]          = "科目名";
            $header["RECEPTNO"]         = "受験番号";
            $header["ORDERNO"]          = "整理番号";
            $header["NAME"]             = "氏名";
            $header["SCORE"]            = "得点";
        } else {
            $header["RECEPTNO"]         = "受験番号";
            if ($this->field["APPLICANTDIV"] == "1") {
                $header["ORDERNO"]      = "整理番号";
            }
            if ($this->field["APPLICANTDIV"] == "2") {
                $header["EXAMTYPE"]         = "入試方式";
            }
            $header["PASS_UNPASS"]      = "合否";
            $header["INTERVIEW"]        = "面接";
            $header["ATTEND"]           = "出欠";
            if ($this->field["APPLICANTDIV"] == "1") {
                $header["EXAMTYPE"]         = "入試方式";
            }
            $datacnt = 1;
            foreach ($this->testdivArray as $testdiv => $testdivAbbv) {
                $header["KEKKA".$datacnt]   = $testdivAbbv;
                $datacnt++;
            }
            if ($this->field["APPLICANTDIV"] == "2") {
                $header["NAITEI"]       = "内定";
            }
            $header["CHK_A"]            = "A";
            $header["CHK_F"]            = "F";
            $header["CHK_T"]            = "T";
            $header["CHK_B"]            = "B";
            $header["CHK_J"]            = "J";
            if ($this->field["APPLICANTDIV"] == "2") {
                $header["NAISINTEN"]    = "内申";
                $header["KESSEKI"]      = "欠席";
            }
            $header["NAME"]             = "氏名";

            foreach ($this->testSubArray as $subCd => $subName) {
                $header["SCORE".$subCd]   = $subName;
            }

            if ($this->field["APPLICANTDIV"] == "1") {
                $header["TOTAL2"]       = "小計";
                $header["TOTAL_RANK2"]  = "小計順位";
            }
            $header["TOTAL4"]           = "合計";
            $header["TOTAL_RANK4"]      = "合計順位";
            $header["REMARK"]           = "備考";
            $header["FINSCHOOL_NAME"]   = "出身校名";
        }


        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //SQL文発行
        if ($this->field["OUTPUT"] == "1") {
            $query = knjl506jQuery::selectMainQuery1($db, $this, $this->examyear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->field["EXAM_TYPE"]);
        } else {
            $query = knjl506jQuery::selectMainQuery2($db, $this, $this->examyear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->field["EXAM_TYPE"]);
        }
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

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
        $AppName = $db->getOne(knjl506jQuery::getNameMst($this->examyear, "L003", $this->field["APPLICANTDIV"]));
        //入試種別名称
        $TestdivName = $db->getOne(knjl506jQuery::getTestDiv($this->examyear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"]));
        //ファイル名
        if ($this->field["OUTPUT"] == "1") {
            $title = $this->examyear.'年度 チェックリスト '.$AppName.' '.$TestdivName;
        } else {
            $title = $this->examyear.'年度 '.$AppName.' '.$TestdivName.' 成績一覧表';
        }

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
