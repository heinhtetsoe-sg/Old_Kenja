<?php

require_once('for_php7.php');


class knjd129mModel extends Model
{
    public $field = array();
    public $data  = array();
    public $testcdArray  = array();
    public $cmd;
    public $usePerfect;
    public $chikokuHyoujiFlg;
    public $useSlumpHyouka;
    public $schoolKind;
    public $subclassAll;
    public $fieldsCalc = array();
    public $testcdMotoArray = array();

    public function knjd129mModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            $this->testcdMotoArray = array();
            common::GetControlMaster_Fnc($this->control);
            $this->dispZumi = false;
        }
        $this->cmd     = VARS::request("cmd");
        if ($this->cmd == "disp") {
            $this->dispZumi = $this->dispZumi ? false : true;
        }
        $this->field = array("SEMESTER"         => VARS::post("SEMESTER"),
                             "GRADE"            => VARS::post("GRADE"),
                             "COURSE"           => VARS::post("COURSE"),
                             "TESTIK"           => VARS::post("TESTIK"),
                             "SUBCLASS_CD"      => VARS::post("SUBCLASS_CD"),
                             );
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        $this->TYUKAN = "010101";
        $this->KIMATU = "020101";

        //プロパティファイル
        $this->getPropertiesAll();

        //学校種別(H:高校 J:中学 P:小学)
        $this->schoolKind = "H";

        //教育課程対応
        $this->subclassAll = "00-{$this->schoolKind}-00-000000";

        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;

        //算出ボタン押し下げ時のみ保持（それ以外は初期化）
        $this->fieldsCalc = array();
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->field["COUNTER"] == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    public function &getUpdateModel()
    {
        //学籍番号を更新時に取得
        $this->data["KEY"] = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            $this->data["KEY"][] = VARS::post("KEY"."_".$i);
        }
        //初期化
        $this->fields = array();
        $this->testInsArray = array();
        foreach ($this->data["KEY"] as $schSub) {
            if (VARS::post("CHK_BOX_".$schSub) == "1") {
                foreach ($this->testcdArray as $key => $codeArray) {
                    $testcd = $codeArray["TESTCD"];
                    if ($testcd == $this->field["SEMESTER"].str_replace("-", "", $this->field["TESTIK"])) {
                        $this->fields[$schSub][$testcd] = VARS::post("SCORE_".$schSub.$testcd);
                    }
                }
            }
        }

        if ($this->validateInput()) {
            knjd129mQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
