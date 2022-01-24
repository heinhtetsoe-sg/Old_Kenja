<?php

require_once('for_php7.php');

class knjz230Model extends Model {
    var $cmd = "";
    var $chaircd;
    var $control = array();
    var $selectdata;
    var $cmb_class;
    var $cmb_sub_cls;
    var $subclasscd = "";

    //メンバ変数の初期化
    function knjz230Model()
    {
        ;
    }

    function init()
    {
        //key値
        if(VARS::get("CHAIRCD")){
            $this->chaircd = VARS::get("CHAIRCD");
        }

        if (VARS::get("PROGRAMID") || VARS::get("init") == "1") {
            $this->chaircd = "";
            $this->subclasscd = "";
        }

        if(VARS::post("CMB_CLASS")){
            $this->cmb_class   = VARS::post("CMB_CLASS");
        }

        if(VARS::post("CMB_SUB_CLS")){
            $this->cmb_sub_cls = VARS::post("CMB_SUB_CLS");
        }

        if(VARS::get("SUBCLSCD")){
            $this->subclasscd = VARS::get("SUBCLSCD");
        }

        if ($this->cmd == "check"){
            $this->selectdata = VARS::post("selectdata");
        }

        $this->cmd            = VARS::request("cmd");
    }

    function &getUpdateModel()
    {
        //KEY値チェック
        if ($this->chaircd == "")
        {
            $this->setWarning("MSG310");
            return;
        }
        $this->update();
        return;
    }

    function &update()
    {
        knjz230Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
