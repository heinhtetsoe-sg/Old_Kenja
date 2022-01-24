<?php

require_once('for_php7.php');
class knjj200aModel extends Model {

    var $field = array();
    var $cmd;
    var $schregno;                  //学籍番号
    var $control = array();         //コントロールマスタデータを保持

    function knjj200aModel()
    {
        common::GetControlMaster_Fnc($this->control);
        unset($this->schregno);
    }
    function init()
    {
        $this->field = array("DIV"              =>  VARS::request("DIV"),           //区分
                             "EXECUTIVECD"      =>  VARS::request("EXECUTIVECD"),   //役職
                             "GUARD_NAME_DIV"   =>  VARS::post("GUARD_NAME_DIV"),   //保護者
                             "REMARK"           =>  VARS::post("REMARK")            //備考
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック1
        if ($this->schregno == "") {
            $this->setWarning("MSG304", "(生徒を選択して下さい。)");
            return false;
        }
        //必須入力チェック2
        if (!strlen($this->field["DIV"]) || !strlen($this->field["EXECUTIVECD"])) {
            if ($this->cmd == "update") {
                $this->setWarning("MSG301");
            } else {
                $this->setWarning("MSG308");
            }
            return false;
        }
        if ($this->cmd == "update") {
            //桁数チェック
            if (strlen($this->field["REMARK"]) > 60){ 
                $this->setWarning("MSG915","(備考は60バイトまでです)");
                return false; 
            }
        }
        //データの存在チェック
        if ($this->cmd == "delete") {
            $db = Query::dbCheckOut();
            $cnt = get_count($db->getOne(knjj200aQuery::getRow($this->schregno, $this->field["DIV"], $this->field["EXECUTIVECD"])));
            Query::dbCheckIn($db);
            if (!$cnt) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if ($this->validateInput()){
            $this->update();
        }
    }
    function &update() {
        $result = knjj200aQuery::getUpdateQuery($this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //削除
    function &getDeleteModel() {
        if ($this->validateInput()){
            $this->delete();
        }
        return;
    }
    function &delete() {
        knjj200aQuery::getDeleteQuery($this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
        $this->field = array();
        return;
    }
}
?>
