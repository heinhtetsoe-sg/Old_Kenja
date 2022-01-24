<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl013pModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjl013pModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init() {
        $this->year = CTRL_YEAR + 1;
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                     );

        $this->cmd = VARS::request("cmd");
    }

    //更新処理
    function &getUpdateModel() {
        knjl013pQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
