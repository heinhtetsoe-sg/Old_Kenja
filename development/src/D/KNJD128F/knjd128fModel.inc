<?php

require_once('for_php7.php');


class knjd128fModel extends Model
{
    public $field = array();
    public $data  = array();
    public $testcdArray  = array();
    public $cmd;
    public $subclassAll;
    public $lastColumn;//CSVの最後の列の値

    public function knjd128fModel()
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
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        //プロパティファイル
        $this->getPropertiesAll();
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->subclassAll = "00-00-00-000000";
        } else {
            $this->subclassAll = "000000";
        }

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
                $score = "SCORE".$testcd."-".$i;

                $this->fields["SCORE".$testcd][] = VARS::post($score);
                $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);

                //成績が１つでも入力された考査コードを配列に保管
                if (strlen(VARS::post($score))) {
                    $this->testInsArray[$testcd] = $testcd;
                }
            }
        }

        if ($this->validateInput()) {
            knjd128fQuery::update($this);
            knjd128fQuery::updateRecordChkfin($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
