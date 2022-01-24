<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjp410Model extends Model
{
    public $field      = array();
    public $cmd;
    public $output;
    public $month;
    public $grade;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjp410Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->cmd    = VARS::request("cmd");
        $this->output = VARS::post("OUTPUT");
        $this->grade  = VARS::post("GRADE");
        $this->month  = VARS::post("MONTH");

        $this->selectdata = VARS::post("selectdata");
        if ($this->selectdata) {
            $this->hr_class = explode(",", $this->selectdata);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        $contents = "";
        //DB接続
        $db = Query::dbCheckOut();
        foreach ($this->hr_class as $key => $val) {
            if ($this->output == "1") {
                list($header_data, $retContents) = $this->getGradeCsv($db, $val);
            } else {
                list($header_data, $retContents) = $this->getHrClassCsv($db, $val);
            }
            $contents .= $retContents;
        }
        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->output == "1") {
                $couse = "授業料納入台帳(学年別)";
            } else {                                //エラー以外
                $couse = "授業料納入台帳(クラス別)";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function getGradeCsv($db, $val)
    {
        $contents = ",,,,,,,授業料納入台帳(学年別)\n";
        $query = knjp410Query::getGradeHrName($this, $val);
        $subTaitle = $db->getOne($query);
        $contents .= ",学年　".$subTaitle."\n";
        $contents .= ",,,,就学支援金\n";

        $header = array("NO"                        => "No",
                        "HR_CLASS"                  => "クラス名",
                        "PASSNO"                    => "在籍人数",
                        "MONEY_DUE"                 => "納入必要額",
                        "REDUCTIONMONEY_C"          => "一律",
                        "REDUCTION_ADD_MONEY_1"     => "4-6加算1",
                        "REDUCTION_ADD_MONEY_2"     => "7-3加算2",
                        "TOTAL_ADD_MONEY"           => "加算計",
                        "TOTAL_REDUCTIONMONEY"      => "合計",
                        "REDUCTIONMONEY_B"          => "府県補助金",
                        "TOTAL_ADJUSTMENT_MONEY"    => "調整金",
                        "TOTAL_BURDEN_CHARGE"       => "学校負担金",
                        "SIJOGEN"                   => "教職員子女減",
                        "SYOUGAKU"                  => "特待・奨学金",
                        "HIKIOTOSI"                 => "引落金額",
                        "PAID_MONEY"                => "振込・現金入金",
                        "SASIHIKI"                  => "差引納入額",
                        "REPAY_MONEY"               => "返戻・給付金",
                        $this->lastColumn           => $this->lastColumn);

        $contents .= implode($header, ",")."\n";

        $header_data = false;
        //データ
        $query = knjp410Query::getCsvData($this, $val);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        $no = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["REDUCTIONMONEY_C"] = $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"];
            $contents .= $no.",";
            $contents .= $row["HR_CLASS"].",";
            $contents .= $row["CNT"].",";
            $setTotal["CNT"] += $row["CNT"];
            $contents .= $row["MONEY_DUE"].",";
            $setTotal["MONEY_DUE"] += $row["MONEY_DUE"];
            $contents .= $row["REDUCTIONMONEY_C"].",";
            $setTotal["REDUCTIONMONEY_C"] += $row["REDUCTIONMONEY_C"];
            $contents .= $row["REDUCTION_ADD_MONEY_1"].",";
            $setTotal["REDUCTION_ADD_MONEY_1"] += $row["REDUCTION_ADD_MONEY_1"];
            $contents .= $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["REDUCTION_ADD_MONEY_2"] += $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["ADD_MONEY"] += $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $row["REDUCTIONMONEY_C"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["TOTAL_REDUC_B"] += $row["REDUCTIONMONEY_C"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $row["REDUCTIONMONEY_B"].",";
            $setTotal["REDUCTIONMONEY_B"] += $row["REDUCTIONMONEY_B"];
            $contents .= $row["TOTAL_ADJUSTMENT_MONEY"].",";
            $setTotal["TOTAL_ADJUSTMENT_MONEY"] += $row["TOTAL_ADJUSTMENT_MONEY"];
            $contents .= $row["TOTAL_BURDEN_CHARGE"].",";
            $setTotal["TOTAL_BURDEN_CHARGE"] += $row["TOTAL_BURDEN_CHARGE"];
            $contents .= $row["SIJOGEN"].",";
            $setTotal["SIJOGEN"] += $row["SIJOGEN"];
            $contents .= $row["SYOUGAKU"].",";
            $setTotal["SYOUGAKU"] += $row["SYOUGAKU"];
            $contents .= $row["HIKIOTOSI"].",";
            $setTotal["HIKIOTOSI"] += $row["HIKIOTOSI"];
            $contents .= $row["PAID_MONEY"].",";
            $setTotal["PAID_MONEY"] += $row["PAID_MONEY"];
            $contents .= $row["SASIHIKI"].",";
            $setTotal["SASIHIKI"] += $row["SASIHIKI"];
            $contents .= $row["REPAY_MONEY"].",";
            $setTotal["REPAY_MONEY"] += $row["REPAY_MONEY"];
            $contents .= $this->lastColumn."\n";
            $header_data = true;
            $no++;
        }
        $result->free();
        $contents .= "学年計,,";
        $contents .= implode($setTotal, ",") ."\n\n";

        return array($header_data, $contents);
    }

    public function getHrClassCsv($db, $val)
    {
        $contents = ",,,,,,,授業料納入台帳(クラス別)\n";
        $query = knjp410Query::getGradeHrName($this, $val);
        $subTaitle = $db->getOne($query);

        $contents .= ",年組　".$subTaitle."\n";
        $contents .= ",,,,就学支援金\n";

        $header = array("ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "PASSNO"                    => "認定番号",
                        "MONEY_DUE"                 => "納入必要額",
                        "REDUCTIONMONEY_C"          => "一律",
                        "REDUCTION_ADD_MONEY_1"     => "4-6加算1",
                        "REDUCTION_ADD_MONEY_2"     => "7-3加算2",
                        "TOTAL_ADD_MONEY"           => "加算計",
                        "TOTAL_REDUCTIONMONEY"      => "合計",
                        "REDUCTIONMONEY_B"          => "府県補助金",
                        "TOTAL_ADJUSTMENT_MONEY"    => "調整金",
                        "TOTAL_BURDEN_CHARGE"       => "学校負担金",
                        "SIJOGEN"                   => "教職員子女減",
                        "SYOUGAKU"                  => "特待・奨学金",
                        "HIKIOTOSI"                 => "引落金額",
                        "PAID_MONEY"                => "振込・現金入金",
                        "SASIHIKI"                  => "差引納入額",
                        "REPAY_MONEY"               => "返戻・給付金",
                        "KAN_MI"                    => "完/未",
                        "REMARK"                    => "備考",
                        $this->lastColumn           => $this->lastColumn);

        $contents .= implode($header, ",")."\n";

        $header_data = false;
        //データ
        $query = knjp410Query::getCsvData($this, $val);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["REDUCTIONMONEY_C"] = $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"];
            $contents .= $row["ATTENDNO"].",";
            $contents .= $row["NAME"].",";
            $contents .= $row["PASSNO"].",";
            $contents .= $row["MONEY_DUE"].",";
            $setTotal["MONEY_DUE"] += $row["MONEY_DUE"];
            $contents .= $row["REDUCTIONMONEY_C"].",";
            $setTotal["REDUCTIONMONEY_C"] += $row["REDUCTIONMONEY_C"];
            $contents .= $row["REDUCTION_ADD_MONEY_1"].",";
            $setTotal["REDUCTION_ADD_MONEY_1"] += $row["REDUCTION_ADD_MONEY_1"];
            $contents .= $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["REDUCTION_ADD_MONEY_2"] += $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["ADD_MONEY"] += $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $row["REDUCTIONMONEY_C"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["TOTAL_REDUC_B"] += $row["REDUCTIONMONEY_C"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $row["REDUCTIONMONEY_B"].",";
            $setTotal["REDUCTIONMONEY_B"] += $row["REDUCTIONMONEY_B"];
            $contents .= $row["TOTAL_ADJUSTMENT_MONEY"].",";
            $setTotal["TOTAL_ADJUSTMENT_MONEY"] += $row["TOTAL_ADJUSTMENT_MONEY"];
            $contents .= $row["TOTAL_BURDEN_CHARGE"].",";
            $setTotal["TOTAL_BURDEN_CHARGE"] += $row["TOTAL_BURDEN_CHARGE"];
            $contents .= $row["SIJOGEN"].",";
            $setTotal["SIJOGEN"] += $row["SIJOGEN"];
            $contents .= $row["SYOUGAKU"].",";
            $setTotal["SYOUGAKU"] += $row["SYOUGAKU"];
            $contents .= $row["HIKIOTOSI"].",";
            $setTotal["HIKIOTOSI"] += $row["HIKIOTOSI"];
            $contents .= $row["PAID_MONEY"].",";
            $setTotal["PAID_MONEY"] += $row["PAID_MONEY"];
            $contents .= $row["SASIHIKI"].",";
            $setTotal["SASIHIKI"] += $row["SASIHIKI"];
            $contents .= $row["REPAY_MONEY"].",";
            $setTotal["REPAY_MONEY"] += $row["REPAY_MONEY"];
            $contents .= $row["KAN_MI"].",";
            $contents .= $row["REMARK"].",";
            $contents .= $this->lastColumn."\n";
            $header_data = true;
        }
        $result->free();
        $contents .= "クラス合計,,,";
        $contents .= implode($setTotal, ",");
        $contents .= ",,,\n\n";

        return array($header_data, $contents);
    }
}
