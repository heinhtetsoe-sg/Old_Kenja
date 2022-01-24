<?php

require_once('for_php7.php');

class knjj212Model extends Model {
    var $field = array();
    var $cmd;
    var $hostcd;

    function knjj212Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->committeecd);
            unset($this->year);
        }

        $this->field = array("COMMITTEECD"    =>  VARS::post("COMMITTEECD"),
                             "NAME"           =>  VARS::post("NAME"),
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),
                             "ZIPCD"          =>  VARS::post("ZIPCD"),
                             "ADDR1"          =>  VARS::post("ADDR1"),
                             "ADDR2"          =>  VARS::post("ADDR2"),
                             "POSITION_DIV"   =>  VARS::post("POSITION_DIV"),
                             "POSITION_CD"    =>  VARS::post("POSITION_CD"),
                             "REMARK"         =>  VARS::post("REMARK"),
                             "UPDATED"        =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        //年度選択コンボ
        $this->year = ($this->year) ? $this->year: CTRL_YEAR;
        if (VARS::request("YEAR")) {
            $this->year = VARS::request("YEAR");
        }

        if(VARS::get("COMMITTEECD")){
            $this->committeecd = VARS::get("COMMITTEECD");
        }
        if(VARS::get("UNSETCD")){
            $this->committeecd = "";
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $cnt  = knjj212Query::getCopyCount($this->year-1);
        $cnt2 = knjj212Query::getCopyCount($this->year);
        if ($cnt2 != '0') {
            $this->setMessage('すでにデータがあります。');
        } else if ($cnt == '0') {
            $this->setMessage('前年度にデータがありませんでした。');
        } else {
            //EVALUATION_COMMITTEE_DAT
            knjj212Query::getCopyDelEvaluation($this);
            knjj212Query::getCopyEvaluation($this);
            //GUARDIAN_COMMITTEE_YDAT
            knjj212Query::getCopyDelGuardCom($this);
            knjj212Query::getCopyGuardCom($this);
            $this->setMessage('MSG201','コピー件数：'.$cnt.'件');
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["COMMITTEECD"]) == "") {
            $this->setWarning("MSG301");
            return false;
        }

        //頭ゼロ埋め
        if (strlen($this->field["COMMITTEECD"]) <= 3) {
            $this->field["COMMITTEECD"] = sprintf("%03d", $this->field["COMMITTEECD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->committeecd != $this->field["COMMITTEECD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->committeecd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjj212Query::checkEvaluationCommitteeDat($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (strlen($this->field["COMMITTEECD"]) > 3 ) {
                $this->setWarning("MSG915", "(委員コード) 3バイトまでです。");
                return false;
            } elseif (strlen($this->field["NAME"]) > 60 ) {
                $this->setWarning("MSG915", "(学校評価委員氏名) 60バイトまでです。");
                return false;
            } elseif (strlen($this->field["NAME_KANA"]) > 240 ) {
                $this->setWarning("MSG915", "(氏名かな) 240バイトまでです。");
                return false;
            } elseif (strlen($this->field["ZIPCD"]) > 8 ) {
                $this->setWarning("MSG915", "(郵便番号) 8バイトまでです。");
                return false;
            } elseif (strlen($this->field["ADDR1"]) > 90 ) {
                $this->setWarning("MSG915", "(学校評価委員住所１) 90バイトまでです。");
                return false;
            } elseif (strlen($this->field["ADDR2"]) > 90 ) {
                $this->setWarning("MSG915", "(学校評価委員住所２) 90バイトまでです。");
                return false;
            } elseif (strlen($this->field["REMARK"]) > 60 ) {
                $this->setWarning("MSG915", "(備考) 60バイトまでです。");
                return false;
            }
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        if ($this->validateInput()){
            knjj212Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjj212Query::getInsertQuery($this);
            $this->committeecd  = $this->field["COMMITTEECD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjj212Query::getDeleteQuery($this);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
