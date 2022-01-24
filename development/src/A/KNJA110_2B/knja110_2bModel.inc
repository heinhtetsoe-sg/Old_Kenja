<?php

require_once('for_php7.php');


class knja110_2bModel extends Model {
    var $field    = array();
    var $cmd;
    var $schregno;          //左画面よりgetで取得
    var $issuedate = "";    //Form1.phpよりgetで取得(有効期間開始日付：YYYY-MM-DD)
    var $no;                //Form1.phpよりgetで取得(学籍番号)
    var $infoDiv;
    var $form2; //チェック用
    var $hist_issuedate;
    var $histField = array();
    var $guardian2;

    function knja110_2bModel(){

    }

    function init()
    {
        $this->field = array("ISSUEDATE"        => str_replace("/","-",VARS::post("ISSUEDATE")),
                             "EXPIREDATE"       => str_replace("/","-",VARS::post("EXPIREDATE")),
                             "ZIPCD"            => VARS::post("ZIPCD"),
                             "AREACD"           => VARS::post("AREACD"),
                             "ADDR1"            => VARS::post("ADDR1"),
                             "ADDR2"            => VARS::post("ADDR2"),
                             "ADDR_FLG"         => VARS::post("ADDR_FLG"),
                             "ADDR1_ENG"        => VARS::post("ADDR1_ENG"),
                             "ADDR2_ENG"        => VARS::post("ADDR2_ENG"),
                             "TELNO"            => VARS::post("TELNO"),
                             "TELNO2"           => VARS::post("TELNO2"),
                             "FAXNO"            => VARS::post("FAXNO"),
                             "EMAIL"            => VARS::post("EMAIL"),

                             "GUARD_ISSUEDATE"  => str_replace("/","-",VARS::post("GUARD_ISSUEDATE")),
                             "GUARD_EXPIREDATE" => str_replace("/","-",VARS::post("GUARD_EXPIREDATE")),
                             "RELATIONSHIP"     => VARS::post("RELATIONSHIP"),
                             "GUARD_NAME"       => VARS::post("GUARD_NAME"),
                             "GUARD_KANA"       => VARS::post("GUARD_KANA"),
                             "GUARD_REAL_NAME"  => VARS::post("GUARD_REAL_NAME"),
                             "GUARD_REAL_KANA"  => VARS::post("GUARD_REAL_KANA"),
                             "GUARD_SEX"        => VARS::post("GUARD_SEX"),
                             "GUARD_BIRTHDAY"   => str_replace("/","-",VARS::post("GUARD_BIRTHDAY")),
                             "GUARD_ZIPCD"      => VARS::post("GUARD_ZIPCD"),
                             "GUARD_ADDR1"      => VARS::post("GUARD_ADDR1"),
                             "GUARD_ADDR2"      => VARS::post("GUARD_ADDR2"),
                             "GUARD_ADDR_FLG"   => VARS::post("GUARD_ADDR_FLG"),
                             "GUARD_TELNO"      => VARS::post("GUARD_TELNO"),
                             "GUARD_TELNO2"     => VARS::post("GUARD_TELNO2"),
                             "GUARD_FAXNO"      => VARS::post("GUARD_FAXNO"),
                             "GUARD_E_MAIL"     => VARS::post("GUARD_E_MAIL"),
                             "GUARD_JOBCD"      => VARS::post("GUARD_JOBCD"),
                             "GUARD_WORK_NAME"  => VARS::post("GUARD_WORK_NAME"),
                             "GUARD_WORK_TELNO" => VARS::post("GUARD_WORK_TELNO"),

                             "EMERGENCYCALL"       => VARS::post("EMERGENCYCALL"),
                             "EMERGENCYNAME"       => VARS::post("EMERGENCYNAME"),
                             "EMERGENCYRELA_NAME"  => VARS::post("EMERGENCYRELA_NAME"),
                             "EMERGENCYTELNO"      => VARS::post("EMERGENCYTELNO"),
                             "EMERGENCYCALL2"      => VARS::post("EMERGENCYCALL2"),
                             "EMERGENCYNAME2"      => VARS::post("EMERGENCYNAME2"),
                             "EMERGENCYRELA_NAME2" => VARS::post("EMERGENCYRELA_NAME2"),
                             "EMERGENCYTELNO2"     => VARS::post("EMERGENCYTELNO2"),
                             "EMERGENCYCALL3"      => VARS::post("EMERGENCYCALL3"),
                             "EMERGENCYNAME3"      => VARS::post("EMERGENCYNAME3"),
                             "EMERGENCYRELA_NAME3" => VARS::post("EMERGENCYRELA_NAME3"),
                             "EMERGENCYTELNO3"     => VARS::post("EMERGENCYTELNO3"),

                             "UPDATED"             => VARS::post("UPDATED"),
                             "GUARD_UPDATED"       => VARS::post("GUARD_UPDATED"),

                             "E_APPDATE"            => VARS::post("E_APPDATE"),
                             "RELATIONSHIP_FLG"     => VARS::post("RELATIONSHIP_FLG"),
                             "GUARD_NAME_FLG"       => VARS::post("GUARD_NAME_FLG"),
                             "GUARD_KANA_FLG"       => VARS::post("GUARD_KANA_FLG"),
                             "GUARD_REAL_NAME_FLG"  => VARS::post("GUARD_REAL_NAME_FLG"),
                             "GUARD_REAL_KANA_FLG"  => VARS::post("GUARD_REAL_KANA_FLG"),
                             "GUARD_SEX_FLG"        => VARS::post("GUARD_SEX_FLG"),
                             "GUARD_BIRTHDAY_FLG"   => VARS::post("GUARD_BIRTHDAY_FLG")
                             );

        $this->cmd            = VARS::request("cmd");

        //履歴日付
        if (VARS::get("HIST_ISSUEDATE")) {
            $this->hist_issuedate = VARS::get("HIST_ISSUEDATE");
        }
        //履歴修正
        if (VARS::request("ISSUEDATE") || $this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel" || $this->cmd == "changeCmb") {
            $setGradeHrAtteFlg = VARS::request("GRADE_HR_ATTE_FLG");
            $setCoursemajorFlg = VARS::request("COURSEMAJOR_FLG");
            $setGradeHr = explode(",", VARS::request("GRADE_CLASS"));
            $setCourseMajor = explode(",", VARS::request("COURSEMAJORCD"));
            $this->histField = array("ISSUEDATE"            => VARS::request("ISSUEDATE"),
                                     "EXPIREDATE"           => VARS::request("EXPIREDATE"),
                                     "SCHREGNO"             => VARS::request("SCHREGNO"),
                                     "ISSUEDATE"            => VARS::request("ISSUEDATE"),
                                     "EXPIREDATE"           => VARS::request("EXPIREDATE"),
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
                                     "GUARD_BIRTHDAY_FLG"   => VARS::request("GUARD_BIRTHDAY_FLG")
                            );
        } else {
            unset($this->histField);
        }

        if(VARS::get("SCHREGNO")){
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if(VARS::get("ISSUEDATE")){
            $this->issuedate        = VARS::get("ISSUEDATE");
        }
        if(VARS::get("GUARD_ISSUEDATE")){
            $this->guard_issuedate  = VARS::get("GUARD_ISSUEDATE");
        }
        if(VARS::request("INFO_DIV")){
            $this->infoDiv  = VARS::request("INFO_DIV");
        }
        if ($this->cmd == ""){
            unset($this->issuedate);
            unset($this->guard_issuedate);
            unset($this->infoDiv);
        }
        //3:保護者情報２
        $this->guardian2 = ($this->infoDiv == "3") ? "2" : "";
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        //更新,削除時にリストから選択しているかをチェック
        if($this->cmd == "update") {
            if($this->issuedate =="") {
                $this->setWarning("MSG308");
                return false;
            }
        } elseif($this->cmd == "update2" || $this->cmd == "subUpdate") {
            if($this->guard_issuedate =="") {
                $this->setWarning("MSG308");
                return false;
            }
        }
        $db = Query::dbCheckOut();

        //追加、更新時の必須項目(有効期間)チェック
        if($this->cmd =="add" || $this->cmd =="update" ){
            if($this->field["ISSUEDATE"] =="" || $this->field["EXPIREDATE"] ==""){
                $this->setWarning("MSG301","( 有効期間 )");
                return false;
            }
            //重複エラーチェック
            $query = knja110_2bQuery::get_cnt_Address($this->schregno,$this->field["ISSUEDATE"]);
            $cnt_Address = $db->getOne($query);
            if ($this->cmd =="add" && $cnt_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        } elseif ($this->cmd =="add2" || $this->cmd =="subAdd" || $this->cmd =="subUpdate" || $this->cmd =="update2" ) {
            if($this->field["GUARD_ISSUEDATE"] =="" || $this->field["GUARD_EXPIREDATE"] ==""){
                $this->setWarning("MSG301","( 有効期間 )");
                return false;
            }
            //重複エラーチェック
            $query = knja110_2bQuery::get_cnt_G_Address($this, $this->schregno,$this->field["GUARD_ISSUEDATE"]);
            $cnt_G_Address = $db->getOne($query);
            if (($this->cmd =="add2" || $this->cmd =="subAdd") && $cnt_G_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }
        Query::dbCheckIn($db);

        //更新日付チェック
        if($this->cmd =="update" || $this->cmd =="delete"){
            $db = Query::dbCheckOut();
            $query = knja110_2bQuery::getRow_Address($this->schregno,$this->issuedate);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            if($row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        } elseif($this->cmd =="update2" || $this->cmd =="delete2") {
            $db = Query::dbCheckOut();
            $query = knja110_2bQuery::getGuardianAddressDat($this, $this->schregno,$this->guard_issuedate);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if($row["UPDATED"] != $this->field["GUARD_UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }

        if ($this->infoDiv == "2" || $this->infoDiv == "3") {
            //入力時の文字数,半角カナチェック
            foreach(array("GUARD_NAME"          => array("( 保護者氏名 )", 60),
                          "GUARD_KANA"          => array("( 保護者かな )", 120),
                          "GUARD_REAL_NAME"     => array("( 戸籍氏名 )", 120),
                          "GUARD_REAL_KANA"     => array("( 戸籍氏名かな )", 240),
                          "GUARD_ZIPCD"         => array("( 郵便番号 )", 8),
                          "GUARD_ADDR1"         => array("( 保護者住所 )", 90),
                          "GUARD_ADDR2"         => array("( 保護者方書き )", 90),
                          "GUARD_TELNO"         => array("( 電話番号 )",14),
                          "GUARD_TELNO2"        => array("( 電話番号２ )",14),
                          "GUARD_FAXNO"         => array("( FAX番号 )", 14),
                          "GUARD_E_MAIL"        => array("( E-Mail )", 20),
                          "GUARD_WORK_NAME"     => array("( 勤務先名称 )", 60),
                          "GUARD_WORK_TELNO"    => array("( 勤務先電話番号 )",14)
            ) as $key => $val) {

                if (mbereg("[｡-ﾟ]",$this->field[$key])) {
                    $this->setWarning("MSG901",$val[0]."半角カナは入力できません");
                    return false;
                }
                if(strlen($this->field[$key]) > $val[1]){
                    $this->setWarning("MSG915", $val[0].$val[1]."バイトまで");
                    return false;
                }
            }
        } else {
            //入力時の文字数,半角カナチェック
            foreach(array("ZIPCD"          => array("( 郵便番号 )", 8),
                          "ADDR1"          => array("( 住所 )", 90),
                          "ADDR2"          => array("( 方書き )", 90),
                          "ADDR1_ENG"      => array("( 英字 住所 )", 70),
                          "ADDR2_ENG"      => array("( 英字 方書き )", 70),
                          "TELNO"          => array("( 電話番号 )",14),
                          "TELNO2"         => array("( 電話番号２ )",14),
                          "FAXNO"          => array("( FAX番号 )", 14),
                          "EMAIL"          => array("( E-Mail )", 20),
                          "EMERGENCYCALL"        => array("( 急用連絡先 )",60),
                          "EMERGENCYNAME"        => array("( 急用連絡氏名 )",60),
                          "EMERGENCYRELA_NAME"   => array("( 急用連絡続柄名 )",30),
                          "EMERGENCYTELNO"       => array("( 急用電話番号 )",14),
                          "EMERGENCYCALL2"       => array("( 急用連絡先２ )",60),
                          "EMERGENCYNAME2"       => array("( 急用連絡氏名２ )",60),
                          "EMERGENCYRELA_NAME2"  => array("( 急用連絡続柄名２ )",30),
                          "EMERGENCYTELNO2"      => array("( 急用電話番号２ )",14),
                          "EMERGENCYCALL3"       => array("( 急用連絡先３ )",60),
                          "EMERGENCYNAME3"       => array("( 急用連絡氏名３ )",60),
                          "EMERGENCYRELA_NAME3"  => array("( 急用連絡続柄名３ )",30),
                          "EMERGENCYTELNO3"      => array("( 急用電話番号３ )",14)
            ) as $key => $val) {

                if (mbereg("[｡-ﾟ]",$this->field[$key])) {
                    $this->setWarning("MSG901",$val[0]."半角カナは入力できません");
                    return false;
                }
                if(strlen($this->field[$key]) > $val[1]){
                    $this->setWarning("MSG915", $val[0].$val[1]."バイトまで");
                    return false;
                }
            }
        }

        //有効期間開始日付と有効期間終了日付の日付の前後関係をチェック
        if ($this->cmd =="add" || $this->cmd =="update") {
            $ISSUEDATE  = date("Y-m-d", strtotime(str_replace("-","/",$this->field["ISSUEDATE"])));
            $EXPIREDATE = date("Y-m-d", strtotime(str_replace("-","/",$this->field["EXPIREDATE"])));
            if ($EXPIREDATE <= $ISSUEDATE) {
                $this->setWarning("MSG916","( 有効期間 )");
                return false;
            }
        } elseif ($this->cmd =="add2" || $this->cmd =="update2") {
            $GUARD_ISSUEDATE  = date("Y-m-d",strtotime(str_replace("-","/",$this->field["GUARD_ISSUEDATE"])));
            $GUARD_EXPIREDATE = date("Y-m-d",strtotime(str_replace("-","/",$this->field["GUARD_EXPIREDATE"])));
            if ($GUARD_EXPIREDATE <= $GUARD_ISSUEDATE) {
                $this->setWarning("MSG916","( 有効期間 )");
                return false;
            }
        }

        $db = Query::dbCheckOut();
        $query = knja110_2bQuery::get_max_issuedate($this);
        $max_issuedate = $db->getOne($query);
        $to_day = str_replace('-', '', $max_issuedate);
        $max_issuedate_1_day_after = date("Y-m-d",strtotime("1 day" ,strtotime($to_day)));
        $max_issuedate_2_day_after = date("Y-m-d",strtotime("2 day" ,strtotime($to_day)));

        //追加,更新時の有効期間の重複チェック
        if($this->field["ISSUEDATE"] !="") {
            $result = $db->query(knja110_2bQuery::getAddress_all($this));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if(($row["ISSUEDATE"] == str_replace("/","-",$this->issuedate)) && ($this->cmd =="update")) {
                    continue;
                }

                $issuedate  = $row["ISSUEDATE"];
                $expiredate = $row["EXPIREDATE"];

                if ($this->cmd == 'add') {
                    if (($issuedate <= $ISSUEDATE  && $ISSUEDATE  <= $expiredate && $ISSUEDATE  <  $max_issuedate_1_day_after) ||
                        ($issuedate <= $EXPIREDATE && $EXPIREDATE <= $expiredate && $EXPIREDATE <= $max_issuedate_1_day_after) ||
                        ($ISSUEDATE <= $issuedate  && $expiredate <= $EXPIREDATE)) {
                            $this->setWarning("MSG302","( 有効期間 )");
                            return false;
                    }
                }
                if ($this->cmd == 'update') {
                    if (($issuedate <= $ISSUEDATE  && $ISSUEDATE  <= $expiredate) ||
                        ($issuedate <= $EXPIREDATE && $EXPIREDATE <= $expiredate) ||
                        ($ISSUEDATE <= $issuedate  && $expiredate <= $EXPIREDATE)) {
                            $this->setWarning("MSG302","( 有効期間 )");
                            return false;
                    }
                }
            }
            $result->free();
        } elseif($this->field["GUARD_ISSUEDATE"] !="") {
            $result = $db->query(knja110_2bQuery::getAddress_all($this));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if(($row["GUARD_ISSUEDATE"] == str_replace("/","-",$this->guard_issuedate)) && ($this->cmd =="update2")) {
                    continue;
                }
                $guard_issuedate  = $row["GUARD_ISSUEDATE"];
                $guard_expiredate = $row["GUARD_EXPIREDATE"];

                if ($this->cmd == 'add2') {
                    if (($guard_issuedate <= $GUARD_ISSUEDATE  && $GUARD_ISSUEDATE  <= $guard_expiredate && $GUARD_ISSUEDATE  <  $max_issuedate_1_day_after) ||
                        ($guard_issuedate <= $GUARD_EXPIREDATE && $GUARD_EXPIREDATE <= $guard_expiredate && $GUARD_EXPIREDATE <= $max_issuedate_1_day_after) ||
                        ($GUARD_ISSUEDATE <= $guard_issuedate  && $guard_expiredate <= $EXPIREDATE)) {
                            $this->setWarning("MSG302","( 有効期間 )");
                            return false;
                    }
                }
                if ($this->cmd == 'update2') {
                    if (($guard_issuedate <= $GUARD_ISSUEDATE  && $GUARD_ISSUEDATE  <= $guard_expiredate) ||
                        ($guard_issuedate <= $GUARD_EXPIREDATE && $GUARD_EXPIREDATE <= $guard_expiredate) ||
                        ($GUARD_ISSUEDATE <= $guard_issuedate  && $guard_expiredate <= $EXPIREDATE)) {
                            $this->setWarning("MSG302","( 有効期間 )");
                            return false;
                    }
                }
            }
            $result->free();

        }
        Query::dbCheckIn($db);

        //文字型チェック
        if ($this->cmd =="add" || $this->cmd =="update") {
            foreach(array("TELNO"           => array("[^-|0-9]",        "( 電話番号 )"),
                         "TELNO2"           => array("[^-|0-9]",        "( 電話番号２ )"),
                         "FAXNO"            => array("[^-|0-9]",        "( FAX番号 )"),
                         "EMAIL"            => array("[^-._@a-z0-9]",   "( E-Mail )"),
                         "EMERGENCYTELNO"   => array("[^-|0-9]",        "( 急用電話番号 )"),
                         "EMERGENCYTELNO2"   => array("[^-|0-9]",       "( 急用電話番号２ )"),
                    ) as $key => $val
            ) {
                if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                    $this->setWarning("MSG901", $val[1]);
                    return false;
                }
            }
        }
        else if ($this->cmd =="add2" || $this->cmd =="update2") {
            foreach(array("GUARD_TELNO"      => array("[^-|0-9]",        "( 電話番号 )"),
                         "GUARD_TELNO2"     => array("[^-|0-9]",        "( 電話番号２ )"),
                         "GUARD_FAXNO"      => array("[^-|0-9]",        "( FAX番号 )"),
                         "GUARD_E_MAIL"     => array("[^-._@a-z0-9]",   "( E-Mail )"),
                         "GUARD_WORK_TELNO" => array("[^-|0-9]",        "( 勤務先電話番号 )"),
                    ) as $key => $val
            ) {
                if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                    $this->setWarning("MSG901", $val[1]);
                    return false;
                }
            }
        }
        /*
            C -- 全角ひらがなを全角カタカナ
            K -- 半角カナを全角カタカナ
            k -- 全角カタカナを半角カタカナ
            A -- 英数字を半角から全角
        */
        //(英字)住所1チェック
        if($this->field["ADDR1_ENG"] !="")
        {
            if(trim($this->field["ADDR1_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"CK")
              || trim($this->field["ADDR1_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"k")
              || trim($this->field["ADDR1_ENG"]) == i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"A")    || trim($this->field["ADDR1_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"a"))
            {
                $this->setWarning("MSG901","\\n半角英字を入力して下さい！ ( 英字 住所 )");
                return false;
            }
        }

        //(英字)住所2チェック
        if($this->field["ADDR2_ENG"] !="")
        {
            if(trim($this->field["ADDR2_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"CK")
              || trim($this->field["ADDR2_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"k")
              || trim($this->field["ADDR2_ENG"]) == i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"A")    || trim($this->field["ADDR2_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"a"))
            {
                $this->setWarning("MSG901","\\n半角英字を入力して下さい！ ( 英字 方書き )");
                return false;
            }
        }

        return true;
    }

    //インサート時の内部メソッド
    function &getInsertModel() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        //入力チェック
        if($this->validateInput()) {
            if ($this->cmd == 'add') {
                knja110_2bQuery::getInsertQuery($this);
                $this->setMessage("MSG201");
                $this->issuedate = $this->field["ISSUEDATE"];
            } else {
                knja110_2bQuery::insert_Guardian_dat($this);
                $this->setMessage("MSG201");
                $this->guard_issuedate = $this->field["GUARD_ISSUEDATE"];
            }
            $db = Query::dbCheckOut();

            $db->autoCommit(false);
            $this->guardUpdFromHist($db, $this->schregno);
            $db->commit();

            Query::dbCheckIn($db);
            return;
        }
    }

    //アップデート時の内部メソッド
    function &getUpdateModel() {
        //入力チェック
        if($this->validateInput()) {
            if ($this->cmd == 'update') {
                knja110_2bQuery::getUpdateModel($this);
                $this->setMessage("MSG201");
                $this->issuedate = $this->field["ISSUEDATE"];
            } else {
                if (str_replace("/", "-", $this->guard_issuedate) != str_replace("/", "-", $this->field["GUARD_ISSUEDATE"])) {
                    $db = Query::dbCheckOut();
                    $query = knja110_2bQuery::get_cnt_G_Address($this, $this->schregno, $this->field["GUARD_ISSUEDATE"]);
                    $gAddCnt = $db->getOne($query);
                    Query::dbCheckIn($db);
                    if ($gAddCnt > 0) {
                        $this->setWarning("MSG302");
                        return false;
                    }
                }

                knja110_2bQuery::update_Guardian_dat($this);
                $this->setMessage("MSG201");
                $this->guard_issuedate = $this->field["GUARD_ISSUEDATE"];
            }
            $db = Query::dbCheckOut();

            $db->autoCommit(false);
            $this->guardUpdFromHist($db, $this->schregno);
            $db->commit();
            Query::dbCheckIn($db);
            return;
        }
    }
    //削除用のモデルを返します
    function &getDeleteModel() {
        if ($this->cmd == 'delete') {
            if($this->field["ISSUEDATE"] =="" && $this->issuedate ==""){
                $this->setWarning("MSG308");
                return false;
            }
            knja110_2bQuery::getDeleteQuery($this);
            unset($this->field["ISSUEDATE"]);
            unset($this->issuedate);
            $this->setMessage("MSG201");
        } else {
            if($this->field["GUARD_ISSUEDATE"] == "" && $this->guard_issuedate == ""){
                $this->setWarning("MSG308");
                return false;
            }
            knja110_2bQuery::getDelete_GUARDIAN_ADDRESS_DAT($this);
            unset($this->field["ISSUEDATE"]);
            unset($this->issuedate);
            $this->setMessage("MSG201");
        }
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $this->guardUpdFromHist($db, $this->schregno);
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }

     //変数をクリアします
    function clean(){}

    function &getUpdateHistModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        if (!$this->validateInputHist($db)) {
            $this->cmd = "histEdit";
            return;
        }

        if ($this->cmd == "histAdd") {
            $this->addHist($db);
        } else if ($this->cmd == "histUpd") {
            $this->delHist($db);
            $this->addHist($db);
        } else {
            $this->delHist($db);
        }
        $this->guardUpdFromHist($db, $this->schregno);

        $this->setMessage("MSG201");

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //入力項目チェック
    function validateInputHist($db) {

        if ($this->schregno == "" &&
           ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel")) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel") && $this->histField["SCHREGNO"] != $this->schregno) {
            $this->setMessage("MSG300","\\n学籍番号は変更できません！");
            return false;
        }

        $checkArray["ISSUEDATE"]       = array("TITLE" => "開始日",      "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["EXPIREDATE"]      = array("TITLE" => "終了日",      "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["RELATIONSHIP"]    = array("TITLE" => "続柄",        "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["GUARD_NAME"]      = array("TITLE" => "氏名",        "HISSU" => "ON", "LENGTH" => "ON", "LEN" => "60");
        $checkArray["GUARD_KANA"]      = array("TITLE" => "氏名かな",    "HISSU" => "ON", "LENGTH" => "ON", "LEN" => "120");
        $checkArray["GUARD_REAL_NAME"] = array("TITLE" => "戸籍氏名",    "HISSU" => "",   "LENGTH" => "ON", "LEN" => "120");
        $checkArray["GUARD_REAL_KANA"] = array("TITLE" => "戸籍氏名かな","HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");
        $checkArray["GUARD_SEX"]       = array("TITLE" => "性別",        "HISSU" => "",   "LENGTH" => "",   "LEN" => "");
        $checkArray["GUARD_BIRTHDAY"]  = array("TITLE" => "生年月日",    "HISSU" => "",   "LENGTH" => "",   "LEN" => "");

        //必須項目チェック
        $flag = false;
        $msg = "";
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd") {

            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["HISSU"]) {
                    if ($this->histField[$fieldName] == "" ) {
                        $flag = true;
                        $msg .= "\\n".$fieldVal["TITLE"];
                    }
                }
            }
        } else {
            //開始日付
            if ($this->histField["ISSUEDATE"] == "" ) {
                $flag = true;
                $msg .= "\\n開始日付";
            }
            //学籍番号
            if($this->histField["SCHREGNO"] == "" ){
                $flag = true;
                $msg .= "\\n学籍番号";
            }
        }
        if ($flag == true) {
            $this->setWarning("MSG301", $msg);
            return false;
        }
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd") {
            if (str_replace("/" , "", $this->histField["ISSUEDATE"]) > str_replace("/", "", $this->histField["EXPIREDATE"])) {
                $this->setWarning("MSG901", "開始終了日付が、逆転しています。");
                return false;
            }

            $query = knja110_2bQuery::getGuardianHistDateCheck($this);
            $dateExists = $db->getOne($query);
            if ($dateExists > "0") {
                $this->setWarning("MSG901", "日付範囲が不正です。");
                return false;
            }
        }

        $query = knja110_2bQuery::getGuardianHistData($this, $this->schregno, $this->histField["ISSUEDATE"]);
        $checkExists = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($this->cmd == "histAdd" && is_array($checkExists)) {
            $this->setWarning("MSG302");
            return false;
        } else if (($this->cmd == "histUpd" || $this->cmd == "histDel") && !is_array($checkExists)) {
            $this->setWarning("MSG308");
            return false;
        }

        foreach (array("GUARD_NAME" => array("( 氏名 )","[｡-ﾟ]"),
                       "GUARD_KANA" => array("( 氏名かな )","[｡-ﾟ]")) as $key => $val) {
            if (mbereg($val[1], $this->histField[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        foreach (array("GUARD_REAL_NAME" => array("( 戸籍氏名 )","[｡-ﾟ]"),
                       "GUARD_REAL_KANA" => array("(戸籍氏名かな )","[｡-ﾟ]")) as $key => $val) {
            if (mbereg($val[1], $this->histField[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        //文字数チェック
        if($this->cmd == "histAdd" || $this->cmd == "histUpd") {
            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["LENGTH"]) {
                    if (strlen($this->histField[$fieldName]) > $fieldVal["LEN"]) {
                        $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) ".$fieldVal["LEN"]."バイトまで");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    function addHist($db) {
        $query = knja110_2bQuery::getInsHistQuery($this);
        $this->hist_issuedate = $this->histField["ISSUEDATE"];
        $this->cmd = "histEdit";
        $db->query($query);
    }

    function delHist($db) {
        $query = knja110_2bQuery::getDelHistQuery($this);
        $this->cmd = "changeCmb";
        $db->query($query);
    }

    function guardUpdFromHist($db, $schregno) {
        $query = knja110_2bQuery::getGuardHistUpd($this, $schregno);
        $histData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $updFlg = false;
        $guard = array();
        if (is_array($histData)) {
            if ($histData["RELATIONSHIP_FLG"] == "1") {
                $updFlg = true;
                $guard["RELATIONSHIP"][TEXT] = $histData["RELATIONSHIP"];
            }
            if ($histData["GUARD_NAME_FLG"] == "1") {
                $updFlg = true;
                $guard["GUARD_NAME"][TEXT] = $histData["GUARD_NAME"];
            }
            if ($histData["GUARD_KANA_FLG"] == "1") {
                $updFlg = true;
                $guard["GUARD_KANA"][TEXT] = $histData["GUARD_KANA"];
            }
            if ($histData["GUARD_REAL_NAME_FLG"] == "1") {
                $updFlg = true;
                $guard["GUARD_REAL_NAME"][TEXT] = $histData["GUARD_REAL_NAME"];
            }
            if ($histData["GUARD_REAL_KANA_FLG"] == "1") {
                $updFlg = true;
                $guard["GUARD_REAL_KANA"][TEXT] = $histData["GUARD_REAL_KANA"];
            }
            if ($histData["GUARD_SEX_FLG"] == "1") {
                $updFlg = true;
                $guard["GUARD_SEX"][TEXT] = $histData["GUARD_SEX"];
            }
            if ($histData["GUARD_BIRTHDAY_FLG"] == "1") {
                $updFlg = true;
                $guard["GUARD_BIRTHDAY"][TEXT] = $histData["GUARD_BIRTHDAY"];
            }

            if ($updFlg) {
                $query = knja110_2bQuery::getGuardUpdFromHist($this, $guard, $schregno);
                $db->query($query);
            }

        }

        return true;
    }
}
?>
