<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjp411Model extends Model
{
    public $field      = array();
    public $cmd;
    public $output;
    public $month;
    public $grade;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjp411Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->output = VARS::post("OUTPUT");
        $this->grade = VARS::post("GRADE");
        $this->month = VARS::post("MONTH");
        $this->kojinBetsu = VARS::post("KOJIN_BETSU");

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
                if ($this->kojinBetsu == "1") {
                    list($header_data, $retContents) = $this->getGradeSchCsv($db, $val);
                } else {
                    list($header_data, $retContents) = $this->getGradeCsv($db, $val);
                }
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

    public function getGradeSchCsv($db, $val)
    {
        $contents = ",,,,,,,授業料納入台帳(学年別)\n";
        $query = knjp411Query::getGradeHrName($this, $val);
        $subTaitle = $db->getOne($query);
        $contents .= ",学年　".$subTaitle."\n";
        $contents .= ",,,,就学支援金\n";

        $header = array("HR_NAME"                   => "年組",
                        "ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "PASSNO"                    => "認定番号",
                        "MONEY_DUE"                 => "納入必要額",
                        "REDUCTIONMONEY_C1"         => "4-6基準1",
                        "REDUCTION_ADD_MONEY_1"     => "4-6加算1",
                        "REDUCTIONMONEY_C2"         => "7-3基準2",
                        "REDUCTION_ADD_MONEY_2"     => "7-3加算2",
                        "TOTAL_MONEY"               => "基準計",
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
        $query = knjp411Query::getCsvData($this, $val);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $row["HR_NAME"].",";
            $contents .= $row["ATTENDNO"].",";
            $contents .= $row["NAME"].",";
            $contents .= $row["PASSNO"].",";
            //納入必要額
            $contents .= $row["MONEY_DUE"].",";
            $setTotal["MONEY_DUE"] += $row["MONEY_DUE"];
            //4-6基準1
            $contents .= $row["REDUCTIONMONEY_C1"].",";
            $setTotal["REDUCTIONMONEY_C1"] += $row["REDUCTIONMONEY_C1"];
            //4-6加算1
            $contents .= $row["REDUCTION_ADD_MONEY_1"].",";
            $setTotal["REDUCTION_ADD_MONEY_1"] += $row["REDUCTION_ADD_MONEY_1"];
            //7-3基準2
            $contents .= $row["REDUCTIONMONEY_C2"].",";
            $setTotal["REDUCTIONMONEY_C2"] += $row["REDUCTIONMONEY_C2"];
            //7-3加算2
            $contents .= $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["REDUCTION_ADD_MONEY_2"] += $row["REDUCTION_ADD_MONEY_2"];
            $setTotal["T_MONEY"] += $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"];
            //基準計
            $contents .= $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"].",";
            //加算計
            $contents .= $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["ADD_MONEY"] += $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            //合計
            $setGoukei = $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $setGoukei.",";
            $setTotal["TOTAL_REDUC_B"] += $setGoukei;
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
            $hikiotoshi = $row["MONEY_DUE"] - ($setGoukei + $row["REDUCTIONMONEY_B"] + $row["TOTAL_ADJUSTMENT_MONEY"] + $row["TOTAL_BURDEN_CHARGE"] + $row["SIJOGEN"] + $setSyougaku);
            $contents .= $hikiotoshi.",";
            $setTotal["HIKIOTOSI"] += $hikiotoshi;
            //振込・現金入金
            $contents .= $row["PAID_MONEY"].",";
            $setTotal["PAID_MONEY"] += $row["PAID_MONEY"];
            //差引納入額
            $contents .= $row["SASIHIKI"].",";
            $setTotal["SASIHIKI"] += $row["SASIHIKI"];
            //返戻・給付金
            $contents .= $row["REPAY_MONEY"].",";
            $setTotal["REPAY_MONEY"] += $row["REPAY_MONEY"];
            $contents .= $row["KAN_MI"].",";
            $contents .= $row["REMARK"].",";
            $contents .= $this->lastColumn."\n";
            $header_data = true;
        }
        $result->free();
        $contents .= "学年合計,,,,";
        $contents .= implode($setTotal, ",");
        $contents .= ",,,\n\n";

        return array($header_data, $contents);
    }

    public function getGradeCsv($db, $val)
    {
        $contents = ",,,,,,,授業料納入台帳(学年別)\n";
        $query = knjp411Query::getGradeHrName($this, $val);
        $subTaitle = $db->getOne($query);
        $contents .= ",学年　".$subTaitle."\n";
        $contents .= ",,,,就学支援金\n";

        $header = array("NO"                        => "No",
                        "HR_CLASS"                  => "クラス名",
                        "PASSNO"                    => "在籍人数",
                        "MONEY_DUE"                 => "納入必要額",
                        "REDUCTIONMONEY_C1"         => "4-6基準1",
                        "REDUCTION_ADD_MONEY_1"     => "4-6加算1",
                        "REDUCTIONMONEY_C2"         => "7-3基準2",
                        "REDUCTION_ADD_MONEY_2"     => "7-3加算2",
                        "TOTAL_MONEY"               => "基準計",
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
        $query = knjp411Query::getCsvData($this, $val);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        $no = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $no.",";
            $contents .= $row["HR_CLASS"].",";
            $contents .= $row["CNT"].",";
            $setTotal["CNT"] += $row["CNT"];
            //納入必要額
            $contents .= $row["MONEY_DUE"].",";
            $setTotal["MONEY_DUE"] += $row["MONEY_DUE"];
            //4-6基準1
            $contents .= $row["REDUCTIONMONEY_C1"].",";
            $setTotal["REDUCTIONMONEY_C1"] += $row["REDUCTIONMONEY_C1"];
            //4-6加算1
            $contents .= $row["REDUCTION_ADD_MONEY_1"].",";
            $setTotal["REDUCTION_ADD_MONEY_1"] += $row["REDUCTION_ADD_MONEY_1"];
            //7-3基準2
            $contents .= $row["REDUCTIONMONEY_C2"].",";
            $setTotal["REDUCTIONMONEY_C2"] += $row["REDUCTIONMONEY_C2"];
            //7-3加算2
            $contents .= $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["REDUCTION_ADD_MONEY_2"] += $row["REDUCTION_ADD_MONEY_2"];
            //基準計
            $contents .= $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["T_MONEY"] += $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"];
            //加算計
            $contents .= $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"].",";
            $setTotal["ADD_MONEY"] += $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            //合計
            $setGoukei = $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $setGoukei.",";
            $setTotal["TOTAL_REDUC_B"] += $setGoukei;
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
            //特待・奨学金 + 留学
            $setSyougaku = $row["SYOUGAKU"] + $row["RYUUGAKU"];
            $contents .= $setSyougaku.",";
            $setTotal["SYOUGAKU"] += $setSyougaku;
            //引落金額
            $hikiotoshi = $row["MONEY_DUE"] - ($setGoukei + $row["REDUCTIONMONEY_B"] + $row["TOTAL_ADJUSTMENT_MONEY"] + $row["TOTAL_BURDEN_CHARGE"] + $row["SIJOGEN"] + $setSyougaku);
            $contents .= $hikiotoshi.",";
            $setTotal["HIKIOTOSI"] += $hikiotoshi;
            //振込・現金入金
            $contents .= $row["PAID_MONEY"].",";
            $setTotal["PAID_MONEY"] += $row["PAID_MONEY"];
            //差引納入額
            $contents .= $row["SASIHIKI"].",";
            $setTotal["SASIHIKI"] += $row["SASIHIKI"];
            //返戻・給付金
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
        $query = knjp411Query::getGradeHrName($this, $val);
        $subTaitle = $db->getOne($query);

        $contents .= ",年組　".$subTaitle."\n";
        $contents .= ",,,,就学支援金\n";

        $header = array("ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "PASSNO"                    => "認定番号",
                        "MONEY_DUE"                 => "納入必要額",
                        "REDUCTIONMONEY_C1"         => "4-6基準1",
                        "REDUCTION_ADD_MONEY_1"     => "4-6加算1",
                        "REDUCTIONMONEY_C2"         => "7-3基準2",
                        "REDUCTION_ADD_MONEY_2"     => "7-3加算2",
                        "TOTAL_MONEY"               => "基準計",
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
        $query = knjp411Query::getCsvData($this, $val);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $row["ATTENDNO"].",";
            $contents .= $row["NAME"].",";
            $contents .= $row["PASSNO"].",";
            //納入必要額
            $contents .= $row["MONEY_DUE"].",";
            $setTotal["MONEY_DUE"] += $row["MONEY_DUE"];
            //4-6基準1
            $contents .= $row["REDUCTIONMONEY_C1"].",";
            $setTotal["REDUCTIONMONEY_C1"] += $row["REDUCTIONMONEY_C1"];
            //4-6加算1
            $contents .= $row["REDUCTION_ADD_MONEY_1"].",";
            $setTotal["REDUCTION_ADD_MONEY_1"] += $row["REDUCTION_ADD_MONEY_1"];
            //7-3基準2
            $contents .= $row["REDUCTIONMONEY_C2"].",";
            $setTotal["REDUCTIONMONEY_C2"] += $row["REDUCTIONMONEY_C2"];
            //7-3加算2
            $contents .= $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["REDUCTION_ADD_MONEY_2"] += $row["REDUCTION_ADD_MONEY_2"];
            $setTotal["T_MONEY"] += $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"];
            //基準計
            $contents .= $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"].",";
            //加算計
            $contents .= $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"].",";
            $setTotal["ADD_MONEY"] += $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            //合計
            $setGoukei = $row["REDUCTIONMONEY_C1"] + $row["REDUCTIONMONEY_C2"] + $row["REDUCTION_ADD_MONEY_1"] + $row["REDUCTION_ADD_MONEY_2"];
            $contents .= $setGoukei.",";
            $setTotal["TOTAL_REDUC_B"] += $setGoukei;
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
            $hikiotoshi = $row["MONEY_DUE"] - ($setGoukei + $row["REDUCTIONMONEY_B"] + $row["TOTAL_ADJUSTMENT_MONEY"] + $row["TOTAL_BURDEN_CHARGE"] + $row["SIJOGEN"] + $setSyougaku);
            $contents .= $hikiotoshi.",";
            $setTotal["HIKIOTOSI"] += $hikiotoshi;
            //振込・現金入金
            $contents .= $row["PAID_MONEY"].",";
            $setTotal["PAID_MONEY"] += $row["PAID_MONEY"];
            //差引納入額
            $contents .= $row["SASIHIKI"].",";
            $setTotal["SASIHIKI"] += $row["SASIHIKI"];
            //返戻・給付金
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
