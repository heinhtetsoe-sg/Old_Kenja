<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz310_2Model extends Model {
    var $cmd;
//    var $group;  
    var $selectdata;
    var $GROUPCD;
    var $GROUPNAME;
    var $year;  //NO001
    /**
     * メンバ変数の初期化
     */
    function knjz310_2Model()
    {
    }
    function init()
    {       
        $this->GROUPCD    = VARS::post("GROUPCD");
        $this->GROUPNAME    = VARS::post("GROUPNAME");
        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        $this->year    = VARS::request("year"); //NO001
        if (VARS::request("GROUPCD") !="") {
            $this->GROUPCD          = VARS::request("GROUPCD");
            $this->GROUPNAME          = VARS::request("GROUPNAME");
        }

        //プロパティファイル
        $this->getPropertiesAll();
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
        $result = knjz310_2Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }
}
?>
