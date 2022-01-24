<?php

require_once('for_php7.php');

class knjf165aModel extends Model {
    var $cmd;
    var $control;
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限
    
    var $sDate;         //来室日付範囲 FROM
    var $eDate;         //来室日付範囲 TO

    var $rowcnt;        //入力行数(=登録行数)
    var $chekArry;      //更新対象
    var $schregnoArry;  //項目 SCHREGNO
    var $dateArry;      //項目 VISIT_DATE
    var $hourArry;      //項目 VISIT_HOUR
    var $minuteArry;    //項目 VISIT_MINUTE
    var $typeArry;      //項目 TYPE
    var $remark2Arry;   //項目 REMARK2
    var $remark3Arry;   //項目 REMARK3
    var $remark4Arry;   //項目 REMARK4
    var $remark5Arry;   //項目 REMARK5
    var $remark6Arry;   //項目 REMARK6
    var $remark7Arry;   //項目 REMARK7
    var $remark8Arry;   //項目 REMARK8

    function knjf165aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->rowcnt);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        if (VARS::post("SDATE")) {
            $this->sDate = VARS::post("SDATE");
        }
        if (VARS::post("EDATE")) {
            $this->eDate = VARS::post("EDATE");
        }
        if (VARS::post("HID_ROWCNT")) {
            $this->rowcnt = VARS::post("HID_ROWCNT");
        }

        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->rowcnt);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //データ格納
        $this->chekArry  = array();
        $this->schregnoArry  = array();
        $this->dateArry  = array();
        $this->hourArry  = array();
        $this->minuteArry  = array();
        $this->typeArry  = array();
        $this->remark2Arry  = array();
        $this->remark3Arry  = array();
        $this->remark4Arry  = array();
        $this->remark5Arry  = array();
        $this->remark6Arry  = array();
        $this->remark7Arry  = array();
        $this->remark8Arry  = array();
        
        if ($this->rowcnt > 0) {
            for ($rl = 0;$rl < $this->rowcnt;$rl++) {
                $this->chekArry[$rl]     = VARS::post("CHECKED_".$rl);
                $this->schregnoArry[$rl] = VARS::post("SCHREGNO_".$rl);
                $this->dateArry[$rl]     = VARS::post("VISIT_DATE_".$rl);
                $this->hourArry[$rl]     = VARS::post("VISIT_HOUR_".$rl);
                $this->minuteArry[$rl]   = VARS::post("VISIT_MINUTE_".$rl);
                $this->typeArry[$rl]     = VARS::post("TYPE_".$rl);
                $this->remark2Arry[$rl]  = VARS::post("REMARK2_".$rl);
                $this->remark3Arry[$rl]  = VARS::post("REMARK3_".$rl);
                $this->remark4Arry[$rl]  = VARS::post("REMARK4_".$rl);
                $this->remark5Arry[$rl]  = VARS::post("REMARK5_".$rl);
                $this->remark6Arry[$rl]  = VARS::post("REMARK6_".$rl);
                $this->remark7Arry[$rl]  = VARS::post("REMARK7_".$rl);
                $this->remark8Arry[$rl]  = VARS::post("REMARK8_".$rl);
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->rowcnt == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数・行数チェック
        $chkFlg = "";
        if ($this->rowcnt > 0) {
            for ($rl = 0;$rl < $this->rowcnt;$rl++) {
                if ($this->chekArry[$rl] != "") {
                    $chkFlg = "1";
                    if ($this->validate_row_cnt($this->remark3Arry[$rl], (12)) > 1) {
                        $this->setWarning("MSG915","(入力は12桁までです)");
                        return false;
                    }
                    if ($this->validate_row_cnt($this->remark4Arry[$rl], (12)) > 1) {
                        $this->setWarning("MSG915","(入力は12桁までです)");
                        return false;
                    }
                    if ($this->validate_row_cnt($this->remark5Arry[$rl], (5)) > 1) {
                        $this->setWarning("MSG915","(入力は5桁までです)");
                        return false;
                    }
                    if ($this->validate_row_cnt($this->remark6Arry[$rl], (7)) > 1) {
                        $this->setWarning("MSG915","(入力は7桁までです)");
                        return false;
                    }
                    if ($this->validate_row_cnt($this->remark7Arry[$rl], (12)) > 1) {
                        $this->setWarning("MSG915","(入力は12桁までです)");
                        return false;
                    }
                    if ($this->validate_row_cnt($this->remark8Arry[$rl], (50 * 2)) > 1) {
                        $this->setWarning("MSG915","(入力は50桁までです)");
                        return false;
                    }
                }
            }
        }

        if($chkFlg == ""){
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
        knjf165aQuery::getUpdateQuery($this, $this->rowcnt);
    }
}
?>
