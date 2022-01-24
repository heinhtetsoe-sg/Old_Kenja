<?php

require_once('for_php7.php');

class knjf150bModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $attendno;
    var $field = array();

    var $visit_date;
    var $visit_hour;
    var $visit_minute;

    var $control_data = array();    //コントロールマスタデータを保持

    function knjf150bModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
        }

        $this->field = array("VISIT_DATE"           =>  str_replace("/", "-", VARS::post("VISIT_DATE")),    //来室日付
                             "VISIT_HOUR"           =>  (strlen(VARS::post("VISIT_HOUR"))) ? sprintf("%02d", VARS::post("VISIT_HOUR")) : "",        //来室時間（時）
                             "VISIT_MINUTE"         =>  (strlen(VARS::post("VISIT_MINUTE"))) ? sprintf("%02d", VARS::post("VISIT_MINUTE")) : "",    //来室時間（分）
                             "OCCUR_CAUSE"          =>  VARS::post("OCCUR_CAUSE"),          //原因
                             "OCCUR_PLACE"          =>  VARS::post("OCCUR_PLACE"),          //発生場所
                             "TREATMENT"            =>  VARS::post("TREATMENT"),            //処置
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //echo "sch : ".$this->schregno;
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("VISIT_DATE") != "") {
            $this->visit_date = VARS::get("VISIT_DATE");
        }
        if (VARS::get("VISIT_HOUR") != "") {
            $this->visit_hour = VARS::get("VISIT_HOUR");
        }
        if (VARS::get("VISIT_MINUTE") != "") {
            $this->visit_minute = VARS::get("VISIT_MINUTE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }
        $this->cmd = "form2A";
        knjf150bQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
        $this->field = array();

    }

    //入力項目チェック
    function validateInput($flg)
    {
        $count = get_count(knjf150bQuery::getExistCheck($this));

        if($flg == "update" && ($this->visit_date != $this->field["VISIT_DATE"] || 
                                $this->visit_hour != $this->field["VISIT_HOUR"] || 
                                $this->visit_minute != $this->field["VISIT_MINUTE"])) {
            $this->setWarning("来室日時が変更されています。");
            return false;
        }
        if($flg == "update" && !$count){
            $this->setWarning("対象データがありません。");
            return false;
        }
        if($flg == "add" && $count){
            $this->setWarning("MSG302");
            return false;
        }
        if (strlen($this->field["OCCUR_PLACE"]) > 60) {
            $this->setWarning("MSG915","(場所は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["OCCUR_CAUSE"]) > 60) {
            $this->setWarning("MSG915","(原因は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["TREATMENT"]) > 60) {
            $this->setWarning("MSG915","(処置は60バイトまでです)");
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel($flg)
    {
        $this->cmd = "form2A";

        if (!$this->validateInput($flg)) {
            return 0;
        }

        knjf150bQuery::getUpdateQuery($this, $this->field, $flg);
        $this->setMessage("MSG201");

        $this->visit_date = $this->field["VISIT_DATE"];
        $this->visit_hour = $this->field["VISIT_HOUR"];
        $this->visit_minute = $this->field["VISIT_MINUTE"];

        return 1;
    }
}
?>
