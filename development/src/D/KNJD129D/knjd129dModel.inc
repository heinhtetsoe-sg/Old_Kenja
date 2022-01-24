<?php

require_once('for_php7.php');

class knjd129dModel extends Model
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
    public $lastColumn;//CSVの最後の列の値

    public function knjd129dModel()
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
        $this->school_kind = "J";
        $this->subclassAll = "00-{$this->school_kind}-00-000000";
        //学校名取得
        $this->z010name1 = knjd129dQuery::getNameMstZ010();
        //欠席記号
        $this->kessekiMarks = ($this->z010name1 == "nichi-ni") ? array("*", "**") : array("*");
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;

        //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
        $rtnRow = knjd129dQuery::getCalcMethod();
        $this->CalcMethod = !strlen($rtnRow["SCHOOL_REMARK1"]) ? "1" : $rtnRow["SCHOOL_REMARK1"];

        $rtnRow = knjd129dQuery::getNameMstSettei("D067", "01");
        $this->noUseSouGaku = $rtnRow["NAMESPARE3"];//1:総学非表示（９０未満を表示とする）

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->z010name1 == "nichi-ni") {
            //テスト種別が中間/期末素点/評価/評定以外の時にスコアが*または**ならエラー
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $scoreDiv = substr($testcd, 5, 2);
                if (in_array($scoreDiv, array('01', '08', '09'))) {
                    continue;
                }

                for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
                    $score = $this->fields["SCORE".$testcd][$i];
                    if ($score == "*" || ($this->z010name1 == "nichi-ni" && $score == "**")) {
                        $addStr = $this->z010name1 == "nichi-ni" ? "および**" : "" ;
                        $errMsg = "*".$addStr."は各学期の「素点」「評価」「評定」列にのみ入力してください。";

                        $this->setWarning("MSG901", $errMsg);
                        return false;
                    }
                }
            }
        }

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

                $score = "SCORE".$testcd."-".$i;
                $this->fields["SCORE".$testcd][] = VARS::post($score);
                $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);

                if ($testcd == "9990009") {
                    //仮評定フラグ
                    $this->fields["PROV_FLG"][] = VARS::post("PROV_FLG"."-".$i);
                }

                //成績が１つでも入力された考査コードを配列に保管
                if (strlen(VARS::post($score))) {
                    $this->testInsArray[$testcd] = $testcd;
                }
            }
        }

        if ($this->validateInput()) {
            knjd129dQuery::update($this);
            knjd129dQuery::updateRecordChkfin($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
