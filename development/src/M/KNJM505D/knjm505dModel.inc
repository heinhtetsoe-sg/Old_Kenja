<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm505dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    /**
     * メンバ変数の初期化
     */
    function knjm505dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {
        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "DBNAME"               =>  VARS::post("DBNAME"),               //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->nextyear = CTRL_YEAR+1;

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
