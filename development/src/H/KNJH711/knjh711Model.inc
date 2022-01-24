<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh711Model extends Model
{
    public $cmd;
    public $capacity;

    /**
     * メンバ変数の初期化
     */
    public function knjh711Model()
    {
        $this->capacity = 0;
    }

    public function init()
    {
        $this->field = array("TESTID"     => VARS::post("TESTID"),     //テスト
                             "PERIODID"   => VARS::post("PERIODID"),   //時限
                             "SUBCLASSCD" => VARS::post("SUBCLASSCD"), //科目
                             "FACCD"      => VARS::post("FACCD"),      //教室
                             "HR_CLASS"   => VARS::post("HR_CLASS")    //クラス
        );

        $this->selectdata = VARS::post("selectdata");
        $this->cmd        = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();
        //時限、教室、生徒での重複チェック
        $rowCnt = $db->getOne(knjh711Query::duplicateCheck($this));
        if ($rowCnt != "0") {
            $this->setWarning("MSG302");
            return false;
        }
        Query::dbCheckIn($db);
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    public function getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    public function update()
    {
        knjh711Query::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
