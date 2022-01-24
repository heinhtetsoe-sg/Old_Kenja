<?php

require_once('for_php7.php');
class knjd130tModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();
    var $gakki;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjd130tModel()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

       if (VARS::get("PROGRAMID")!="") {
           unset($this->schregno);
       }
       if (VARS::get("PROGRAMID")!="") {
           unset($this->name);
           unset($this->gakki);
       }
        $this->field = array("TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")        //総合的な学習の時間
                            ,"SPECIALACTREMARK" =>  VARS::post("SPECIALACTREMARK")      //奉仕
                            ,"COMMUNICATION"    =>  VARS::post("COMMUNICATION")         //通信欄
                             );

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        $this->grade = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->gakki);
        }
        if (VARS::post("GAKKI")!="") {
            $this->gakki = VARS::post("GAKKI");
        }
    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knjd130tQuery::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], (21 * 2)) > 5) {
            $this->setWarning("MSG915","(総合的な学習の時間は5行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (21 * 2)) > 5) {
            $this->setWarning("MSG915","(奉仕は5行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], (21 * 2)) > 5) {
            $this->setWarning("MSG915","(通信欄は5行までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd130tQuery::getDeleteQuery($this->schregno, $this->gakki);
        knjd130tQuery::getInsertQuery($this->field,$this->schregno, $this->gakki);
        return $result;
    }
}
?>
