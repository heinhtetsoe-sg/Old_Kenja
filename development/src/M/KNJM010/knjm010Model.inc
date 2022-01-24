<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm010Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm010Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GRADE"        =>  VARS::post("GRADE"),            //学年
                             "BUSU"         =>  VARS::post("BUSU"),             //部数
                             "DBNAME"       =>  VARS::post("DBNAME"),          //DB変更に備えて
                             "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "TAISHOUSHA"           =>  VARS::post("TAISHOUSHA"));           //対象者ラジオボタン

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
