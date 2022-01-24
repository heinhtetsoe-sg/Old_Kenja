<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp330Model extends Model {
    var $field      = array();
    var $output;
    var $todoufuken;
    var $hrclass;
    var $selectdata = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp330Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "DATE1"            =>  VARS::post("DATE1"),            //日付FROM
                             "DATE2"            =>  VARS::post("DATE2"),            //日付TO
                             "YOUSIKI"          =>  VARS::post("YOUSIKI"),          //出力様式
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->output     = VARS::post("OUTPUT");
        $this->todoufuken = VARS::post("TODOUFUKEN");
        $this->hrclass    = VARS::post("HRCLASS");
        $this->cmd        = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * CSV出力
     */
    function getDownloadModel()
    {
        $data = false;

        $db = Query::dbCheckOut();

        $inState = "('".str_replace(",", "','", VARS::post("selectdata"))."')";
        //SQL文発行
        if ($this->output == "1") {
            $query = knjp330Query::todoufukenMeisai($this, $inState);
        } else if ($this->output == "2") {
            $query = knjp330Query::classMeisai($this, $inState);
        } else {
            $query = knjp330Query::kojinMeisai($this, $inState);
        }

        $result = $db->query($query);

        list($date1Y, $date1M, $date1D) = preg_split("/\//", $this->field["DATE1"]);
        $nendo1Y = $date1M < "04" ? $date1Y - 1 : $date1Y;
        $date1WarekiNendo = common::Calc_Wareki($nendo1Y, $date1M, $date1D);
        $date1WarekiNen = common::Calc_Wareki($date1Y, $date1M, $date1D);
        $date1WarekiDate = $date1WarekiNen.$date1M."月".$date1D."日";

        list($date2Y, $date2M, $date2D) = preg_split("/\//", $this->field["DATE2"]);
        $date2WarekiNen = common::Calc_Wareki($date2Y, $date2M, $date2D);
        $date2WarekiDate = $date2WarekiNen.$date2M."月".$date2D."日";
        $date2WarekiYoubi = common::DateConv1($this->field["DATE2"], 3);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $contents .= ",授業料軽減決定通知書";
            $contents .= "\n\n";
            $contents .= ",,,,,".$date1WarekiDate;
            $contents .= "\n\n";
            $contents .= "申請者（学資負担者）";
            $contents .= "\n\n";
            $contents .= $row["GUARANTOR_NAME"]."殿";
            $contents .= "\n\n";
            $contents .= "（".$row["HR_NAME"]." ".$row["ATTENDNO"]."番  ".$row["NAME"]." ）\n\n";
            $contents .= "\n\n\n";
            $contents .= ",,,,,近 畿 大 学 附 属 高 等 学 校";
            $contents .= "\n\n\n";
            $contents .= ",,,,,事　務　部";
            $contents .= "\n\n\n\n";
            if ($this->field["YOUSIKI"] == "1") {
                $contents .= ",,　このたび国より". $date1WarekiNendo ."度高等学校等就学支援金の交付を受ける";
                $contents .= "\n\n";
                $contents .= ",,ことになりましたので、あなたが納付すべき授業料を下記のとおり";
                $contents .= "\n\n";
                $contents .= ",,軽減いたします。";
            } else {
                $contents .= ",,　このたび国より". $date1WarekiNendo ."度高等学校等就学支援金、府県より";
                $contents .= "\n\n";
                $contents .= ",,授業料支援(軽減)補助金の交付を受けることになりましたので、";
                $contents .= "\n\n";
                $contents .= ",,あなたが納付すべき授業料を下記のとおり軽減いたします。";
            }
            $contents .= "\n\n\n\n";
            $contents .= ",,,　　　　　　　　　　　　　記";
            $contents .= "\n\n\n\n";
            $reductionmoney = number_format($row["REDUCTIONMONEY"] + $row["B_PAID_MONEY"] + $row["A_PAID_MONEY"] + $row["ADJUSTMENT_MONEY"]);
            $contents .= ",,1 . 決定軽減額,\"". $reductionmoney ."円\"";
            $contents .= "\n\n\n";
            $contents .= ",,2 . 軽減方法,". $date2WarekiYoubi ."授業料引落し分と相殺";
            $contents .= "\n\n\n";
            $money_due = number_format($row["MONEY_DUE"]);
            $setTimes = $this->field["YOUSIKI"] == "1" ? "2" : "3";
            $contents .= ",,,授業料,①,\"". $money_due ."円\"";
            $contents .= "\n\n";
            $baseMoney = number_format($row["B_PAID_MONEY"]);
            $contents .= ",,,就学支援金(国)　基本,②,\"". $baseMoney ."円\"";
            $contents .= "\n\n";
            $addMoney = number_format($row["A_PAID_MONEY"]);
            $contents .= ",,,就学支援金(国)　加算,③,\"". $addMoney ."円\"";
            $contents .= "\n\n";
            if ($this->field["YOUSIKI"] == "2") {
                $reductionmoney = number_format($row["REDUCTIONMONEY"]);
                $contents .= ",,,授業料支援(軽減)補助金(府県),④,\"". $reductionmoney ."円\"";
                $contents .= "\n\n";
            }
            $adjustMoney = number_format($row["ADJUSTMENT_MONEY"]);
            $contents .= ",,,就学支援奨学金(学校負担分),⑤,\"". $adjustMoney ."円\"";
            $contents .= "\n\n";

            $item = "";
            $fee = "";
            $note1 = "";
            $note2 = "";
            $total = "";
            if ($row["FRMID"] == "1") {
                if ($row["MONEY"] == "0") {
                    $item = "引落し額";
                    $note1 = "今回引落しはありません。";
                    $total = "0";
                } else {
                    $item = "引落し額";
                    $fee = "　　　　　　　　　（引落し手数料". $row["FEE"] ."円含む）";
                    $total = $row["TOTAL_MONEY"];
                }
            } else {
                $item = "差額";
                $note1 = "差額分は12月下旬に授業料登録口座に";
                $note2 = "振り込みます。今回引落しはありません。";
                $total = $row["TOTAL_MONEY"];
            }
            $total = number_format($total);
            if ($this->field["YOUSIKI"] == "1") {
                $contents .= ",,,". $item .",①-(②+③),\"". $total ."円\"";
            } else {
                $contents .= ",,,". $item .",①-(②+③+④+⑤),\"". $total ."円\"";
            }
            $contents .= "\n\n";
            if ($note1) {
                $contents .= ",,,". $note1;
                $contents .= "\n\n";
            } else {
                $contents .= ",,,". $fee;
                $contents .= "\n\n";
            }
            $contents .= ",,,". $note2;

            $contents .= "\n\n\n\n\n\n\n\n\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($data) {
            $filename = CTRL_YEAR."年度_授業料軽減決定通知書.csv";
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
