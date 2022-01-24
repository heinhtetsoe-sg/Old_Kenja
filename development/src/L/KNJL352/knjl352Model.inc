<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl352Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    function knjl352Model()
    {
    }
    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),    //入試制度
                             "JYUKU_NAME"       =>  VARS::post("JYUKU_NAME"),      //塾名
                             "CLASS_ROOM"       =>  VARS::post("CLASS_ROOM")       //教室名
                     );

        $this->cmd         = VARS::request("cmd");
    }
    //入力項目チェック
    function validateInput()
    {
        if (!strlen($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (strlen($this->field["JYUKU_NAME"]) > 30) {
            $this->setWarning("MSG915","塾名は30バイトまでです");
            return false;
        }
        if (strlen($this->field["CLASS_ROOM"]) > 30) {
            $this->setWarning("MSG915","教室名は30バイトまでです");
            return false;
        }
        return true;
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl352Query::getUpdateQuery($this);
        }
        return;
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {

        //変数
        $contents = "";
        $header_data = false;

        //DB接続
        $db = Query::dbCheckOut();

        //入試区分マスタ
        $testdivArray = array();
        $query = knjl352Query::getTestdivMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivArray[] = $row;
        }
        $result->free();

        //ヘッダ
        $date = ",,".common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $time = "　".date('H時i分現在');
        $header1Contents = $date.$time."\n";

        $header2Contents = ",,".common::DateConv1($this->ObjYear."/04/01", 10)."　大宮開成中学　入学試験　受験者データ(塾別)\n\n";

        $header3Contents = ",,,,,";
        $cnt = 0;
        $comma = "";
        foreach ($testdivArray as $key => $testdivRow) {
            $cnt++;
            $header3Contents .= $comma . $cnt . "回目";
            $comma = ",,";
        }
        $header3Contents .= "\n";

        $header3Contents .= "受験番号,氏名,性別,塾名,教室名";
        $comma = ",";
        foreach ($testdivArray as $key => $testdivRow) {
            $header3Contents .= $comma . "合否";
            $header3Contents .= $comma . "4科";
        }
        $header3Contents .= ",合否,手続,入学\n";

        //データ
        $studentArray = array();
        $query = knjl352Query::getStudentSql($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $key = $row["EXAMNO"] . "-" . $row["PRISCHOOLCD"];
            $studentArray[$key] = $row;
        }
        $result->free();

        //得点・合否データ
        $receptArray = array();
        foreach ($studentArray as $key => $studentRow) {
            $query = knjl352Query::getReceptSql($this, $studentRow["EXAMNO"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $receptArray[$studentRow["EXAMNO"]][$row["TESTDIV"]] = $row;
            }
            $result->free();
        }

        //データをセット
        $contents .= $header1Contents;
        $contents .= $header2Contents;
        $contents .= $header3Contents;
        foreach ($studentArray as $key => $studentRow) {
            $judgeMax = "";
            $contents .= $studentRow["EXAMNO"] . ",";
            $contents .= $studentRow["NAME"] . ",";
            $contents .= $studentRow["SEX"] . ",";
            $contents .= $studentRow["PRISCHOOL_NAME"] . ",";
            $contents .= $studentRow["REMARK2"] . ",";
            foreach ($testdivArray as $key => $testdivRow) {
                if (isset($receptArray[$studentRow["EXAMNO"]][$testdivRow["TESTDIV"]])) {
                    $receptRow = $receptArray[$studentRow["EXAMNO"]][$testdivRow["TESTDIV"]];
                    $judgeMark = $this->getJudgeMark($receptRow["JUDGEDIV"], $receptRow["HONORDIV"], $receptRow["JUDGECLASS"]);
                    $contents .= $judgeMark . ",";
                    $judgeMax = $this->getJudgeMax($judgeMax, $judgeMark);
                    $contents .= $receptRow["TOTAL4"] . ",";
                } else {
                    $contents .= "" . ",";
                    $contents .= "" . ",";
                }
            }
            $contents .= $judgeMax.",";
            $contents .= $studentRow["PROCEDUREDIV"] == "1" ? "〇," : ",";
            $contents .= $studentRow["ENTDIV"] == "1" ? "〇" : "";
            $contents .= "\n";
            $header_data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","入学試験受験者データ(塾別)");
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
        } else 
        if ($judgeDiv == "1" && ($judgeClass == "2" || $judgeClass == "3")) {
            $retSt = "◎";
        } else 
        if ($judgeDiv == "1" && ($judgeClass == "1" || $judgeClass == "4" || $judgeClass == "6")) {
            $retSt = "〇";
        } else 
        if ($judgeDiv == "2") {
            $retSt = "×";
        }
        return $retSt;
    }

    function getJudgeMax($judgeMax, $judgeMark) {
        if ("" == $judgeMax) {
            return $judgeMark;
        }
        if ("" == $judgeMark) {
            return $judgeMax;
        }
        $judge = array();
        $judge["☆"] = "4";
        $judge["◎"] = "3";
        $judge["〇"] = "2";
        $judge["×"] = "1";
        $max = $judge[$judgeMax];
        $mark = $judge[$judgeMark];
        $retStr = $max >= $mark ? $judgeMax : $judgeMark;
        return $retStr;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
