<?php

require_once('for_php7.php');

class knjh530_2Model extends Model {
    var $field      = array();
    var $cmd;

    function knjh530_2Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $temp = VARS::get("PROFICIENCYCD");
        $this->field = array("PROFICIENCYDIV"   => isset($temp) ? VARS::get("PROFICIENCYDIV") : VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD"    => isset($temp) ? VARS::get("PROFICIENCYCD") : VARS::post("PROFICIENCYCD"),
                             "PROFICIENCYNAME1" => VARS::post("PROFICIENCYNAME1"),
                             "PROFICIENCYNAME2" => VARS::post("PROFICIENCYNAME2"),
                             "PROFICIENCYNAME3" => VARS::post("PROFICIENCYNAME3"),
                             "COUNTFLG"         => VARS::post("COUNTFLG")
                             );
        if (VARS::get("mode") == "1" || VARS::get("PROGRAMID")){
            unset($this->proficiencyDiv);
            unset($this->proficiencyCd);
        }
        if (VARS::get("year_code")){
            $this->parentParam = array("year_code"              => VARS::get("year_code"),
                                       "semester_code"          => VARS::get("semester_code"),
                                       "grade_code"             => VARS::get("grade_code"),
                                       "proficiencydiv_code"    => VARS::get("proficiencydiv_code"),
                                       "year_add_code"          => VARS::get("year_add_code"),
                                       );
            unset($this->proficiencyDiv);
            unset($this->proficiencyCd);
        }
        $temp = VARS::get("PROFICIENCYDIV");
        if (isset($temp)){
            $this->proficiencyDiv = VARS::get("PROFICIENCYDIV");
            $this->proficiencyCd  = VARS::get("PROFICIENCYCD");
        }
    }

    //入力項目チェック
    function validateInput()
    {

        //キーのチェック
        if (trim($this->field["PROFICIENCYDIV"]) == "" || trim($this->field["PROFICIENCYCD"]) == "") {
            $this->setWarning("MSG301");
            return false;
        }

        if (strlen($this->field["PROFICIENCYNAME1"]) == 0) {
            $this->setWarning("MSG301", "(実力テスト名称)");
            return false;
        }
        if ($this->cmd == "update" && 
            ($this->proficiencyDiv != $this->field["PROFICIENCYDIV"] ||
             $this->proficiencyCd != $this->field["PROFICIENCYCD"])
         ) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $query = knjh530_2Query::getProficiency($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        $checkStrLen["PROFICIENCYNAME1"] = array("LEN" => 60, "MESAGE" => "(実力テスト名称)");
        $checkStrLen["PROFICIENCYNAME2"] = array("LEN" => 60, "MESAGE" => "(実力テスト略称１)");
        $checkStrLen["PROFICIENCYNAME3"] = array("LEN" => 60, "MESAGE" => "(実力テスト略称２)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->proficiencyDiv = $this->field["PROFICIENCYDIV"];
            $this->proficiencyCd = $this->field["PROFICIENCYCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        if ($this->proficiencyDiv != $this->field["PROFICIENCYDIV"] ||
            $this->proficiencyCd != $this->field["PROFICIENCYCD"]
         ) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }

        if (knjh530_2Query::IsExisting($this->field)){
            $this->setWarning("MSG919", "年度マスタに登録されています。");
            return;
        }

        knjh530_2Query::getDeleteQuery($this->field);
        $this->proficiencyCd = "";
        $this->proficiencyDiv = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjh530_2Query::getInsertQuery($this->field);
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh530_2Query::getUpdateQuery($this->field);
        return $result;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
