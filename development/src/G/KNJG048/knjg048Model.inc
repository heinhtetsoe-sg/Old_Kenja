<?php

require_once('for_php7.php');

class knjg048Model extends Model {

    var $field  = array();  //画面データ格納
    var $cmd;               //コマンド
    var $diaryDate;         //日付
    var $control;           //コントロールマスタ

    function knjg048Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->diaryDate  = VARS::post("DIARY_DATE");
        if ($this->diaryDate == "") $this->diaryDate  = str_replace("-","/",CTRL_DATE);
        $this->field = array("SCHKIND"                      =>  VARS::post("SCHKIND"),
                             "WEATHER"                      =>  VARS::post("WEATHER"),
                             "WEATHER2"                     =>  VARS::post("WEATHER2"),
                             "NEWS"                         =>  VARS::post("NEWS"),
                             "STAFFNEWS"                    =>  VARS::post("STAFFNEWS")
                            );

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->validate_row_cnt($this->field["NEWS"], (93 * 2)) > 8) {
            $this->setWarning("MSG915","(記事は8行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["STAFFNEWS"], (93 * 2)) > 12) {
            $this->setWarning("MSG915","(職員事項は12行までです)");
            return false;
        }
        if (strlen($this->field["NEWS"]) > 1500) {
            $this->setWarning("MSG915","(記事は1500バイトまでです)");
            return false;
        }
        if (strlen($this->field["STAFFNEWS"]) > 2000) {
            $this->setWarning("MSG915","(職員事項は2000バイトまでです)");
            return false;
        }

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
            if ($this->cmd == "update") {
                if ($this->validateInput()) {
                    knjg048Query::getDeleteQuery($this);
                    knjg048Query::getInsertQuery($this);
                }
            } else if ($this->cmd == "delete") {
                knjg048Query::getDeleteQuery($this);
            }
            $this->setMessage("MSG201");
    }
}
?>
