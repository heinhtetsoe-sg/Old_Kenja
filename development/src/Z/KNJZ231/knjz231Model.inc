<?php

require_once('for_php7.php');

class knjz231Model extends Model {
    var $cmd = "";
    var $chaircd;
    var $control = array();
    var $selectdata;
    var $cmb_class;
    var $subclasscd = "";
    var $chaircode;
    var $count = "";

    //メンバ変数の初期化
    function knjz231Model()
    {
    }

    function init()
    {
        //key値
        if(VARS::get("CHAIRCHK")){
            $this->chaircd = VARS::get("CHAIRCHK");
        }
        if(VARS::get("COUNT")){
            $this->count = VARS::get("COUNT");
        }
        if(VARS::post("CHAIRCODE")){
            $this->chaircode = VARS::post("CHAIRCODE");
        }
        if (VARS::get("PROGRAMID") || VARS::get("init") == "1") {
            $this->chaircd = "";
            $this->subclasscd = "";
            $this->chaircode = "";
            $this->count = "";
        }

        if(VARS::post("CMB_CLASS")){
            $this->cmb_class   = VARS::post("CMB_CLASS");
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
        if ($this->chaircd == "" || $this->chaircd == "FALSE")
        {
            $this->setWarning("MSG310");
            return;
        }
        if($this->chaircode == ""){
            $this->chaircode = $_COOKIE["CHAIRCD"];
        }
        $this->update();
        return;
    }

    function &update()
    {
       $con=strrpos($this->chaircode,",");
       $this->chaircode=substr($this->chaircode,0,$con);

        if($this->chaircode != "")
        {

            $cd = explode(",",$this->chaircode);
            for ($i = 0; $i < get_count($cd); $i++)
            {
                   knjz231Query::getUpdateQuery($this,$cd[$i]);
            }
        }else{
            knjz231Query::getUpdateQuery($this,$this->chaircd);
        }
        $this->setMessage("MSG201");
        $this->chaircd = "";
        $this->count = "";
        $this->chaircode = "";
        return;
    }
}
?>
