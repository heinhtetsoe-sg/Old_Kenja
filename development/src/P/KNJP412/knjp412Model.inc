<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp412Model extends Model {
    var $field      = array();
    var $cmd;
    var $output;
    var $month;
    var $grade;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjp412Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->grade = VARS::post("GRADE");
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

        list($header_data, $retContents) = $this->getGradeSchCsv($db);

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $setNendo = common::DateConv1(CTRL_YEAR."/04/01", 10);
            if ($this->grade == "") {
                $couse = "{$setNendo}　学年別授業料　納入状況表";
            } else {                                //エラー以外
                $couse = "高等学校{$this->grade}学年　クラス別授業料　納入状況表";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($retContents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    function getGradeSchCsv($db) {
        $setNendo = common::DateConv1(CTRL_YEAR."/04/01", 10);
        $setTilte = $this->grade == "" ? ",,,,{$setNendo}　学年別授業料　納入状況表\n" : ",,,,,,,高等学校{$this->grade}学年　クラス別授業料　納入状況表\n";
        $contents = $setTilte;

        $header = array("HR_NAME"                   => $this->grade == "" ? "学年" : "年組",
                        "MONEY_DUE"                 => "納入必要額",
                        "NINZU"                     => "人数",
                        "TOTAL_REDUCTIONMONEY"      => "就学支援金合計",
                        "REDUCTIONMONEY_B"          => "府県補助金",
                        "TOTAL_ADJUSTMENT_MONEY"    => "調整金",
                        "TOTAL_BURDEN_CHARGE"       => "学校負担金",
                        "SIJOGEN"                   => "教職員子女減",
                        "SYOUGAKU"                  => "特待・奨学金",
                        "HIKIOTOSI"                 => "引落金額",
                        "NOUNYUUGOUKEI"             => "納入金額合計");

        $contents .= implode($header, ",")."\n";

        $header_data = false;
        //データ
        $query = knjp412Query::getCsvData($this);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($this->grade != "") {
                $contents .= $row["HR_NAME"].",";
            } else {
                $contents .= $row["GRADE"]."学年,";
            }
            //納入必要額
            $contents .= $row["MONEY_DUE"].",";
            $setTotal["MONEY_DUE_DUMMY"] = "";
            //人数
            $contents .= $row["CNT"].",";
            $setTotal["CNT_DUMMY"] = "";
            //就学支援金合計
            $setSien = $row["REDUCTIONMONEY_C1"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTIONMONEY_C2"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $setSien.",";
            $setTotal["REDUCITON_MONEY"] += $setSien;
            //府県補助金
            $contents .= $row["REDUCTIONMONEY_B"].",";
            $setTotal["REDUCTIONMONEY_B"] += $row["REDUCTIONMONEY_B"];
            //調整金
            $contents .= $row["TOTAL_ADJUSTMENT_MONEY"].",";
            $setTotal["TOTAL_ADJUSTMENT_MONEY"] += $row["TOTAL_ADJUSTMENT_MONEY"];
            //学校負担金
            $contents .= $row["TOTAL_BURDEN_CHARGE"].",";
            $setTotal["TOTAL_BURDEN_CHARGE"] += $row["TOTAL_BURDEN_CHARGE"];
            //教職員子女減
            $contents .= $row["SIJOGEN"].",";
            $setTotal["SIJOGEN"] += $row["SIJOGEN"];
            //特待・奨学金
            $setSyougaku = $row["SYOUGAKU"] + $row["RYUUGAKU"];
            $contents .= $setSyougaku.",";
            $setTotal["SYOUGAKU"] += $setSyougaku;
            //引落金額
            $setGoukei = $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $hikiotoshi = $row["SUM_MONEY_DUE"] - ($setGoukei + $row["REDUCTIONMONEY_B"] + $row["TOTAL_ADJUSTMENT_MONEY"] + $row["TOTAL_BURDEN_CHARGE"] + $row["SIJOGEN"] + $setSyougaku);
            $contents .= $hikiotoshi.",";
            $setTotal["HIKIOTOSI"] += $hikiotoshi;
            //納入金額合計
            $contents .= $row["MONEY_DUE"] * $row["CNT"]."";
            $setTotal["MONEY_DUE"] += $row["MONEY_DUE"] * $row["CNT"];
            //返戻・給付金
            $contents .= "\n";
            $header_data = true;
        }
        $result->free();
        $contents .= "\n";
        if ($this->grade != "") {
            $contents .= "{$this->grade}学年合計,";
        } else {
            $contents .= "123年総合計,";
        }
        $contents .= implode($setTotal, ",");

        return array($header_data, $contents);
    }

}
?>
