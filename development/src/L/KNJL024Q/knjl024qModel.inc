<?php

require_once('for_php7.php');

class knjl024qModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl024qModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;

        //フィールド
        $this->field = array("REMARK1"              =>  VARS::post("REMARK1"),                  //活動実績1
                             "REMARK2"              =>  VARS::post("REMARK2"),                  //活動実績2
                             "REMARK3"              =>  VARS::post("REMARK3"),                  //活動実績3
                             "REMARK9"              =>  VARS::post("REMARK9"),                  //特記事項
                            );

        //テキストエリア（文字数・行数）
        $this->TextSize["REMARK1"] = array("moji" => 25, "gyo" => 4);
        $this->TextSize["REMARK2"] = array("moji" => 25, "gyo" => 4);
        $this->TextSize["REMARK3"] = array("moji" => 25, "gyo" => 4);
        $this->TextSize["REMARK9"] = array("moji" => 25, "gyo" => 2);
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->TextSize["REMARK1"]["moji"] * 2)) > $this->TextSize["REMARK1"]["gyo"]) {
            $this->setWarning("MSG915","活動実績1は".$this->TextSize["REMARK1"]["gyo"]."行までです。");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->TextSize["REMARK2"]["moji"] * 2)) > $this->TextSize["REMARK2"]["gyo"]) {
            $this->setWarning("MSG915","活動実績2は".$this->TextSize["REMARK2"]["gyo"]."行までです。");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], ($this->TextSize["REMARK3"]["moji"] * 2)) > $this->TextSize["REMARK3"]["gyo"]) {
            $this->setWarning("MSG915","活動実績3は".$this->TextSize["REMARK3"]["gyo"]."行までです。");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK9"], ($this->TextSize["REMARK9"]["moji"] * 2)) > $this->TextSize["REMARK9"]["gyo"]) {
            $this->setWarning("MSG915","特記事項は".$this->TextSize["REMARK9"]["gyo"]."行までです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl024qQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>