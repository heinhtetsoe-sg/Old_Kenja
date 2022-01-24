<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj081Model extends Model {
    var $cmd;
    var $selectdata;
    var $staffcd;

    function knjj081Model() {
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;
    }
    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("YEAR"          =>  VARS::post("YEAR"),         //年度
                             "SCHKIND"       =>  VARS::post("SCHKIND"),      //校種
                             "GAKKI"         =>  VARS::post("GAKKI"),        //学期
                             "COMMITTEECD"   =>  VARS::post("COMMITTEECD"),  //委員会
                             "DBNAME"        =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");

        if(!isset($this->field['YEAR'])){
            $this->field['YEAR'] = CTRL_YEAR;
        }
        $this->year = $this->field['YEAR'];

    }
    //入力項目チェック
    function validateInput() {
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        $result = knjj081Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }
}
?>
