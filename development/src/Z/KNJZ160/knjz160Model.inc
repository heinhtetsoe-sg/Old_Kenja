<?php

require_once('for_php7.php');

class knjz160Model extends Model {

    var $cmd;
    var $year = "";  //年度
    var $selectdata;

    function knjz160Model()
    {
    }

    function init()
    {   
        $temp = VARS::post("year");
        if (isset($temp)) {
            $this->year         = VARS::post("year");
        }else{
            $this->year         = CTRL_YEAR;
        }
        $this->selectdata       = VARS::post("selectdata");
        $this->cmd              = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を指定して下さい。";
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    
    function &update()
    {
        $result = knjz160Query::getUpdateQuery($this);
        return $result;
    }
}
?>
