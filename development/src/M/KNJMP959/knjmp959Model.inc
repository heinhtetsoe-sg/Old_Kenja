<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp959Model extends Model {
    var $field      = array();
    var $cmd;
    var $output;
    var $grade;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjmp959Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->output = VARS::post("OUTPUT");
        $this->grade = VARS::post("GRADE");

        $this->selectdata = VARS::post("selectdata");
        if ($this->selectdata) {
            $this->hr_class = explode(",", $this->selectdata);
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $contents = "";
        //DB接続
        $db = Query::dbCheckOut();

        $hrClassArray = array();
        if ($this->output == "1") {
            foreach ($this->hr_class as $key => $val) {
                $query = knjmp959Query::GetClass($val);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $hrClassArray[] = $row["CD"];
                }
            }
        } else {
            $hrClassArray = $this->hr_class;
        }
        $this->totalGrade = array();
        $befGrade = "";
        foreach ($hrClassArray as $key => $gradeHr) {
            $grade = substr($gradeHr, 0, 2);
            if ($this->output == "1" && $befGrade != "" && $befGrade != $grade) {
                $contents .= ",学年合計,";
                $contents .= $this->totalGrade[$befGrade]["TOTAL_DUE"].",";
                foreach ($this->monthArray as $key => $month) {
                    $contents .= $this->totalGrade[$befGrade]["MONEY_DUE{$month}"].",";
                }
                //納入額計
                $contents .= $this->totalGrade[$befGrade]["TOTAL_PAID"].",";
                //未納額計
                $contents .= $this->totalGrade[$befGrade]["TOTAL_MINOU"].",";
                $contents .= "\n";
            }
            list($header_data, $retContents) = $this->getCsvData($db, $gradeHr);
            $befGrade = $grade;
            $contents .= $retContents;
        }
        if ($this->output == "1" && $befGrade != "") {
            $contents .= ",学年合計,";
            $contents .= $this->totalGrade[$befGrade]["TOTAL_DUE"].",";
            foreach ($this->monthArray as $key => $month) {
                $contents .= $this->totalGrade[$befGrade]["MONEY_DUE{$month}"].",";
            }
            //納入額計
            $contents .= $this->totalGrade[$befGrade]["TOTAL_PAID"].",";
            //未納額計
            $contents .= $this->totalGrade[$befGrade]["TOTAL_MINOU"].",";
            $contents .= "\n";
        }
        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "生徒別徴収金台帳";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    function getCsvData($db, $gradeHr) {
        $this->monthArray = array();
        for ($monthCnt = 4; $monthCnt < 16; $monthCnt++) {
            $setMonth = $monthCnt < 12 ? $monthCnt : $monthCnt - 12;
            $this->monthArray[] = $setMonth;
        }
        $contents = ",,,,,,,生徒別徴収金台帳\n";
        $query = knjmp959Query::getHrAndStaffName($this, $gradeHr);
        $subTaitle = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $contents .= ",{$subTaitle["HR_NAME"]},{$subTaitle["STAFFNAME"]}\n";
        $contents .= ",,,,就学支援金\n";

        $header["ATTENDNO"]     = "出席番号";
        $header["NAME"]         = "氏名";
        $header["TOTAL_DUE"]    = "請求額計";
        foreach ($this->monthArray as $key => $month) {
            $header["MONTH{$month}"]         = "{$month}月";
        }
        $header["TOTAL_PAID"]   = "納入額計";
        $header["TOTAL_MINOU"]  = "未納額計";
        $header["REMARK"]       = "備考";

        $contents .= implode($header, ",")."\n";

        $header_data = false;
        //データ
        $query = knjmp959Query::getCsvData($this, $gradeHr);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        $befKey = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= $row["ATTENDNO"].",";
            $contents .= $row["NAME"].",";
            $totalMoney = 0;
            $totalPaid = 0;
            $totalMinou = 0;
            foreach ($this->monthArray as $key => $month) {
                $totalMoney += $row["MONEY_DUE{$month}"];
                $totalPaid += $row["PAID_MONEY{$month}"];
                $totalMinou += $row["MINOU{$month}"];
            }
            //請求額計
            $contents .= $totalMoney.",";
            $setTotal["TOTAL_DUE"] += $totalMoney;
            $this->totalGrade[$row["GRADE"]]["TOTAL_DUE"] += $totalMoney;
            foreach ($this->monthArray as $key => $month) {
                $contents .= $row["MONEY_DUE{$month}"].",";
                $setTotal["MONEY_DUE{$month}"] += $row["MONEY_DUE{$month}"];
                $this->totalGrade[$row["GRADE"]]["MONEY_DUE{$month}"] += $row["MONEY_DUE{$month}"];
            }
            //納入額計
            $contents .= $totalPaid.",";
            $setTotal["TOTAL_PAID"] += $totalPaid;
            $this->totalGrade[$row["GRADE"]]["TOTAL_PAID"] += $totalPaid;
            //未納額計
            $contents .= $totalMinou.",";
            $setTotal["TOTAL_MINOU"] += $totalMinou;
            $this->totalGrade[$row["GRADE"]]["TOTAL_MINOU"] += $totalMinou;
            $contents .= "\n";

            $befKey = $row["GRADE"].$row["HR_CLASS"];
            $befGrade = $row["GRADE"];
            $header_data = true;
        }
        $result->free();

        $contents .= ",クラス合計,";
        $contents .= $setTotal["TOTAL_DUE"].",";
        foreach ($this->monthArray as $key => $month) {
            $contents .= $setTotal["MONEY_DUE{$month}"].",";
        }
        //納入額計
        $contents .= $setTotal["TOTAL_PAID"].",";
        //未納額計
        $contents .= $setTotal["TOTAL_MINOU"].",";
        $contents .= "\n";

        return array($header_data, $contents);
    }
}
?>
