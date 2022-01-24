<?php

require_once('for_php7.php');

class knjl501a_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $examyear;
    var $disp_hopecoursecode;
    var $sendAuth;
    var $auth;
    var $getPrgId;

    function knjl501a_2Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if(VARS::get("SEND_PRGID")){
            unset($getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
        }

        $this->cmd          = VARS::request("cmd");
        if (!$this->cmd) {
            $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        }

        if ($this->cmd == "") {
            unset($this->examyear);
            unset($this->disp_hopecoursecode);
        }

        $this->examyear = CTRL_YEAR + 1;

        $this->field = array("HOPE_COURSECODE" =>  VARS::post("HOPE_COURSECODE"),
                             "HOPE_NAME"       =>  VARS::post("HOPE_NAME"),
                             "COURSECODE"      =>  VARS::post("COURSECODE"),
                             "NOTICE_NAME"     =>  VARS::post("NOTICE_NAME"),
                             "PASS_NAME"       =>  VARS::post("PASS_NAME"),
                             "NOT_PASS_NAME"   =>  VARS::post("NOT_PASS_NAME")
                             );

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SEND_FLG") == "1"){
            $this->field["HOPE_COURSECODE"] = VARS::get("HOPE_COURSECODE");
        }
        if (VARS::post("DISP_HOPECOURSECODE")) {
            $this->disp_hopecoursecode = VARS::post("DISP_HOPECOURSECODE");
        }

    }

    //入力項目チェック
    function validateInput() {
        //addかupdateのみが対象。
        //必須入力がされていないならエラー
        if ((trim($this->field["HOPE_COURSECODE"]) == "" || trim($this->field["COURSECODE"]) == "")) {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->cmd == "update") {
            //元々空データからの更新ボタン押下であれば、エラー
            if (!strlen($this->disp_hopecoursecode)) {
                $this->setWarning("MSG308");
                return false;
            }
            //入力されているキー項目が、表示時の必須項目の値と一致しない場合はエラー
            if ($this->disp_hopecoursecode != $this->field["HOPE_COURSECODE"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        //頭ゼロ埋め
        if (strlen($this->field["HOPE_COURSECODE"]) <= 4) {
            $this->field["HOPE_COURSECODE"] = sprintf("%04d", $this->field["HOPE_COURSECODE"]);
        }
        
        $db = Query::dbCheckOut();
        //入力データで検索
        $query = knjl501a_2Query::getHopeCourseDat($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //DATのチェック (UPDATE)
        //更新対象がなければエラー
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        //桁数チェック
        if (strlen($this->field["HOPE_COURSECODE"]) > 4 ) {
            $this->setWarning("MSG915", "(志望区分コード) 4バイトまでです。");
            return false;
        } elseif (strlen($this->field["HOPE_NAME"]) > 20 * 2 ) {
            $this->setWarning("MSG915", "(志望区分名称) 20文字までです。");
            return false;
        } elseif (strlen($this->field["COURSECODE"]) > 4 ) {
            $this->setWarning("MSG915", "(学籍コースコード) 4バイトまでです。");
            return false;
        } elseif (strlen($this->field["NOTICE_NAME"]) > 20 * 2 ) {
            $this->setWarning("MSG915", "(通知用名称) 20文字までです。");
            return false;
        } elseif (strlen($this->field["PASS_NAME"]) > 20 * 2 ) {
            $this->setWarning("MSG915", "(コース合格名称) 20文字までです。");
            return false;
        } elseif (strlen($this->field["NOT_PASS_NAME"]) > 20 * 2 ) {
            $this->setWarning("MSG915", "(コース不合格名称) 20文字までです。");
            return false;
        }

        return true;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            knjl501a_2Query::getInsertMstQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            knjl501a_2Query::getUpdateMstQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        //キーの入力チェック
        if (!strlen(trim($this->field["HOPE_COURSECODE"]))) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $db    = Query::dbCheckOut();
        $query = knjl501a_2Query::getHopeCourseDat($this);
        $row   = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        knjl501a_2Query::getDeleteQuery($this);
        unset($this->field);
        $this->setMessage("MSG201");
    }

}
?>
