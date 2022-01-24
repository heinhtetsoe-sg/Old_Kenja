<?php

require_once('for_php7.php');


class knjm432kModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $chikokuHyoujiFlg;
    public $useSlumpHyouka;
    public $subclassAll;
    public $subD065;
    public $lastColumn;//CSVの最後の列の値

    public function knjm432kModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
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
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjm432kQuery::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける
        //D065登録科目か
        $this->subD065 = VARS::post("SUB_D065");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //編集可能になっている値のみ取得
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //未入力チェック
            if ($this->fields["SCORE"][$i] != "") {
                //数値もしくは*
                if ($this->fields["SCORE"][$i] != '*') {
                    if (!is_numeric($this->fields["SCORE"][$i])) {
                        $this->setWarning("0点～100点以内もしくは*を入力してください。");
                        return false;
                    }
                    if ($this->fields["SCORE"][$i] > 100) {
                        $this->setWarning("MSG914");
                        return false;
                    }
                    if ($this->fields["SCORE"][$i] < 0) {
                        $this->setWarning("MSG914");
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
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $testcd = "9990009";
            //成績が１つでも入力された考査コードを配列に保管
            if (strlen(VARS::post($score)) || ($testcd == "9990009" && (strlen(VARS::post("COMP_CREDIT"."-".$i)) || strlen(VARS::post("GET_CREDIT"."-".$i))))) {
                $this->testInsArray[$testcd] = $testcd;
            }
            $this->fields["PROV_FLG"][] = VARS::post("PROV_FLG"."-".$i);
            $this->fields["SCORE"][] = VARS::post("SCORE"."-".$i);
            $this->fields["COMP_CREDIT"][] = VARS::post("COMP_CREDIT"."-".$i);
            $this->fields["GET_CREDIT"][] = VARS::post("GET_CREDIT"."-".$i);
        }

        if ($this->validateInput()) {
            knjm432kQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
