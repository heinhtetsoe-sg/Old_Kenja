<?php

require_once('for_php7.php');

class knjh112cModel extends Model {
    var $semester;
    var $hrClass;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjh112cModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SEMESTER"  =>  VARS::post("SEMESTER"),           //学期
                             "SIKAKUCD"  =>  VARS::post("SIKAKUCD"),           //資格コンボ
                             "TAISYOU"   =>  VARS::post("TAISYOU"),            //ラジオボタン
                             "FJ_DATE"   =>  VARS::post("FJ_DATE"),            //受験期間from
                             "TJ_DATE"   =>  VARS::post("TJ_DATE")             //受験期間to
                             );

        $this->hrClass = VARS::post("GRADE_HR_CLASS");

        $this->cmd          = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
