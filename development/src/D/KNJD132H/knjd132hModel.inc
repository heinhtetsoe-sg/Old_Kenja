<?php

require_once('for_php7.php');

class knjd132hModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;

    function knjd132hModel() {
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
            unset($this->schKind);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),         //学期
                             "REMARK1_01_01"        =>  VARS::post("REMARK1_01_01"),    //委員会・係・部活動の成果
                             "REMARK1_01_02"        =>  VARS::post("REMARK1_01_02"),    //資格取得・検定・表彰など
                             "REMARK1_02_01"        =>  VARS::post("REMARK1_02_01"),    //研究活動テーマ
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),    //通信欄
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade    = VARS::get("GRADE");
            $db = Query::dbCheckOut();
            $this->schKind      = $db->getOne(knjd132hQuery::getSchkind($this->exp_grade));
            Query::dbCheckIn($db);
        }
        $this->schKind = ($this->schKind != "") ? $this->schKind: "J";

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

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //委員会・係・部活動の成果
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$this->schKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$this->schKind]);
            $this->getPro["REMARK1_01_01"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1_01_01"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1_01_01"]["moji"] = 30;
            if ($this->schKind == "J") {
                $this->getPro["REMARK1_01_01"]["gyou"] = 7;
            } else {
                $this->getPro["REMARK1_01_01"]["gyou"] = 5;
            }
        }

        //資格取得・検定・表彰など
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE_".$this->schKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE_".$this->schKind]);
            $this->getPro["REMARK1_01_02"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1_01_02"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1_01_02"]["moji"] = 20;
            if ($this->schKind == "J") {
                $this->getPro["REMARK1_01_02"]["gyou"] = 7;
            } else {
                $this->getPro["REMARK1_01_02"]["gyou"] = 5;
            }
        }

        //研究活動テーマ
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_".$this->schKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_".$this->schKind]);
            $this->getPro["REMARK1_02_01"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1_02_01"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1_02_01"]["moji"] = 23;
            $this->getPro["REMARK1_02_01"]["gyou"] = 5;
        }

        //通信欄
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->schKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->schKind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 30;
            if ($this->schKind == "J") {
                $this->getPro["COMMUNICATION"]["gyou"] = 3;
            } else {
                $this->getPro["COMMUNICATION"]["gyou"] = 2;
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["REMARK1_01_01"], ($this->getPro["REMARK1_01_01"]["moji"] * 2)) > $this->getPro["REMARK1_01_01"]["gyou"]) {
            $this->setWarning("MSG915","(委員会・係・部活動の成果は{$this->getPro["REMARK1_01_01"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1_01_02"], ($this->getPro["REMARK1_01_02"]["moji"] * 2)) > $this->getPro["REMARK1_01_02"]["gyou"]) {
            $this->setWarning("MSG915","(資格取得・検定・表彰などは{$this->getPro["REMARK1_01_02"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1_02_01"], ($this->getPro["REMARK1_02_01"]["moji"] * 2)) > $this->getPro["REMARK1_02_01"]["gyou"]) {
            $this->setWarning("MSG915","(研究活動テーマは{$this->getPro["REMARK1_02_01"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(通信欄は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd132hQuery::getInsertQuery($this);
        return ;
    }
}
?>
