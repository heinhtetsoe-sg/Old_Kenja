<?php

require_once('for_php7.php');

class knjz235vModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjz235vModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("GRADE"        =>  VARS::post("GRADE"),        //学年
                             "MAJORCD"      =>  VARS::post("MAJORCD"),      //課程学科
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataLabel  = VARS::post("selectdataLabel");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新のモデルを返します
    function &getUpdateModel() {
        knjz235vQuery::getUpdateQuery($this);
        if (!isset($model->warning)) {
            $this->setWarning('MSG201');
            unset($this->selectdata);
            unset($this->selectdataLabel);
        }
        return true;
    }

    //前年度からコピーのモデルを返します
    function &getCopyModel(){
        $cntThisYear  = knjz235vQuery::cntThisYearData($this);
        $cntLastYear = knjz235vQuery::cntLastYearData($this);
        if ($cntThisYear > 0) {
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntLastYear == 0) {
            $this->setWarning('MSG303');
            return false;
        } else {
            knjz235vQuery::getCopyQuery($this);
            if (!isset($model->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return true;
        }
    }
}
?>
