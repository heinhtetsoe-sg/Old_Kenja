<?php

require_once('for_php7.php');

class knjd187aModel extends Model {
    var $field = array();
    var $cmd;

    function knjd187aModel() {}

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
                             "ATTSEMES_DATE"    =>  VARS::post("ATTSEMES_DATE"),    //出欠集計日付
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //印刷日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
