<?php

require_once('for_php7.php');

class knjz404Model extends Model {
    var $cmd;
    var $control;
    var $year;
    var $selectdata;

    function knjz404Model(){
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
        }
        
        $this->field = array("GRADE"          =>  VARS::post("GRADE"),           //学年
                             "RECORD_DIV"     =>  VARS::post("RECORD_DIV")       //クラス形態
                             );
        

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //年度
        $this->year = VARS::post("year");

        $this->selectdata = VARS::post("selectdata");
        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (!is_numeric($this->year)) {
            $this->warning = "対象年度を選択して下さい。";
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        knjz404Query::getUpdateQuery($this);
        return;
    }
}
?>
