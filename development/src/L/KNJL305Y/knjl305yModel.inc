<?php

require_once('for_php7.php');

class knjl305yModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $selectdata;

    function knjl305yModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"	    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	        =>  VARS::post("TESTDIV"),          //入試区分
                             "APP_HOLD"	        =>  VARS::post("APP_HOLD"),         //入試区分
                             "COMMENT"	        =>  VARS::post("COMMENT"),          //コメント
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0){
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMENT"], (30 * 2)) > 3) {
            $this->setWarning("MSG915","(コメントは3行までです)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjl305yQuery::getDeleteQuery($this, $this->field);
            knjl305yQuery::getInsertQuery($this, $this->field);
        }
        return;
    }
}
?>
