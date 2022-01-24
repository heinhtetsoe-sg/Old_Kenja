<?php

require_once('for_php7.php');

class knjh566aModel extends Model {
    var $field          = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjh566aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //学年
                             "JUNI"             =>  VARS::post("JUNI"),             //出力順位
                             "KIJUNTEN"         =>  VARS::post("KIJUNTEN"),         //順位の基準点
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
