<?php

require_once('for_php7.php');

class knjd184lModel extends Model {
    var $field = array();
    var $control;
    var $cmd;

    function knjd184lModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_HYOSHI"     =>  VARS::post("PRINT_HYOSHI"),     //表紙
                             "PRINT_SEISEKI"    =>  VARS::post("PRINT_SEISEKI"),    //成績
                             "PRINT_SHURYO"     =>  VARS::post("PRINT_SHURYO"),     //修了
                             "SHURYO_DATE"      =>  VARS::post("SHURYO_DATE"),      //修了日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
