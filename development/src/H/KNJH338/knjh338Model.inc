<?php

require_once('for_php7.php');

class knjh338Model extends Model
{
    public $cmd;
    public $field = array();
    public $reset = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjh338Model()
    {
        $this->field = array();
    }

    public function init()
    {
        $this->cmd               = VARS::request("cmd");
        /* 編集対象データを取得 */
        $this->field["SCHREGNO"]         = VARS::post("SCHREGNO");          //学籍番号
        $this->field["SCORE"]            = VARS::post("SCORE");             //得点
        $this->field["RANK"]             = VARS::post("RANK");              //席次
        $this->field["DEVIATION"]        = VARS::post("DEVIATION");         //偏差値
        $this->field["SEMESTER"]         = VARS::post("SEMESTER");          //学期
        $this->field["GRADE_HR_CLASS"]   = VARS::post("GRADE_HR_CLASS");    //年組
        $this->field["DATA_DIV"]         = VARS::post("DATA_DIV");          //データ種別
        $this->field["MOCK_TARGET"]      = VARS::post("MOCK_TARGET");       //テスト
        $this->field["MOCK_SUBCLASS_CD"] = VARS::post("MOCK_SUBCLASS_CD");  //模試科目

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
        if ("" == $this->field["MOCK_SUBCLASS_CD"]) {
            $this->setMessage("MSG304", "(模試科目)");
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
        $result = knjh338Query::getUpdateQuery($this);
        return $result;
    }

    /* 削除処理 */
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            if ($this->delete()) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    public function &delete()
    {
        $result = knjh338Query::getDeleteQuery($this);
        return $result;
    }
}
