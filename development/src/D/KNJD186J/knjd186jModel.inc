<?php

require_once('for_php7.php');

class knjd186jModel extends Model {
    var $ibprg_course;
    var $grade;
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $field;

    function knjd186jModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->ibprg_course = VARS::post("IBPRG_COURSE");
        $this->semester = VARS::post("SEMESTER");
        $this->grade = VARS::post("GRADE");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");
        $this->field = array("DATE"             =>  VARS::post("DATE"),              //異動対象日付
                             "PRINT_SIDE1"      =>  VARS::post("PRINT_SIDE1"),       //印刷対象
                             "PRINT_SIDE2"      =>  VARS::post("PRINT_SIDE2"),       //印刷対象
                             "PRINT_SIDE3_1"    =>  VARS::post("PRINT_SIDE3_1"),     //印刷対象
                             "PRINT_SIDE3_2"    =>  VARS::post("PRINT_SIDE3_2"),     //印刷対象
                             "PRINT_SIDE4"      =>  VARS::post("PRINT_SIDE4"),       //印刷対象
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        if ($this->cmd == '') {
            unset($this->ibprg_course);
            unset($this->grade);
            unset($this->semester);
            unset($this->hrClass);
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
