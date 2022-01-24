<?php

require_once('for_php7.php');
class knjd422Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限

    function knjd422Model() {
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
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("SEMESTER"            =>  VARS::post("SEMESTER"),
                             "TOTALSTUDYTIME"      =>  VARS::post("TOTALSTUDYTIME"),
                             "REMARK01"            =>  VARS::post("REMARK01"),
                             "SPECIALACTREMARK"    =>  VARS::post("SPECIALACTREMARK"),
                             "REMARK02"            =>  VARS::post("REMARK02"),
                             "ATTENDREC_REMARK"    =>  VARS::post("ATTENDREC_REMARK")
                             );

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
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

        //文字数・行数設定
        //学習内容
        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE"]);
            $this->totalstudytime_moji = (int)trim($moji);
            $this->totalstudytime_gyou = (int)trim($gyou);
        } else { 
            $this->totalstudytime_moji = 35;
            $this->totalstudytime_gyou = 2;
        }
        //評価
        if ($this->Properties["reportSpecialSize05_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize05_01"]);
            $this->remark01_moji = (int)trim($moji);
            $this->remark01_gyou = (int)trim($gyou);
        } else {  
            $this->remark01_moji = 35;
            $this->remark01_gyou = 3;
        }
        //特別活動の記録
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else { 
            $this->specialactremark_moji = 40;
            $this->specialactremark_gyou = 6;
        }
        //総合所見
        if ($this->Properties["reportSpecialSize05_02"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize05_02"]);
            $this->remark02_moji = (int)trim($moji);
            $this->remark02_gyou = (int)trim($gyou);
        } else {
            $this->remark02_moji = 25;
            $this->remark02_gyou = 6;
        }
        //出欠の記録備考
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 13;
            $this->attendrec_remark_gyou = 3;
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
            $this->setWarning("MSG304");
            return false;
        }


        //文字数・行数チェック
        if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->totalstudytime_moji * 2)) > $this->totalstudytime_gyou) {
            $this->setWarning("MSG915","(学習内容は{$this->totalstudytime_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK01"], ($this->remark01_moji * 2)) > $this->remark01_gyou) {
            $this->setWarning("MSG915","(評価は{$this->remark01_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
            $this->setWarning("MSG915","(特別活動の記録は{$this->specialactremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK02"], ($this->remark02_moji * 2)) > $this->remark02_gyou) {
            $this->setWarning("MSG915","(総合所見は{$this->remark02_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
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
        knjd422Query::getUpdateQuery($this->field, $this);
        return $result;
    }
}
?>
