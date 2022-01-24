<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz450_2Model extends Model {
    var $cmd;
//    var $group;  
    var $selectdata;
    var $QUALIFIED_CD;
    var $QUALIFIED_NAME;
    var $year;	//NO001
    var $setKey;
    /**
     * メンバ変数の初期化
     */
    function knjz450_2Model()
    {
    }
    function init()
    {       
        $this->QUALIFIED_CD    = VARS::post("QUALIFIED_CD");
        $this->QUALIFIED_NAME    = VARS::post("QUALIFIED_NAME");
        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        $this->year    = VARS::request("year");	//NO001
        if (VARS::request("QUALIFIED_CD") !="") {
            $this->QUALIFIED_CD          = VARS::request("QUALIFIED_CD");
            $this->QUALIFIED_NAME          = VARS::request("QUALIFIED_NAME");
        }
        //プロパティファイル
        $this->getPropertiesAll();
    }
    //入力項目チェック
    function validateInput()
    {
        //数値チェック
        if ($this->cmd == "update2" and (is_object($model->setKey) or is_array($model->setKey))) {
            foreach ($this->setKey as $rank) {
                if (VARS::post("SCORE-".$rank) != "" && !is_numeric(VARS::post("SCORE-".$rank))) {
                    $this->setWarning("MSG905","(得点)");
                    return false;
                }
            }
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
        } 
    }
    function &getUpdateModel2()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update2();
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz450_2Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }

    function &update2()
    {
        $result = knjz450_2Query::getUpdateQuery2($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }
}
?>
