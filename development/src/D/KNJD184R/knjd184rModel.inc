<?php

require_once('for_php7.php');

class knjd184rModel extends Model {
    var $semester;
    var $hrClass;
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;
    var $selectdataText;
    var $schoolKind;

    function knjd184rModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }
    
        $this->field = array("DISP"             =>  VARS::post("DISP"),              //表示選択 1:クラス 2:個人
                             "DATE"             =>  VARS::post("DATE"),              //異動対象日付
                             "DESC_DATE"        =>  VARS::post("DESC_DATE"),         //記載日付
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),             //年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");
        $this->schoolKind   = VARS::post("SCHOOL_KIND");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
