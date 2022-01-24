<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjc110bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjc110bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {
        $this->field = array("DATE1"            =>  VARS::post("DATE1"),            //日付FROM
                             "DATE2"            =>  VARS::post("DATE2"),            //日付TO
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票ラジオ
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
