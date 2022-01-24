<?php

require_once('for_php7.php');

class knja120oModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $exp_year;       //左フレームから送られてくる年度
    var $exp_semester;   //左フレームから送られてくる学期

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja120oModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
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

        $this->getPropertiesAll();  //プロパティファイル

        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //年度コンボ（通知表所見）
        $this->year_cmb = VARS::request("YEAR_CMB");
    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knja120oQuery::getProgrampath());
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
        if (strlen($this->field["TOTALSTUDYACT"]) > 534) {
            $this->setWarning("MSG915","(活動内容は534バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYVAL"]) > 802) {
            $this->setWarning("MSG915","(評価は802バイトまでです)");
            return false;
        }
        if (strlen($this->field["SPECIALACTREMARK"]) > 208) {
            $this->setWarning("MSG915","(特別活動所見は208バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALREMARK"]) > 802) {
            $this->setWarning("MSG915","(総合所見は802バイトまでです)");
            return false;
        }
        if (strlen($this->field["ATTENDREC_REMARK"]) > 122) {
            $this->setWarning("MSG915","(出欠の記録備考は122バイトまでです)");
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
        knja120oQuery::getDeleteQuery($this->schregno);
        knja120oQuery::getDeleteHdat($this->schregno);
        knja120oQuery::getInsertQuery($this->field,$this->schregno);
        knja120oQuery::getInsertHdat($this->field,$this->schregno);
        return $result;
    }
}
?>
