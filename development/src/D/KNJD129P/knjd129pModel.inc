<?php

require_once('for_php7.php');


class knjd129pModel extends Model
{
    public $field = array();
    public $data  = array();
    public $testcdArray  = array();
    public $cmd;
    public $usePerfect;
    public $chikokuHyoujiFlg;
    public $useSlumpHyouka;
    public $subclassAll;
    public $z010name1;
    public $lastColumn;//CSVの最後の列の値

    public function knjd129pModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        $this->cmd     = VARS::request("cmd");
        $this->field["COURSE_MAJOR"] = VARS::post("COURSE_MAJOR"); //課程学科コンボ
        $this->field["SUBCLASS_DIV"] = VARS::post("SUBCLASS_DIV"); //科目ラジオ
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
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjd129pQuery::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける
        //学校名取得
        $this->z010name1 = knjd129pQuery::getNameMstZ010();

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
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $controlFlg = $codeArray["CONTROL_FLG"];

                //管理者コントロール
                if ($controlFlg == "1") {
                    $score = "SCORE".$testcd."-".$i;
                    $this->fields["SCORE".$testcd][] = VARS::post($score);
                    //成績が１つでも入力された考査コードを配列に保管
                    if (strlen(VARS::post($score)) || ($testcd == "9990009" && (strlen(VARS::post("COMP_CREDIT"."-".$i)) || strlen(VARS::post("GET_CREDIT"."-".$i))))) {
                        $this->testInsArray[$testcd] = $testcd;
                    }
                    //履修単位・修得単位
                    if ($testcd == "9990009") {
                        $this->fields["COMP_CREDIT"][] = VARS::post("COMP_CREDIT"."-".$i);
                        $this->fields["GET_CREDIT"][] = VARS::post("GET_CREDIT"."-".$i);
                        //仮評定フラグ
                        $this->fields["PROV_FLG"][] = VARS::post("PROV_FLG"."-".$i);
                    }

                    $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);
                }
            }
        }

        if ($this->validateInput()) {
            knjd129pQuery::update($this);
            knjd129pQuery::updateRecordChkfin($this);
            $this->setMessage("MSG201");
        }
    }
}
