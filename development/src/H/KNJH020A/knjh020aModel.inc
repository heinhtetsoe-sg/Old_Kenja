<?php

require_once('for_php7.php');
class knjh020aModel extends Model
{
    public $field = array();
    public $cmd;
    public $GradeClass;
    public $schregno;
    public $addno;
    public $prg;
    public $auth;
    public $clear;
    public $histField = array();
    public $control_data = array();    //コントロールマスタデータを保持
    public $nameSLen;  //氏名の入力文字数
    public $addrSLen;  //住所の入力文字数

    /**
     * メンバ変数の初期化
     */
    public function knjh020aModel()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->auth);
    }
    public function init()
    {
        //生徒保護者情報
        $this->field["GUARD_NAME"]          =  VARS::post("GUARD_NAME");       //保護者氏名
        $this->field["GUARD_KANA"]          =  VARS::post("GUARD_KANA");       //かな
        $this->field["GUARD_REAL_NAME"]     =  VARS::post("GUARD_REAL_NAME");  //戸籍氏名
        $this->field["GUARD_REAL_KANA"]     =  VARS::post("GUARD_REAL_KANA");  //戸籍氏名かな
        $this->field["GUARD_SEX"]           =  VARS::post("GUARD_SEX");        //性別
        $this->field["GUARD_BIRTHDAY"]      =  VARS::post("GUARD_BIRTHDAY");   //誕生日
        $this->field["RELATIONSHIP"]        =  VARS::post("RELATIONSHIP");     //続柄
        $this->field["GUARD_ZIPCD"]         =  VARS::post("GUARD_ZIPCD");      //郵便番号
        $this->field["GUARD_ADDR1"]         =  VARS::post("GUARD_ADDR1");      //住所１
        $this->field["GUARD_ADDR2"]         =  VARS::post("GUARD_ADDR2");      //住所２
        $this->field["GUARD_TELNO"]         =  VARS::post("GUARD_TELNO");      //電話番号
        $this->field["GUARD_FAXNO"]         =  VARS::post("GUARD_FAXNO");      //ＦＡＸ番号
        $this->field["GUARD_E_MAIL"]        =  VARS::post("GUARD_E_MAIL");     //E-mailアドレス
        $this->field["GUARD_JOBCD"]         =  VARS::post("GUARD_JOBCD");      //職種コード
        $this->field["GUARD_WORK_NAME"]     =  VARS::post("GUARD_WORK_NAME");  //勤務先名称
        $this->field["GUARD_WORK_TELNO"]    =  VARS::post("GUARD_WORK_TELNO"); //勤務先電話番号
        $this->field["UPDATED"]             =  VARS::post("UPDATED");          //更新日付
        //生徒保証人情報
        $this->field["GUARANTOR_NAME"]          =  VARS::post("GUARANTOR_NAME");         //氏名
        $this->field["GUARANTOR_KANA"]          =  VARS::post("GUARANTOR_KANA");         //かな
        $this->field["GUARANTOR_REAL_NAME"]     =  VARS::post("GUARANTOR_REAL_NAME");    //戸籍氏名
        $this->field["GUARANTOR_REAL_KANA"]     =  VARS::post("GUARANTOR_REAL_KANA");    //戸籍氏名かな
        $this->field["GUARANTOR_SEX"]           =  VARS::post("GUARANTOR_SEX");          //性別
        $this->field["GUARANTOR_RELATIONSHIP"]  =  VARS::post("GUARANTOR_RELATIONSHIP"); //続柄
        $this->field["GUARANTOR_ZIPCD"]         =  VARS::post("J_GUARANTOR_ZIPCD");      //郵便番号
        $this->field["GUARANTOR_ADDR1"]         =  VARS::post("GUARANTOR_ADDR1");        //住所１
        $this->field["GUARANTOR_ADDR2"]         =  VARS::post("GUARANTOR_ADDR2");        //住所２
        $this->field["GUARANTOR_TELNO"]         =  VARS::post("GUARANTOR_TELNO");        //電話番号
        $this->field["GUARANTOR_JOBCD"]         =  VARS::post("GUARANTOR_JOBCD");        //職種コード
        $this->field["PUBLIC_OFFICE"]           =  VARS::post("PUBLIC_OFFICE");          //保証人等の勤務先タイトル名称

        //その他情報
        $div_array = array('1', '2', '3');
        foreach ($div_array as $div) {
            $div_name = "_".$div;
            $this->field["SEND_NAME".$div_name]             =  VARS::post("SEND_NAME".$div_name);           //氏名
            $this->field["SEND_KANA".$div_name]             =  VARS::post("SEND_KANA".$div_name);           //かな
            $this->field["SEND_SEX".$div_name]              =  VARS::post("SEND_SEX".$div_name);            //性別
            $this->field["SEND_RELATIONSHIP".$div_name]     =  VARS::post("SEND_RELATIONSHIP".$div_name);   //続柄
            $this->field["SEND_ZIPCD".$div_name]            =  VARS::post("SEND_ZIPCD".$div_name);        //郵便番号
            $this->field["SEND_AREACD".$div_name]           =  VARS::post("SEND_AREACD".$div_name);         //地域コード
            $this->field["SEND_ADDR1".$div_name]            =  VARS::post("SEND_ADDR1".$div_name);          //住所１
            $this->field["SEND_ADDR2".$div_name]            =  VARS::post("SEND_ADDR2".$div_name);          //住所２
            $this->field["SEND_ADDR_FLG".$div_name]         =  VARS::post("SEND_ADDR_FLG".$div_name);       //住所フラグ
            $this->field["SEND_TELNO".$div_name]            =  VARS::post("SEND_TELNO".$div_name);          //電話番号
            $this->field["SEND_TELNO2".$div_name]           =  VARS::post("SEND_TELNO2".$div_name);         //電話番号２
            $this->field["SEND_JOBCD".$div_name]            =  VARS::post("SEND_JOBCD".$div_name);          //職種コード
            $this->field["SEND_PUBLIC_OFFICE".$div_name]    =  VARS::post("SEND_PUBLIC_OFFICE".$div_name);  //保証人等の勤務先タイトル名称
        }

        //保護者履歴関係
        $this->field["E_APPDATE"]               =  VARS::post("E_APPDATE");
        $this->field["RELATIONSHIP_FLG"]        =  VARS::post("RELATIONSHIP_FLG");
        $this->field["GUARD_NAME_FLG"]          =  VARS::post("GUARD_NAME_FLG");
        $this->field["GUARD_KANA_FLG"]          =  VARS::post("GUARD_KANA_FLG");
        $this->field["GUARD_REAL_NAME_FLG"]     =  VARS::post("GUARD_REAL_NAME_FLG");
        $this->field["GUARD_REAL_KANA_FLG"]     =  VARS::post("GUARD_REAL_KANA_FLG");
        $this->field["GUARD_SEX_FLG"]           =  VARS::post("GUARD_SEX_FLG");
        $this->field["GUARD_BIRTHDAY_FLG"]      =  VARS::post("GUARD_BIRTHDAY_FLG");

        //保証人履歴関係
        $this->field["E_APPDATE2"]                  =  VARS::post("E_APPDATE2");
        $this->field["GUARANTOR_RELATIONSHIP_FLG"]  =  VARS::post("GUARANTOR_RELATIONSHIP_FLG");
        $this->field["GUARANTOR_NAME_FLG"]          =  VARS::post("GUARANTOR_NAME_FLG");
        $this->field["GUARANTOR_KANA_FLG"]          =  VARS::post("GUARANTOR_KANA_FLG");
        $this->field["GUARANTOR_REAL_NAME_FLG"]     =  VARS::post("GUARANTOR_REAL_NAME_FLG");
        $this->field["GUARANTOR_REAL_KANA_FLG"]     =  VARS::post("GUARANTOR_REAL_KANA_FLG");
        $this->field["GUARANTOR_SEX_FLG"]           =  VARS::post("GUARANTOR_SEX_FLG");

        //学籍番号
        $this->field["tmpSCHREGNO"]     =  VARS::post("tmpSCHREGNO");

        //履歴修正
        if (
            VARS::request("ISSUEDATE") ||
            $this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel" || $this->cmd == "changeCmb" ||
            $this->cmd == "histAdd2" || $this->cmd == "histUpd2" || $this->cmd == "histDel2" || $this->cmd == "changeCmb2"
        ) {
            $setGradeHrAtteFlg = VARS::request("GRADE_HR_ATTE_FLG");
            $setCoursemajorFlg = VARS::request("COURSEMAJOR_FLG");
            $setGradeHr = explode(",", VARS::request("GRADE_CLASS"));
            $setCourseMajor = explode(",", VARS::request("COURSEMAJORCD"));
            $this->histField = array("ISSUEDATE"            => VARS::request("ISSUEDATE"),
                                     "EXPIREDATE"           => VARS::request("EXPIREDATE"),
                                     "SCHREGNO"             => VARS::request("SCHREGNO"),
                                     "RELATIONSHIP"         => VARS::request("RELATIONSHIP"),
                                     "GUARD_NAME"           => VARS::request("GUARD_NAME"),
                                     "GUARD_KANA"           => VARS::request("GUARD_KANA"),
                                     "GUARD_REAL_NAME"      => VARS::request("GUARD_REAL_NAME"),
                                     "GUARD_REAL_KANA"      => VARS::request("GUARD_REAL_KANA"),
                                     "GUARD_SEX"            => VARS::request("GUARD_SEX"),
                                     "GUARD_BIRTHDAY"       => VARS::request("GUARD_BIRTHDAY"),
                                     "RELATIONSHIP_FLG"     => VARS::request("RELATIONSHIP_FLG"),
                                     "GUARD_NAME_FLG"       => VARS::request("GUARD_NAME_FLG"),
                                     "GUARD_KANA_FLG"       => VARS::request("GUARD_KANA_FLG"),
                                     "GUARD_REAL_NAME_FLG"  => VARS::request("GUARD_REAL_NAME_FLG"),
                                     "GUARD_REAL_KANA_FLG"  => VARS::request("GUARD_REAL_KANA_FLG"),
                                     "GUARD_SEX_FLG"        => VARS::request("GUARD_SEX_FLG"),
                                     "GUARD_BIRTHDAY_FLG"   => VARS::request("GUARD_BIRTHDAY_FLG"),

                                     "GUARANTOR_RELATIONSHIP"       => VARS::request("GUARANTOR_RELATIONSHIP"),
                                     "GUARANTOR_NAME"               => VARS::request("GUARANTOR_NAME"),
                                     "GUARANTOR_KANA"               => VARS::request("GUARANTOR_KANA"),
                                     "GUARANTOR_REAL_NAME"          => VARS::request("GUARANTOR_REAL_NAME"),
                                     "GUARANTOR_REAL_KANA"          => VARS::request("GUARANTOR_REAL_KANA"),
                                     "GUARANTOR_SEX"                => VARS::request("GUARANTOR_SEX"),
                                     "GUARANTOR_RELATIONSHIP_FLG"   => VARS::request("GUARANTOR_RELATIONSHIP_FLG"),
                                     "GUARANTOR_NAME_FLG"           => VARS::request("GUARANTOR_NAME_FLG"),
                                     "GUARANTOR_KANA_FLG"           => VARS::request("GUARANTOR_KANA_FLG"),
                                     "GUARANTOR_REAL_NAME_FLG"      => VARS::request("GUARANTOR_REAL_NAME_FLG"),
                                     "GUARANTOR_REAL_KANA_FLG"      => VARS::request("GUARANTOR_REAL_KANA_FLG"),
                                     "GUARANTOR_SEX_FLG"            => VARS::request("GUARANTOR_SEX_FLG")
                            );
        } else {
            unset($this->histField);
        }

        $this->cmd      = VARS::request("cmd");
        $post_year      = VARS::request("YEAR");
        $this->year     = VARS::post("year");
        $this->clear    = VARS::post("clear");

        $this->addno    = VARS::post("J_STUCD");

        if (VARS::post("GrCl")) {
            $this->GradeClass = VARS::post("GrCl");
        }
        if (VARS::get("init") == 1) {
            $this->schregno = "";
        } elseif (VARS::request("SCHREGNO")) {
            $this->schregno = VARS::request("SCHREGNO");
        }
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->auth);
        }
        //KNJH160からのコール
        if (VARS::get("PRG")) {
            $this->prg = VARS::get("PRG");
        }
        if (VARS::get("AUTH")) {
            $this->auth = VARS::get("AUTH");
        }
        //履歴日付
        if (VARS::get("HIST_ISSUEDATE")) {
            $this->hist_issuedate = VARS::get("HIST_ISSUEDATE");
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if ($this->Properties["NAME_INPUT_SIZE"] != "") {
            $this->nameSLen = $this->Properties["NAME_INPUT_SIZE"];
        } else {
            $this->nameSLen = 40;
        }
        if ($this->Properties["ADDR_INPUT_SIZE"] != "") {
            $this->addrSLen = $this->Properties["ADDR_INPUT_SIZE"];
        } else {
            $this->addrSLen = 30;
        }

        //保証人等の勤務先タイトル名称設定
        $this->publicOffice_TitleName = $this->Properties["PublicOffice_TitleNameSettei"] == "1" ? "就業先" : "兼ねている公職";
    }

    //入力項目チェック
    public function validateInput($type)
    {
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT) {
            $this->setWarning("MSG300");
            return false;
        }

        if (!$this->schregno) {
            $this->setWarning("MSG308");
            return false;
        }

        $db = Query::dbCheckOut();
        if ($type == "add") {
            $query = knjh020aQuery::getParentsdataCnt($this->addno);
            $row = $db->getOne($query);
            if ($row != 0) {
                $this->setWarning("MSG104", "学籍保護者データ");
                return false;
            }
        }

        if ($type == "delete") {
            $query = knjh020aQuery::getGuardian($this->schregno);
            $row = $db->getOne($query);

            if ($row == 0) {
                $this->setWarning("MSG303");
                return false;
            }
        }
        Query::dbCheckIn($db);
        if ($this->Properties["chkNameInputHankaku"] == "1") {
            //氏名入力半角チェック
            if (!$this->chkHankakuIpt($this->field, "GUARD_NAME", "保護者氏名漢字")) {
                return false;
            }
            if (!$this->chkHankakuIpt($this->field, "GUARD_REAL_NAME", "戸籍氏名")) {
                return false;
            }
            if (!$this->chkHankakuIpt($this->field, "GUARANTOR_NAME", "保証人氏名漢字")) {
                return false;
            }
            if (!$this->chkHankakuIpt($this->field, "GUARANTOR_REAL_NAME", "保証人戸籍氏名")) {
                return false;
            }
            $div_array = array("1" => "その他", "2" => "その他２", "3" => "下宿保証人");
            foreach ($div_array as $div => $label) {
                if (!$this->chkHankakuIpt($this->field, "SEND_NAME_".$div, "(".$label."氏名漢字)")) {
                    return false;
                }
            }
        }

        if ($this->Properties["chkAddrInputHankaku"] == "1") {
            //住所入力半角チェック
            if (!$this->chkHankakuIpt($this->field, "GUARD_ADDR1", "保護者住所1")) {
                return false;
            }
            if (!$this->chkHankakuIpt($this->field, "GUARD_ADDR2", "保護者住所2")) {
                return false;
            }
            if (!$this->chkHankakuIpt($this->field, "GUARANTOR_ADDR1", "保証人住所1")) {
                return false;
            }
            if (!$this->chkHankakuIpt($this->field, "GUARANTOR_ADDR2", "保証人住所2")) {
                return false;
            }
            $div_array = array("1" => "その他", "2" => "その他２", "3" => "下宿保証人");
            foreach ($div_array as $div => $label) {
                $div_name = "_".$div;
                if (!$this->chkHankakuIpt($this->field, "SEND_ADDR1".$div_name, "(".$label."住所1)")) {
                    return false;
                }
                if (!$this->chkHankakuIpt($this->field, "SEND_ADDR2".$div_name, "(".$label."住所2)")) {
                    return false;
                }
            }
        }

        //文字数チェック
        if ($type == "update") {
            if ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["GUARD_NAME"]) > (intval($this->nameSLen) * 3)) {
                $this->setWarning(MSG901, "(保護者氏名漢字) {intval($this->nameSLen) * 3}バイトまで");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["GUARD_NAME"], 'UTF-8') > $this->nameSLen) {
                $this->setWarning(MSG901, "(保護者氏名漢字) 全角{$this->nameSLen}文字を超えています。");
                return false;
            } elseif (strlen($this->field["GUARD_KANA"]) > 240) {
                $this->setWarning("MSG901", "(保護者氏名かな) 240バイトまで");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["GUARD_REAL_NAME"]) > (intval($this->nameSLen) * 3)) {
                $this->setWarning(MSG901, "(戸籍氏名) {intval($this->nameSLen) * 3}バイトまで");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["GUARD_REAL_NAME"], 'UTF-8') > $this->nameSLen) {
                $this->setWarning(MSG901, "(戸籍氏名) 全角{$this->nameSLen}文字を超えています。");
                return false;
            } elseif (strlen($this->field["GUARD_REAL_KANA"]) > 240) {
                $this->setWarning("MSG901", "(戸籍氏名かな) 240バイトまで");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["GUARD_ADDR1"]) > (intval($this->addrSLen) * 3)) {
                $this->setWarning("MSG901", "(住所1) intval($this->addrSLen) * 3{}バイトまで");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["GUARD_ADDR1"], 'UTF-8') > intval($this->addrSLen)) {
                $this->setWarning("MSG901", "(住所1) 全角{$this->addrSLen}文字を超えています。");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["GUARD_ADDR2"]) > (intval($this->addrSLen) * 3)) {
                $this->setWarning("MSG901", "(住所2) {intval($this->addrSLen) * 3}バイトまで");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["GUARD_ADDR2"], 'UTF-8') > intval($this->addrSLen)) {
                $this->setWarning("MSG901", "(住所2) 全角{$this->addrSLen}文字を超えています。");
                return false;
            } elseif (strlen($this->field["GUARD_TELNO"]) > 14) {
                $this->setWarning("MSG901", "(電話番号) 14バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_FAXNO"]) > 14) {
                $this->setWarning("MSG901", "(FAX番号) 14バイトまで");
                return false;
            } elseif (strlen($this->field["E_MAIL"]) > 50) {
                $this->setWarning("MSG901", "(GUARD_E_MAIL) 50バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_WORK_NAME"]) > 120) {
                $this->setWarning("MSG901", "(勤務先名称) 120バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_WORK_TELNO"]) > 14) {
                $this->setWarning("MSG901", "(勤務先電話番号) 14バイトまで");
                return false;
            } elseif ($this->field["RELATIONSHIP"] == "00") {
                $this->setWarning("MSG901", "(続柄)");
                return false;
            }

            if ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["GUARANTOR_NAME"]) > (intval($this->nameSLen) * 3)) {
                $this->setWarning(MSG901, "(保証人氏名漢字) {intval($this->nameSLen) * 3}バイトまで");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["GUARANTOR_NAME"], 'UTF-8') > $this->nameSLen) {
                $this->setWarning(MSG901, "(保証人氏名漢字) 全角{$this->nameSLen}文字を超えています。");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["GUARANTOR_REAL_NAME"]) > (intval($this->nameSLen) * 3)) {
                $this->setWarning(MSG901, "(保証人戸籍氏名) {intval($this->nameSLen) * 3}バイトまで");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["GUARANTOR_REAL_NAME"], 'UTF-8') > $this->nameSLen) {
                $this->setWarning(MSG901, "(保証人戸籍氏名) 全角{$this->nameSLen}文字を超えています。");
                return false;
            } elseif (strlen($this->field["GUARANTOR_KANA"]) > 240) {
                $this->setWarning("MSG901", "(保証人氏名かな) 240バイトまで");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["GUARANTOR_ADDR1"]) > (intval($this->addrSLen) * 3)) {
                $this->setWarning("MSG901", "(住所1) {(intval($this->addrSLen) * 3)}バイトまで");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && strlen($this->field["GUARANTOR_ADDR1"]) > $this->addrSLen) {
                $this->setWarning("MSG901", "(住所1) 全角{$this->addrSLen}文字を超えています。");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["GUARANTOR_ADDR2"]) > (intval($this->addrSLen) * 3)) {
                $this->setWarning("MSG901", "(住所2) {(intval($this->addrSLen) * 3)}バイトまで");
                return false;
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && strlen($this->field["GUARANTOR_ADDR2"]) > $this->addrSLen) {
                $this->setWarning("MSG901", "(住所2) 全角{$this->addrSLen}文字を超えています。");
                return false;
            } elseif (strlen($this->field["GUARANTOR_TELNO"]) > 14) {
                $this->setWarning("MSG901", "(電話番号) 14バイトまで");
                return false;
            } elseif (strlen($this->field["PUBLIC_OFFICE"]) > 30) {
                $this->setWarning("MSG901", "(".$this->publicOffice_TitleName.") 30バイトまで");
                return false;
            }

            $div_array = array("1" => "その他", "2" => "その他２", "3" => "下宿保証人");
            foreach ($div_array as $div => $label) {
                $div_name = "_".$div;

                if ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["SEND_NAME".$div_name]) > (intval($this->nameSLen) * 3)) {
                    $this->setWarning(MSG901, "(".$label."氏名漢字) {(intval($this->nameSLen) * 3)}バイトまで");
                    return false;
                } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["SEND_NAME".$div_name], 'UTF-8') > $this->nameSLen) {
                    $this->setWarning(MSG901, "(".$label."氏名漢字) 全角{$this->nameSLen}文字を超えています。");
                    return false;
                } elseif (strlen($this->field["SEND_KANA".$div_name]) > 240) {
                    $this->setWarning("MSG901", "(".$label."氏名かな) 240バイトまで");
                    return false;
                } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["SEND_ADDR1".$div_name]) > (intval($this->addrSLen) * 3)) {
                    $this->setWarning("MSG901", "(".$label."住所1) {(intval($this->addrSLen) * 3)}バイトまで");
                    return false;
                } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["SEND_ADDR1".$div_name], 'UTF-8') > $this->addrSLen) {
                    $this->setWarning("MSG901", "(".$label."住所1) 全角{$this->addrSLen}文字を超えています。");
                    return false;
                } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["SEND_ADDR2".$div_name]) > (intval($this->addrSLen) * 3)) {
                    $this->setWarning("MSG901", "(".$label."住所2) {(intval($this->addrSLen) * 3)}バイトまで");
                    return false;
                } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["SEND_ADDR2".$div_name], 'UTF-8') > $this->addrSLen) {
                    $this->setWarning("MSG901", "(".$label."住所2) 全角{$this->addrSLen}文字を超えています。");
                    return false;
                } elseif (strlen($this->field["SEND_TELNO".$div_name]) > 14) {
                    $this->setWarning("MSG901", "(".$label."電話番号1) 14バイトまで");
                    return false;
                } elseif (strlen($this->field["SEND_TELNO2".$div_name]) > 14) {
                    $this->setWarning("MSG901", "(".$label."電話番号2) 14バイトまで");
                    return false;
                } elseif (strlen($this->field["SEND_PUBLIC_OFFICE".$div_name]) > 30) {
                    $this->setWarning("MSG901", "(".$label.$this->publicOffice_TitleName.") 30バイトまで");
                    return false;
                }
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

    /**
     * 編集完了用のモデルを返します
    */
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            if ($this->cmd == 'update') {
                knjh020aQuery::getInsertQuery($this);
                knjh020aQuery::getInsert2Query($this);
                $db = Query::dbCheckOut();

                $db->autoCommit(false);
                $this->guardUpdFromHist($db, $this->schregno);
                $db->commit();

                Query::dbCheckIn($db);
                $this->setMessage("MSG201");
            } else {
                knjh020aQuery::updateGuardianDat($this);
                $db = Query::dbCheckOut();

                $db->autoCommit(false);
                $this->guardUpdFromHist($db, $this->schregno);
                $db->commit();

                Query::dbCheckIn($db);
                $this->setMessage("MSG201");
            }
            return;
        }
    }

    public function &getAddingModel()
    {
        //入力チェック
        if ($this->validateInput("add")) {
            if ($this->reflect()) {
                if ($this->add()) {
                    $this->setMessage("MSG201");
                }
            }
        }
    }

    /**
     * 削除用のモデルを返します
     */
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
            $this->setMessage("MSG201");
        }
    }

    public function &delete()
    {
        $result  = knjh020aQuery::getDeleteQuery($this->schregno);
        $result2 = knjh020aQuery::getDelete2Query($this->schregno);
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $this->guardUpdFromHist($db, $this->schregno);
        $db->commit();

        Query::dbCheckIn($db);
        return $result;
        return $result2;
    }

    public function &insert()
    {
        $result  = knjh020aQuery::getInsertQuery($this);
        $result2 = knjh020aQuery::getInsert2Query($this);
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $this->guardUpdFromHist($db, $this->schregno);
        $db->commit();

        Query::dbCheckIn($db);
        return $result;
        return $result2;
    }

    //兄弟姉妹データからコピー
    public function &reflect()
    {
        $reflect = knjh020aQuery::getRowParents($this->addno);
        return $reflect;
    }
    public function &add()
    {
        $result = knjh020aQuery::getAddingQuery($this);
        return $result;
    }

    //親履歴の更新
    public function &getUpdateHistModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        if (!$this->validateInputHist($db)) {
            if ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel") {
                $this->cmd = "histEdit";
            } else {
                $this->cmd = "histEdit2";
            }
            return;
        }

        if ($this->cmd == "histAdd") {
            $this->addHist($db);
        } elseif ($this->cmd == "histUpd") {
            $this->delHist($db);
            $this->addHist($db);
        } elseif ($this->cmd == "histDel") {
            $this->delHist($db);
        } elseif ($this->cmd == "histAdd2") {
            $this->addHistHosyouNin($db);
        } elseif ($this->cmd == "histUpd2") {
            $this->delHistHosyouNin($db);
            $this->addHistHosyouNin($db);
        } else {
            $this->delHistHosyouNin($db);
        }

        $this->guardUpdFromHist($db, $this->schregno);

        $this->setMessage("MSG201");

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //入力項目チェック
    public function validateInputHist($db)
    {
        if (
            $this->schregno == "" &&
            ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel" ||
            $this->cmd == "histAdd2" || $this->cmd == "histUpd2" || $this->cmd == "histDel2")
        ) {
            $this->setWarning("MSG308");
            return false;
        }
        if (
            ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel"
             || $this->cmd == "histAdd2" || $this->cmd == "histUpd2" || $this->cmd == "histDel2")
            && $this->histField["SCHREGNO"] != $this->schregno
        ) {
            $this->setMessage("MSG300", "\\n学籍番号は変更できません！");
            return false;
        }

        $checkArray["ISSUEDATE"]       = array("TITLE" => "開始日",      "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["EXPIREDATE"]      = array("TITLE" => "終了日",      "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel") {
            $checkArray["RELATIONSHIP"]    = array("TITLE" => "続柄",        "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
            $checkArray["GUARD_NAME"]      = array("TITLE" => "氏名",        "HISSU" => "ON", "LENGTH" => "ON", "LEN" => $this->nameSLen * 3);
            $checkArray["GUARD_KANA"]      = array("TITLE" => "氏名かな",    "HISSU" => "ON", "LENGTH" => "ON", "LEN" => "240");
            $checkArray["GUARD_REAL_NAME"] = array("TITLE" => "戸籍氏名",    "HISSU" => "",   "LENGTH" => "ON", "LEN" => $this->nameSLen * 3);
            $checkArray["GUARD_REAL_KANA"] = array("TITLE" => "戸籍氏名かな","HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");
            $checkArray["GUARD_SEX"]       = array("TITLE" => "性別",        "HISSU" => "",   "LENGTH" => "",   "LEN" => "");
            $checkArray["GUARD_BIRTHDAY"]  = array("TITLE" => "生年月日",    "HISSU" => "",   "LENGTH" => "",   "LEN" => "");
        } else {
            $checkArray["GUARANTOR_RELATIONSHIP"]   = array("TITLE" => "続柄",        "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
            $checkArray["GUARANTOR_NAME"]           = array("TITLE" => "氏名",        "HISSU" => "ON", "LENGTH" => "ON", "LEN" => $this->nameSLen * 3);
            $checkArray["GUARANTOR_KANA"]           = array("TITLE" => "氏名かな",    "HISSU" => "ON", "LENGTH" => "ON", "LEN" => "240");
            $checkArray["GUARANTOR_REAL_NAME"]      = array("TITLE" => "戸籍氏名",    "HISSU" => "",   "LENGTH" => "ON", "LEN" => $this->nameSLen * 3);
            $checkArray["GUARANTOR_REAL_KANA"]      = array("TITLE" => "戸籍氏名かな","HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");
            $checkArray["GUARANTOR_SEX"]            = array("TITLE" => "性別",        "HISSU" => "",   "LENGTH" => "",   "LEN" => "");
        }

        //必須項目チェック
        $flag = false;
        $msg = "";
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histAdd2" || $this->cmd == "histUpd2") {
            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["HISSU"]) {
                    if ($this->histField[$fieldName] == "") {
                        $flag = true;
                        $msg .= "\\n".$fieldVal["TITLE"];
                    }
                }
            }
        } else {
            //開始日付
            if ($this->histField["ISSUEDATE"] == "") {
                $flag = true;
                $msg .= "\\n開始日付";
            }
            //学籍番号
            if ($this->histField["SCHREGNO"] == "") {
                $flag = true;
                $msg .= "\\n学籍番号";
            }
        }
        if ($flag == true) {
            $this->setWarning("MSG301", $msg);
            return false;
        }
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histAdd2" || $this->cmd == "histUpd2") {
            if (str_replace("/", "", $this->histField["ISSUEDATE"]) > str_replace("/", "", $this->histField["EXPIREDATE"])) {
                $this->setWarning("MSG901", "開始終了日付が、逆転しています。");
                return false;
            }

            if ($this->cmd == "histAdd" || $this->cmd == "histUpd") {
                $query = knjh020aQuery::getGuardianHistDateCheck($this);
            } else {
                $query = knjh020aQuery::getGuarantorHistDateCheck($this);
            }
            $dateExists = $db->getOne($query);
            if ($dateExists > "0") {
                $this->setWarning("MSG901", "日付範囲が不正です。");
                return false;
            }
        }

        if ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel") {
            $query = knjh020aQuery::getGuardianHistData($this->schregno, $this->histField["ISSUEDATE"]);
        } else {
            $query = knjh020aQuery::getGuarantorHistData($this->schregno, $this->histField["ISSUEDATE"]);
        }
        $checkExists = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (($this->cmd == "histAdd" || $this->cmd == "histAdd2") && is_array($checkExists)) {
            $this->setWarning("MSG302");
            return false;
        } elseif (($this->cmd == "histUpd" || $this->cmd == "histDel" || $this->cmd == "histUpd2" || $this->cmd == "histDel2") && !is_array($checkExists)) {
            $this->setWarning("MSG308");
            return false;
        }

        foreach (
                 array("GUARD_NAME" => array("( 氏名 )","[｡-ﾟ]"),
                       "GUARANTOR_NAME" => array("( 氏名 )","[｡-ﾟ]"),
                       "GUARD_KANA" => array("( 氏名かな )","[｡-ﾟ]"),
                       "GUARANTOR_KANA" => array("( 氏名かな )","[｡-ﾟ]")) as $key => $val
        ) {
            if (mbereg($val[1], $this->histField[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        foreach (
                 array("GUARD_REAL_NAME" => array("( 戸籍氏名 )","[｡-ﾟ]"),
                       "GUARANTOR_REAL_NAME" => array("( 戸籍氏名 )","[｡-ﾟ]"),
                       "GUARD_REAL_KANA" => array("( 戸籍氏名かな )","[｡-ﾟ]"),
                       "GUARANTOR_REAL_KANA" => array("( 戸籍氏名かな )","[｡-ﾟ]")) as $key => $val
        ) {
            if (mbereg($val[1], $this->histField[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        if ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histAdd2" || $this->cmd == "histUpd2") {
            if ($this->Properties["chkNameInputHankaku"] != "") {
                foreach (
                         array("GUARD_NAME" => "( 氏名 )",
                               "GUARANTOR_NAME" => "( 氏名 )",
                               "GUARD_REAL_NAME" => "( 戸籍氏名 )",
                               "GUARANTOR_REAL_NAME" => "( 戸籍氏名 )") as $key => $val
                ) {
                    if (!$this->chkHankakuIpt($this->histField, $key, $val)) {
                        return false;
                    }
                }
            }
        }

        //文字数チェック
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd") {
            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["LENGTH"]) {
                    if ($this->Properties["NAME_INPUT_SIZE"] != "" && in_array($fieldName, array("GUARD_NAME", "GUARANTOR_NAME", "GUARD_REAL_NAME", "GUARANTOR_REAL_NAME"), true)) {
                        if (mb_strlen($this->histField[$fieldName], 'UTF-8') > $this->nameSLen) {
                            $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) 全角".$this->nameSLen."文字まで");
                            return false;
                        }
                    } else {
                        if (strlen($this->histField[$fieldName]) > $fieldVal["LEN"]) {
                            $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) ".$fieldVal["LEN"]."バイトまで");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public function addHist($db)
    {
        $query = knjh020aQuery::getInsHistQuery($this);
        $this->hist_issuedate = $this->histField["ISSUEDATE"];
        $this->cmd = "histEdit";
        $db->query($query);
    }

    public function delHist($db)
    {
        $query = knjh020aQuery::getDelHistQuery($this);
        $this->cmd = "histEdit";
        $db->query($query);
    }

    public function addHistHosyouNin($db)
    {
        $query = knjh020aQuery::getInsHistHosyouNinQuery($this);
        $this->hist_issuedate = $this->histField["ISSUEDATE"];
        $this->cmd = "histEdit2";
        $db->query($query);
    }

    public function delHistHosyouNin($db)
    {
        $query = knjh020aQuery::getDelHistHosyouNinQuery($this);
        $this->cmd = "histEdit2";
        $db->query($query);
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    public function guardUpdFromHist($db, $schregno)
    {
        $query = knjh020aQuery::getGuardHistUpd($this, $schregno);
        $guardHistData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = knjh020aQuery::getGuarantHistUpd($this, $schregno);
        $guarantHistData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $guardUpdFlg = false;
        $guard = array();
        $guarantUpdFlg = false;
        $guarant = array();
        if (is_array($guardHistData)) {
            if ($guardHistData["RELATIONSHIP_FLG"] == "1") {
                $guardUpdFlg = true;
                $guard["RELATIONSHIP"][TEXT] = $guardHistData["RELATIONSHIP"];
            }
            if ($guardHistData["GUARD_NAME_FLG"] == "1") {
                $guardUpdFlg = true;
                $guard["GUARD_NAME"][TEXT] = $guardHistData["GUARD_NAME"];
            }
            if ($guardHistData["GUARD_KANA_FLG"] == "1") {
                $guardUpdFlg = true;
                $guard["GUARD_KANA"][TEXT] = $guardHistData["GUARD_KANA"];
            }
            if ($guardHistData["GUARD_REAL_NAME_FLG"] == "1") {
                $guardUpdFlg = true;
                $guard["GUARD_REAL_NAME"][TEXT] = $guardHistData["GUARD_REAL_NAME"];
            }
            if ($guardHistData["GUARD_REAL_KANA_FLG"] == "1") {
                $guardUpdFlg = true;
                $guard["GUARD_REAL_KANA"][TEXT] = $guardHistData["GUARD_REAL_KANA"];
            }
            if ($guardHistData["GUARD_SEX_FLG"] == "1") {
                $guardUpdFlg = true;
                $guard["GUARD_SEX"][TEXT] = $guardHistData["GUARD_SEX"];
            }
            if ($guardHistData["GUARD_BIRTHDAY_FLG"] == "1") {
                $guardUpdFlg = true;
                $guard["GUARD_BIRTHDAY"][TEXT] = $guardHistData["GUARD_BIRTHDAY"];
            }

            if ($guardUpdFlg) {
                $query = knjh020aQuery::getGuardUpdFromHist($this, $guard, $schregno);
                $db->query($query);
            }
        }

        if (is_array($guarantHistData)) {
            if ($guarantHistData["GUARANTOR_RELATIONSHIP_FLG"] == "1") {
                $guarantUpdFlg = true;
                $guarant["GUARANTOR_RELATIONSHIP"][TEXT] = $guarantHistData["GUARANTOR_RELATIONSHIP"];
            }
            if ($guarantHistData["GUARANTOR_NAME_FLG"] == "1") {
                $guarantUpdFlg = true;
                $guarant["GUARANTOR_NAME"][TEXT] = $guarantHistData["GUARANTOR_NAME"];
            }
            if ($guarantHistData["GUARANTOR_KANA_FLG"] == "1") {
                $guarantUpdFlg = true;
                $guarant["GUARANTOR_KANA"][TEXT] = $guarantHistData["GUARANTOR_KANA"];
            }
            if ($guarantHistData["GUARANTOR_REAL_NAME_FLG"] == "1") {
                $guarantUpdFlg = true;
                $guarant["GUARANTOR_REAL_NAME"][TEXT] = $guarantHistData["GUARANTOR_REAL_NAME"];
            }
            if ($guarantHistData["GUARANTOR_REAL_KANA_FLG"] == "1") {
                $guarantUpdFlg = true;
                $guarant["GUARANTOR_REAL_KANA"][TEXT] = $guarantHistData["GUARANTOR_REAL_KANA"];
            }
            if ($guarantHistData["GUARANTOR_SEX_FLG"] == "1") {
                $guarantUpdFlg = true;
                $guarant["GUARANTOR_SEX"][TEXT] = $guarantHistData["GUARANTOR_SEX"];
            }

            if ($guarantUpdFlg) {
                $query = knjh020aQuery::getGuardUpdFromHist($this, $guarant, $schregno);
                $db->query($query);
            }
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
}
