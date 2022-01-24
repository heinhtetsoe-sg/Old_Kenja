<?php

require_once('for_php7.php');
class knje716Model extends Model
{
    public $slipremark = array();
    public $field = array();
    public $cmd;

    public function knje716Model()
    {
    }

    public function init()
    {
        //学年クラス名
        $this->field = array("GRADE_HRCLASS_NAME" => VARS::post("GRADE_HRCLASS_NAME"));

        //cmd取得
        $this->cmd = VARS::request("cmd");

        //生徒情報と所見を取得
        for ($i = 0; $i < VARS::post("studentCount"); $i++) {
            $this->field["SCHREGNO"][]   = VARS::post("SCHREGNO" . "-" . $i);
            $this->field["SLIPREMARK"][] = VARS::post("SLIPREMARK" . "-" . $i);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        $gyou = 4;
        $moji = 25;
        $this->slipremark = $this->field["SLIPREMARK"];
        foreach ($this->field["SCHREGNO"] as $i => $schregno) {
            $this->slipremark[$i] = rtrim($this->slipremark[$i]);
            if ($this->validate_row_cnt($this->slipremark[$i], ($moji * 2)) > $gyou || strlen($this->slipremark[$i]) > 300) {
                $this->setWarning("MSG915", "{$moji}×{$gyou}行までです。");
                return false;
            }
        }
        return true;
    }

    //更新
    public function getUpdateModel()
    {
        if ($this->validateInput() && knje716Query::update($this)) {
            $this->setMessage("MSG201");
        }
    }
}
