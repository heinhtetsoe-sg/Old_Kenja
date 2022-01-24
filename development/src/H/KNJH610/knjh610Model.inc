<?php

require_once('for_php7.php');

class knjh610Model extends Model
{
    public $field      = array();
    public $cmd;
    public $center_class_cd = "";           //primary
    public $center_subclass_cd = "";        //primary
    public $thisIsGet = false;

    public function knjh610Model()
    {
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("CENTER_CLASS_CD"    =>  VARS::post("CENTER_CLASS_CD"),
                             "CENTER_SUBCLASS_CD" =>  VARS::post("CENTER_SUBCLASS_CD"),
                             "BUNRIDIV"           =>  VARS::post("BUNRIDIV"),
                             "SUBCLASS_NAME"      =>  VARS::post("SUBCLASS_NAME"),
                             "SUBCLASS_ABBV"      =>  VARS::post("SUBCLASS_ABBV"),
                             "CLASSCD"            =>  VARS::post("CLASSCD"),
                             "SUBCLASSCD"         =>  VARS::post("SUBCLASSCD"),
                             "SUBCLASS_DIV"       =>  VARS::post("SUBCLASS_DIV"),
                             "PERFECT"            =>  VARS::post("PERFECT"),
                             "ALLOT_POINT"        =>  VARS::post("ALLOT_POINT")
                             );

        $this->cmd          = VARS::request("cmd");
        $temp = VARS::get("CENTER_SUBCLASS_CD");
        if (isset($temp)) {
            $this->center_class_cd = VARS::get("CENTER_CLASS_CD");
            $this->center_subclass_cd = VARS::get("CENTER_SUBCLASS_CD");
            $this->thisIsGet = true;
        } else {
            $this->thisIsGet = false;
        }
        if ($this->cmd == "firstEdit") {
            unset($this->center_class_cd);
            unset($this->center_subclass_cd);
        }
    }

    //前年度からのコピーのモデルを返します
    public function &getCopyModel()
    {
        $cnt  = knjh610Query::getCopyCountQuery();
        $cnt2 = knjh610Query::getCopyCount2();
        if ($cnt2 != '0') {
            $this->setMessage('すでにデータがあります。');
        } elseif ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            $result = knjh610Query::getCopyDelClassQuery();
            $result = knjh610Query::getCopyClassQuery();
            $result = knjh610Query::getCopyDelSubQuery();
            $result = knjh610Query::getCopySubQuery();
            $this->setMessage('MSG201', 'コピー件数：'.$cnt.'件');
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //キーのチェック
        if (trim($this->field["CENTER_CLASS_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(共通テスト教科コード)");
            return false;
        }
        if (trim($this->field["CENTER_SUBCLASS_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(共通テスト科目コード)");
            return false;
        }
        if (strlen($this->field["SUBCLASS_NAME"]) == 0) {
            $this->setWarning("MSG301", "(共通テスト科目名称)");
            return false;
        }
        if (strlen($this->field["SUBCLASS_ABBV"]) == 0) {
            $this->setWarning("MSG301", "(科目略称)");
            return false;
        }
        if (strlen($this->field["CLASSCD"]) == 0 && $this->Properties["usePerfSubclasscd_Touroku"] == '1') {
            $this->setWarning("MSG301", "(教科コード)");
            return false;
        }

        if (($this->center_class_cd == "" || $this->center_subclass_cd == "") && $this->cmd == "update") {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->center_class_cd != $this->field["CENTER_CLASS_CD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }
        if ($this->cmd == "update" && $this->center_subclass_cd != $this->field["CENTER_SUBCLASS_CD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }
        //１レコード取得
        $db = Query::dbCheckOut(); //DB接続
        $row = knjh610Query::getRow($this->field["CENTER_SUBCLASS_CD"], $db, $this, $this->field["CENTER_CLASS_CD"]);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(共通テスト科目コード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["SUBCLASS_NAME"] = array("LEN" => 60, "MESAGE" => "(共通テスト科目名称)");
        $checkStrLen["SUBCLASS_ABBV"] = array("LEN" => 15,  "MESAGE" => "(科目略称)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        return true;
    }
    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->center_class_cd    = $this->field["CENTER_CLASS_CD"];
            $this->center_subclass_cd = $this->field["CENTER_SUBCLASS_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    public function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->center_class_cd == "" || $this->center_subclass_cd == "") {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if ($this->center_class_cd != $this->field["CENTER_CLASS_CD"] || $this->center_subclass_cd != $this->field["CENTER_SUBCLASS_CD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }
        $db = Query::dbCheckOut(); //DB接続
        $row = knjh610Query::getRow($this->center_subclass_cd, $db, $this, $this->center_class_cd);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        knjh610Query::getDeleteQuery($this->field);
        $this->center_class_cd = "";
        $this->center_subclass_cd = "";
        $this->setMessage("MSG201");
        
        $this->field = array();
    }

    //インサート
    public function &insert()
    {
        $result = knjh610Query::getInsertQuery($this->field, $this);
        return $result;
    }
    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjh610Query::getUpdateQuery($this->field, $this);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
