<?php

require_once('for_php7.php');

class knjs580Model extends Model {

    var $cmd;
    var $control_data = array();        //コントロールのデータを保持
    var $field = array();

    function knjs580Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->field = array();
            unset($this->trCount);
        }
        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("YEAR"     => VARS::post("YEAR"),
                             "GRADE"    => VARS::post("GRADE")
                             );

        if (is_array($this->updSubclass)) {
            foreach ($this->updSubclass as $subclassCd => $subclassName) {
                $this->updMeisai[$subclassCd] = VARS::post("MEISAI".$subclassCd);
            }
        }

    }

    /* 入力項目チェック */
    function validateInput()
    {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjs580Query::delInsSql($db, $this);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
