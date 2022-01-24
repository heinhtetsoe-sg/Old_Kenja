<?php

require_once('for_php7.php');


class knjd128v_sankouModel extends Model {
    var $field = array();
    var $data  = array();
    var $testcdArray  = array();
    var $cmd;
    var $usePerfect;
    var $chikokuHyoujiFlg;
    var $useSlumpHyouka;
    var $prgid;
    var $auth;
    var $subclassAll;
    var $z010name1;

    function knjd128v_sankouModel() {
    }

    function init() {
        if (VARS::get("prgid")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            common::GetControlMaster_Fnc($this->control);
        }

        if (VARS::get("prgid")) $this->prgid = VARS::get("prgid");
        if (VARS::get("auth")) $this->auth = VARS::get("auth");
        $this->info_seq = "001"; //001:参考点入力

        $this->cmd     = VARS::request("cmd");
        $this->field["COURSE_MAJOR"] = VARS::request("COURSE_MAJOR"); //課程学科コンボ
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        $this->field["CHAIRCD"] = VARS::request("CHAIRCD");
        if ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        }
        $this->field["TESTCD"] = VARS::request("TESTCD");
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjd128v_sankouQuery::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける
        //学校名取得
        $this->z010name1 = knjd128v_sankouQuery::getNameMstZ010();
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $this->fields["SANKOU"][]   = VARS::post("SANKOU"."-".$i);
        }
        if ($this->validateInput()) {
            knjd128v_sankouQuery::updateRecordInfo($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
