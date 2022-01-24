<?php

require_once('for_php7.php');


class knjd129lModel extends Model
{
    public $field = array();
    public $data  = array();
    public $testcdArray  = array();
    public $testcdMotoArray = array();
    public $cmd;
    public $usePerfect;
    public $chikokuHyoujiFlg;
    public $school_kind;
    public $subclassAll;
    public $z010name1;
    public $isKeiai;
    public $isKasiwara;
    public $useCsv;
    public $lastColumn;//CSVの最後の列の値

    public function knjd129lModel()
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
        } elseif ($this->cmd == "update") {
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
        //「ＣＳＶは、非表示とする？」のフラグを取得
        $rtnRow = knjd129lQuery::getNameMstD058();
        $this->useCsv = $rtnRow["NAMESPARE1"] == "1" ? false : true; // 1:非表示
        //学校情報を取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjd129lQuery::getNameMstZ010();
        $this->z010name1    = $rtnRow["NAME1"];
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";
        //各種設定を取得
        $rtnRow = knjd129lQuery::getNameMstSettei("D067", "01");
        $this->noUseSouGaku     = $rtnRow["NAMESPARE1"];//1:総学非表示（９０未満を表示とする）
        $this->noUseBtnAttend   = $rtnRow["NAMESPARE2"];//1:出欠入力ボタン非表示
        $this->wordBtnPrint     = $rtnRow["ABBV1"] != "" ? $rtnRow["ABBV1"] : "小 表";//小表ボタン文言

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        $this->testInsArray = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];

                //自動計算の値を保持
                $this->fields["SCORE_KEEP".$testcd][] = VARS::post("SCORE_KEEP".$testcd."-".$i);

                $this->fields["SCORE".$testcd][] = VARS::post("SCORE".$testcd."-".$i);
                $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);

                //履修単位・修得単位
                if ($testcd == "9990009") {
                    $this->fields["COMP_CREDIT"][] = VARS::post("COMP_CREDIT"."-".$i);
                    $this->fields["GET_CREDIT"][] = VARS::post("GET_CREDIT"."-".$i);
                    //仮評定フラグ
                    $this->fields["PROV_FLG"][] = VARS::post("PROV_FLG"."-".$i);
                }

                //成績が１つでも入力された考査コードを配列に保管
                if (strlen(VARS::post("SCORE".$testcd."-".$i))) {
                    $this->testInsArray[$testcd] = $testcd;
                }
                //履修単位・修得単位
                if ($testcd == "9990009") {
                    if (strlen(VARS::post("COMP_CREDIT"."-".$i))) {
                        $this->testInsArray[$testcd] = $testcd;
                    }
                    if (strlen(VARS::post("GET_CREDIT"."-".$i))) {
                        $this->testInsArray[$testcd] = $testcd;
                    }
                }
            }
        }

        if ($this->validateInput()) {
            knjd129lQuery::update($this);
            knjd129lQuery::updateRecordChkfin($this);
            $this->setMessage("MSG201");
        }
    }
}
