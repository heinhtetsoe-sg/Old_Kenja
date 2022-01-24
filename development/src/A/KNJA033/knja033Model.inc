<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja033Model extends Model {
    var $field = array();
    var $select_data = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knja033Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init()
    {
        $this->field = array("THIS_YEAR"        =>  VARS::post("THIS_YEAR"),        //処理年月日
                             "MAX_SEMESTER"     =>  VARS::post("MAX_SEMESTER"),     //年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
