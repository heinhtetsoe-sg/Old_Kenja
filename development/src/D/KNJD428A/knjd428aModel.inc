<?php

require_once('for_php7.php');

class knjd428aModel extends Model {
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

    function knjd428aModel() {
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
                             "REMARK1_1"        => VARS::post("REMARK1_1"),
                             "REMARK2_1"        => VARS::post("REMARK2_1"),
                             "REMARK3_1"        => VARS::post("REMARK3_1"),
                             "REMARK3_2"        => VARS::post("REMARK3_2"),
                             "REMARK4_1"        => VARS::post("REMARK4_1"),
                             "REMARK4_2"        => VARS::post("REMARK4_2"),
                             "REMARK5_1"        => VARS::post("REMARK5_1"),
                             "REMARK5_2"        => VARS::post("REMARK5_2"),
                             "REMARK5_3"        => VARS::post("REMARK5_3"),
                             "ATTENDREC_REMARK" => VARS::post("ATTENDREC_REMARK"), //出欠の備考
                             "REMARK6_1"        => VARS::post("REMARK6_1")
                             );

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
        
        knjd428aQuery::getUpdateQuery($this->field, $this);
        return $result;
    }
}
?>
