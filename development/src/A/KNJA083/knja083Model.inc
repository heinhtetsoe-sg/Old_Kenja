<?php

require_once('for_php7.php');
class knja083Model extends Model
{
    public $field = array();
    public $cmd;
    public $new_year;
    public $new_semester;
    public $new_semestername;
    public $lastColumn;//CSVの最後の列の値

    public function knja083Model()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),
                             "SEMESTER"         =>  VARS::post("SEMESTER"),
                             "SEMESTERNAME"     =>  VARS::post("SEMESTERNAME"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "S_GRADE"          =>  VARS::post("S_GRADE")
                            );

        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["COURSE"]); $i++) {
            $this->fields["START_CLASS"][$i]    = VARS::post("START_CLASS:".$this->data["COURSE"][$i]);
            $this->fields["CLASS_CNT"][$i]      = VARS::post("CLASS_CNT:".$this->data["COURSE"][$i]);
            $this->fields["TARGET_CLASS"][$i]   = VARS::post("TARGET_CLASS:".$this->data["COURSE"][$i]);
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

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //割り振り処理実行
    public function &getExecuteModel()
    {
        if ($this->validateInput()) {
            knja083Query::getExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //CSV出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["YEAR"]                 = "年度";
        $header["SCHREGNO"]             = "学籍番号";
        $header["SEMESTER"]             = "学期";
        $header["NAME"]                 = "氏名漢字";
        $header["NAME_KANA"]            = "氏名かな";
        $header["SEX"]                  = "性別";
        $header["GRADE"]                = "学年";
        $header["HR_CLASS"]             = "組";
        $header["ATTENDNO"]             = "出席番号";
        $header["COURSECD"]             = "課程コード";
        $header["MAJORCD"]              = "学科コード";
        $header["COURSECODE"]           = "コースコード";
        $header["REMAINGRADE_FLG"]      = "留年フラグ";
        $header["OLD_SCHREGNO"]         = "旧学籍番号";
        $header["OLD_GRADE"]            = "旧学年";
        $header["OLD_HR_CLASS"]         = "旧組";
        $header["OLD_ATTENDNO"]         = "旧出席番号";
        $header["SCORE"]                = "成績";
        $header[$this->lastColumn]      = $this->lastColumn;
        if (!$this->field["S_GRADE"]) {
            $header["VALUE_AVG"]            = "評定平均";
            $header["SCORE_AVG"]            = "成績平均";
            $header["COURSE"]               = "履修パターンコード";
            $header["COURSENAME"]           = "履修パターン名称";
        }

        //ヘッダ出力
        $sep = "";
        foreach ($header as $field => $label) {
            $contents .= $sep.$label;
            $sep = ",";
        }
        $contents .= "\n";

        //データ出力
        $data = false;
        $query = knja083Query::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
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
            $title = $this->field["YEAR"]."年度".$this->field["SEMESTERNAME"]."クラス分けCSV";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
