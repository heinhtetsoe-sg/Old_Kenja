<?php

require_once('for_php7.php');

class knjd186uModel extends Model {
    var $field = array();
    var $cmd;

    function knjd186uModel() {}

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
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //印刷日
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                             "GRADING_PRINT"    =>  VARS::post("GRADING_PRINT")     //学年末評定を出力しない
                     );


        if (VARS::get("PROGRAMID")) {
            $this->field["GRADING_PRINT"] = '1';
        }

        $this->cmd = VARS::request("cmd");
    }
}
?>
