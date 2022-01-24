<?php

require_once('for_php7.php');

class knjb010Model extends Model {

    var $cmd;
    var $groupcd = "";
    var $name = "";
    var $ctrl;
    var $field = array();

    function knjb010Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->groupcd);
            unset($this->name);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("GROUPCD")!="") {
            $this->groupcd = VARS::get("GROUPCD");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        
        $this->field = array("LESSONCNT"   => VARS::post("LESSONCNT"),
                             "FRAMECNT"    => VARS::post("FRAMECNT"),
                             "CHAIRCD"     => VARS::post("selectdata"),
                             "ATTENDCLASS" => VARS::post("selectdata2"));
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            
            knjb010Query::UpdateQuery($this);
        
        
            $this->setMessage("MSG201");
        }
    }
    
    //前年度コピー
    function getCopyModel()
    {
        knjb010Query::CopyQuery($this);
    }
    
    //削除
    function getDeleteModel()
    {
        $this->setMessage("MSG201");
    }
}
?>
