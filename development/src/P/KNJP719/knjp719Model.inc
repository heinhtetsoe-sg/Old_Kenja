<?php

require_once('for_php7.php');

class knjp719Model extends Model {
    var $year;
    var $semester;
    var $hrClass;
    var $cmd;
    var $control;        //コントロールマスタ
    

    function knjp719Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->monthArray = array("4", "5", "6", "7", "8", "9", "10", "11", "12", "1", "2", "3");

        $this->field = array("COLLECT_GRP_CD"               =>  VARS::post("COLLECT_GRP_CD"),               //入金グループ
                             "LIST_COLLECT_PATTERN_CD"      =>  VARS::post("LIST_COLLECT_PATTERN_CD"),      //入金パターン
                             "COLLECT_PATTERN_CD"           =>  VARS::post("COLLECT_PATTERN_CD")            //入金パターン
                             );

        $this->hrClass = VARS::post("GRADE_HR_CLASS");

        $this->cmd        = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;
        $this->selectdata = VARS::post("selectdata");
        $this->dataDiv    = VARS::post("DATADIV");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新前チェック
    function validateInput() {
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        knjp719Query::getUpdateQuery($this);
        return;
    }
}
?>
