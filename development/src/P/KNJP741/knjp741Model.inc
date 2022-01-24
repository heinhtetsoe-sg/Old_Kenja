<?php

require_once('for_php7.php');


class knjp741Model extends Model {

    var $field        = array();
    var $cmd;
    var $schregno;

    function knjp741Model()
    {
    }

    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->checked);
            $this->year = array();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")){
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->checked);
            unset($this->field);
        }
        if(VARS::get("GRADE")){
            $this->grade = VARS::get("GRADE");
        }
        if(VARS::get("HR_CLASS")){
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if(VARS::get("ATTENDNO")){
            $this->attendno = VARS::get("ATTENDNO");
        }

        $this->field = array("SCHOOLCD"         =>  VARS::post("SCHOOLCD"),
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),
                             "YEAR"             =>  VARS::post("YEAR"),
                             "REPAY_SLIP_NO"    =>  VARS::post("REPAY_SLIP_NO"),
                             "REPAY_DIV"        =>  VARS::post("REPAY_DIV"),
                             "REPAY_DATE"       =>  VARS::post("REPAY_DATE"),
                             "REPAY_MONEY"      =>  VARS::post("REPAY_MONEY"),
                             "CANCEL_FLG"      =>  VARS::post("CANCEL_FLG")
                             );
        if (VARS::get("CHECKED")) {
            $this->checked = VARS::get("CHECKED");
            $setKey = $row["SCHOOLCD"].":".$row["SCHOOL_KIND"].":".$row["YEAR"].":".$row["REPAY_SLIP_NO"];
            list($schoolCd, $schoolKind, $year, $repaySlip) = explode(":", $this->checked);
            $this->field = array("SCHOOLCD"         =>  $schoolCd,
                                 "SCHOOL_KIND"      =>  $schoolKind,
                                 "YEAR"             =>  $year,
                                 "REPAY_SLIP_NO"    =>  $repaySlip
                                 );
        }

        if(VARS::get("init") == 1){
            unset($this->schregno);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入力チェック
        if ($this->validateInput()){
            $this->field["SCHOOLCD"]    = sprintf("%012d", SCHOOLCD);
            $this->field["SCHOOL_KIND"] = $this->getGrdInfo["SCHOOL_KIND"];
            $this->field["YEAR"]        = CTRL_YEAR;
            knjp741Query::insertData($db, $this);
            $this->setMessage("MSG201");
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入力チェック
        if ($this->validateInput()){
            knjp741Query::updateData($db, $this);
            $this->setMessage("MSG201");
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if ($this->validateInput()) {
            knjp741Query::deleteData($db, $this);
            $this->setMessage("MSG201");
        }
        $db->commit();
        $this->field = array();
        unset($this->checked);
        Query::dbCheckIn($db);
    }
}
?>
