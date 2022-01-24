<?php

require_once('for_php7.php');

class knjz333Model extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjz333Model()
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

        $this->getPropertiesAll(); //プロパティファイル


        $this->field = array("MENUNAME"             =>  VARS::post("MENUNAME"),
                             "MENUID"               =>  VARS::post("MENUID") != "" ? VARS::post("MENUID") : "A",
                             "SUB_MENUID"           =>  VARS::post("SUB_MENUID") != "" ? VARS::post("SUB_MENUID") : "1000",
                             "SUB_MENUNAME"         =>  VARS::post("SUB_MENUNAME"),
                             "SYORI_MONTH"          =>  VARS::post("SYORI_MONTH"),
                             "HIDE_FLG"             =>  VARS::post("HIDE_FLG"),
                             "TITLE_NAME"           =>  VARS::post("TITLE_NAME"),
                             "SELECT_MENUNAME"      =>  VARS::post("SELECT_MENUNAME"),
                             "SELECT_SUB_MENUID"    =>  VARS::post("SELECT_SUB_MENUID"),
                             "MENU_CNT"             =>  VARS::post("MENU_CNT")
                            );


    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz333Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
    
    function setFormatData()
    {
        $db = Query::dbCheckOut();
        
        //対象のMENU_SUB_MST削除
        $query = knjz333Query::deleteSubmst($this, $this->field["MENUID"], $this->field["SUB_MENUID"]);
        $db->query($query);
        
        //MENU_SUB_MSTにMENU_SUB_DEFALT_MSTからINSERTする
        $query = knjz333Query::insertDefalt($this, $this->field["MENUID"], $this->field["SUB_MENUID"]);
        $db->query($query);
        
        Query::dbCheckIn($db);
        
        $this->setMessage("初期化しました。");
    }
}
?>
