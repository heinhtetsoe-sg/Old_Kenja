<?php

require_once('for_php7.php');
class knja121kModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $record = array();
    var $control;

    function knja121kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "VIEWREMARK"           =>  VARS::post("VIEWREMARK"),
                             "BEHAVEREC_REMARK"     =>  VARS::post("BEHAVEREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }
        //特別活動の記録
        for($i=1; $i<4; $i++)
        {
            $ival = "2" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //年度コンボ（通知表所見）
        $this->year_cmb = VARS::request("YEAR_CMB");
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYACT"]) > 246) {
            $this->setWarning("MSG915","(学習活動は246バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYVAL"]) > 246) {
            $this->setWarning("MSG915","(評価は246バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALREMARK"]) > 1361) {
            $this->setWarning("MSG915","(総合所見は1361バイトまでです)");
            return false;
        }
        if (strlen($this->field["ATTENDREC_REMARK"]) > 60) {
            $this->setWarning("MSG915","(出欠の記録備考は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["VIEWREMARK"]) > 126) {
            $this->setWarning("MSG915","(観点は126バイトまでです)");
            return false;
        }
/***
        if (strlen($this->field["BEHAVEREC_REMARK"]) > 122) {
            $this->setWarning("MSG915","(行動の記録備考は122バイトまでです)");
            return false;
        }
***/
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        if ($this->cmd == "update") $this->update();
        if ($this->cmd == "update2") $this->update2();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knja121kQuery::getDeleteQuery($this->schregno, $this->exp_year);
        knja121kQuery::getInsertQuery($this->field,$this->schregno, $this->exp_year, $this->exp_semester);
        return $result;
    }

    function &update2()
    {
        knja121kQuery::getDeleteQuery2($this);
        knja121kQuery::getInsertQuery2($this);

        return $result;
    }
}
?>
