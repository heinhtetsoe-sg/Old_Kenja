<?php

require_once('for_php7.php');

class knjd126hModel extends Model {

    var $cmd;
    var $control_data = array();    //コントロールのデータを保持
    var $field = array();
    var $auth;                      //権限
    var $setupFlg;                  //編集フラグ 1:編集中

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            common::GetControlMaster_Fnc($this->control_data);
            $this->field = array();
            unset($this->auth);
            unset($this->setupFlg);
        }

        $this->cmd = VARS::request("cmd");
        $this->auth = AUTHORITY;
        $this->setupFlg = VARS::post("setupFlg");

        $this->field["GRADE_HR_CLASS"]  = VARS::post("GRADE_HR_CLASS");
        $this->field["SUBCLASSCD"]      = VARS::post("SUBCLASSCD");
        $this->field["SEMESTER"]        = VARS::post("SEMESTER");
    }

    /* 入力項目チェック */
    function validateInput() {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel() {
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
            $this->setupFlg = ""; //編集中フラグOFF
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjd126hQuery::getUpdateQuery($db, $this);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
