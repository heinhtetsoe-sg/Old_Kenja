<?php

require_once('for_php7.php');

class knjd428dModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $semester = "";
    var $field = array();
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限
    var $grade;
    var $hrClass;

    function knjd428dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->schregno);
            unset($this->name);
            unset($this->semester);
            unset($this->grade);
            unset($this->hrClass);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array(
                             "REMARK1"        => VARS::post("REMARK1"),
                             "REMARK2"        => VARS::post("REMARK2"),
                             "REMARK3"        => VARS::post("REMARK3"),
                             "REMARK4"        => VARS::post("REMARK4"),
                             "REMARK5"        => VARS::post("REMARK5"),
                             "REMARK6"        => VARS::post("REMARK6"),
                             "REMARK7"        => VARS::post("REMARK7"),
                             "REMARK8"        => VARS::post("REMARK8"),
                             "REMARK9"        => VARS::post("REMARK9")
                             );

        for ($i = 1; $i <= 6; $i++) {
            $this->staffField[$i]   = VARS::post("STAFFCD{$i}");
        }

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //学期
        if (VARS::post("SEMESTER")) {
            $this->semester = VARS::post("SEMESTER");
        }
        $this->semester = ($this->semester) ? $this->semester : CTRL_SEMESTER;

        //年
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        //組
        if (VARS::get("HR_CLASS") != "") {
            $this->hrClass = VARS::get("HR_CLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        
        knjd428dQuery::getUpdateQuery($this->field, $this);
        return $result;
    }
}
?>
