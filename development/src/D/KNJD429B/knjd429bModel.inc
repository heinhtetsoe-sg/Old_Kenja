<?php

require_once('for_php7.php');

class knjd429bModel extends Model {
    var $semester;
    var $hrClass;
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;
    var $selectdataText;

    function knjd429bModel() {
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
    
        $this->field = array("HUKUSIKI_RADIO"   =>  VARS::post("HUKUSIKI_RADIO"),    //クラス選択 1:法廷クラス 2:実クラス
                             "DATE"             =>  VARS::post("DATE"),              //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "PRINT_PATTERN"    =>  VARS::post("PRINT_PATTERN"),     //帳票パターン
                             "PRINT_SIDE1"      =>  VARS::post("PRINT_SIDE1"),       //表示・裏表紙
                             "PRINT_SIDE2"      =>  VARS::post("PRINT_SIDE2"),       //学習の記録
                             "PRINT_SIDE3"      =>  VARS::post("PRINT_SIDE3"),       //出欠の記録
                             "OUTPUT1"          =>  VARS::post("OUTPUT1"),           //修了証書
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

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
