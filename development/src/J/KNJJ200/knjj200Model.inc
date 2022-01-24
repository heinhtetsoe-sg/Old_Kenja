<?php

require_once('for_php7.php');
class knjj200Model extends Model {

    var $field = array();
    var $cmd;
    var $schregno;                  //学籍番号
    var $control = array();         //コントロールマスタデータを保持

    function knjj200Model()
    {
        common::GetControlMaster_Fnc($this->control);
        unset($this->schregno);
    }
    function init()
    {
        $this->field = array("EXECUTIVECD"      =>  VARS::post("EXECUTIVECD"),  //役職
                             "REMARK"           =>  VARS::post("REMARK")        //備考
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
    function validateInput()
    {
        //必須入力
        if ($this->schregno == "") {
            $this->setWarning("MSG304", "(生徒を選択して下さい。)");
            return false;
        }

        if ($this->cmd == "update") {
            //必須入力
            if($this->field["EXECUTIVECD"] == "") {
                $this->setWarning("MSG301");
                return false;
            }

            //桁数チェック
            if (strlen($this->field["REMARK"]) > 60){ 
                $this->setWarning("MSG915","(備考は60バイトまでです)");
                return false; 
            }
        }

        //データの存在チェック
        if ($this->cmd == "delete") {
            $db = Query::dbCheckOut();
            $cnt = get_count($db->getOne(knjj200Query::getRow($this)));
            Query::dbCheckIn($db);
            if(!$cnt){
                $this->setWarning("MSG303");
                return false;
            }
        }
        return true;
    }

    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }
    function &update()
    {
        $db = Query::dbCheckOut();
        $cnt = get_count($db->getOne(knjj200Query::getRow($this)));
        $cmd = ($cnt > 0) ? "update" : "insert";
        Query::dbCheckIn($db);

        $result = knjj200Query::getUpdateQuery($this, $cmd);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //削除
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
        }
        return;
    }
    function &delete()
    {
        knjj200Query::getDeleteQuery($this);

        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
        $this->field = array();
        return;
    }
}
?>

