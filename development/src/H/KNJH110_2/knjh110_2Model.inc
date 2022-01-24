<?php
class knjh110_2Model extends Model {

    var $field = array();
    var $cmd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    var $schregNo;      //親画面の学籍番号
    var $subclassCd;    //親画面の科目コード
    var $seq;           //子画面のSEQ
    var $infSeq;        //(情報)親画面のSEQ

    function knjh110_2Model() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);

            unset($this->schregNo);
            unset($this->subclassCd);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        //親画面
        if (VARS::get("SEND_SCHREGNO")) {
            $this->schregNo     = VARS::get("SEND_SCHREGNO");
            $this->subclassCd   = VARS::get("SEND_SUBCLASSCD");
            $this->infSeq       = VARS::get("INF_SEQ");

            unset($this->seq);
        }
        //子画面（リンクから取得）
        if (VARS::get("SEQ")) {
            $this->seq          = VARS::get("SEQ");
        }
        //入力項目
        $this->field = array("REGDDATE"     => VARS::post("REGDDATE"),
                             "MINUTES"      => VARS::post("MINUTES"),
                             "REMARK"       => VARS::post("REMARK")
                             );

        $this->cmd = VARS::request("cmd");
    }

    function validateInput($kind)
    {
        //データ指定チェック
        if (!strlen($this->schregNo) || !strlen($this->subclassCd)) {
           $this->setWarning("MSG304", "(生徒・科目)");
           return false;
        }
        //更新・削除
        if ($kind=="update" || $kind=="delete") {
            //データ選択チェック
            if (!strlen($this->field["REGDDATE"]) || !strlen($this->seq)) {
               $this->setWarning("MSG308");
               return false;
            }
        }
        //更新・追加
        if ($kind=="update" || $kind=="insert") {
            //入力必須チェック
            if (!strlen($this->field["REGDDATE"]) || !strlen($this->field["MINUTES"])) {
               $this->setWarning("MSG301");
               return false;
            }
            //制限チェック
            if (strlen($this->field["REMARK"]) > 90) {
                $this->setWarning(MSG915, "(備考は90バイトまでです)");
                return false;
            }
        }

        return true;
    }

    function &getInsertModel() {
        if ($this->validateInput("insert")) {
            $this->seq = knjh110_2Query::insertQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getUpdateModel() {
        if ($this->validateInput("update")) {
            knjh110_2Query::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput("delete")) {
            knjh110_2Query::deleteQuery($this);
            $this->setMessage("MSG201");
            unset($this->seq);
            $this->field = array();
        }
    }
}
?>
