<?php

require_once('for_php7.php');


class knjx_guardian_addressModel extends Model
{
    public $field    = array();
    public $cmd;
    public $schregno;          //左画面よりgetで取得
    public $issuedate = "";    //Form1.phpよりgetで取得(有効期間開始日付：YYYY-MM-DD)
    public $no;                //Form1.phpよりgetで取得(学籍番号)
    public $form2; //チェック用
    public $hist_issuedate;

    public function knjx_guardian_addressModel()
    {
    }

    public function init()
    {
        $this->field = array("GUARD_ISSUEDATE"  => str_replace("/", "-", VARS::post("GUARD_ISSUEDATE")),
                             "GUARD_EXPIREDATE" => str_replace("/", "-", VARS::post("GUARD_EXPIREDATE")),
                             "GUARD_ZIPCD"      => VARS::post("GUARD_ZIPCD"),
                             "GUARD_ADDR1"      => VARS::post("GUARD_ADDR1"),
                             "GUARD_ADDR2"      => VARS::post("GUARD_ADDR2"),
                             "GUARD_ADDR_FLG"   => VARS::post("GUARD_ADDR_FLG"),
                             "GUARD_TELNO"      => VARS::post("GUARD_TELNO"),
                             "GUARD_TELNO2"     => VARS::post("GUARD_TELNO2"),
                             "GUARD_FAXNO"      => VARS::post("GUARD_FAXNO"),
                             "GUARD_E_MAIL"     => VARS::post("GUARD_E_MAIL"),
                             "GUARD_UPDATED"    => VARS::post("GUARD_UPDATED")
                             );

        $this->cmd            = VARS::request("cmd");

        if (VARS::get("SCHREGNO")) {
            $this->schregno  = VARS::get("SCHREGNO");
            $this->sendPrgid = VARS::get("SEND_PRGID");
            $this->outh = VARS::get("SEND_AUTH");
        }
        if (VARS::get("GUARD_ISSUEDATE")) {
            $this->guard_issuedate  = VARS::get("GUARD_ISSUEDATE");
        }
        if ($this->cmd == "") {
            unset($this->guard_issuedate);
        }
        $this->getPropertiesAll();   //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //更新,削除時にリストから選択しているかをチェック
        if ($this->cmd == "update2") {
            if ($this->guard_issuedate =="") {
                $this->setWarning("MSG308");
                return false;
            }
        }
        $db = Query::dbCheckOut();

        //追加、更新時の必須項目(有効期間)チェック
        if ($this->cmd =="add2" || $this->cmd =="update2") {
            if ($this->field["GUARD_ISSUEDATE"] =="" || $this->field["GUARD_EXPIREDATE"] =="") {
                $this->setWarning("MSG301", "( 有効期間 )");
                return false;
            }
            //重複エラーチェック
            $query = knjx_guardian_addressQuery::getCntGAddress($this->schregno, $this->field["GUARD_ISSUEDATE"]);
            $cnt_G_Address = $db->getOne($query);
            if (($this->cmd =="add2") && $cnt_G_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }
        Query::dbCheckIn($db);

        //更新日付チェック
        if ($this->cmd =="update2" || $this->cmd =="delete2") {
            $db = Query::dbCheckOut();
            $query = knjx_guardian_addressQuery::getGuardianAddressDat($this->schregno, $this->guard_issuedate);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if ($row["UPDATED"] != $this->field["GUARD_UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }

        $addrSLen = $this->Properties["ADDR_INPUT_SIZE"] != "" ? $this->Properties["ADDR_INPUT_SIZE"] : 30;
        if ($this->Properties["chkAddrInputHankaku"] == "1") {
            //住所入力半角チェック
            if (!$this->chkHankakuIpt($this->field, "GUARD_ADDR1", "保護者住所1")) {
                return false;
            }
            if (!$this->chkHankakuIpt($this->field, "GUARD_ADDR2", "保護者住所2")) {
                return false;
            }
        }

        //入力時の文字数,半角カナチェック
        foreach (
                 array("GUARD_ZIPCD"     => array("( 郵便番号 )", 8),
                       "GUARD_ADDR1"     => array("( 保護者住所 )", intval($addrSLen) * 3),
                       "GUARD_ADDR2"     => array("( 保護者方書き )", intval($addrSLen) * 3),
                       "GUARD_TELNO"     => array("( 電話番号 )",14),
                       "GUARD_TELNO2"    => array("( 電話番号２ )",14),
                       "GUARD_FAXNO"     => array("( FAX番号 )", 14),
                       "GUARD_E_MAIL"    => array("( E-Mail )", 50)
                      ) as $key => $val
        ) {
            if (mbereg("[｡-ﾟ]", $this->field[$key])) {
                $this->setWarning("MSG901", $val[0]."半角カナは入力できません");
                return false;
            }
            if ($this->Properties["ADDR_INPUT_SIZE"] != "" && in_array($key, array("GUARD_ADDR1", "GUARD_ADDR2"), true)) {
                if (mb_strlen($this->field[$key], 'UTF-8') > intval($addrSLen)) {
                    $this->setWarning("MSG915", $val[0]."全角{$addrSLen}文字を超えています。");
                    return false;
                }
            } else {
                if (strlen($this->field[$key]) > $val[1]) {
                    $this->setWarning("MSG915", $val[0].$val[1]."バイトまで");
                    return false;
                }
            }
        }

        //有効期間開始日付と有効期間終了日付の日付の前後関係をチェック
        if ($this->cmd =="add2" || $this->cmd =="update2") {
            $GUARD_ISSUEDATE  = date("Y-m-d", strtotime(str_replace("-", "/", $this->field["GUARD_ISSUEDATE"])));
            $GUARD_EXPIREDATE = date("Y-m-d", strtotime(str_replace("-", "/", $this->field["GUARD_EXPIREDATE"])));
            if ($GUARD_EXPIREDATE <= $GUARD_ISSUEDATE) {
                $this->setWarning("MSG916", "( 有効期間 )");
                return false;
            }
        }

        $db = Query::dbCheckOut();
        $query = knjx_guardian_addressQuery::getMaxIssueDate($this);
        $max_issuedate = $db->getOne($query);
        $to_day = str_replace('-', '', $max_issuedate);
        $max_issuedate_1_day_after = date("Y-m-d", strtotime("1 day", strtotime($to_day)));
        $max_issuedate_2_day_after = date("Y-m-d", strtotime("2 day", strtotime($to_day)));

        //追加,更新時の有効期間の重複チェック
        if ($this->field["GUARD_ISSUEDATE"] != "") {
            $result = $db->query(knjx_guardian_addressQuery::getAddressAll($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (($row["GUARD_ISSUEDATE"] == str_replace("/", "-", $this->guard_issuedate)) && ($this->cmd =="update2")) {
                    continue;
                }
                $guard_issuedate  = $row["GUARD_ISSUEDATE"];
                $guard_expiredate = $row["GUARD_EXPIREDATE"];

                if ($this->cmd == 'add2') {
                    if (
                        ($guard_issuedate <= $GUARD_ISSUEDATE  && $GUARD_ISSUEDATE  <= $guard_expiredate && $GUARD_ISSUEDATE  <  $max_issuedate_1_day_after) ||
                        ($guard_issuedate <= $GUARD_EXPIREDATE && $GUARD_EXPIREDATE <= $guard_expiredate && $GUARD_EXPIREDATE <= $max_issuedate_1_day_after) ||
                        ($GUARD_ISSUEDATE <= $guard_issuedate  && $guard_expiredate <= $EXPIREDATE)
                    ) {
                        $this->setWarning("MSG302", "( 有効期間 )");
                        return false;
                    }
                }
                if ($this->cmd == 'update2') {
                    if (
                        ($guard_issuedate <= $GUARD_ISSUEDATE  && $GUARD_ISSUEDATE  <= $guard_expiredate) ||
                        ($guard_issuedate <= $GUARD_EXPIREDATE && $GUARD_EXPIREDATE <= $guard_expiredate) ||
                        ($GUARD_ISSUEDATE <= $guard_issuedate  && $guard_expiredate <= $EXPIREDATE)
                    ) {
                        $this->setWarning("MSG302", "( 有効期間 )");
                        return false;
                    }
                }
            }
            $result->free();
        }
        Query::dbCheckIn($db);

        //文字型チェック
        foreach (
                 array("GUARD_TELNO"  => array("[^-|0-9]", "( 電話番号 )"),
                       "GUARD_TELNO2" => array("[^-|0-9]", "( 電話番号２ )"),
                       "GUARD_FAXNO"  => array("[^-|0-9]", "( FAX番号 )"),
                       "GUARD_E_MAIL" => array("[^-._@a-z0-9]", "( E-Mail )")
                      ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }

        return true;
    }

    public function chkHankakuIpt($fld, $name, $msgInputName)
    {
        if (!$this->chkHankaku($fld[$name])) {
            $this->setWarning(MSG901, "{$msgInputName} 半角文字が入力されています。");
            return false;
        }
        return true;
    }

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
    }

    //インサート時の内部メソッド
    public function &getInsertModel()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        //入力チェック
        if ($this->validateInput()) {
            knjx_guardian_addressQuery::insertGuardianDat($this);
            $this->setMessage("MSG201");
            $this->guard_issuedate = $this->field["GUARD_ISSUEDATE"];
            return;
        }
    }

    //アップデート時の内部メソッド
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if (str_replace("/", "-", $this->guard_issuedate) != str_replace("/", "-", $this->field["GUARD_ISSUEDATE"])) {
                $db = Query::dbCheckOut();
                $query = knjx_guardian_addressQuery::getCntGAddress($this->schregno, $this->field["GUARD_ISSUEDATE"]);
                $gAddCnt = $db->getOne($query);
                Query::dbCheckIn($db);
                if ($gAddCnt > 0) {
                    $this->setWarning("MSG302");
                    return false;
                }
            }

            knjx_guardian_addressQuery::updateGuardianDat($this);
            $this->setMessage("MSG201");
            $this->guard_issuedate = $this->field["GUARD_ISSUEDATE"];

            return;
        }
    }
    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        if ($this->field["ISSUEDATE"] == "" && $this->issuedate == "") {
            $this->setWarning("MSG308");
            return false;
        }
        knjx_guardian_addressQuery::deleteGurdianAddressDat($this);
        unset($this->field["ISSUEDATE"]);
        unset($this->issuedate);
        $this->setMessage("MSG201");

        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
