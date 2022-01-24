<?php

require_once('for_php7.php');

class knjd107vModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd107vModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("DATE"             =>  VARS::post("DATE"),            //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),  //年組
                             "OUTPUT_DIV"       =>  VARS::post("OUTPUT_DIV"),      //出力帳票
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),       //グループ区分
                             "RANK_DIV"         =>  VARS::post("RANK_DIV"),        //順位の基準点
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
                            );

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
