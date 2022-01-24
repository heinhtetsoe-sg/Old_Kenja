<?php

require_once('for_php7.php');

class knjl051nModel extends model
{
    public $field      = array();
    public $cmd;
    public $applicantdiv;
    public $appHold;
    public $testdiv;
    public $exam_type;
    public $examno;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl051nModel()
    {
    }

    public function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");
        $this->field = array("MOVE_ENTER"       =>  VARS::post("MOVE_ENTER")
                            );   //受験番号の開始
        
        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->totalcd        = strlen(VARS::post("TOTALCD")) ? VARS::post("TOTALCD") : VARS::post("HID_TOTALCD");
        
        $this->exam_type      = "1";
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");//"RECEPTNO"-"COUNT"で取得
        
        //面接評価を受験番号ごとに配置
        $this->interView_Value = array();
        $this->interView_Remark = array();
        $this->interView_Value2 = array();
        $this->interView_Remark2 = array();
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $this->interView_Value[$receptno_array[1]]   = VARS::post("INTERVIEW_VALUE-".$receptno_array[1]);
            $this->interView_Remark[$receptno_array[1]]  = VARS::post("INTERVIEW_REMARK-".$receptno_array[1]);
            $this->interView_Value2[$receptno_array[1]]  = VARS::post("INTERVIEW_VALUE2-".$receptno_array[1]);
            $this->interView_Remark2[$receptno_array[1]] = VARS::post("INTERVIEW_REMARK2-".$receptno_array[1]);
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

    //受験番号をセット
    public function &getMainModel()
    {
        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            if (strlen($this->interView_Value[$receptno_array[1]]) > 1) {
                $this->setWarning("MSG914", "面接評価は1バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if (strlen($this->interView_Value2[$receptno_array[1]]) > 1) {
                $this->setWarning("MSG914", "音読評価は1バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if (strlen($this->interView_Remark[$receptno_array[1]]) > 90) {
                $this->setWarning("MSG914", "面接は90バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if (strlen($this->interView_Remark2[$receptno_array[1]]) > 90) {
                $this->setWarning("MSG914", "音読は90バイトまでです。(受験番号:".$receptno_array[0].")");
                return false;
            }
        }
        
        knjl051nQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダデータ
        $header = array();
        $header["RECEPTNO"]                 = "受験番号";
        $header["NAME"]                     = "氏名";
        $header["FINSCHOOL_NAME"]           = "中学校名";
        $header["REMARK2"]                  = "備考2";
        $header["INTERVIEW_VALUE"]          = "面接評価";
        $header["INTERVIEW_REMARK"]         = "面接";
        $header["INTERVIEW_VALUE2"]         = "音読評価";
        $header["INTERVIEW_REMARK2"]        = "音読";
        $header[$this->lastColumn]          = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = true;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        //データ
        $query = knjl051nQuery::selectQuery($this, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row[$this->lastColumn] = $this->lastColumn;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        //入試区分
        $l004Row = $db->getRow(knjl051nQuery::getNameMst("L004", $this->ObjYear, $this->testdiv), DB_FETCHMODE_ASSOC);
        //志望区分
        $courseRow = $db->getRow(knjl051nQuery::getEntExamCourse($this, $this->totalcd), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "面接結果CSV"."_".$l004Row["CSV_NAME"]."_".$courseRow["CSV_NAME"];
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
