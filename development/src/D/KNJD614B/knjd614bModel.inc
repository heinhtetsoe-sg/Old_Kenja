<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd614bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd614bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),        //年度
                             "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"), //校種
                             "SEMESTER"     =>  VARS::post("SEMESTER"),    //学期
                             "GRADE"        =>  VARS::post("GRADE"),       //学年
                             "TESTCD"       =>  VARS::post("TESTCD"),      //テスト
                             "ATTEND_DATE"  =>  VARS::post("ATTEND_DATE"), //出欠対象日付
                             "DBNAME"       =>  VARS::post("DBNAME")       //DB変更に備えて
                            );
        $this->selectdata           = VARS::post("selectdata");
        $this->selectSubclass       = VARS::post("selectSubclass");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
