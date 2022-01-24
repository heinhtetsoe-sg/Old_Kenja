<?php

require_once('for_php7.php');

class knjb020Model extends Model {
    var $cmd = "";
    var $chaircd;
    var $selectdata;
    var $chaircd;
    var $classcd;

    //メンバ変数の初期化
    function knjb020Model()
    {
    }
    
    function init()
    {
        //key値
        if(VARS::get("CHAIRCD")!=""){
            $this->chaircd = VARS::get("CHAIRCD");
        }

        if (VARS::get("PROGRAMID") || VARS::get("init") == "1") {
            $this->chaircd = "";
            $this->subclasscd = "";
        }

        if (VARS::post("selectdata")!=""){
            $this->selectdata = VARS::post("selectdata");
        }

        $this->cmd     = VARS::request("cmd");
        $this->classcd = VARS::post("CLASSCD");
        if (VARS::get("init") == "1") {
            unset($this->chaircd);
        }
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
        knjb020Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
