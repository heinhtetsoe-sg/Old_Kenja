<?php

require_once('for_php7.php');

class knjz422Model extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;          //セキュリティー用スタッフコード
    var $sec_competence;   //権限保持

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        
        $this->field = array("QUESTIONNAIRECD"                  => VARS::post("QUESTIONNAIRECD"),
                             "UPDATED"                          => VARS::post("UPDATED")
                             );
        
        for ($i = 1; $i <= $this->koumoku; $i++) {
            $this->field["QUESTION_CONTENTS".$i]    = VARS::post("QUESTION_CONTENTS".$i);
            $this->field["ANSWER_PATTERN".$i]       = VARS::post("ANSWER_PATTERN".$i);
            $this->field["ANSWER_SELECT_COUNT".$i]  = VARS::post("ANSWER_SELECT_COUNT".$i);
        }
        
        if ($this->cmd == "") {
            $this->field = array();
        }
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function validateInput() {
        for ($i = 1; $i <= $this->koumoku; $i++) {
            if ($this->field["QUESTION_CONTENTS".$i] != "") {
                if ($this->field["ANSWER_PATTERN".$i] == "") {
                    $this->setWarning("MSG301","(".$i."の回答入力形式が選択されていません。)");
                    return false;
                } else {
                    if ($this->field["ANSWER_PATTERN".$i] == "1" || $this->field["ANSWER_PATTERN".$i] == "2") {
                        if ($this->field["ANSWER_SELECT_COUNT".$i] == "") {
                            $this->setWarning("MSG301","(".$i."の回答の選択数が入力されていません。)");
                            return false;
                        }
                    }
                }
            } else if ($this->field["ANSWER_PATTERN".$i] != "") {
                if ($this->field["QUESTION_CONTENTS".$i] == "") {
                    $this->setWarning("MSG301","(".$i."の設問が入力されていません。)");
                    return false;
                } else {
                    if ($this->field["ANSWER_PATTERN".$i] == "1" || $this->field["ANSWER_PATTERN".$i] == "2") {
                        if ($this->field["ANSWER_SELECT_COUNT".$i] == "") {
                            $this->setWarning("MSG301","(".$i."の回答の選択数が入力されていません。)");
                            return false;
                        }
                    }
                }
            }
            if ($this->validate_row_cnt($this->field["QUESTION_CONTENTS".$i], (30 * 2)) > 10) {
                $this->setWarning("MSG915","(".$i."の設問が10行を超えています。)");
                return false;
            }
        }
        return true;
    }
    
    //編集完了用のモデルを返します
    function &getInsertModel() {
    
        //入力チェック
        if ($this->validateInput()) {
            knjz422Query::getInsertQuery($this);
        }
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }

        return;
    }
}
?>
