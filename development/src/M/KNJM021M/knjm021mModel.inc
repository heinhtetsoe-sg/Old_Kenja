<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm021mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm021mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),    //クラス
                             "BUSU"              =>  VARS::post("BUSU"),              //部数
                             "FORM_ONLY"         =>  VARS::post("FORM_ONLY"),         //フォームのみ出力
                             "DBNAME"            =>  VARS::post("DBNAME"));           //DB変更に備えて



        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
