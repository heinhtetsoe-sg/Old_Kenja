<?php

require_once('for_php7.php');

class knjl050gModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examhallcd;
    public $testsubclasscd;
    public $ObjYear;
    public $isKeiai;
    public $isKasiwara;
    public $special_reason_div;
    public $lastColumn;//CSVの最後の列の値

    public function knjl050gModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        //学校コードを取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjl050gQuery::getNameMstZ010();
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";

        $this->applicantdiv     = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        if ($this->isKeiai == "1") {
            $this->exam_type        = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");
        }
        $this->testsubclasscd     = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examhallcd         = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->tmp_score        = VARS::post("SCORE");    //配列
        $this->hid_receptno     = VARS::post("HID_RECEPTNO");

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
        $this->score = array();

        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->score[$receptno]  = $this->tmp_score[$key];
        }

        //更新
        $result = knjl050gQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201", "\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
        }
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //CSV出力ファイル名
        $setTitle = $this->ObjYear."年度入試_得点データ(全科目)";

        //試験科目名
        $subclassList = array();
        $result = $db->query(knjl050gQuery::getTestSubclasscd($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclassList[$row["VALUE"]] = $row["CSV_NAME"];
        }
        $result->free();

        //CSVヘッダ名
        $csvhead = array();
        $csvhead["ENTEXAMYEAR"]         = "入試年度";
        $csvhead["APPLICANTDIV"]        = "入試制度コード";
        $csvhead["APPLICANTDIV_NAME"]   = "入試制度名";
        $csvhead["TESTDIV"]             = "入試区分コード";
        $csvhead["TESTDIV_NAME"]        = "入試区分名";
        $csvhead["RECEPTNO"]            = "受験番号";
        foreach ($subclassList as $subclassCd => $subclassName) {
            $csvhead["TESTSUBCLASSCD".$subclassCd]      = "試験科目コード";
            $csvhead["TESTSUBCLASSCD_NAME".$subclassCd] = "試験科目名";
            $csvhead["SCORE".$subclassCd]               = "得点";
        }
        $contents .= implode($csvhead, ",") ."\n";

        //書き出し用CSVデータ
        $data = false;
        $result = $db->query(knjl050gQuery::getOutputQueryCsv($this, $subclassList));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "", str_replace("\r", "", str_replace("\n", "", $row)));   //データ内に改行があればブランクに置換

            foreach ($subclassList as $subclassCd => $subclassName) {
                if ($row["ATTEND_FLG".$subclassCd] === '0') {
                    $row["SCORE".$subclassCd] = '*';
                }
            }

            $sep = "";
            foreach ($csvhead as $key => $label) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $filename = sprintf("%s.csv", $setTitle);
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
