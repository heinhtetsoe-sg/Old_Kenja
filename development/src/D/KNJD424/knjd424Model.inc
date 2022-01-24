<?php

require_once('for_php7.php');
class knjd424Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限

    function knjd424Model() {
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
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("REMARK1"            =>  VARS::post("REMARK1"),
                             "REMARK2"            =>  VARS::post("REMARK2"),
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

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //文字数・行数設定
        //評価
        if ($this->Properties["hreportGuidanceSchregSemesterRemark1Size"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["hreportGuidanceSchregSemesterRemark1Size"]);
            $this->remark1_moji = (int)trim($moji);
            $this->remark1_gyou = (int)trim($gyou);
        } else {  
            $this->remark1_moji = 70;
            $this->remark1_gyou = 20;
        }

        if ($this->Properties["hreportGuidanceSchregSemesterRemark2Size"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["hreportGuidanceSchregSemesterRemark2Size"]);
            $this->remark2_moji = (int)trim($moji);
            $this->remark2_gyou = (int)trim($gyou);
        } else {  
            $this->remark2_moji = 70;
            $this->remark2_gyou = 20;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数・行数チェック
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->remark1_moji * 2)) > $this->remark1_gyou) {
            $this->setWarning("MSG915","(保護者の願いは{$this->remark1_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->remark2_moji * 2)) > $this->remark2_gyou) {
            $this->setWarning("MSG915","(生徒別年間目標は{$this->remark2_gyou}行までです)");
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
        knjd424Query::getUpdateQuery($this->field, $this);
        return $result;
    }
}
?>
