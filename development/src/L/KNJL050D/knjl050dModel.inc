<?php

require_once('for_php7.php');
class knjl050dModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examhallcd;
    public $testsubclasscd;
    public $s_examno;
    public $e_examno;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl050dModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhallcd);
            unset($this->testsubclasscd);
            unset($this->s_examno);
            unset($this->e_examno);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examhallcd       = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        $this->testsubclasscd   = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->s_examno         = strlen(VARS::post("S_EXAMNO")) ? VARS::post("S_EXAMNO") : VARS::post("HID_S_EXAMNO");
        $this->e_examno         = strlen(VARS::post("E_EXAMNO")) ? VARS::post("E_EXAMNO") : VARS::post("HID_E_EXAMNO");

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $this->score = array();
        $array = explode(",", $this->hid_examno);
        foreach ($array as $key => $examno) {
            $this->score[$examno]     = VARS::post("SCORE_".$examno);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //更新
        knjl050dQuery::getUpdateQuery($this);
        $this->setMessage("MSG201", "\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
    }

    //CSV出力
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]      = "入試年度";
        $header["TESTDIV"]          = "受験種別";
        $header["EXAMHALLCD"]       = "会場";
        $header["TESTSUBCLASSCD"]   = "入力教科";
        $header["EXAMNO"]           = "受験番号";
        $header["ATTEND"]           = "出欠";
        $header["SCORE"]            = "得点";
        $header[$this->lastColumn]  = $this->lastColumn;

        //ヘッダ出力
        $sep = "";
        foreach ($header as $field => $label) {
            $contents .= $sep.$label;
            $sep = ",";
        }
        $contents .= "\n";

        //データ出力
        $data = false;
        $query = knjl050dQuery::selectQuery($this, "csv");
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
            $title = $this->ObjYear."年度"."得点入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
