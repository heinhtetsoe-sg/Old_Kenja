<?php

require_once('for_php7.php');

class knje373gModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;
    public $lastColumn;//CSVの最後の列の値

    public function knje373gModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "OUTDIV"       =>  VARS::post("OUTDIV"),
                             "YEARS"        =>  VARS::post("YEARS")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //CSV出力
    public function getDownloadModel()
    {
        $lf = "\n";
        $contents = "";

        //初期化
        $header = array();

        if ($this->field["OUTDIV"] == "1" || $this->field["OUTDIV"] == "2") {
            $header["SCHOOL_CD"]           = "大学コード";
            $header["SCHOOL_NAME"]         = "大学名";
            $header["SCHOOL_GROUP"]        = "学校系列コード";
            $header["SCHOOL_GROUP_NAME"]   = "学校系列名";
        } elseif ($this->field["OUTDIV"] == "3") {
            $header["COMPANY_CD"]          = "会社コード";
            $header["COMPANY_NAME"]        = "会社名";
            $header["KINMUCHI"]            = "勤務地";
        }

        $year = $this->field["YEAR"];
        for ($i = 1; $i <= $this->field["YEARS"]; $i++) {
            $header[$year."_GK_SUM"]       = $year."年度_合算";
            $header[$year."_GENEKI_CNT"]   = $year."年度_現役";
            $header[$year."_KISOTU_CNT"]   = $year."年度_既卒";
            $year--;
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $contents .= implode(",", $header).$lf;

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $data = false;
        $dataTemp = array();

        $year = $this->field["YEAR"];
        for ($i = 1; $i <= $this->field["YEARS"]; $i++) {
            $query = knje373gQuery::getCsvQuery($this, $year);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",", ".", $row);

                if ($this->field["OUTDIV"] == "1" || $this->field["OUTDIV"] == "2") {
                    $dataTemp[$row["SCHOOL_CD"]]["SCHOOL_CD"]         = $row["SCHOOL_CD"];
                    $dataTemp[$row["SCHOOL_CD"]]["SCHOOL_NAME"]       = $row["SCHOOL_NAME"];
                    $dataTemp[$row["SCHOOL_CD"]]["SCHOOL_GROUP"]      = $row["SCHOOL_GROUP"];
                    $dataTemp[$row["SCHOOL_CD"]]["SCHOOL_GROUP_NAME"] = $row["SCHOOL_GROUP_NAME"];
                    $dataTemp[$row["SCHOOL_CD"]][$year."_GK_SUM"]     = $row["GK_SUM"];
                    $dataTemp[$row["SCHOOL_CD"]][$year."_GENEKI_CNT"] = $row["GENEKI_CNT"];
                    $dataTemp[$row["SCHOOL_CD"]][$year."_KISOTU_CNT"] = $row["KISOTU_CNT"];
                } elseif ($this->field["OUTDIV"] == "3") {
                    if (mb_strpos($row["SHUSHOKU_ADDR"], "福岡") !== false) {
                        $row["KINMUCHI"] = "内";
                    } else {
                        $row["KINMUCHI"] = "外";
                    }
                    $dataTemp[$row["COMPANY_CD"]]["COMPANY_CD"]        = $row["COMPANY_CD"];
                    $dataTemp[$row["COMPANY_CD"]]["COMPANY_NAME"]      = $row["COMPANY_NAME"];
                    $dataTemp[$row["COMPANY_CD"]]["KINMUCHI"]          = $row["KINMUCHI"];
                    $dataTemp[$row["COMPANY_CD"]][$year."_GK_SUM"]     = $row["GK_SUM"];
                    $dataTemp[$row["COMPANY_CD"]][$year."_GENEKI_CNT"] = $row["GENEKI_CNT"];
                    $dataTemp[$row["COMPANY_CD"]][$year."_KISOTU_CNT"] = $row["KISOTU_CNT"];
                }
            }
            $result->free();
            $year--;
        }

        foreach ($dataTemp as $key => $val) {
            //ヘッダー順で出力
            $sep = "";
            foreach ($header as $hkey => $hval) {
                if (strpos($hkey, "_GK_SUM") !== false
                    || strpos($hkey, "_GENEKI_CNT") !== false
                    || strpos($hkey, "_KISOTU_CNT") !== false) {
                    $dataTemp[$key][$hkey] = ($dataTemp[$key][$hkey]) ? $dataTemp[$key][$hkey] : 0;
                }
                //データセット
                $contents .= $sep.$dataTemp[$key][$hkey];
                $sep = ",";
            }

            $contents .= $this->lastColumn.$lf;
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            if ($this->field["OUTDIV"] == "1") {
                $title = "進学実績";
            } elseif ($this->field["OUTDIV"] == "2") {
                $title = "専門各種学校";
            } elseif ($this->field["OUTDIV"] == "3") {
                $title = "内定企業";
            }
            $filename = sprintf("%s年度_%s.csv", $this->field["YEAR"], $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
