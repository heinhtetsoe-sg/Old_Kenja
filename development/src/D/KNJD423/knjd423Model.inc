<?php

require_once('for_php7.php');
class knjd423Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限

    function knjd423Model() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),
                             "REMARK1"      =>  VARS::post("REMARK1"),
                             "REMARK2"      =>  VARS::post("REMARK2"),
                             "REMARK3"      =>  VARS::post("REMARK3")
                             );

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        $this->exp_year = ($this->exp_year != "") ? $this->exp_year: CTRL_YEAR;
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
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
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "（学期）");
            return false;
        }

        //文字数チェック
        $moji = 20;
        for ($i = 1; $i <= 3; $i++) {
            if ($this->validate_row_cnt($this->field["REMARK".$i], ($moji * 2)) > 1) {
                $this->setWarning("MSG915","(全角{$moji}文字までです)");
                return false;
            }
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
        knjd423Query::getUpdateQuery($this->field, $this);
        return;
    }

    //削除
    function &getDeleteModel() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "（学期）");
            return false;
        }

        //データの存在チェック
        $db = Query::dbCheckOut();
        $dataCheck = $db->getOne(knjd423Query::getRow($this));
        Query::dbCheckIn($db);
        if (strlen($dataCheck) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //削除
        knjd423Query::getDeleteQuery($this->field, $this);
        $this->setMessage("MSG201");
    }
}
?>
