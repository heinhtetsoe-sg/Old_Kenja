<?php

require_once('for_php7.php');
class knjd138jModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $Field = array();
    var $control;

    function knjd138jModel()
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
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->Field = array("TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "REMARK03_03"          =>  VARS::post("REMARK03_03"),
                             "REMARK03_04"          =>  VARS::post("REMARK03_04"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (is_array($this->itemArray)) {
            foreach ($this->itemArray as $key => $val) {
                $this->Field["REMARK".$key] = VARS::post("REMARK".$key);
            }
        }

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

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 13;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 8;
        }

        if ($this->Properties["reportSpecialSize03_03"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize03_03"]);
            $this->getPro["REMARK03_03"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK03_03"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK03_03"]["moji"] = 13;
            $this->getPro["REMARK03_03"]["gyou"] = 9;
        }

        if ($this->Properties["reportSpecialSize03_04"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize03_04"]);
            $this->getPro["REMARK03_04"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK03_04"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK03_04"]["moji"] = 13;
            $this->getPro["REMARK03_04"]["gyou"] = 9;
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 25;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 5;
        }

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 47;
            $this->getPro["COMMUNICATION"]["gyou"] = 1;
        }
        
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

        if ($this->validate_row_cnt($this->Field["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915","(学習内容は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->Field["REMARK03_03"], ($this->getPro["REMARK03_03"]["moji"] * 2)) > $this->getPro["REMARK03_03"]["gyou"]) {
            $this->setWarning("MSG915","(観点は{$this->getPro["REMARK03_03"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->Field["REMARK03_04"], ($this->getPro["REMARK03_04"]["moji"] * 2)) > $this->getPro["REMARK03_04"]["gyou"]) {
            $this->setWarning("MSG915","(評価は{$this->getPro["REMARK03_04"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(学校からの所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }
        foreach ($this->itemArray as $key => $val) {
            $setData = array();

            $moji = $this->getPro["reportSpecial".$key."_moji"];
            $gyou = $this->getPro["reportSpecial".$key."_gyou"];
            if ($this->validate_row_cnt($this->Field["REMARK".$key], ($moji * 2)) > $gyou) {
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
        if ($this->cmd == "update") $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd138jQuery::getInsertQuery($this);

        return ;
    }

}
?>
