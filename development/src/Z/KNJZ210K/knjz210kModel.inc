<?php

require_once('for_php7.php');

class knjz210kModel extends Model {
    var $cmd;
    var $field;
    var $groupcd;
    var $subclasscd;
    var $subclasscd2;
    var $asses;
    var $grade;
    var $record_dat_flg = "0";
    var $year;

    //メンバ変数の初期化
    function knjz210kModel()
    {
    }
    
    function init()
    {
        $this->year = CTRL_YEAR;
        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("YEAR"             => $this->year,
                             "TYPE_GROUP_CD"    => VARS::post("TYPE_GROUP_CD"),
                             "TYPE_GROUP_NAME"  => VARS::post("TYPE_GROUP_NAME"),
                             "TYPE_ASSES_CD"    => VARS::post("TYPE_ASSES_CD"),
                             "GRADE"            => VARS::post("GRADE"),
                             "SUBCLASSCD"       => VARS::post("SUBCLASSCD"),
                             "SELECTLENGTH"     => VARS::post("selectlength"),
                             "SELECTTEXT"       => VARS::post("selecttext"),
                             "SELECTVALUE"      => VARS::post("selectvalue")
                             );
        
        $temp = VARS::get("TYPE_GROUP_CD");
        if (isset($temp)) {
            $this->groupcd = VARS::get("TYPE_GROUP_CD");
        }
        $temp = VARS::request("GRADE");
        if (isset($temp)) {
            $this->grade  = VARS::request("GRADE");
        }
        if (VARS::request("SUBCLASSCD") && VARS::request("cmd") != "list") {
            $this->subclasscd2 = VARS::request("SUBCLASSCD");
        } else if (VARS::request("SUBCLASSCD")) {
            $this->subclasscd = VARS::request("SUBCLASSCD");
        }
        $temp = VARS::request("TYPE_ASSES_CD");
        if (isset($temp)) {
            $this->asses  = VARS::request("TYPE_ASSES_CD");
        }
        $temp = VARS::request("cmd");
        if (isset($temp)) {
            $this->cmd          = VARS::request("cmd");
        } else {
            unset($this->cmd);
            unset($this->groupcd);
            unset($this->subclasscd);
            unset($this->grade);
            unset($this->subclasscd2);
            unset($this->asses);
        }
        $this->record_dat_flg   = VARS::post("record_dat_flg");
        //プロパティファイル
        $this->getPropertiesAll();
    }
    
    function validateInput($cmd_kind)
    {
        //入力必須項目チェック
        if ($cmd_kind == "add") {
            if (trim($this->field["TYPE_GROUP_CD"]) == "") {
                $this->setWarning("MSG301", "(コード)");
                return false;
            }
        }
        if ($cmd_kind == "update" || $cmd_kind == "delete") {
            if (trim($this->field["TYPE_GROUP_CD"]) == "") {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->groupcd != $this->field["TYPE_GROUP_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        if ($cmd_kind == "add" || $cmd_kind == "update") {
            if ($this->field["SELECTVALUE"] == "") {
                $this->setWarning("MSG301", "(類型グループクラス)");
                return false;
            }
        }
        
        //グループコードの 0 埋め
        if (strlen($this->field["TYPE_GROUP_CD"]) < 6) {
            $this->field["TYPE_GROUP_CD"] = sprintf("%06d", $this->field["TYPE_GROUP_CD"]);
        }
        if ($cmd_kind == "add") {
            if (knjz210kQuery::IsExisting($this->field)) {
                $this->setWarning("MSG918");
                return false;
            }
        }
        return true;
    }

    function &getInsertModel()
    {
        if ($this->validateInput("add")) {
            $this->insert();
            $this->groupcd = $this->field["TYPE_GROUP_CD"];
            $this->setMessage("MSG201");
        }
    }
    function &getUpdateModel()
    {
        if ($this->validateInput("update")) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    function &getDeleteModel()
    {
        if ($this->validateInput("delete")) {
            $this->delete();
            $this->setMessage("MSG201");
        }
    }
    function &getCopyYearModel()
    {
        knjz210kQuery::getCopyYearQuery($this->year, $this);
    }
    
    function &insert()
    {
        knjz210kQuery::getInsertQuery($this->field, $this);
    }
    function &update()
    {
        knjz210kQuery::getDeleteQuery($this->field);
        knjz210kQuery::getInsertQuery($this->field, $this);
    }
    function &delete()
    {
        knjz210kQuery::getDeleteQuery($this->field);
    }
}
?>
