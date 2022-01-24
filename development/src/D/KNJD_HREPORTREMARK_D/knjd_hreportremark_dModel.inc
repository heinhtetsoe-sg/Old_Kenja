<?php

require_once('for_php7.php');

class knjd_hreportremark_dModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $field = array();
    var $record = array();
    var $control;

    function knjd_hreportremark_dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("CALL_PRG") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array();
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        if (is_array($this->itemArray)) {
            foreach ($this->itemArray as $key => $val) {
                $this->field["REMARK".$key] = VARS::post("REMARK".$key);
            }
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        if ($this->Properties["reportSpecialSize01_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_01"]);
            $this->remark01_moji = (int)trim($moji);
            $this->remark01_gyou = (int)trim($gyou);
        } else {
            $this->remark01_moji = 30; //デフォルトの値
            $this->remark01_gyou = 1;  //デフォルトの値
        }
        
        if ($this->Properties["reportSpecialSize01_02"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_02"]);
            $this->remark02_moji = (int)trim($moji);
            $this->remark02_gyou = (int)trim($gyou);
        } else {
            $this->remark02_moji = 30; //デフォルトの値
            $this->remark02_gyou = 5;  //デフォルトの値
        }
        
        if ($this->Properties["reportSpecialSize01_03"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_03"]);
            $this->remark03_moji = (int)trim($moji);
            $this->remark03_gyou = (int)trim($gyou);
        } else {
            $this->remark03_moji = 30; //デフォルトの値
            $this->remark03_gyou = 1;  //デフォルトの値
        }
        
        if ($this->Properties["reportSpecialSize01_04"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_04"]);
            $this->remark04_moji = (int)trim($moji);
            $this->remark04_gyou = (int)trim($gyou);
        } else {
            $this->remark04_moji = 30; //デフォルトの値
            $this->remark04_gyou = 1;  //デフォルトの値
        }

        $this->tutisyoSyokenTitle = $this->Properties["tutisyoSyokenTitle"] ? $this->Properties["tutisyoSyokenTitle"] : '通信欄';

    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        foreach ($this->itemArray as $key => $val) {
            $setData = array();

            $moji = $this->getPro["reportSpecial".$key."_moji"];
            $gyou = $this->getPro["reportSpecial".$key."_gyou"];
            if ($this->validate_row_cnt($this->field["REMARK".$key], ($moji * 2)) > $gyou) {
                $this->setWarning("MSG915","(".$val["NAME1"]."は{$gyou}行までです)");
                return false;
            }
        }

        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }

        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd_hreportremark_dQuery::getInsertQuery($this);

        return ;
    }
}
?>
