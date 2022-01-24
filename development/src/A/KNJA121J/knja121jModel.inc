<?php

require_once('for_php7.php');
class knja121jModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $record = array();
    var $control;

    function knja121jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
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

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
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
        if (strlen($this->field["TOTALSTUDYACT"]) > 184) {
            $this->setWarning("MSG915","(活動内容は184バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYVAL"]) > 400) {
            $this->setWarning("MSG915","(評価は400バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALREMARK"]) > 796) {
            $this->setWarning("MSG915","(総合所見は796バイトまでです)");
            return false;
        }
        if (strlen($this->field["ATTENDREC_REMARK"]) > 122) {
            $this->setWarning("MSG915","(出欠の記録備考は122バイトまでです)");
            return false;
        }
        if (strlen($this->field["VIEWREMARK"]) > 184) {
            $this->setWarning("MSG915","(観点は184バイトまでです)");
            return false;
        }
        if (strlen($this->field["BEHAVEREC_REMARK"]) > 122) {
            $this->setWarning("MSG915","(行動の記録備考は122バイトまでです)");
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
        if ($this->cmd == "update2") $this->update2();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knja121jQuery::getDeleteQuery($this->schregno);
        knja121jQuery::getInsertQuery($this->field,$this->schregno);
        return $result;
    }

    function &update2()
    {
        knja121jQuery::getDeleteQuery2($this);
        knja121jQuery::getInsertQuery2($this);
        return $result;
    }
}
?>
