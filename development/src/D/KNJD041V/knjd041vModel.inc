<?php

require_once('for_php7.php');

class knjd041vModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;

    function knjd041vModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),       //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),   //学期コード
                             "TESTCD"       =>  VARS::post("TESTCD"),     //テスト
                             "OUTPUT"       =>  VARS::post("OUTPUT"),     //帳票出力指定
                             "OUTPUTCOL"    =>  VARS::post("OUTPUTCOL"),  //出力項目指定
                             "MAJOR"        =>  VARS::post("MAJOR"),      //学科
                             "DBNAME"       =>  VARS::post("DBNAME")      //DB
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
