<?php

require_once('for_php7.php');

class knjd185rModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd185rModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DESC_DATE"        =>  VARS::post("DESC_DATE"),        //記載日付
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "HYOUSI"           =>  VARS::post("HYOUSI"),           //表紙付かの指定
                             "KKESSEKI"         =>  VARS::post("KKESSEKI"),         //皆勤条件
                             "KCHIKOKU_SOUTAI"  =>  VARS::post("KCHIKOKU_SOUTAI"),  //皆勤条件
                             "KCHIKOKU"         =>  VARS::post("KCHIKOKU"),         //皆勤条件
                             "KSOUTAI"          =>  VARS::post("KSOUTAI"),          //皆勤条件
                             "KKEKKA"           =>  VARS::post("KKEKKA"),           //皆勤条件
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
