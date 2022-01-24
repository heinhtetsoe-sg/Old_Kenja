<?php

require_once('for_php7.php');

class knjb131Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjb131Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init() {
        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "DATE1"            =>  VARS::post("DATE1"),            //日付FROM
                             "DATE2"            =>  VARS::post("DATE2"),            //日付TO
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイル
    }
}
?>