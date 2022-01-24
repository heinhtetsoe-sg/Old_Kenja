<?php

require_once('for_php7.php');

//定数定義

class knjz402j_3Model extends Model {
    var $cmd;
    var $subclasscd = "";    //primary
    var $year_code = "";
    //教育課程対応
    var $classcd = "";
    var $school_kind = "";
    var $curriculum_cd = "";

    //メンバ変数の初期化
    function knjz402j_3Model()
    {
    }

    function init()
    {
        $this->cmd             = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        if (VARS::get("year_code")){
            unset($this->subclasscd);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->classcd);
                unset($this->school_kind);
                unset($this->curriculum_cd);
            }
            unset($this->schkind);
            $this->fields = array();
        }
        if (VARS::get("mode") == "1"){
            unset($this->subclasscd);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->classcd);
                unset($this->school_kind);
                unset($this->curriculum_cd);
            }
            unset($this->schkind);
            $this->fields = array();
        }
        if (VARS::get("SUBCLASSCD") != ""){
            $this->subclasscd = VARS::get("SUBCLASSCD");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd = VARS::get("CLASSCD");
                $this->school_kind = VARS::get("SCHOOL_KIND");
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
        }
        //左画面
        if (VARS::post("SCHKIND")) {
            $this->schkind = VARS::post("SCHKIND");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        if ($this->subclasscd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        $this->update();
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $this->fields = array();
        $setWeightValue = 0;
        for ($i = 0; $i < get_count($this->data["VIEWCD"]); $i++) {
            (int)$this->fields["WEIGHT"][$i]    = (int)VARS::post("WEIGHT"."_".$i);
            (int)$setWeightValue += (int)$this->fields["WEIGHT"][$i];
        }
        if ($setWeightValue > 100) {
            $this->setWarning("MSG203", "重みの合計が100を超えています。");
            return false;
        }
        knjz402j_3Query::getUpdateQuery($this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
