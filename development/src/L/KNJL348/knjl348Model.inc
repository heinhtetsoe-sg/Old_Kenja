<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl348Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl348Model()
    {
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {

        //変数
        $contents = "";
        $header_data = false;
        $title1 = "中学　" . common::DateConv1($this->ObjYear."/04/01", 10) . "入試";
        $title2 = "在校生　弟・妹受験　一覧";

        //DB接続
        $db = Query::dbCheckOut();

        //入試区分マスタ
        $testdivArray = array();
        $query = knjl348Query::getTestdivMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivArray[] = $row;
        }
        $result->free();

        //ヘッダ
        $header1Contents = "";
        $header2Contents = "";
        $header3Contents = "";
        $header = array();
        $header["NO"] = "No";
        $header["EXAMNO"] = "受験番号";
        $header["NAME"] = "受験生氏名";
        $header["SEX"] = "受験生性別";
        $header["REMARK1"] = "第１志望";
        $header["REMARK2"] = "在校生氏名";
        $header["REMARK3"] = "在籍クラス";
        $header["REMARK4"] = "併願予定校";
        foreach ($header as $key => $val) {
            if ($key == "NO") {
                $header1Contents .= $title1 . ",";
                $header2Contents .= $title2 . ",";
                $header3Contents .= $val . ",";
            } else if ($key == "REMARK4") {
                foreach ($testdivArray as $key => $testdivRow) {
                    $header1Contents .= $testdivRow["MONTH"] . "月" . $testdivRow["DAY"] . "日" . ",";
                    $header1Contents .= ",";
                    $header2Contents .= $testdivRow["ABBV"] . ",";
                    $header2Contents .= ",";
                    $header3Contents .= "得点" . ",";
                    $header3Contents .= "合否" . ",";
                }
                $header1Contents .= "\n";
                $header2Contents .= "\n";
                $header3Contents .= $val . "\n";
                $header_data = true;
            } else {
                $header1Contents .= ",";
                $header2Contents .= ",";
                $header3Contents .= $val . ",";
            }
        }

        //弟・妹受験データ
        $studentArray = array();
        $query = knjl348Query::getStudentSql($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $studentArray[$row["EXAMNO"]] = $row;
            if (strlen($row["RECOM_EXAMNO1"])) {
                $studentArray[$row["RECOM_EXAMNO1"]] = $row;
            }
            if (strlen($row["RECOM_EXAMNO2"])) {
                $studentArray[$row["RECOM_EXAMNO2"]] = $row;
            }
            if (strlen($row["RECOM_EXAMNO3"])) {
                $studentArray[$row["RECOM_EXAMNO3"]] = $row;
            }
        }
        $result->free();

        //得点・合否データ
        $receptArray = array();
        $desireArray = array();
        $baseTestdivArray = array();
        foreach ($studentArray as $examno => $studentRow) {
            $query = knjl348Query::getReceptSql($this, $examno);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $receptArray[$examno][$row["TESTDIV"]] = $row;
            }
            $result->free();
            //欠席・受験
            $query = knjl348Query::getDesireSql($this, $examno);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $desireArray[$examno][$row["TESTDIV"]] = $row;
            }
            $result->free();
            //出願
            $query = knjl348Query::getBaseTestdivSql($this, $examno);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                for ($no = 0; $no <= 6; $no++) {
                    if (strlen($row["TESTDIV".$no])) {
                        $baseTestdivArray[$examno][$row["TESTDIV".$no]] = $row;
                    }
                }
            }
            $result->free();
        }

        //データをセット
        $dataContents = "";
        $no = 0;
        foreach ($studentArray as $examno => $studentRow) {
            if ($examno != $studentRow["RECOM_EXAMNO1"] && $examno != $studentRow["RECOM_EXAMNO2"] && $examno != $studentRow["RECOM_EXAMNO3"]) {
                $no++;
            }
            $dataContents .= $no . ",";
            $dataContents .= $examno . ",";
            $dataContents .= $studentRow["NAME"] . ",";
            $dataContents .= $studentRow["SEX_NAME"] . ",";
            $remark1 = ($studentRow["REMARK1"] == "1") ? "〇" : "";
            $dataContents .= $remark1 . ",";
            $dataContents .= $studentRow["REMARK2"] . ",";
            $dataContents .= $studentRow["REMARK3"] . ",";
            foreach ($testdivArray as $key => $testdivRow) {
                $baseTestdivMark = $desireMark = $receptMark = $total = "";
                if (isset($baseTestdivArray[$examno][$testdivRow["TESTDIV"]])) {
                    $baseTestdivMark = "出";
                }
                if (isset($desireArray[$examno][$testdivRow["TESTDIV"]])) {
                    $desireRow = $desireArray[$examno][$testdivRow["TESTDIV"]];
                    $desireMark = $this->getDesireMark($desireRow["EXAMINEE_DIV"]);
                }
                if (isset($receptArray[$examno][$testdivRow["TESTDIV"]])) {
                    $receptRow = $receptArray[$examno][$testdivRow["TESTDIV"]];
                    $receptMark = $this->getJudgeMark($receptRow["JUDGEDIV"], $receptRow["HONORDIV"], $receptRow["JUDGECLASS"]);
                    $total = $receptRow["TOTAL4"];
                }
                if (strlen($desireMark) || strlen($receptMark) || strlen($total)) {
                    $judgeMark = "";
                    if (strlen($receptMark)) {
                        $judgeMark = $receptMark;
                    } else if (!strlen($total)) {
                        $judgeMark = $desireMark;
                    }
                    $dataContents .= $total . ",";
                    $dataContents .= $judgeMark . ",";
                } else if (strlen($baseTestdivMark)) {
                    $judgeMark = $baseTestdivMark;
                    $dataContents .= "" . ",";
                    $dataContents .= $judgeMark . ",";
                } else {
                    $dataContents .= "" . ",";
                    $dataContents .= "" . ",";
                }
            }
            $dataContents .= $studentRow["REMARK4"] . "\n";
        }

        //ＣＳＶ出力
        $contents .= $header1Contents;
        $contents .= $header2Contents;
        $contents .= $header3Contents;
        $contents .= $dataContents;

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","弟・妹受験一覧");
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    function getJudgeMark($judgeDiv, $honorDiv, $judgeClass) {
        $retSt = "";
        if ($judgeDiv == "1" && $honorDiv == "1") {
            $retSt = "☆";
        }
        if ($judgeDiv == "1" && ($judgeClass == "2" || $judgeClass == "3")) {
            $retSt = $retSt . "◎";
        }
        if ($judgeDiv == "1" && ($judgeClass == "1" || $judgeClass == "4" || $judgeClass == "6")) {
            $retSt = $retSt . "〇";
        }
        if ($judgeDiv == "2") {
            $retSt = $retSt . "×";
        }
        return $retSt;
    }

    function getDesireMark($examineeDiv) {
        $retSt = "";
        if ($examineeDiv == "1") {
            $retSt = "出";
        }
        if ($examineeDiv == "2") {
            $retSt = "▲";
        }
        return $retSt;
    }
}
?>
