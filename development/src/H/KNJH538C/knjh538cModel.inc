<?php

require_once('for_php7.php');

class knjh538cModel extends Model
{
    public $cmd;
    public $field = array();
    public $reset = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjh538cModel()
    {
        $this->field = array();
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        common::GetControlMaster_Fnc($this->control);

        /* 編集対象データを取得 */
        $this->field["SCHREGNO"]                = VARS::post("SCHREGNO");                //学籍番号
        $this->field["SCORE"]                   = VARS::post("SCORE");                   //得点
        $this->field["RANK"]                    = VARS::post("RANK");                    //席次
        $this->field["DEVIATION"]               = VARS::post("DEVIATION");               //偏差値
        $this->field["SEMESTER"]                = VARS::post("SEMESTER");                //学期
        $this->field["GRADE"]                   = VARS::post("GRADE");                   //年
        $this->field["PROFICIENCYDIV"]          = VARS::post("PROFICIENCYDIV");          //データ種別
        $this->field["PROFICIENCY_TARGET"]      = VARS::post("PROFICIENCY_TARGET");      //テスト
        $this->field["PROFICIENCY_SUBCLASS_CD"] = VARS::post("PROFICIENCY_SUBCLASS_CD"); //実力科目
        $this->field["CHAIRCD"]                 = VARS::post("CHAIRCD");                 //講座

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->reset = array();
        if (is_array($this->field["SCHREGNO"])) {
            foreach ($this->field["SCHREGNO"] as $key => $schregno) {
                $this->reset[$schregno] = array("SCORE"     => $this->field["SCORE"][$key],
                                                "RANK"      => $this->field["RANK"][$key],
                                                "DEVIATION" => $this->field["DEVIATION"][$key]);
            }
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /* 入力項目チェック */
    public function validateInput()
    {
        if (!isset($this->field["SCHREGNO"])) {
            $this->setMessage("MSG304");
            return false;
        }
        if ("" == $this->field["PROFICIENCY_SUBCLASS_CD"]) {
            $this->setMessage("MSG304", "(実力科目)");
            return false;
        }
        return true;
    }

    /* 更新処理 */
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    public function &update()
    {
        $result = knjh538cQuery::getUpdateQuery($this);
        return $result;
    }
}
