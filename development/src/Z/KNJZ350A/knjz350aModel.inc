<?php

require_once('for_php7.php');


class knjz350aModel extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $selectdata;
    var $selectdata2Left;
    var $selectdata2Right;
    var $subclassAll;
    var $selectdata2;
    var $selectdata3;
    var $selectdataJview;

    function knjz350aModel()
    {
    }
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }

        $this->year             = CTRL_YEAR;
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdata2Left  = VARS::post("selectdata2Left");
        $this->selectdata2Right = VARS::post("selectdata2Right");
        $this->selectdata2      = VARS::post("selectdata2");
        $this->selectdata3      = VARS::post("selectdata3");
        $this->selectdataJview  = VARS::post("selectdataJview");
        $this->cmd              = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->field["SEL_SCHOOL_KIND"] = VARS::post("SEL_SCHOOL_KIND");
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->subclassAll = "00-00-00-000000";
        } else {
            $this->subclassAll = "000000";
        }
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択してください。";
            return false;
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        //パーツ
        if ($this->cmd == 'update') {
            $result = knjz350aQuery::getUpdateQuery($this);
        //管理者コントロール
        } else if ($this->cmd == 'update2') {
            $result = knjz350aQuery::getUpdateControlQuery($this);
        //出欠・実力テスト・観点
        } else if ($this->cmd == 'update3' || $this->cmd == 'update4' || $this->cmd == 'updateJview') {
            $result = knjz350aQuery::getUpdateAttendProficiencyQuery($this);
        }
        return $result;
    }
}
?>
