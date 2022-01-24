<?php

require_once('for_php7.php');


class knjd129bModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $usePerfect;
    var $useRecordChkfinDat;
    var $show_all;          //処理済表示フラグ

    function knjd129bModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->show_all      = VARS::post("shw_flg");
        $this->cmd     = VARS::request("cmd");
        $this->field["GRADE"] = VARS::post("GRADE");
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        $this->field["TESTKIND"] = VARS::post("TESTKIND");
        $this->field["CLASSCD"] = VARS::post("CLASSCD");
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["GRADE"] = VARS::post("H_GRADE");
            $this->field["SEMESTER"] = VARS::post("H_SEMESTER");
            $this->field["TESTKIND"] = VARS::post("H_TESTKIND");
            $this->field["CLASSCD"] = VARS::post("H_CLASSCD");
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数
        //見込点指定チェック
        $this->fields = array();
        if ($this->cmd == "calculate") {
            for ($i = 0; $i < $this->field["COUNT"]; $i++) {
                $this->fields["CHK_BOX"][] = VARS::post("CHK_BOX"."-".$i);
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->useRecordChkfinDat = (strlen($this->Properties["useRecordChkfinDat"])) ? $this->Properties["useRecordChkfinDat"] : 0;
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        //配列
        $testcdArray = array("10101" => "SEM1_INTR_SCORE"
                            ,"10201" => "SEM1_TERM_SCORE"
                            ,"19900" => "SEM1_VALUE"
                            ,"20101" => "SEM2_INTR_SCORE"
                            ,"20201" => "SEM2_TERM_SCORE"
                            ,"29900" => "SEM2_VALUE"
                            ,"30101" => "SEM3_INTR_SCORE"
                            ,"30201" => "SEM3_TERM_SCORE"
                            ,"39900" => "SEM3_VALUE");
        //指定学期テスト
        $testcd = $this->field["SEMESTER"].$this->field["TESTKIND"];
        //初期化
        $this->fields = array();
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            $chk_box    = "CHK_BOX"."-".$i;
            $score_pass = $testcdArray[$testcd]."-".$i;
            $score      = $testcdArray[$testcd]."_SCORE"."-".$i;
            $this->fields["CHK_BOX"][]      = VARS::post($chk_box);     //見込点指定チェック
            $this->fields["SCORE_PASS"][]   = VARS::post($score_pass);  //見込点
            $this->fields["SCORE"][]        = VARS::post($score);       //調査用
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
        }

        if ($this->validateInput()) {
            knjd129bQuery::updateSuppExaDat($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
