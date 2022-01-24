<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd191Model.inc 56586 2017-10-22 12:52:35Z maeshiro $

class knjd191Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knjd191Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),                //年度
                             "SEMESTER"          =>  VARS::post("SEMESTER"),            //学期
                             "GRADE"             =>  VARS::post("GRADE"),               //学年
                             "HR_CLASS"          =>  VARS::post("HR_CLASS"),            //クラス
                             "TESTCD"            =>  VARS::post("TESTCD"),              //テスト
                             "CATEGORY_IS_CLASS" =>  VARS::post("CATEGORY_IS_CLASS")    //カテゴリ区分
                     );
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
