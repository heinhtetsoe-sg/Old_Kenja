<?php

require_once('for_php7.php');


class knja110_2aModel extends Model {
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

    function knja110_2aModel(){

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
                             "GUARD_FAXNO"      => VARS::post("GUARD_FAXNO"),
                             "GUARD_E_MAIL"     => VARS::post("GUARD_E_MAIL"),
                             "GUARD_JOBCD"      => VARS::post("GUARD_JOBCD"),
                             "GUARD_WORK_NAME"  => VARS::post("GUARD_WORK_NAME"),
                             "GUARD_WORK_TELNO" => VARS::post("GUARD_WORK_TELNO"),

                             "GUARANTOR_ISSUEDATE"      => str_replace("/","-",VARS::post("GUARANTOR_ISSUEDATE")),
                             "GUARANTOR_EXPIREDATE"     => str_replace("/","-",VARS::post("GUARANTOR_EXPIREDATE")),
                             "GUARANTOR_RELATIONSHIP"   => VARS::post("GUARANTOR_RELATIONSHIP"),
                             "GUARANTOR_NAME"           => VARS::post("GUARANTOR_NAME"),
                             "GUARANTOR_KANA"           => VARS::post("GUARANTOR_KANA"),
                             "GUARANTOR_REAL_NAME"      => VARS::post("GUARANTOR_REAL_NAME"),
                             "GUARANTOR_REAL_KANA"      => VARS::post("GUARANTOR_REAL_KANA"),
                             "GUARANTOR_SEX"            => VARS::post("GUARANTOR_SEX"),
                             "GUARANTOR_ZIPCD"          => VARS::post("GUARANTOR_ZIPCD"),
                             "GUARANTOR_ADDR1"          => VARS::post("GUARANTOR_ADDR1"),
                             "GUARANTOR_ADDR2"          => VARS::post("GUARANTOR_ADDR2"),
                             "GUARANTOR_ADDR_FLG"       => VARS::post("GUARANTOR_ADDR_FLG"),
                             "GUARANTOR_TELNO"          => VARS::post("GUARANTOR_TELNO"),
                             "GUARANTOR_JOBCD"          => VARS::post("GUARANTOR_JOBCD"),
                             "PUBLIC_OFFICE"            => VARS::post("PUBLIC_OFFICE"),

                             "SEND_DIV"                 => VARS::post("SEND_DIV"),
                             "SEND_RELATIONSHIP"        => VARS::post("SEND_RELATIONSHIP"),
                             "SEND_NAME"                => VARS::post("SEND_NAME"),
                             "SEND_KANA"                => VARS::post("SEND_KANA"),
                             "SEND_SEX"                 => VARS::post("SEND_SEX"),
                             "SEND_ZIPCD"               => VARS::post("SEND_ZIPCD"),
                             "SEND_AREACD"              => VARS::post("SEND_AREACD"),
                             "SEND_ADDR1"               => VARS::post("SEND_ADDR1"),
                             "SEND_ADDR2"               => VARS::post("SEND_ADDR2"),
                             "SEND_ADDR_FLG"            => VARS::post("SEND_ADDR_FLG"),
                             "SEND_TELNO"               => VARS::post("SEND_TELNO"),
                             "SEND_TELNO2"              => VARS::post("SEND_TELNO2"),
                             "SEND_JOBCD"               => VARS::post("SEND_JOBCD"),
                             "SEND_PUBLIC_OFFICE"       => VARS::post("SEND_PUBLIC_OFFICE"),

                             "RELA_NO"                  => VARS::post("RELA_NO"),
                             "RELA_NAME"                => VARS::post("RELA_NAME"),
                             "RELA_KANA"                => VARS::post("RELA_KANA"),
                             "RELA_SEX"                 => VARS::post("RELA_SEX"),
                             "RELA_BIRTHDAY"            => VARS::post("RELA_BIRTHDAY"),
                             "RELA_RELATIONSHIP"        => VARS::post("RELA_RELATIONSHIP"),
                             "RELA_OCCUPATION"          => VARS::post("RELA_OCCUPATION"),
                             "RELA_SCHREGNO"            => VARS::post("RELA_SCHREGNO"),
                             "RELA_REGIDENTIALCD"       => VARS::post("RELA_REGIDENTIALCD"),
                             "RELA_REMARK"              => VARS::post("RELA_REMARK"),

                             "EMERGENCYCALL"       => VARS::post("EMERGENCYCALL"),
                             "EMERGENCYNAME"       => VARS::post("EMERGENCYNAME"),
                             "EMERGENCYRELA_NAME"  => VARS::post("EMERGENCYRELA_NAME"),
                             "EMERGENCYTELNO"      => VARS::post("EMERGENCYTELNO"),
                             "EMERGENCYCALL2"      => VARS::post("EMERGENCYCALL2"),
                             "EMERGENCYNAME2"      => VARS::post("EMERGENCYNAME2"),
                             "EMERGENCYRELA_NAME2" => VARS::post("EMERGENCYRELA_NAME2"),
                             "EMERGENCYTELNO2"     => VARS::post("EMERGENCYTELNO2"),

                             "UPDATED"              => VARS::post("UPDATED"),
                             "GUARD_UPDATED"        => VARS::post("GUARD_UPDATED"),
                             "GUARANTOR_UPDATED"    => VARS::post("GUARANTOR_UPDATED"),
                             "SEND_UPDATED"         => VARS::post("SEND_UPDATED"),
                             "RELA_UPDATED"         => VARS::post("RELA_UPDATED"),

                             "E_APPDATE"            => VARS::post("E_APPDATE"),
                             "RELATIONSHIP_FLG"     => VARS::post("RELATIONSHIP_FLG"),
                             "GUARD_NAME_FLG"       => VARS::post("GUARD_NAME_FLG"),
                             "GUARD_KANA_FLG"       => VARS::post("GUARD_KANA_FLG"),
                             "GUARD_REAL_NAME_FLG"  => VARS::post("GUARD_REAL_NAME_FLG"),
                             "GUARD_REAL_KANA_FLG"  => VARS::post("GUARD_REAL_KANA_FLG"),
                             "GUARD_SEX_FLG"        => VARS::post("GUARD_SEX_FLG"),
                             "GUARD_BIRTHDAY_FLG"   => VARS::post("GUARD_BIRTHDAY_FLG"),

                             "GUARANTOR_RELATIONSHIP_FLG"   => VARS::post("GUARANTOR_RELATIONSHIP_FLG"),
                             "GUARANTOR_NAME_FLG"           => VARS::post("GUARANTOR_NAME_FLG"),
                             "GUARANTOR_KANA_FLG"           => VARS::post("GUARANTOR_KANA_FLG"),
                             "GUARANTOR_REAL_NAME_FLG"      => VARS::post("GUARANTOR_REAL_NAME_FLG"),
                             "GUARANTOR_REAL_KANA_FLG"      => VARS::post("GUARANTOR_REAL_KANA_FLG"),
                             "GUARANTOR_SEX_FLG"            => VARS::post("GUARANTOR_SEX_FLG")
                             );

