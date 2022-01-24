<?php

require_once('for_php7.php');


class knje372jModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;
    public $lastColumn;//CSVの最後の列の値

    public function knje372jModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function clean()
    {
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";
        //ヘッダ1
        $header1 = array();
        $header1["FACULTY_LIST_NAME"]       = "志望学部・学科・専攻";
        $header1["DEPARTMENT_LIST_NAME"]    = "";
        $header1["DEPARTMENT_LIST_CD"]      = "";
        $header1["JUKEN_NO"]                = "";
        $header1["NAME_KANA_S"]             = "氏名（カタカナ）";
        $header1["NAME_KANA_M"]             = "";
        $header1["BASE_DIV1"]               = "推薦基準の区分";
        $header1["BASE_DIV2"]               = "";
        $header1["BASE_DIV3"]               = "";
        $header1["REMARK"]                  = "備考";
        $header1[$this->lastColumn]         = $this->lastColumn;
        //ヘッダ2
        $header2 = array();
        $header2["FACULTY_LIST_NAME"]       = "学部";
        $header2["DEPARTMENT_LIST_NAME"]    = "学科（専攻）";
        $header2["DEPARTMENT_LIST_CD"]      = "学科コード";
        $header2["JUKEN_NO"]                = "受験番号";
        $header2["NAME_KANA_S"]             = "（姓）";
        $header2["NAME_KANA_M"]             = "（名）";
        $header2["BASE_DIV1"]               = "ア";
        $header2["BASE_DIV2"]               = "イ";
        $header2["BASE_DIV3"]               = "ウ";
        $header2["REMARK"]                  = "";
        $header2[$this->lastColumn]         = $this->lastColumn;

        //ヘッダ出力
        $contents .= implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        //データ出力
        $data = false;
        $query = knje372jQuery::getCsvList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";

            //氏名（カタカナ）(スペースで姓名を分ける)
            $name_kana = explode('　', $row["NAME_KANA"]);
            $row["NAME_KANA_S"] = (get_count($name_kana) > 0) ? $name_kana[0] : "";
            $row["NAME_KANA_M"] = (get_count($name_kana) > 1) ? $name_kana[1] : "";
            //推薦基準の区分
            $row["BASE_DIV1"] = ($row["BASE_DIV"] == "ア") ? "〇" : "";
            $row["BASE_DIV2"] = ($row["BASE_DIV"] == "イ") ? "〇" : "";
            $row["BASE_DIV3"] = ($row["BASE_DIV"] == "ウ") ? "〇" : "";

            foreach ($header2 as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = CTRL_YEAR."年度推薦名簿";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
