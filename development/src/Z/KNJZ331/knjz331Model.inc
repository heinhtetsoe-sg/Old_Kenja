<?php

require_once('for_php7.php');

class knjz331Model extends Model {
    var $cmd;

    function knjz331Model() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("MENUNAME"       =>  VARS::post("MENUNAME"),
                             "SUB_MENUID"     =>  VARS::post("SUB_MENUID"),
                             "SUB_MENUNAME"   =>  VARS::post("SUB_MENUNAME"),
                             "MENU_CNT"       =>  VARS::post("MENU_CNT")
                            );
        $this->selectMenu = VARS::post("selectMenu");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput()) {
            knjz331Query::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }
}
?>
