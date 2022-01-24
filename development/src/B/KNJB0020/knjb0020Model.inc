<?php

require_once('for_php7.php');

class knjb0020Model extends Model {

    var $cmd;
    var $staffcd = "";
    var $name = "";
    var $control;
    var $sectioncd;

    function knjb0020Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->staffcd);
            unset($this->fusestaffcd);
            unset($this->name);
        }

        $this->cmd = VARS::request("cmd");
        $this->data = VARS::post("data");
        if (strlen($this->data))
            $this->data = explode(",",$this->data);
        
        $this->sectioncd = VARS::post("sectioncd");

        if (VARS::get("STAFFCD")!="") {
            $this->staffcd = VARS::get("STAFFCD");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::get("FUSE_STAFFCD")!="") {
            $this->fusestaffcd = VARS::get("FUSE_STAFFCD");
        }
        if ($this->cmd == "combo") {
            unset($this->staffcd);
            unset($this->name);
        }

        $this->getPropertiesAll();//プロパティファイルの読み込み

    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新
    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        knjb0020Query::UpdateQuery($this);
        $this->setMessage("MSG201");
    }
    
    //前年度コピー
    function getCopyModel()
    {
        $cnt = knjb0020Query::CopyQuery($this);
        $this->setMessage("MSG201","(".$cnt."件)");
    }
    
    //削除
    function getDeleteModel()
    {
        if (knjb0020Query::DeleteQuery($this->staffcd)) {
            $this->setMessage("MSG201");
        }
    }
}
?>
