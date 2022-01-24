<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd623Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd623Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),        //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),    //学期
                             "GRADE"        =>  VARS::post("GRADE"),       //学年
                             "TESTCD"       =>  VARS::post("TESTCD"),      //テスト
                             "SUBCLASSDIV"  =>  VARS::post("SUBCLASSDIV"), //出力区分
                             "DBNAME"       =>  VARS::post("DBNAME")       //DB変更に備えて
                            );
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>