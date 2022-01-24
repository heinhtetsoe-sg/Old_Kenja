<?php

require_once('for_php7.php');

class knjl433hModel extends model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $testdiv;
    public $totalcd;
    public $examtype;
    public $examno;
    public $examclass;
    public $csvInOutMode;
    public $includeHeader;

    public function init()
    {
        $this->year = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");
        $this->field["MOVE_ENTER"] = VARS::post("MOVE_ENTER");  //受験番号の開始

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->totalcd        = strlen(VARS::post("TOTALCD")) ? VARS::post("TOTALCD") : VARS::post("HID_TOTALCD");
        $this->examtype       = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");
        $this->examclass      = strlen(VARS::post("EXAM_CLASS")) ? VARS::post("EXAM_CLASS") : VARS::post("HID_EXAM_CLASS");
        $this->csvInOutMode   = strlen(VARS::post("CSV_IO")) ? VARS::post("CSV_IO") : VARS::post("HID_CSV_IO");
        $this->includeHeader  = strlen(VARS::post("HEADER")) ? VARS::post("HEADER") : VARS::post("HID_HEADER");

        $this->hid_receptno   = VARS::post("HID_RECEPTNO");

        if ($this->csvInOutMode == "") {
            //CSV入出力モード
            $this->csvInOutMode = 1;
        }
    }

    // 編集完了用のモデルを返します
    public function getUpdateModel()
    {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_receptno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];
            $this->field["SCORE"][$receptno] = VARS::post("SCORE"."-".$receptno);
            $this->field["JUDGEDIV"][$receptno] = VARS::post("JUDGEDIV"."-".$receptno);
        }
        $result = knjl433hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //CSV振舞い（インポート／エクスポート）
    public function performCSV()
    {
        switch ($this->csvInOutMode) {
            case "1":   //取込
                return false;
                break;
            case "2":   //書出
                return $this->downloadCSV();
            default:
                $this->setError(new PEAR_Error("未対応のCSV操作{$this->csvInOutMode}です"));
                break;
        }
        return false;
    }

    //CSVファイルダウンロード
    public function downloadCSV()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //対象レコード取得
        $query = knjl433hQuery::selectQuery($this);
        $result = $db->query($query);

        //データなし
        if ($result->numRows() == 0) {
            //DB切断
            Query::dbCheckIn($db);

            $this->setMessage("MSG303");
            return false;
        }

        //満点マスタより点数上限を取得
        $perfect = $db->getOne(knjl433hQuery::getPerfectAtClass($this));

        //CSVリソース
        $contents = "";

        //ヘッダ出力
        if ($this->includeHeader == "on") {
            $header = array();
            $header["ENTEXAMYEAR"]    = "入試年度";
            $header["APPLICANTDIV"]   = "入試制度";
            $header["TESTDIV"]        = "入試回数";
            $header["EXAM_TYPE"]      = "受験型";
            $header["RECEPTNO"]       = "受験番号";
            $header["NAME"]           = "氏名";
            $header["TESTSUBCLASSCD"] = "受験科目";
            $header["SCORE"]          = "得点";
            $contents .= implode(",", $header) . "\n";
        }

        //CSVデータ取得
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scoreRow = array();
            $scoreRow["ENTEXAMYEAR"] = $this->year;
            $scoreRow["APPLICANTDIV"] = $this->applicantdiv;
            $scoreRow["TESTDIV"] = $this->testdiv;
            $scoreRow["EXAM_TYPE"] = $this->examtype;
            $scoreRow["RECEPTNO"] = $row["RECEPTNO"];
            $scoreRow["NAME"] = $row["NAME"];
            $scoreRow["TESTSUBCLASSCD"] = $this->examclass;
            $scoreRow["SCORE"] = $row["SCORE"];

            //CSV書出し
            $contents .= implode(",", $scoreRow) . "\n";
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力前確認
        if (strlen($contents) < 1) {
            $this->setWarning("MSG303");
            return false;
        }

        //CSVファイル出力
        common::downloadContents($contents, "{$this->year}年度入試得点入力.csv", "SJIS-win");
        return true;
    }
}
