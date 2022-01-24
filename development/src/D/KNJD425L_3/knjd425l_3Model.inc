<?php

require_once('for_php7.php');

class knjd425l_3Model extends Model {
    var $cmd;
    var $schregno;
    var $name;
    var $control;
    var $exp_year;                 //左フレームから送られてくる年度
    var $exp_semester;             //左フレームから送られてくる学期
    var $sendPrgId;                //コールされたか
    var $sendAuth;                 //親画面の権限
    var $auth;                     //権限
    var $grade;                    //左フレームから送られてくる年組
    var $selKindNo;                //別画面に引き継ぐKIND_NO
    var $upddate;                  //別画面に引き継ぐ更新日
    var $subclassname;             //前年度「年間のまとめ」参照 に引き継ぐ科目名
    var $subclasscd;               //前年度「年間のまとめ」参照 に引き継ぐ科目コード

    function knjd425l_3Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->sendPrgId);
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
            $this->sendPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

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
            $this->recordDate = VARS::get("UPDDATE");
        }
        if (VARS::post("RECORD_DATE")) {
            $this->recordDate = VARS::post("RECORD_DATE");
        }
        if ($this->recordDate) {
            $this->recordDate = str_replace("/", "-", $this->recordDate);
        }

        //「年間まとめ」GETパラメタ
        if (VARS::get("SUBCLASSNAME")) {
            $this->subclassname = VARS::get("SUBCLASSNAME");
        }
        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }

        $subclass = "";
        if (VARS::post("SUBCLASS_LIST")) {
            $subclass = VARS::post("SUBCLASS_LIST");
        }
        $this->subclassList = array();
        if ($subclass) {
            $this->subclassList = explode(",", $subclass);
        }

        $this->field = array();
        //テキストエリアの値取得
        foreach ($this->subclassList as $subclass) {
            $this->field["REMARK_TITLE_".$subclass] = VARS::post("REMARK_TITLE_".$subclass);
            $this->field["REMARK_".$subclass] = VARS::post("REMARK_".$subclass);
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

        $moji = 45;
        $gyou = 25;
        //文字数・行数チェック
        foreach ($this->subclassList as $subclass) {
            if ($this->validate_row_cnt($this->field["REMARK_{$subclass}"], ($moji * 2)) > $gyou) {
                $this->setWarning("MSG915","({$this->field["REMARK_TITLE_{$subclass}"]}は{$moji}文字X{$gyou}行までです。)");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            $this->setCmd("check");
            return;
        }

        knjd425l_3Query::getUpdateQuery($this);
        $this->setCmd("edit");
        $this->setMessage("MSG201");
    }

}
?>
