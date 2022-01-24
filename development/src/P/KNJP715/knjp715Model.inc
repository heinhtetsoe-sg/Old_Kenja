<?php

require_once('for_php7.php');

class knjp715Model extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $schregno;
    var $collect_money;
    var $collect_grp_cd;

    function knjp715Model() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->schoolKind);
        }

        if (strlen(VARS::get("SCHREGNO"))) {
            $this->schregno = VARS::get("SCHREGNO");
            $this->search_div = VARS::get("SEARCH_DIV");
        }

        if (VARS::get("init") != "") {
            unset($this->schregno);
            unset($this->schoolKind);
        }

        $this->mst_field = array("SCHREGNO"             => VARS::request("SCHREGNO"),
                                 "SELECT_YEAR"          => VARS::request("SELECT_YEAR"),
                                 "SLIP_NO"              => VARS::request("SLIP_NO"),
                                 "PAY_DIV"              => VARS::post("PAY_DIV"),
                                 "SLIP_DATE"            => VARS::post("SLIP_DATE"),
                                 "GROUPCD"              => VARS::post("GROUPCD"),
                                 "COLLECT_PATTERN_CD"   => VARS::post("COLLECT_PATTERN_CD"),
                                 "CANCEL_DATE"          => VARS::post("CANCEL_DATE"),
                                 "CANCEL_REASON"        => VARS::post("CANCEL_REASON"),
                                 "maxSeq"               => VARS::post("maxSeq"),
                                 "DIRECT_DEBIT_DATE"    => VARS::post("DIRECT_DEBIT_DATE"),
                                 "CREDITS"              => VARS::post("CREDITS")
                                 );

        for ($i = 1; $i <= 12; $i++) {
            $this->mst_field["COLLECT_MONTH_".$i] = VARS::post("COLLECT_MONTH_".$i);
        }

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "changeYear" || $this->cmd == "changeSlip") {
            $array = ($this->cmd == "changeYear") ? array("SCHREGNO", "SELECT_YEAR") : array("SCHREGNO", "SELECT_YEAR", "SLIP_NO");
            foreach ($this->mst_field as $field => $val) {
                if (!in_array($field, $array)) {
                    unset($this->mst_field[$field]);
                }
            }
        }

        $counter = 1;
        $this->updField = array();
        $maxSeq = $this->mst_field["maxSeq"];
        if ($maxSeq > 0) {
            for ($updCnt = 1; $updCnt <= $maxSeq; $updCnt++) {
                if ($this->cmd == "delLine" && VARS::post("DELCHK_{$updCnt}") == "1") {
                    $this->mst_field["maxSeq"]--;
                    continue;
                }
                $this->updField[$counter] = array("DELCHK"          => VARS::post("DELCHK_{$updCnt}"),              //削除
                                                  "TEXTBOOKDIV"     => VARS::post("HIDDEN_TEXTBOOKDIV_{$updCnt}"),  //教科書区分
                                                  "COLLECT_LM_CD"   => VARS::post("COLLECT_LM_CD_{$updCnt}"),       //LMコード
                                                  "COLLECT_MONEY"   => VARS::post("COLLECT_MONEY_{$updCnt}"),       //単価
                                                  "COLLECT_CNT"     => VARS::post("COLLECT_CNT_{$updCnt}")          //数量
                                                 );
                $counter++;
            }
        }

        if ($this->cmd == "addLine") {
            $this->mst_field["maxSeq"] = $this->mst_field["maxSeq"] + 1;
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $db = Query::dbCheckOut();
        $this->schoolKind = $db->getOne(knjp715Query::getSchKind($this));
        Query::dbCheckIn($db);
    }

    function validateInput($div) {
        //必須チェック
        if (!strlen($this->schregno)) {
            $this->setWarning("MSG310", "生徒を選択してください");
            return false;
        }

        if ($div == 'update') {

            if (!$this->mst_field["SLIP_NO"]) {
                //必須チェック
                if (!strlen($this->mst_field["DIRECT_DEBIT_DATE"])) {
                    $this->setWarning("MSG301","( 引き落とし日 )");
                    return false;
                }
                $errFlg = true;
                for ($i = 1; $i <= 12; $i++) {
                    if (strlen($this->mst_field["COLLECT_MONTH_".$i])) $errFlg = false;
                }
                if ($errFlg) {
                    $this->setWarning("MSG310","( 入金月 )");
                    return false;
                }
            }

            if (!$this->mst_field["CANCEL_DATE"]) {
                //必須チェック
                if (!strlen($this->mst_field["SLIP_DATE"])) {
                    $this->setWarning("MSG301","( 伝票日付 )");
                    return false;
                }
                if (!$this->mst_field["maxSeq"]) {
                    $this->setWarning("MSG304","( 入金項目 )");
                    return false;
                }

                //入金項目の重複チェック
                $array = array();
                foreach ($this->updField as $seq => $setData) {
                    $array[$setData["COLLECT_LM_CD"]]++;
                }
                $errFlg = false;
                foreach ($array as $lmcd => $cnt) {
                    if ($cnt > 1) $errFlg = true;
                }
                if ($errFlg) {
                    $this->setWarning("MSG302","( 入金項目 )");
                    return false;
                }
            } else {
                //文字数チェック
                if ($this->validate_row_cnt($this->mst_field["CANCEL_REASON"], (30 * 2)) > 1) {
                    $this->setWarning("MSG915","全角30文字までです");
                    return false;
                }
            }
        }

        if ($div == 'delete') {
            //必須チェック
            if (!strlen($this->mst_field["SLIP_NO"])) {
                $this->setWarning("MSG310", "伝票を選択してください");
                return false;
            }
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            $slipno = knjp715Query::updateQuery($this);
            $this->mst_field["SLIP_NO"] = $slipno;
            $this->setMessage("MSG201");
        }
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp715Query::deleteQuery($this);
            unset($this->mst_field["SLIP_NO"]);
            $this->setMessage("MSG201");
        }
    }
}
?>
