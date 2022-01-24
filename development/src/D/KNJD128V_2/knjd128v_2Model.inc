<?php

require_once('for_php7.php');


class knjd128v_2Model extends Model
{
    public $field = array();
    public $data  = array();
    public $testcdArray  = array();
    public $cmd;
    public $usePerfect;
    public $chikokuHyoujiFlg;
    public $useSlumpHyouka;
    public $prgid;
    public $auth;
    public $subclassAll;
    public $z010name1;
    public $lastColumn;//CSVの最後の列の値

    public function knjd128v_2Model()
    {
    }

    public function init()
    {
        if (VARS::get("prgid")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            common::GetControlMaster_Fnc($this->control);
        }

        if (VARS::get("prgid")) {
            $this->prgid = VARS::get("prgid");
        }
        if (VARS::get("auth")) {
            $this->auth = VARS::get("auth");
        }

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

        //追指導回数
        $this->field["SLUMP_SEQ"] = VARS::post("SLUMP_SEQ");
        if ($this->cmd == "update") {
            $this->field["SLUMP_SEQ"] = VARS::post("H_SLUMP_SEQ");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjd128v_2Query::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける
        //学校名取得
        $this->z010name1 = knjd128v_2Query::getNameMstZ010();

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
                $controlFlg = $codeArray["CONTROL_FLG"];

                //管理者コントロール
                if ($controlFlg == "1") {
                    $score = "SCORE".$testcd."-".$i;
                    $this->fields["SCORE".$testcd][] = VARS::post($score);

                    $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);

                    //成績が１つでも入力された考査コードを配列に保管
                    if (strlen(VARS::post($score))) {
                        $this->testInsArray[$testcd] = $testcd;
                    }
                }
            }

            //指導データ
            $this->fields["SCORE_MARK"][]   = VARS::post("SCORE_MARK"."-".$i);
            $this->fields["REMARK"][]       = VARS::post("REMARK"."-".$i);
            $this->fields["PASS_FLG"][]     = VARS::post("PASS_FLG"."-".$i);
        }

        if ($this->validateInput()) {
            knjd128v_2Query::update($this);
            knjd128v_2Query::updateRecordChkfin($this);
            knjd128v_2Query::updateSlump($this);
            if ($this->Properties["useSlumpSeq"] == '1') {
                //RECORD_SLUMP_SDIV_DATへ反映（追指導回数のMAXの物）
                knjd128v_2Query::updateSlumpMax($this);
            }
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
