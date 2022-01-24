<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp984_2Model extends Model {
    var $cmd;
    var $selectdata;
    var $groupcd;
    var $groupname;
    var $year;
    /**
     * メンバ変数の初期化
     */
    function knjp984_2Model()
    {
    }
    function init()
    {       
        $this->groupcd      = VARS::post("LEVY_GROUP_CD");
        $this->groupname    = VARS::post("LEVY_GROUP_NAME");
        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        $this->year         = VARS::request("year");
        if (VARS::request("LEVY_GROUP_CD") !="") {
            $this->groupcd    = VARS::request("LEVY_GROUP_CD");
            $this->groupname  = VARS::request("LEVY_GROUP_NAME");
        }
    }
    //入力項目チェック
    function validateInput()
    {
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
        } 
    }
         
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjp984_2Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }
}
?>
