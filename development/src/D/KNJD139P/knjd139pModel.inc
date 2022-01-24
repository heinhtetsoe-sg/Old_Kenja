<?php

require_once('for_php7.php');
class knjd139pModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $Field = array();
    var $control;

    function knjd139pModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }
        
        //学期用
        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),
                             "REMARK2"              =>  VARS::post("REMARK2"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );
        //通年用
        $this->Field = array("TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "REMARK1"              =>  VARS::post("REMARK1"),
                             "REMARK3"              =>  VARS::post("REMARK3"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //テキストエリアサイズ設定
        //委員会
        $this->getPro["REMARK1"]["moji"] = 15;
        $this->getPro["REMARK1"]["gyou"] = 1;
        
        //係り
        $this->getPro["REMARK2"]["moji"] = 10;
        $this->getPro["REMARK2"]["gyou"] = 1;
        
        //特別活動
        $this->getPro["REMARK3"]["moji"] = 42;
        $this->getPro["REMARK3"]["gyou"] = 4;

        //明小タイム
        $this->getPro["TOTALSTUDYTIME"]["moji"] = 18;
        $this->getPro["TOTALSTUDYTIME"]["gyou"] = 5;

        //生活や学習について気がついたこと
        $this->getPro["COMMUNICATION"]["moji"] = 25;
        $this->getPro["COMMUNICATION"]["gyou"] = 6;

    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        
        //バイトチェック、行数チェック
        if ($this->validate_row_cnt($this->Field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(委員会は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
            
        if ($this->validate_row_cnt($this->Field["REMARK3"], ($this->getPro["REMARK3"]["moji"] * 2)) > $this->getPro["REMARK3"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動は{$this->getPro["REMARK3"]["gyou"]}行までです)");
            return false;
        }
            
        if ($this->validate_row_cnt($this->Field["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915","(明小タイムは{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
            
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動の係りは{$this->getPro["REMARK2"]["gyou"]}行までです)");
            return false;
        }
            
		if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(生活や学習について気がづいたことは{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        if ($this->cmd == "update") $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd139pQuery::getInsertQuery($this);

        return ;
    }

}
?>