        $this->cmd            = VARS::request("cmd");

        //履歴日付
        if (VARS::get("HIST_ISSUEDATE")) {
            $this->hist_issuedate = VARS::get("HIST_ISSUEDATE");
        }
        //履歴修正
        if (VARS::request("ISSUEDATE") || in_array($this->cmd, array("histAdd","histUpd","histDel","changeCmb","histAdd2","histUpd2","histDel2","changeCmb2"))) {
            $setGradeHrAtteFlg = VARS::request("GRADE_HR_ATTE_FLG");
            $setCoursemajorFlg = VARS::request("COURSEMAJOR_FLG");
            $setGradeHr = explode(",", VARS::request("GRADE_CLASS"));
            $setCourseMajor = explode(",", VARS::request("COURSEMAJORCD"));
            $this->histField = array("SCHREGNO"             => VARS::request("SCHREGNO"),
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

        if(VARS::get("SCHREGNO")){
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if(VARS::get("ISSUEDATE")){
            $this->issuedate        = VARS::get("ISSUEDATE");
        }
        if(VARS::get("GUARD_ISSUEDATE")){
            $this->guard_issuedate  = VARS::get("GUARD_ISSUEDATE");
        }
        if(VARS::get("GUARANTOR_ISSUEDATE")) {
            $this->guarantor_issuedate  = VARS::get("GUARANTOR_ISSUEDATE");
        }
        if(VARS::get("SEND_DIV")) {
            $this->send_div  = VARS::get("SEND_DIV");
        }
        if(VARS::get("RELA_NO")) {
            $this->rela_no  = VARS::get("RELA_NO");
        }
        if(VARS::request("INFO_DIV")){
            $this->infoDiv  = VARS::request("INFO_DIV");
        }
        if ($this->cmd == ""){
            unset($this->issuedate);
            unset($this->guard_issuedate);
            unset($this->guarantor_issuedate);
            unset($this->send_div);
            unset($this->rela_no);
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
        } else if ($this->cmd == "update4" || ($this->infoDiv == "4" && $this->cmd == "subUpdate")) {
            if ($this->guarantor_issuedate == "") {
                $this->setWarning("MSG308");
                return false;
            }
        } else if ($this->cmd == "update5") {
            if ($this->send_div == "") {
                $this->setWarning("MSG308");
                return false;
            }
        } else if ($this->cmd == "update6") {
            if ($this->rela_no == "") {
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
            $query = knja110_2aQuery::get_cnt_Address($this->schregno,$this->field["ISSUEDATE"]);
            $cnt_Address = $db->getOne($query);
            if ($this->cmd =="add" && $cnt_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        } else if ($this->cmd == "add4" || $this->cmd == "update4") {
            if ($this->field["GUARANTOR_ISSUEDATE"] == "" || $this->field["GUARANTOR_EXPIREDATE"] == "") {
                $this->setWarning("MSG301","( 有効期間 )");
                return false;
            }
            //重複エラーチェック
            $query = knja110_2aQuery::getGuarantorAddressDat($this->schregno, $this->field["GUARANTOR_ISSUEDATE"], "cnt");
            $cnt_Address = $db->getOne($query);
            if ($this->cmd == "add4" && $cnt_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        } else if ($this->cmd == "add5" || $this->cmd == "update5") {
            if ($this->field["SEND_DIV"] == "") {
                $this->setWarning("MSG301","( 区分 )");
                return false;
            }
            //重複エラーチェック
            $query = knja110_2aQuery::getSchregSendAddressDat($this->schregno, $this->field["SEND_DIV"], "cnt");
            $cnt_Address = $db->getOne($query);
            if ($this->cmd == "add5" && $cnt_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        } else if ($this->cmd == "update6") {
            //存在エラーチェック
            $query = knja110_2aQuery::getSchregRelaDat($this->schregno, $this->rela_no, "cnt");
            $cnt = $db->getOne($query);
            if ($this->cmd == "update6" && $cnt == 0) {
                $this->setWarning("MSG303");
                return false;
            }
        } else if ($this->cmd =="add2" || $this->cmd =="update2") {
            if($this->field["GUARD_ISSUEDATE"] =="" || $this->field["GUARD_EXPIREDATE"] ==""){
                $this->setWarning("MSG301","( 有効期間 )");
                return false;
            }
            //重複エラーチェック
            $query = knja110_2aQuery::get_cnt_G_Address($this, $this->schregno,$this->field["GUARD_ISSUEDATE"]);
            $cnt_Address = $db->getOne($query);
            if (($this->cmd =="add2" || $this->cmd =="subAdd") && $cnt_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        } else if ($this->cmd =="subAdd" || $this->cmd =="subUpdate") {
            if ($this->infoDiv == "4") {
                if ($this->field["GUARANTOR_ISSUEDATE"] == "" || $this->field["GUARANTOR_EXPIREDATE"] == "") {
                    $this->setWarning("MSG301","( 有効期間 )");
                    return false;
                }
                $query = knja110_2aQuery::getGuarantorAddressDat($this->schregno, $this->field["GUARANTOR_ISSUEDATE"], "cnt");
            } else {
                if($this->field["GUARD_ISSUEDATE"] =="" || $this->field["GUARD_EXPIREDATE"] ==""){
                    $this->setWarning("MSG301","( 有効期間 )");
                    return false;
                }
                $query = knja110_2aQuery::get_cnt_G_Address($this, $this->schregno,$this->field["GUARD_ISSUEDATE"]);
            }
            //重複エラーチェック
            $cnt_Address = $db->getOne($query);
            if ($this->cmd =="subAdd" && $cnt_Address > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }
        Query::dbCheckIn($db);

        //更新日付チェック
        if($this->cmd =="update" || $this->cmd =="delete"){
            $db = Query::dbCheckOut();
            $query = knja110_2aQuery::getRow_Address($this->schregno,$this->issuedate);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            if($row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        } else if ($this->cmd == "update4" || $this->cmd == "delete4") {
            $db = Query::dbCheckOut();
            $query = knja110_2aQuery::getGuarantorAddressDat($this->schregno, $this->guarantor_issuedate, "list");
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if($row["UPDATED"] != $this->field["GUARANTOR_UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        } else if ($this->cmd == "update5" || $this->cmd == "delete5") {
            $db = Query::dbCheckOut();
            $query = knja110_2aQuery::getSchregSendAddressDat($this->schregno, $this->send_div, "list");
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if($row["UPDATED"] != $this->field["SEND_UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        } else if ($this->cmd == "update6" || $this->cmd == "delete6") {
            $db = Query::dbCheckOut();
            $query = knja110_2aQuery::getSchregRelaDat($this->schregno, $this->rela_no, "list");
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if($row["UPDATED"] != $this->field["RELA_UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        } elseif($this->cmd =="update2" || $this->cmd =="delete2") {
            $db = Query::dbCheckOut();
            $query = knja110_2aQuery::getGuardianAddressDat($this, $this->schregno,$this->guard_issuedate);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if($row["UPDATED"] != $this->field["GUARD_UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }

        if ($this->infoDiv == "2" || $this->infoDiv == "3") {
            $checkdata = array( "GUARD_NAME"            => array("( 氏名 )",            60),
                                "GUARD_KANA"            => array("( 氏名かな )",        120),
                                "GUARD_REAL_NAME"       => array("( 戸籍氏名 )",        120),
                                "GUARD_REAL_KANA"       => array("( 戸籍氏名かな )",    240),
                                "GUARD_ZIPCD"           => array("( 郵便番号 )",        8),
                                "GUARD_ADDR1"           => array("( 住所 )",            90),
                                "GUARD_ADDR2"           => array("( 方書き )",          90),
                                "GUARD_TELNO"           => array("( 電話番号 )",        14),
                                "GUARD_FAXNO"           => array("( FAX番号 )",         14),
                                "GUARD_E_MAIL"          => array("( E-Mail )",          20),
                                "GUARD_WORK_NAME"       => array("( 勤務先名称 )",      60),
                                "GUARD_TELNO"           => array("( 勤務先電話番号 )",  14)
                                );
        } else if ($this->infoDiv == "4") {
            $checkdata = array( "GUARANTOR_NAME"        => array("( 氏名 )",            60),
                                "GUARANTOR_KANA"        => array("( 氏名かな )",        120),
                                "GUARANTOR_REAL_NAME"   => array("( 戸籍氏名 )",        120),
                                "GUARANTOR_REAL_KANA"   => array("( 戸籍氏名かな )",    240),
                                "GUARANTOR_ZIPCD"       => array("( 郵便番号 )",        8),
                                "GUARANTOR_ADDR1"       => array("( 住所 )",            90),
                                "GUARANTOR_ADDR2"       => array("( 方書き )",          90),
                                "GUARANTOR_TELNO"       => array("( 電話番号 )",        14),
                                "GUARANTOR_WORK_NAME"   => array("( 勤務先名称 )",      60),
                                "PUBLIC_OFFICE"         => array("( 兼ねている公職 )",  30)
                                );
        } else if ($this->infoDiv == "5") {
            $checkdata = array( "SEND_NAME"             => array("( 氏名 )",            60),
                                "SEND_KANA"             => array("( 氏名かな )",        120),
                                "SEND_ZIPCD"            => array("( 郵便番号 )",        8),
                                "SEND_ADDR1"            => array("( 住所 )",            90),
                                "SEND_ADDR2"            => array("( 方書き )",          90),
                                "SEND_TELNO"            => array("( 電話番号１ )",      14),
                                "SEND_TELNO2"           => array("( 電話番号２ )",      14),
                                "SEND_PUBLIC_OFFICE"    => array("( 兼ねている公職 )",  30)
                                );
        } else if ($this->infoDiv == "6") {
            $checkdata = array( "RELA_NAME"             => array("( 氏名 )",            60),
                                "RELA_KANA"             => array("( 氏名かな )",        120),
                                "RELA_OCCUPATION"       => array("( 職業又は学校 )",    60),
                                "RELA_SCHREGNO"         => array("( 兄弟姉妹学籍番号 )",8),
                                "RELA_REMARK"           => array("( 備考 )",            45)
                                );
        } else {
            $checkdata = array( "ZIPCD"                 => array("( 郵便番号 )",        8),
                                "ADDR1"                 => array("( 住所 )",            90),
                                "ADDR2"                 => array("( 方書き )",          90),
                                "ADDR1_ENG"             => array("( 英字 住所 )",       70),
                                "ADDR2_ENG"             => array("( 英字 方書き )",     70),
                                "TELNO"                 => array("( 電話番号 )",        14),
                                "FAXNO"                 => array("( FAX番号 )",         14),
                                "EMAIL"                 => array("( E-Mail )",          20),
                                "EMERGENCYCALL"         => array("( 急用連絡先 )",      60),
                                "EMERGENCYNAME"         => array("( 急用連絡氏名 )",    60),
                                "EMERGENCYRELA_NAME"    => array("( 急用連絡続柄名 )",  30),
                                "EMERGENCYTELNO"        => array("( 急用電話番号 )",    14),
                                "EMERGENCYCALL2"        => array("( 急用連絡先２ )",    60),
                                "EMERGENCYNAME2"        => array("( 急用連絡氏名２ )",  60),
                                "EMERGENCYRELA_NAME2"   => array("( 急用連絡続柄名２ )",30),
                                "EMERGENCYTELNO2"       => array("( 急用電話番号２ )",  14)
                                );
        }
        //入力時の文字数,半角カナチェック
        foreach($checkdata as $key => $val) {
            if (mbereg("[｡-ﾟ]", $this->field[$key])) {
                $this->setWarning("MSG901",$val[0]."半角カナは入力できません");
                return false;
            }
            if (strlen($this->field[$key]) > $val[1]) {
                $this->setWarning("MSG915", $val[0].$val[1]."バイトまで");
                return false;
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
        } else if ($this->cmd =="add4" || $this->cmd =="update4") {
            $GUARANTOR_ISSUEDATE  = date("Y-m-d",strtotime(str_replace("-","/",$this->field["GUARANTOR_ISSUEDATE"])));
            $GUARANTOR_EXPIREDATE = date("Y-m-d",strtotime(str_replace("-","/",$this->field["GUARANTOR_EXPIREDATE"])));
            if ($GUARANTOR_EXPIREDATE <= $GUARANTOR_ISSUEDATE) {
                $this->setWarning("MSG916","( 有効期間 )");
                return false;
            }
        }

        $db = Query::dbCheckOut();
        $query = knja110_2aQuery::get_max_issuedate($this);
        $max_issuedate = $db->getOne($query);
        $to_day = str_replace('-', '', $max_issuedate);
        $max_issuedate_1_day_after = date("Y-m-d",strtotime("1 day" ,strtotime($to_day)));
        $max_issuedate_2_day_after = date("Y-m-d",strtotime("2 day" ,strtotime($to_day)));

        //追加,更新時の有効期間の重複チェック
        if($this->field["ISSUEDATE"] !="") {
            $result = $db->query(knja110_2aQuery::getAddress_all($this));
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
            $result = $db->query(knja110_2aQuery::getAddress_all($this));
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

        } else if ($this->field["GUARANTOR_ISSUEDATE"] != "") {
            $result = $db->query(knja110_2aQuery::getAddress_all($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (($row["GUARANTOR_ISSUEDATE"] == str_replace("/","-",$this->guarantor_issuedate)) && ($this->cmd =="update4")) {
                    continue;
                }
                $guarantor_issuedate  = $row["GUARANTOR_ISSUEDATE"];
                $guarantor_expiredate = $row["GUARANTOR_EXPIREDATE"];

                if ($this->cmd == 'add4') {
                    if (($guarantor_issuedate <= $GUARANTOR_ISSUEDATE  && $GUARANTOR_ISSUEDATE  <= $guarantor_expiredate && $GUARANTOR_ISSUEDATE  <  $max_issuedate_1_day_after) ||
                        ($guarantor_issuedate <= $GUARANTOR_EXPIREDATE && $GUARANTOR_EXPIREDATE <= $guarantor_expiredate && $GUARANTOR_EXPIREDATE <= $max_issuedate_1_day_after) ||
                        ($GUARANTOR_ISSUEDATE <= $guarantor_issuedate  && $guarantor_expiredate <= $GUARANTOR_EXPIREDATE)) {
                            $this->setWarning("MSG302","( 有効期間 )");
                            return false;
                    }
                }
                if ($this->cmd == 'update4') {
                    if (($guarantor_issuedate <= $GUARANTOR_ISSUEDATE  && $GUARANTOR_ISSUEDATE  <= $guarantor_expiredate) ||
                        ($guarantor_issuedate <= $GUARANTOR_EXPIREDATE && $GUARANTOR_EXPIREDATE <= $guarantor_expiredate) ||
                        ($GUARANTOR_ISSUEDATE <= $guarantor_issuedate  && $guarantor_expiredate <= $GUARANTOR_EXPIREDATE)) {
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
        else if ($this->cmd =="add4" || $this->cmd =="update4") {
            foreach (array("GUARANTOR_TELNO" => array("[^-|0-9]", "( 電話番号 )")) as $key => $val) {
                if ($this->field[$key] != "" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                    $this->setWarning("MSG901", $val[1]);
                    return false;
                }
            }
        }
        else if ($this->cmd =="add5" || $this->cmd =="update5") {
            foreach(array("SEND_TELNO"      => array("[^-|0-9]",        "( 電話番号１ )"),
                          "SEND_TELNO2"     => array("[^-|0-9]",        "( 電話番号２ )")
                    ) as $key => $val) {
                if ($this->field[$key] != "" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
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
                knja110_2aQuery::getInsertQuery($this);
                $this->setMessage("MSG201");
                $this->issuedate = $this->field["ISSUEDATE"];
            } else if ($this->cmd == 'add4' || ($this->infoDiv == "4" && $this->cmd == 'subAdd')) {
                knja110_2aQuery::insertGuarantorData($this);
                $this->setMessage("MSG201");
                $this->guarantor_issuedate = $this->field["GUARANTOR_ISSUEDATE"];
            } else if ($this->cmd == 'add5') {
                knja110_2aQuery::insertSendAddressData($this);
                $this->setMessage("MSG201");
                $this->send_div = $this->field["SEND_DIV"];
            } else if ($this->cmd == 'add6') {
                $rela_max = knja110_2aQuery::getRela_Maxno($this);
                $rela_no = (($rela_max=="")? 1 : ($rela_max+1));
                knja110_2aQuery::insertRelaData($this, $rela_no);
                $this->setMessage("MSG201");
                $this->rela_no = $rela_no;
            } else {
                knja110_2aQuery::insert_Guardian_dat($this);
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
                knja110_2aQuery::getUpdateModel($this);
                $this->setMessage("MSG201");
                $this->issuedate = $this->field["ISSUEDATE"];
            } else if ($this->cmd == 'update4' || ($this->infoDiv == "4" && $this->cmd == 'subUpdate')) {
                if (str_replace("/", "-", $this->guarantor_issuedate) != str_replace("/", "-", $this->field["GUARANTOR_ISSUEDATE"])) {
                    $db = Query::dbCheckOut();
                    $query = knja110_2aQuery::getGuarantorAddressDat($this->schregno, $this->field["GUARANTOR_ISSUEDATE"], "cnt");
                    $cnt_Address = $db->getOne($query);
                    Query::dbCheckIn($db);
                    if ($cnt_Address > 0) {
                        $this->setWarning("MSG302");
                        return false;
                    }
                }
                //保証人住所データ更新
                knja110_2aQuery::updateGuarantorData($this);
                $this->setMessage("MSG201");
                $this->guarantor_issuedate = $this->field["GUARANTOR_ISSUEDATE"];
            } else if ($this->cmd == 'update5') {
                if ($this->send_div != $this->field["SEND_DIV"]) {
                    $db = Query::dbCheckOut();
                    $query = knja110_2aQuery::getSchregSendAddressDat($this->schregno, $this->field["SEND_DIV"], "cnt");
                    $cnt_Address = $db->getOne($query);
                    Query::dbCheckIn($db);
                    if ($cnt_Address > 0) {
                        $this->setWarning("MSG302");
                        return false;
                    }
                }
                //その他住所データ更新
                knja110_2aQuery::updateSendAddressData($this);
                $this->setMessage("MSG201");
                $this->send_div = $this->field["SEND_DIV"];
            } else if ($this->cmd == 'update6') {
                //家族情報データ更新
                knja110_2aQuery::updateRelaData($this);
                $this->setMessage("MSG201");
            } else {
                if (str_replace("/", "-", $this->guard_issuedate) != str_replace("/", "-", $this->field["GUARD_ISSUEDATE"])) {
                    $db = Query::dbCheckOut();
                    $query = knja110_2aQuery::get_cnt_G_Address($this, $this->schregno, $this->field["GUARD_ISSUEDATE"]);
                    $gAddCnt = $db->getOne($query);
                    Query::dbCheckIn($db);
                    if ($gAddCnt > 0) {
                        $this->setWarning("MSG302");
                        return false;
                    }
                }

                knja110_2aQuery::update_Guardian_dat($this);
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
            knja110_2aQuery::getDeleteQuery($this);
            unset($this->field["ISSUEDATE"]);
            unset($this->issuedate);
            $this->setMessage("MSG201");
        } else if ($this->cmd == 'delete4') {
            if($this->field["GUARANTOR_ISSUEDATE"] == "" && $this->guarantor_issuedate == ""){
                $this->setWarning("MSG308");
                return false;
            }
            knja110_2aQuery::deleteGuarantorAddressDat($this);
            unset($this->field["ISSUEDATE"]);
            unset($this->issuedate);
            $this->setMessage("MSG201");
        } else if ($this->cmd == 'delete5') {
            if($this->field["SEND_DIV"] == "" && $this->send_div == ""){
                $this->setWarning("MSG308");
                return false;
            }
            knja110_2aQuery::deleteSchregSendAddressDat($this);
            unset($this->field["ISSUEDATE"]);
            unset($this->issuedate);
            $this->setMessage("MSG201");
        } else if ($this->cmd == 'delete6') {
            if($this->rela_no == ""){
                $this->setWarning("MSG308");
                return false;
            }
            knja110_2aQuery::deleteRelaData($this);
            unset($this->field["ISSUEDATE"]);
            unset($this->issuedate);
            unset($this->rela_no);
            $this->setMessage("MSG201");
        } else {
            if($this->field["GUARD_ISSUEDATE"] == "" && $this->guard_issuedate == ""){
                $this->setWarning("MSG308");
                return false;
            }
            knja110_2aQuery::getDelete_GUARDIAN_ADDRESS_DAT($this);
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

            $query = knja110_2aQuery::getGuardianHistDateCheck($this);
            $dateExists = $db->getOne($query);
            if ($dateExists > "0") {
                $this->setWarning("MSG901", "日付範囲が不正です。");
                return false;
            }
        }

        $query = knja110_2aQuery::getGuardianHistData($this, $this->schregno, $this->histField["ISSUEDATE"]);
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
                       "GUARD_REAL_KANA" => array("( 戸籍氏名かな )","[｡-ﾟ]")) as $key => $val) {
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
        $query = knja110_2aQuery::getInsHistQuery($this);
        $this->hist_issuedate = $this->histField["ISSUEDATE"];
        $this->cmd = "histEdit";
        $db->query($query);
    }

    function delHist($db) {
        $query = knja110_2aQuery::getDelHistQuery($this);
        $this->cmd = "changeCmb";
        $db->query($query);
    }

    function guardUpdFromHist($db, $schregno) {
        if ($this->infoDiv == "4") {
            $updField   = array("GUARANTOR_RELATIONSHIP_FLG"    => "GUARANTOR_RELATIONSHIP",
                                "GUARANTOR_NAME_FLG"            => "GUARANTOR_NAME",
                                "GUARANTOR_KANA_FLG"            => "GUARANTOR_KANA",
                                "GUARANTOR_REAL_NAME_FLG"       => "GUARANTOR_REAL_NAME",
                                "GUARANTOR_REAL_KANA_FLG"       => "GUARANTOR_REAL_KANA",
                                "GUARANTOR_SEX_FLG"             => "GUARANTOR_SEX");

            $table = " GUARANTOR_HIST_DAT ";
        } else {
            $updField   = array("RELATIONSHIP_FLG"      => "RELATIONSHIP",
                                "GUARD_NAME_FLG"        => "GUARD_NAME",
                                "GUARD_KANA_FLG"        => "GUARD_KANA",
                                "GUARD_REAL_NAME_FLG"   => "GUARD_REAL_NAME",
                                "GUARD_REAL_KANA_FLG"   => "GUARD_REAL_KANA",
                                "GUARD_SEX_FLG"         => "GUARD_SEX",
                                "GUARD_BIRTHDAY_FLG"    => "GUARD_BIRTHDAY");

            $table = " GUARDIAN{$this->guardian2}_HIST_DAT ";
        }

        $query = knja110_2aQuery::getGuardHistUpd($this, $schregno, $table);
        $histData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $updFlg = false;
        $guard = array();
        if (is_array($histData)) {
            foreach ($updField as $key => $val) {
                if ($histData[$key] == "1") {
                    $updFlg = true;
                    $guard[$val][TEXT] = $histData[$val];
                }
            }

            if ($updFlg) {
                $query = knja110_2aQuery::getGuardUpdFromHist($this, $guard, $schregno, $table);
                $db->query($query);
            }

        }

        return true;
    }

    //保証人履歴データ更新
    function &getUpdateHistModel2() {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        if (!$this->validateInputHist2($db)) {
            $this->cmd = "histEdit2";
            return;
        }

        if ($this->cmd == "histAdd2") {
            $this->addHist2($db);
        } else if ($this->cmd == "histUpd2") {
            $this->delHist2($db);
            $this->addHist2($db);
        } else {
            $this->delHist2($db);
            unset($this->histField);
        }

        $this->setMessage("MSG201");
        $db->commit();

        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //入力項目チェック
    function validateInputHist2($db) {

        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->histField["SCHREGNO"] != $this->schregno) {
            $this->setMessage("MSG300","\\n学籍番号は変更できません！");
            return false;
        }

        $checkArray = array();
        $checkArray["ISSUEDATE"]                = array("TITLE" => "開始日",      "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["EXPIREDATE"]               = array("TITLE" => "終了日",      "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["GUARANTOR_RELATIONSHIP"]   = array("TITLE" => "続柄",        "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["GUARANTOR_NAME"]           = array("TITLE" => "氏名",        "HISSU" => "ON", "LENGTH" => "ON", "LEN" => "60");
        $checkArray["GUARANTOR_KANA"]           = array("TITLE" => "氏名かな",    "HISSU" => "ON", "LENGTH" => "ON", "LEN" => "120");
        $checkArray["GUARANTOR_REAL_NAME"]      = array("TITLE" => "戸籍氏名",    "HISSU" => "",   "LENGTH" => "ON", "LEN" => "120");
        $checkArray["GUARANTOR_REAL_KANA"]      = array("TITLE" => "戸籍氏名かな","HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");
        $checkArray["GUARANTOR_SEX"]            = array("TITLE" => "性別",        "HISSU" => "",   "LENGTH" => "",   "LEN" => "");

        //必須項目チェック
        $flag = false;
        $msg = "";
        if ($this->cmd == "histAdd2" || $this->cmd == "histUpd2") {

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
        if ($this->cmd == "histAdd2" || $this->cmd == "histUpd2") {
            if (str_replace("/" , "", $this->histField["ISSUEDATE"]) > str_replace("/", "", $this->histField["EXPIREDATE"])) {
                $this->setWarning("MSG901", "開始終了日付が、逆転しています。");
                return false;
            }

            $query = knja110_2aQuery::getGuardianHistDateCheck($this, "GUARANTOR_HIST_DAT");
            $dateExists = $db->getOne($query);
            if ($dateExists > "0") {
                $this->setWarning("MSG901", "日付範囲が不正です。");
                return false;
            }
        }

        $query = knja110_2aQuery::getGuarantorHistDat($this, $this->histField["ISSUEDATE"]);
        $checkExists = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($this->cmd == "histAdd2" && is_array($checkExists)) {
            $this->setWarning("MSG302");
            return false;
        } else if (($this->cmd == "histUpd2" || $this->cmd == "histDel2") && !is_array($checkExists)) {
            $this->setWarning("MSG308");
            return false;
        }

        foreach (array("GUARANTOR_NAME" => array("( 氏名 )","[｡-ﾟ]"),
                       "GUARANTOR_KANA" => array("( 氏名かな )","[｡-ﾟ]")) as $key => $val) {
            if (mbereg($val[1], $this->histField[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        foreach (array("GUARANTOR_REAL_NAME" => array("( 戸籍氏名 )","[｡-ﾟ]"),
                       "GUARANTOR_REAL_KANA" => array("( 戸籍氏名かな )","[｡-ﾟ]")) as $key => $val) {
            if (mbereg($val[1], $this->histField[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        //文字数チェック
        if($this->cmd == "histAdd2" || $this->cmd == "histUpd2") {
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

    //保証人履歴データ追加
    function addHist2($db) {
        $query = knja110_2aQuery::insertGuarantorHistDat($this);
        $this->hist_issuedate = $this->histField["ISSUEDATE"];
        $this->cmd = "histEdit2";
        $db->query($query);
    }

    //保証人履歴データ削除
    function delHist2($db) {
        $query = knja110_2aQuery::deleteGuarantorHistDat($this);
        $this->cmd = "changeCmb2";
        $db->query($query);
    }
}
?>
