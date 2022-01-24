<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz220bModel extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjz220bModel()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");

        //プロパティ設定
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("CLASSCD"			=>  VARS::post("CLASSCD"),      //教科
                             "SUBCLASSCD"	=>  VARS::post("SUBCLASSCD"));	    //科目

        if ($this->cmd == "update"){
            $this->selectdata   = VARS::post("selectdata");
        }
    }

    function &getUpdateModel()
    {
        knjz220bQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
