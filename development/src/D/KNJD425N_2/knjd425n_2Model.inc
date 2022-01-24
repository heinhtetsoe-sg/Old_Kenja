<?php

require_once('for_php7.php');

class knjd425n_2Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $control;
    var $exp_year;                 //左フレームから送られてくる年度
    var $exp_semester;             //左フレームから送られてくる学期
    var $getPrgId;                 //コールされたか
    var $sendAuth;                 //親画面の権限
    var $auth;                     //権限
    var $grade;                    //左フレームから送られてくる年組
    var $selKindNo;                //別画面に引き継ぐKIND_NO
    var $upddate;                  //別画面に引き継ぐ更新日
    var $subclassname;             //前年度「年間のまとめ」参照 に引き継ぐ科目名
    var $subclasscd;               //前年度「年間のまとめ」参照 に引き継ぐ科目コード
    var $remarkarry;               //入力項目
    var $subclasscdarry = array(); //入力項目の科目コード

    function knjd425n_2Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->selKindNo);
            unset($this->upddate);
            unset($this->subclassname);
            unset($this->subclasscd);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        //前画面から送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
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
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("KINDNO")) {
            $this->selKindNo = VARS::get("KINDNO");
        }
        if (VARS::get("UPDDATE")) {
            $this->upddate = VARS::get("UPDDATE");
        }
        if (VARS::get("SUBCLASSNAME")) {
            $this->subclassname = VARS::get("SUBCLASSNAME");
        }
        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }

        //入力データ格納
        $this->remarkarry = array();
        foreach ($this->subclasscdarry as $subclasscd) {
            if (VARS::post("REMARK_".$subclasscd)) {
                $this->remarkarry[$subclasscd] = VARS::post("REMARK_".$subclasscd);
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->exp_year == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->selKindNo == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->upddate == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数・行数チェック
        foreach ($this->subclasscdarry as $subclasscd) {
            if ($this->validate_row_cnt($this->remarkarry[$subclasscd], (45 * 2)) > 25) {
                $this->setWarning("MSG915","(入力は25行までです)");
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
        knjd425n_2Query::getUpdateQuery($this);
    }
}
?>
