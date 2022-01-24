<?php

require_once('for_php7.php');

class knjd187fModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd187fModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DISP"             =>  VARS::post("DISP"),             //表示選択 1:クラス 2:個人
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_SIDE1"      =>  VARS::post("PRINT_SIDE1"),      //1.表紙
                             "PRINT_SIDE2"      =>  VARS::post("PRINT_SIDE2"),      //2.学習の記録
                             "ADDR_PRINT"       =>  VARS::post("ADDR_PRINT"),       //郵送用の住所・氏名を印字しない
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
