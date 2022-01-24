<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd625fModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd625fModel()
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

                             "STRT_YEAR"    => VARS::post("STRT_YEAR"),    //抽出開始年度
                             "END_YEAR"     => VARS::post("END_YEAR"),     //抽出修了年度
                             "TESTTYPE"     => VARS::post("TESTTYPE"),     //考査/模試ラジオ
                             "PASS_ONLY"    => VARS::post("PASS_ONLY"),    //合格者のみ出力

                             "DBNAME"       =>  VARS::post("DBNAME")       //DB変更に備えて
                            );
        $this->selectCollege        = VARS::post("selectCollege");
        $this->selectTestInfo       = VARS::post("selectTestInfo");
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
