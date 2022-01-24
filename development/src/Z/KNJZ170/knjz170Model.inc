<?php

require_once('for_php7.php');

class knjz170Model extends Model {

    var $cmd;
    var $year;  //年度
    var $selectdata;

    function knjz170Model()
    {
        $this->year = "";
        $this->selectdata = "";
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
            $this->setMessage("MSG304","(対象年度)");
//            $this->warning = "対象年度を選択してください。";
            return false;
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
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
        $result = knjz170Query::getUpdateQuery($this);
        return $result;
    }
}
?>
