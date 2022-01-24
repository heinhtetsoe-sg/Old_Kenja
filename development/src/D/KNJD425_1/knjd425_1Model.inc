<?php

require_once('for_php7.php');

class knjd425_1Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限
    var $grade;         //左フレームから送られてくる年組
    var $selKindNo;     //別画面に引き継ぐKIND_NO
    var $upddate;       //別画面に引き継ぐ更新日
    var $rowcnt;        //入力行数(=登録行数)
    var $colcnt;        //入力項目数(=登録項目数)
    var $remarkarry;    //入力項目

    function knjd425_1Model() {
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
            unset($this->colcnt);
            unset($this->rowcnt);
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
        if (VARS::post("HID_COLCNT")) {
            $this->colcnt = VARS::post("HID_COLCNT");
        }
        if (VARS::post("HID_ROWCNT")) {
            $this->rowcnt = VARS::post("HID_ROWCNT");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->selKindNo);
            unset($this->upddate);
            unset($this->colcnt);
            unset($this->rowcnt);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //入力データ格納
        $this->remarkarry = array();
        if ($this->colcnt > 0 && $this->rowcnt > 0) {
            for ($rl = 0;$rl < $this->rowcnt;$rl++) {
                for ($cl = 0;$cl < $this->colcnt;$cl++) {
                    if (VARS::post("REMARK_".$rl."_".$cl)) {
                        $this->remarkarry[(int)$rl*(int)$this->colcnt+$cl] = VARS::post("REMARK_".$rl."_".$cl);
                    }
                }
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
        if ($this->colcnt == "" || $this->rowcnt == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //日付チェック

        //文字数・行数チェック
        $mxmojilist = array(18, 10, 10);
        if ($this->colcnt > 0 && $this->rowcnt > 0) {
            for ($rl = 0;$rl < $this->rowcnt;$rl++) {
                for ($cl = 0;$cl < $this->colcnt;$cl++) {
                    if ($cl == 0) {
                        //未入力チェック
                        if (strlen($this->remarkarry[(int)$rl*(int)$this->colcnt + $cl]) == 0) {
                            $this->setWarning("MSG915","(障害種別が未設定です)");
                            // Add by HPA for textarea_cursor start 2020/02/03
                            $this->message915="REMARK_0_0";
                            // Add by HPA for textarea_cursor end 2020/02/20
                            return false;
                        }
                    } else if ($cl == 1)  {
                        //日付チェック
                        if (!preg_match('/^([1-9][0-9]{3})\/(0[1-9]{1}|1[0-2]{1})\/(0[1-9]{1}|[1-2]{1}[0-9]{1}|3[0-1]{1})$/', $this->remarkarry[(int)$rl*(int)$this->colcnt + $cl])) {
                            $this->setWarning("MSG915","(作成日の入力が不正です)");
                            // Add by HPA for textarea_cursor start 2020/02/03
                            $this->message915="REMARK_0_1";
                            // Add by HPA for textarea_cursor end 2020/02/20
                            return false;
                        }
                    } else if ($cl == 2) {
                        //未入力チェック
                        if (strlen($this->remarkarry[(int)$rl*(int)$this->colcnt + (int)$cl]) == 0) {
                            $this->setWarning("MSG915","(作成者が未設定です)");
                            // Add by HPA for textarea_cursor start 2020/02/03
                            $this->message915="REMARK_0_2";
                            // Add by HPA for textarea_cursor end 2020/02/20
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel1() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd425_1Query::getUpdateQuery($this, $this->remarkarry, $this->colcnt);
    }
}
?>
