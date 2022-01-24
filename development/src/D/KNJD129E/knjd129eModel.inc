<?php

require_once('for_php7.php');


class knjd129eModel extends Model {
    var $field = array();
    var $data  = array();
    var $testcdArray  = array();
    var $testcdMotoArray = array();
    var $cmd;
    var $usePerfect;
    var $chikokuHyoujiFlg;
    var $school_kind;
    var $subclassAll;

    function knjd129eModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            $this->testcdMotoArray = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        $this->cmd     = VARS::request("cmd");
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::request("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::request("CHAIRCD");
        } else if ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        $this->field["PROV_FLG_ALL"]  = VARS::post("PROV_FLG_ALL"); //仮評定フラグALLチェックボックス
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        //プロパティファイル
        $this->getPropertiesAll();
        //学校種別(H:高校 J:中学 P:小学)
        $this->school_kind = "H";
        $this->subclassAll = "00-{$this->school_kind}-00-000000";
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        $this->testInsArray = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];

                $score = "SCORE".$testcd."-".$i;
                $this->fields["SCORE".$testcd][] = VARS::post($score);
                $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);

                //履修単位・修得単位
                if ($testcd == "9990009") {
                    $this->fields["COMP_CREDIT"][] = VARS::post("COMP_CREDIT"."-".$i);
                    $this->fields["GET_CREDIT"][] = VARS::post("GET_CREDIT"."-".$i);
                    //仮評定フラグ
                    $this->fields["PROV_FLG"][] = VARS::post("PROV_FLG"."-".$i);
                }

                //成績が１つでも入力された考査コードを配列に保管
                if (strlen(VARS::post($score))) $this->testInsArray[$testcd] = $testcd;
                //履修単位・修得単位
                if ($testcd == "9990009") {
                    if (strlen(VARS::post("COMP_CREDIT"."-".$i))) $this->testInsArray[$testcd] = $testcd;
                    if (strlen(VARS::post("GET_CREDIT"."-".$i))) $this->testInsArray[$testcd] = $testcd;
                }
            }
        }

        if ($this->validateInput()) {
            knjd129eQuery::update($this);
            knjd129eQuery::updateRecordChkfin($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
