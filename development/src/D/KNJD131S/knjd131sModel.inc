<?php

require_once('for_php7.php');

class knjd131sModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;
    var $hid_recordnamelist;
    var $hid__recactvidlist;
    var $record = array();
    var $idlist = array();

    function knjd131sModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "REMARK1"          =>  VARS::post("REMARK1"),
                             "COMMUNICATION"    =>  VARS::post("COMMUNICATION"),
                             "ATTENDREC_REMARK" =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );


        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade = VARS::get("GRADE");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }

        $this->hid__recactvidlist = VARS::post("HID_RECACTVIDLIST");
        $this->hid_recordnamelist = VARS::post("HID_RECACTVNAMELIST");
        if ($this->hid_recordnamelist) {
            $this->idlist = explode(",", $this->hid_recordnamelist);
            //生徒の記録
            $this->record["RECORD"] = array();
            for ($i = 0; $i < get_count($this->idlist); $i++) {
                $tmpval = $i + 1;
                $ival = sprintf("%02d", $tmpval);
                $this->record["RECORD"][$ival] = VARS::post($this->idlist[$i]);
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_J"]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 20;
            $this->getPro["REMARK1"]["gyou"] = 3;
        }
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 20;
            $this->getPro["COMMUNICATION"]["gyou"] = 9;
        }

        //固定設定
        $this->getPro["ATTENDREC_REMARK"]["moji"] = 10;
        $this->getPro["ATTENDREC_REMARK"]["gyou"] = 4;

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(その他は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }

        return true;
    }

    //更新用モデル
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    //更新
    function &update() {
        knjd131sQuery::getUpdateQuery($this);
        if ($this->hid_recordnamelist) {
            knjd131sQuery::getInsertQuery2($this);
        }
        return ;
    }
}
?>
