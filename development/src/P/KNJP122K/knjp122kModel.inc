<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjp122kModel extends Model
{
    public $field      = array();
    public $cmd;
    public $output;
    public $grade;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjp122kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->output       = VARS::post("OUTPUT");
        $this->grade        = VARS::post("GRADE");
        $this->kojinBetsu   = VARS::post("KOJIN_BETSU");

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

        $flg = "";
        if ($this->output == "1") {
            if ($this->kojinBetsu == "1") {
                $flg = "1";
            } else {
                $flg = "2";
            }
        } else {
            $flg = "3";
        }

        foreach ($this->hr_class as $key => $val) {
            list($header_data, $retContents) = $this->getCsv($db, $val, $flg);

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

    //CSV出力(1：学年別【個人別】、2：学年、3：クラス)
    public function getCsv($db, $val, $flg)
    {
        $title = $sTitle = "";
        if ($flg == '3') {
            $title  = "クラス別";
            $sTitle = "年組";
        } else {
            $title  = "学年別";
            $sTitle = "学年";
        }
        $contents = ",,,授業料納入台帳({$title})\n";
        $query = knjp122kQuery::getGradeHrName($this, $val);
        $subTaitle = $db->getOne($query);
        $contents .= ",{$sTitle}　".$subTaitle."\n";

        $header = array();
        if ($flg == '1') {
            $header["HR_NAME"] = "年組";
        }
        if ($flg == '2') {
            $header["NO"]          = "No";
            $header["HR_CLASS"]    = "クラス名";
            $header["CNT"]         = "在籍人数";
        } else {
            $header["ATTENDNO"]    = "出席番号";
            $header["NAME"]        = "氏名";
        }
        $header["MONEY_DUE"]       = "納入必要額";
        $header["NYUKIN"]          = "入金額";
        $header["SIEN_KANPU"]      = "支援金還付額";
        $header["SASIHIKI"]        = "差引納入額";
        if ($flg != '2') {
            $header["REMARK"]      = "備考";
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $contents .= implode($header, ",")."\n";

        $header_data = false;

        //データ
        $query = knjp122kQuery::getCsvData($this, $val);

        //SQL文発行
        $result = $db->query($query);
        $setTotal = array();
        $no = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if ($flg == '1') {
                $contents .= $row["HR_NAME"].",";
            }
            if ($flg == '2') {
                $contents .= $no.",";
                $contents .= $row["HR_CLASS"].",";
                $contents .= $row["CNT"].",";
                $setTotal["CNT"] += $row["CNT"];
            } else {
                $contents .= $row["ATTENDNO"].",";
                $contents .= $row["NAME"].",";
            }

            //納入必要額
            $contents .= $row["MONEY_DUE"].",";
            $setTotal["MONEY_DUE"] += $row["MONEY_DUE"];

            //入金額
            $contents .= $row["NYUKIN"].",";
            $setTotal["NYUKIN"] += $row["NYUKIN"];

            //支援金還付額
            $contents .= $row["SIEN_KANPU"].",";
            $setTotal["SIEN_KANPU"] += $row["SIEN_KANPU"];

            //差引納入額
            $sasihiki = $row["NYUKIN"] - $row["SIEN_KANPU"];
            $contents .= $sasihiki.",";
            $setTotal["SASIHIKI"] += $sasihiki;

            if ($flg != '2') {
                //備考
                $contents .= $row["REMARK"].",";
            }

            $contents .= $this->lastColumn."\n";
            $header_data = true;
            $no++;
        }
        $result->free();
        if ($flg == "3") {
            $contents .= "クラス合計,,";
        } else {
            $contents .= "学年合計,,";
            if ($flg == "1") {
                $contents .= ",";
            }
        }
        $contents .= implode($setTotal, ",");
        $contents .= ",,,\n\n";

        return array($header_data, $contents);
    }
}
