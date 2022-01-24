<?php

require_once('for_php7.php');


class knjd129nModel extends Model {
    var $field = array();
    var $data  = array();
    var $testcdArray  = array();
    var $testcdMotoArray = array();
    var $cmd;
    var $usePerfect;
    var $chikokuHyoujiFlg;
    var $school_kind;
    var $subclassAll;
    var $z010name1;
    var $isKeiai;
    var $isKasiwara;

    function knjd129nModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            $this->testcdMotoArray = array();
            common::GetControlMaster_Fnc($this->control);
            $this->dispZumi = false;
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        $this->cmd     = VARS::request("cmd");
        if ($this->cmd == "disp") {
            $this->dispZumi = $this->dispZumi ? false : true;
        }
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
        //学校情報を取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjd129nQuery::getNameMstZ010();
        $this->z010name1    = $rtnRow["NAME1"];
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";
        //各種設定を取得
        $rtnRow = knjd129nQuery::getNameMstSettei("D067", "01");
        $this->noUseSouGaku     = $rtnRow["NAMESPARE1"];//1:総学非表示（９０未満を表示とする）
        $this->noUseBtnAttend   = $rtnRow["NAMESPARE2"];//1:出欠入力ボタン非表示
        $this->wordBtnPrint     = $rtnRow["ABBV1"] != "" ? $rtnRow["ABBV1"] : "小 表";//小表ボタン文言
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
            $this->fields["SUPP_SCORE"][] = VARS::post("SUPP_SCORE"."-".$i);
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];

                $this->fields["SCORE".$testcd][] = VARS::post("SCORE".$testcd."-".$i);

                //履修単位・修得単位
                if ($testcd == "9990009") {
                    $this->fields["COMP_CREDIT"][] = VARS::post("COMP_CREDIT"."-".$i);
                    $this->fields["GET_CREDIT"][] = VARS::post("GET_CREDIT"."-".$i);
                }
            }
        }

        if ($this->validateInput()) {
            knjd129nQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
