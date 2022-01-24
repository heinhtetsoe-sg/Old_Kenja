<?php

require_once('for_php7.php');
class knjz331aModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjz331aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataLabel  = VARS::post("selectdataLabel");

        $this->field = array("MENUNAME"             =>  VARS::post("MENUNAME"),
                             "SUB_MENUID"           =>  VARS::post("SUB_MENUID"),
                             "SUB_MENUNAME"         =>  VARS::post("SUB_MENUNAME"),
                             "TITLE_NAME"           =>  VARS::post("TITLE_NAME"),
                             "SELECT_MENUNAME"      =>  VARS::post("SELECT_MENUNAME"),
                             "SELECT_SUB_MENUID"    =>  VARS::post("SELECT_SUB_MENUID"),
                             "MENU_CNT"             =>  VARS::post("MENU_CNT")
                            );

        //プロパティファイル
        $this->getPropertiesAll();
    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz331aQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
