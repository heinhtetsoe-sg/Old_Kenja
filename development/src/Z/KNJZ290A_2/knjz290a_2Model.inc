<?php

require_once('for_php7.php');
class knjz290a_2Model extends Model
{
    public $field      = array();
    public $subField2  = array();
    public $cmd;
    public $year;
    public $staffcd;
    public $auth;
    public $prgid;
    public $control_data = array();    //コントロールマスタデータを保持
    public $lastColumn;//CSVの最後の列の値

    public function knjz290a_2Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        common::GetControlMaster_Fnc($this->control_data);
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->staffcd);

            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSubmit);
        }

        $this->field = array("STAFFCD"              =>  VARS::post("STAFFCD"),
                             "STAFFNAME"            =>  VARS::post("STAFFNAME"),
                             "STAFFNAME_SHOW"       =>  VARS::post("STAFFNAME_SHOW"),
                             "STAFFNAME_KANA"       =>  VARS::post("STAFFNAME_KANA"),
                             "STAFFNAME_ENG"        =>  VARS::post("STAFFNAME_ENG"),
                             "STAFFNAME_REAL"       =>  VARS::post("STAFFNAME_REAL"),
                             "STAFFNAME_KANA_REAL"  =>  VARS::post("STAFFNAME_KANA_REAL"),
                             "JOBCD"                =>  VARS::post("JOBCD"),
                             "SECTIONCD"            =>  VARS::post("SECTIONCD"),
                             "DUTYSHARECD"          =>  VARS::post("DUTYSHARECD"),
                             "DUTYSHARECD2"         =>  VARS::post("DUTYSHARECD2"),
                             "CHARGECLASSCD"        =>  VARS::post("CHARGECLASSCD"),
                             "STAFFSEX"             =>  VARS::post("STAFFSEX"),
                             "STAFFBIRTHDAY"        =>  VARS::post("STAFFBIRTHDAY"),
                             "STAFFZIPCD"           =>  VARS::post("STAFFZIPCD"),
                             "STAFFADDR1"           =>  VARS::post("STAFFADDR1"),
                             "STAFFADDR2"           =>  VARS::post("STAFFADDR2"),
                             "STAFFTELNO"           =>  VARS::post("STAFFTELNO"),
                             "STAFFFAXNO"           =>  VARS::post("STAFFFAXNO"),
                             "STAFFE_MAIL"          =>  VARS::post("STAFFE_MAIL"),

                             "NAME_SDATE"           => VARS::post("NAME_SDATE"),
                             "ADDRESS_SDATE"        => VARS::post("ADDRESS_SDATE"),
                             "POSITIONCD1"          => VARS::post("POSITIONCD1"),
                             "POSITIONCD2"          => VARS::post("POSITIONCD2"),
                             "POSITIONCD3"          => VARS::post("POSITIONCD3"),
                             "POSITIONCD_NUM"       => VARS::post("POSITIONCD_NUM"),
                             "POSITIONCD1_MANAGER"  => VARS::post("POSITIONCD1_MANAGER"),
                             "POSITION1_EXT"        => VARS::post("POSITION1_EXT"),
                             "POSITIONCD2_MANAGER"  => VARS::post("POSITIONCD2_MANAGER"),
                             "POSITION2_EXT"        => VARS::post("POSITION2_EXT"),
                             "POSITIONCD3_MANAGER"  => VARS::post("POSITIONCD3_MANAGER"),
                             "POSITION3_EXT"        => VARS::post("POSITION3_EXT"),
                             "CHARGENURSEOFF"       => VARS::post("CHARGENURSEOFF"),
                             "GAKUINSYUNIN_DATE"    => VARS::post("GAKUINSYUNIN_DATE"),   //学院就任年月日
                             "KOUTOUBUSYUNIN_DATE"  => VARS::post("KOUTOUBUSYUNIN_DATE"), //高等部就任年月日
                             "HEADER"               => VARS::post("HEADER"),              //ヘッダの有無
                             "OUTPUT"               => VARS::post("OUTPUT"),              //処理の種類(1:取込、2:書出、3:エラー出力)
                             "UPDATED"              => VARS::post("UPDATED")
                             );

        //担当校種チェックボックス
        $this->checkArray = explode(",", VARS::post("checkArray"));
        $this->schoolKindCheck = array();
        foreach ($this->checkArray as $key => $val) {
            $this->schoolKindCheck[] = VARS::post($val);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        if (!$this->year) {
            $this->year = CTRL_YEAR;
        }

        if (VARS::get("mode") == "1") {
            unset($this->staffcd);
        }

        if (VARS::get("STAFFCD")) {
            $this->staffcd  = VARS::get("STAFFCD");
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendPrgid    = VARS::get("SEND_PRGID");
            $this->sendSubmit   = VARS::get("SEND_SUBMIT");
        }

        $this->auth = ($this->sendAuth == "") ? AUTHORITY : $this->sendAuth;
        $this->prgid = ($this->sendPrgid == "") ? PROGRAMID : $this->sendPrgid;

        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");
        }
        if (VARS::file("FILE_2")) {
            $this->file_2 = VARS::file("FILE_2");
        }
        
        /****************/
        /* 資格教科登録 */
        /****************/
        if ($this->cmd == "subform1") {
            $this->subField2 = array();
            unset($this->classcd);
            unset($this->sdate);
            //教育課程用
            unset($this->school_kind);
            unset($this->curriculum_cd);
        }

        $this->subField2 = array("STAFFCD"          =>  VARS::post("STAFFCD"),
                                 "CLASSCD"          =>  VARS::post("CLASSCD"),
                                 "SDATE"            =>  str_replace("/", "-", VARS::post("SDATE")),
                                 "EDATE"            =>  str_replace("/", "-", VARS::post("EDATE"))
                                 );

        if (VARS::get("CLASSCD")) {
            $this->classcd          = VARS::get("CLASSCD");
            //教育課程用
            $this->school_kind      = VARS::get("SCHOOL_KIND");
            $this->curriculum_cd    = VARS::get("CURRICULUM_CD");
        }
        if (VARS::get("SDATE")) {
            $this->sdate = VARS::get("SDATE");
        }
        if (VARS::get("EDATE")) {
            $this->edate = VARS::get("EDATE");
        }

        $this->positionArray[1] = explode(",", $this->field["POSITION1_EXT"]);
        $this->positionArray[2] = explode(",", $this->field["POSITION2_EXT"]);
        $this->positionArray[3] = explode(",", $this->field["POSITION3_EXT"]);

        $db = Query::dbCheckOut();

        $query = knjz290a_2Query::getDataBaseInfo();
        $this->dataBaseinfo = $db->getOne($query); //教育委員会情報

        //学校名取得
        $this->getSchoolName = $db->getOne(knjz290a_2Query::getSchoolName());

        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getExecModel()
    {
        if ($this->field["STAFFCD"] == "") {
            $this->warning = "職員コードが不正です。";
            return false;
        }
        //ファイル名取得
        $target = $_FILES['FILE_2']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);

        //拡張子チェック
        if ($kakutyousi != "jpg" && $kakutyousi != "JPG") {
            $this->warning = "JPGファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        $filename = DOCUMENTROOT ."/" .$this->control_data["LargePhotoPath"] .'/T' .$this->field["STAFFCD"] .'.jpg';
        if (!is_uploaded_file($this->file_2['tmp_name'])) {
            $this->warning = "ファイル名又は\\nサイズ(100KB以上)等が不正です。";
        } else {
            if (move_uploaded_file($this->file_2['tmp_name'], $filename) == false) {
                $this->warning = "顔写真の取込失敗です。";
            } else {
                $this->message = "顔写真の取込正常です。";
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $setsize = "";
        $zero_ume = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
            $zero_ume = "%010d";
        } else {
            $setsize = 8;
            $zero_ume = "%08d";
        }

        //キーのチェック
        if (trim($this->field["STAFFCD"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901', "(職員コード)");
            return false;
        }
        if (trim($this->field["STAFFNAME"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901', "(職員氏名)");
            return false;
        }
        if (trim($this->field["STAFFNAME_SHOW"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901', "(職員氏名表示用)");
            return false;
        }
        if ($this->staffcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->staffcd != $this->field["STAFFCD"]) {
            $this->setWarning('MSG308');
            return false;
        }

        //コードのゼロ埋め
        if (strlen($this->field["STAFFCD"]) < $setsize) {
            $this->field["STAFFCD"] = sprintf($zero_ume, $this->field["STAFFCD"]);
        }

        //１レコード取得
        $query = knjz290a_2Query::getRow($this, $this->field["STAFFCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning('MSG918', "(職員コード)");
            return false;
        }
        //肩書きの重複チェック
        if ($this->cmd == "update" || $this->cmd == "add") {
            if ($this->field["POSITIONCD1"] != "" && $this->field["POSITIONCD2"] != "" && $this->field["POSITIONCD1"] == $this->field["POSITIONCD2"]) {
                if ($this->field["POSITIONCD1"] == $this->field["POSITIONCD3"]) {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
                if ($this->field["POSITIONCD1"] != "1050") {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
            }
            if ($this->field["POSITIONCD2"] != "" && $this->field["POSITIONCD3"] != "" && $this->field["POSITIONCD2"] == $this->field["POSITIONCD3"]) {
                if ($this->field["POSITIONCD2"] == $this->field["POSITIONCD1"]) {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
                if ($this->field["POSITIONCD2"] != "1050") {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
            }
            if ($this->field["POSITIONCD3"] != "" && $this->field["POSITIONCD1"] != "" && $this->field["POSITIONCD3"] == $this->field["POSITIONCD1"]) {
                if ($this->field["POSITIONCD3"] == $this->field["POSITIONCD2"]) {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
                if ($this->field["POSITIONCD3"] != "1050") {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
            }
        }

        if ($this->cmd == "add") {
            if ($this->sendSubmit == "") {
                //学校別教職員外テーブル存在チェック
                $query = knjz290a_2Query::chkStaffAnotherDat($this->field["STAFFCD"]);
                $another = $db->getRow($query);
                if (is_array($another)) {
                    $this->setWarning('MSG918', "学校に登録済み");
                    return;
                }
            } else {
                if ($this->iinkai == "" && $this->dataBaseinfo === '2') {
                    //重複チェック（教育委員会）
                    $query = knjz290a_2Query::getRow($this, $this->field["STAFFCD"]);
                    $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                    if (is_array($row)) {
                        $this->setWarning('MSG918', "教育委員会に登録済み");
                        return;
                    }

                    //教育委員会用学校コード存在チェック
                    if ($this->edboard_schoolcd == "") {
                        $this->setWarning('MSG305', "教育委員会用学校コード未登録");
                        return;
                    }
                }
            }

            //有効開始日の必須入力チェック
            if (!$this->staffcd && !$this->iinkai) {
                if (trim($this->field["NAME_SDATE"]) == "") {
                    $this->setWarning('MSG301', "(氏名有効開始日)");
                    return false;
                }
                if (trim($this->field["ADDRESS_SDATE"]) == "") {
                    $this->setWarning('MSG301', "(住所有効開始日)");
                    return false;
                }
            }
        }

        //文字型チェック
        foreach (
                array("STAFFTELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "STAFFFAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }

        //桁数チェック
        if ($this->validate_row_cnt($this->field["STAFFCD"], ($setsize * 1)) > 1) {
            $this->setWarning("MSG915", "(職員コード) ".$setsize."バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFNAME"], (60 * 1)) > 1) {
            $this->setWarning("MSG915", "(職員氏名) 60バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFNAME_SHOW"], (15 * 1)) > 1) {
            $this->setWarning("MSG915", "(職員氏名表示用) 15バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFNAME_KANA"], (120 * 1)) > 1) {
            $this->setWarning("MSG915", "(職員氏名かな) 120バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFNAME_ENG"], (60 * 1)) > 1) {
            $this->setWarning("MSG915", "(職員氏名英字) 60バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFNAME_REAL"], (120 * 1)) > 1) {
            $this->setWarning("MSG915", "(戸籍氏名) 120バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFNAME_KANA_REAL"], (240 * 1)) > 1) {
            $this->setWarning("MSG915", "(戸籍氏名かな) 240バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFZIPCD"], (8 * 1)) > 1) {
            $this->setWarning("MSG915", "(郵便番号) 8バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFADDR1"], (90 * 1)) > 1) {
            $this->setWarning("MSG915", "(住所1) 90バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFADDR2"], (90 * 1)) > 1) {
            $this->setWarning("MSG915", "(住所2) 90バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFTELNO"], (14 * 1)) > 1) {
            $this->setWarning("MSG915", "(電話番号) 14バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFFAXNO"], (14 * 1)) > 1) {
            $this->setWarning("MSG915", "(FAX番号) 14バイトまで");
            return false;
        } elseif ($this->validate_row_cnt($this->field["STAFFE_MAIL"], (50 * 1)) > 1) {
            $this->setWarning("MSG915", "(E-Mail) 50バイトまで");
            return false;
        }

        //青山学院
        if ($this->getSchoolName == "aoyama") {
            //高等部就任年月日が学院就任年月日より過去日の場合
            if ($this->field["KOUTOUBUSYUNIN_DATE"] != "" &&
                strtotime($this->field["KOUTOUBUSYUNIN_DATE"]) < strtotime($this->field["GAKUINSYUNIN_DATE"])) {
                $this->setWarning("高等部就任年月日には学院就任年月日より以降の日付を入力してください");
                return false;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return true;
    }

    //追加処理
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->staffcd = $this->field["STAFFCD"];
            $this->setMessage('MSG201');
            $this->year = "ALL";
        }
    }

    //追加
    public function &insert()
    {
        knjz290a_2Query::getInsertQuery($this, $this->field);
        knjz290a_2Query::newDataUpdate($this, $this->field);

        if ($this->sendSubmit != "" && $this->dataBaseinfo === '2') {
            knjz290a_2Query::insertStaffAnotherDat($this, $this->field);
        }

        if (!$this->staffcd && !$this->iinkai) {
            knjz290a_2Query::insertRirekiData($this->field);
        }
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //更新
    public function &update()
    {
        knjz290a_2Query::getUpdateQuery($this, $this->field);
        knjz290a_2Query::newDataUpdate($this, $this->field);
    }

    //削除処理
    public function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->staffcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->staffcd != $this->field["STAFFCD"]) {
            $this->setWarning('MSG308');
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //職員マスタ存在チェック
        $query = knjz290a_2Query::getRow($this, $this->staffcd);
        $row1 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!is_array($row1)) {
            $this->setWarning('MSG303');
            return;
        }

        //年度データ存在チェック
        $query = knjz290a_2Query::isExisting($this->staffcd);
        $row2 = $db->getRow($query);
        if (is_array($row2)) {
            $this->setWarning('MSG919', "年度データに登録されています。");
            return;
        }

        if ($this->sendSubmit == "") {
            //学校別教職員外テーブル存在チェック
            $query = knjz290a_2Query::chkStaffAnotherDat($this->staffcd);
            $another = $db2->getRow($query);
            if (is_array($another)) {
                $this->setWarning('MSG919', "学校に登録されています。");
                return;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //削除
        knjz290a_2Query::getDeleteQuery($this, $this->staffcd);
        $this->setMessage('MSG201');
        $this->staffcd = "";
    }

    /**
     * 前年度コピー(職員詳細データ)のモデルを返します
    */
    public function &getCopyModel()
    {
        $result = knjz290a_2Query::getCopyDetail($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
    }

    /********************/
    /* 資格教科登録画面 */
    /********************/
    //入力項目チェック
    public function validateInputSubform()
    {
        //キーのチェック
        if (trim($this->staffcd) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301');
            return false;
        }
        if (trim($this->subField2["CLASSCD"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301', "(教科)");
            return false;
        }
        if (trim($this->subField2["SDATE"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301', "(開始日付)");
            return false;
        }
        if (trim($this->subField2["EDATE"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301', "(終了日付)");
            return false;
        }

        //更新,削除時にリストから選択しているかをチェック
        if ($this->sdate  == "" && ($this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG308');
            return false;
        }

        //開始日付と終了日付の日付の前後関係をチェック
        if ($this->cmd =="subform1_add" || $this->cmd =="subform1_update") {
            $SDATE  = date("Y-m-d", strtotime(str_replace("-", "/", $this->subField2["SDATE"])));
            $EDATE  = date("Y-m-d", strtotime(str_replace("-", "/", $this->subField2["EDATE"])));

            if ($EDATE <= $SDATE) {
                $this->setWarning("MSG916", "( 有効期間 )");
                return false;
            }
        }

        $db = Query::dbCheckOut();

        //重複教科エラーチェック
        if ($this->cmd =="subform1_add") {
            $query = knjz290a_2Query::getCntClasscd($this->staffcd, $this->subField2["CLASSCD"], $this);
            $cnt_G_Address = $db->getOne($query);
            if (($this->cmd =="subform1_add") && $cnt_G_Address > 0) {
                $this->setWarning("MSG302", "( 同一の教科 )");
                return false;
            }
        }

        //重複エラーチェック
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $query = knjz290a_2Query::getRow2check($this->staffcd, $this->subField2["CLASSCD"], $this->subField2["SDATE"], $this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $query = knjz290a_2Query::getRow2($this->staffcd, $this->subField2["CLASSCD"], $this->subField2["SDATE"], $this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        Query::dbCheckIn($db);

        if ($this->cmd == "subform1_add" && is_array($Row)) {
            $this->setWarning('MSG302');
            return false;
        }
        
        if (($this->cmd == "subform1_update" || $this->cmd == "subform1_delete") && !is_array($Row)) {
            $this->setWarning('MSG303');
            return false;
        }
        
        return true;
    }

    //追加処理
    public function &getInsertSubformModel()
    {
        //入力チェック
        if ($this->validateInputSubform()) {
            $this->insert2();
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $class_array = array();
                $class_array = explode("-", $this->subField2["CLASSCD"]);
                $this->classcd = $class_array[0];
                $this->school_kind = $class_array[1];
                $this->curriculum_cd = $class_array[2];
            } else {
                $this->classcd = $this->subField2["CLASSCD"];
            }
            $this->sdate = $this->subField2["SDATE"];
            $this->setMessage('MSG201');
        }
    }
    //追加
    public function &insert2()
    {
        knjz290a_2Query::getInsert2Query($this->subField2, $this);
    }

    //更新処理
    public function &getUpdateSubformModel()
    {
        //入力チェック
        if ($this->validateInputSubform()) {
            $this->update2();
            $this->setMessage('MSG201');
        }
    }
    //更新
    public function &update2()
    {
        knjz290a_2Query::getUpdate2Query($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    public function &getDeleteSubformModel()
    {
        //入力チェック
        if ($this->validateInputSubform()) {
            $this->delete2();
            $this->setMessage('MSG201');
        }
    }
    //削除
    public function &delete2()
    {
        $result = knjz290a_2Query::getDelete2Query($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
    
    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        $zero_ume = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $zero_ume = "000000000";
        } else {
            $zero_ume = "0000000";
        }

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $headerDuplicate = array("YEAR"      => "年度",
                                     "STAFFCD"   => "職員コード",
                                     "STAFF_SEQ" => "職員SEQ");
            $tables = array();
            $tables = array("1" => "STAFF_MST", "2" => "STAFF_DETAIL_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode(",", $headerErr)."\n";
            }
            $contents .= implode(",", $header)."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header = array("※職員コード",
                            "※職員氏名",
                            "職員氏名表示用",
                            "職員氏名かな",
                            "職員氏名英字",
                            "戸籍氏名",
                            "戸籍氏名かな",
                            "職員性別",
                            "職員生年月日",
                            "年齢年月日",
                            "学院就任年月日",
                            "高等部就任年月日",
                            "学院勤続",
                            "高等部勤続",
                            "職員郵便番号",
                            "職員住所1",
                            "職員住所2",
                            "職員電話番号",
                            "職員FAX番号",
                            "職員メールアドレス",
                            "※年度",
                            "職名コード",
                            "所属コード",
                            "校務分掌部コード1",
                            "校務分掌部コード2",
                            "授業受持区分",
                            "肩書き1(教務主任等コード)",
                            "学年主任または教科主任(教科)",
                            "教科主任(学校校種)",
                            "肩書き2(教務主任等コード)",
                            "学年主任または教科主任(教科)",
                            "教科主任(学校校種)",
                            "肩書き3(教務主任等コード)",
                            "学年主任または教科主任(教科)",
                            "教科主任(学校校種)",
                            "担当保健室(保健室コード)",
                            $this->lastColumn);

            //青山学院以外
            if ($this->getSchoolName != "aoyama") {
                $aryHeaderAoyama = array(
                    "年齢年月日",
                    "学院就任年月日",
                    "高等部就任年月日",
                    "学院勤続",
                    "高等部勤続"
                );
                //headerから削除
                $header = array_diff($header, $aryHeaderAoyama);
            }

            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjz290a_2Query::getCsvData($this);
        }
        if ($this->field["OUTPUT"] == "4") {
            $staffcd_rei = "";
            //STAFFCDフィールドサイズ変更対応
            if ($this->Properties["useStaffcdFieldSize"] === '10') {
                $staffcd_rei = "1234567890";
            } else {
                $staffcd_rei = "12345678";
            }
        
            //青山学院
            $contentsAoyama = "";
            if ($this->getSchoolName == "aoyama") {
                $contentsAoyama .= ",";
                $contentsAoyama .= (CTRL_YEAR - 1)."-04-01,";
                $contentsAoyama .= CTRL_YEAR."-04-01,";
                $contentsAoyama .= ",";
                $contentsAoyama .= ",";
            }

            $contents .= $staffcd_rei.",";
            $contents .= "広島　太郎,";
            $contents .= "広島　太郎,";
            $contents .= "ひろしま　たろう,";
            $contents .= "tarou hiroshima,";
            $contents .= "広島　太郎,";
            $contents .= "ひろしま　たろう,";
            $contents .= "1,";
            $contents .= "1960-12-31,";
            $contents .= $contentsAoyama;
            $contents .= "900-0032,";
            $contents .= "広島県あああ市いいい１２３,";
            $contents .= "広島アパート１２３号室,";
            $contents .= "090-123-1234,";
            $contents .= "090-123-1234,";
            $contents .= "aaa123@bbb.co.jp,";
            $contents .= CTRL_YEAR.",";
            $contents .= "9999,";
            $contents .= "9999,";
            $contents .= "9999,";
            $contents .= "9999,";
            $contents .= "1,";
            $contents .= "0200,";
            $contents .= "01,";
            $contents .= ",";
            $contents .= "1050,";
            $contents .= "01,";
            $contents .= "H,";
            $contents .= "1050,";
            $contents .= "02,";
            $contents .= "H,";
            $contents .= "01,";
            $contents .= $this->lastColumn."\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "2") {
                    if ($this->getSchoolName == "aoyama") {
                        //年齢年月日
                        $row["STAFFAGE"] = $this->getDateDiffStr($row["STAFFBIRTHDAY"], "歳");
                        //学院勤続
                        $row["GAKUINKINZOKU"] = $this->getDateDiffStr($row["GAKUINSYUNIN_DATE"], "年");
                        //高等部勤続
                        $row["KOUTOUBUKINZOKU"] = $this->getDateDiffStr($row["KOUTOUBUSYUNIN_DATE"], "年");
                    }
                }

                //配列の値を,で連結
                $contents .= implode(",", $row) ."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="職員マスタエラー";
            } else {
                $couse="職員マスタ一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //クライアント ⇒ サーバ側の処理
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                knjz290a_2Query::insertQueryCsv($db, $this, $this->data_arr);
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $setsize = "";
        $zero_ume = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
            $zero_ume = "%010s";
        } else {
            $setsize = 8;
            $zero_ume = "%08s";
        }

        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array( "STAFFCD"                => array($setsize   ,"職員コード"),
                                "STAFFNAME"              => array(60  ,"職員氏名" ),
                                "STAFFNAME_SHOW"         => array(15  ,"職員氏名表示用"),
                                "STAFFNAME_KANA"         => array(120 ,"職員氏名かな"),
                                "STAFFNAME_ENG"          => array(60  ,"職員氏名英字"),
                                "STAFFNAME_REAL"         => array(120 ,"戸籍氏名"),
                                "STAFFNAME_KANA_REAL"    => array(240 ,"戸籍氏名かな"),
                                "STAFFSEX"               => array(1   ,"職員性別"),
                                "STAFFBIRTHDAY"          => array(10  ,"職員生年月日"),
                                "STAFFAGE"               => array(0   ,"年齢年月日"),
                                "GAKUINSYUNIN_DATE"      => array(10  ,"学院就任年月日"),
                                "KOUTOUBUSYUNIN_DATE"    => array(10  ,"高等部就任年月日"),
                                "GAKUINKINZOKU"          => array(0   ,"学院勤続"),
                                "KOUTOUBUKINZOKU"        => array(0   ,"高等部勤続"),
                                "STAFFZIPCD"             => array(8   ,"職員郵便番号"),
                                "STAFFADDR1"             => array(90  ,"職員住所1"),
                                "STAFFADDR2"             => array(90  ,"職員住所2"),
                                "STAFFTELNO"             => array(14  ,"職員電話番号"),
                                "STAFFFAXNO"             => array(14  ,"職員FAX番号"),
                                "STAFFE_MAIL"            => array(50  ,"職員メールアドレス"),
                                "YEAR"                   => array(4   ,"年度"),
                                "JOBCD"                  => array(4   ,"職名コード"),
                                "SECTIONCD"              => array(4   ,"所属コード"),
                                "DUTYSHARECD"            => array(4   ,"校務分掌部コード1"),      //注意:登録順番->チェック順番
                                "DUTYSHARECD2"           => array(4   ,"校務分掌部コード2"),      //注意:登録順番->チェック順番
                                "CHARGECLASSCD"          => array(1   ,"授業受持区分"),
                                "POSITIONCD1"            => array(4   ,"肩書き1(教務主任等コード)"),
                                "POSITIONCD1_MANAGER1"   => array(2   ,"学年主任または教科主任(教科)"),
                                "POSITIONCD1_MANAGER2"   => array(1   ,"教科主任(学校校種)"),
                                "POSITIONCD2"            => array(4   ,"肩書き2(教務主任等コード)"),
                                "POSITIONCD2_MANAGER1"   => array(2   ,"学年主任または教科主任(教科)"),
                                "POSITIONCD2_MANAGER2"   => array(1   ,"教科主任(学校校種)"),
                                "POSITIONCD3"            => array(4   ,"肩書き3(教務主任等コード)"),
                                "POSITIONCD3_MANAGER1"   => array(2   ,"学年主任または教科主任(教科)"),
                                "POSITIONCD3_MANAGER2"   => array(1   ,"教科主任(学校校種)"),
                                "CHARGENURSEOFF"         => array(2   ,"担当保健室(保健室コード)")
                               );

            //青山学院以外
            if ($this->getSchoolName != "aoyama") {
                unset($checkdata["STAFFAGE"]);
                unset($checkdata["GAKUINSYUNIN_DATE"]);
                unset($checkdata["KOUTOUBUSYUNIN_DATE"]);
                unset($checkdata["GAKUINKINZOKU"]);
                unset($checkdata["KOUTOUBUKINZOKU"]);
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey2   = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('STAFF_MST');
            $primaryKey2   = $this->getPrimaryKey('STAFF_DETAIL_MST');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $cnt = 0;
                    //checkdata項目でループして取得データをセット
                    foreach ($checkdata as $key => $val) {
                        $data[$key] = trim($tmp[$cnt++]);
                    }
                    $data["STAFF_SEQ"] = "";

                    $data["STAFFCD"] = (strlen(trim($data["STAFFCD"]))) ? sprintf($zero_ume, $data["STAFFCD"]) : "";
                    $data["JOBCD"] = (strlen(trim($data["JOBCD"]))) ? sprintf("%04s", $data["JOBCD"]) : "";
                    $data["SECTIONCD"] = (strlen(trim($data["SECTIONCD"]))) ? sprintf("%04s", $data["SECTIONCD"]) : "";
                    $data["DUTYSHARECD"] = (strlen(trim($data["DUTYSHARECD"]))) ? sprintf("%04s", $data["DUTYSHARECD"]) : "";
                    $data["DUTYSHARECD2"] = (strlen(trim($data["DUTYSHARECD2"]))) ? sprintf("%04s", $data["DUTYSHARECD2"]) : "";

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, get_count($tmp), $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $chkCnt, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;  //必須エラーフラグ
        $lengthErrFlg       = false;  //レングスエラーフラグ
        $colCntErrFlg       = false;  //項目数エラーフラグ
        $duplicateCsvErrFlg = false;  //重複(CSV)エラーフラグ
        $existenceErrFlg    = false;  //存在エラーフラグ
        $otherErrFlg        = false;  //その他エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $colCntErrMsg       = "";     //項目数エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ
        $existenceErrMsg    = "";     //存在エラーメッセージ
        $otherErrMsg        = "";     //その他エラーメッセージ

        $hissuSep        = "";
        $lengthSep       = "";
        $colCntSep       = "";
        $duplicateCsvSep = "";
        $existenceSep    = "";
        $otherSep        = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        //項目数チェックエラー
        if (get_count($checkdata) + 1 > $chkCnt) { //ダミー数を加味して項目数をチェック
            $returnFlg = $this->errSet($colCntErrFlg, $colCntErrMsg, "※項目数エラー:", $colCntSep, "古いFILEVERSIONの可能性あり");
        } else {
            foreach ($checkdata as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("STAFFCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                if (in_array($key, array("STAFFNAME"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                if (in_array($key, array("YEAR"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //文字数チェック
                if (!in_array($key, array("STAFFAGE", "GAKUINKINZOKU", "KOUTOUBUKINZOKU"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                //日付チェック
                if (in_array($key, array("STAFFBIRTHDAY", "GAKUINSYUNIN_DATE", "KOUTOUBUSYUNIN_DATE"))) {
                    if (strlen(trim($data[$key])) && $this->dateRep($data[$key]) == "") {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付:", $otherSep, $val[1]);
                    }
                }
            }
            //重複登録チェック(チェック順番注意)
            if (strlen(trim($data["DUTYSHARECD"])) && strlen(trim($data["DUTYSHARECD2"]))) {
                if (trim($data["DUTYSHARECD"]) == trim($data["DUTYSHARECD2"])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "校務分掌部コード1は校務分掌部コード2を重複します。");
                }
            }

            //青山学院
            if ($this->getSchoolName == "aoyama") {
                //高等部就任年月日が学院就任年月日より過去日の場合
                if (strlen(trim($data["KOUTOUBUSYUNIN_DATE"])) && strlen(trim($data["GAKUINSYUNIN_DATE"]))) {
                    if (strtotime(trim($data["KOUTOUBUSYUNIN_DATE"])) < strtotime(trim($data["GAKUINSYUNIN_DATE"]))) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付:", $otherSep, "高等部就任年月日には学院就任年月日より以降の日付を入力してください。");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$existenceErrMsg.$colCntErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz290a_2Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }

    public function getDateDiffStr($strDate, $defaultDispYear)
    {
        $formatDate = "";

        //ログイン日付
        $ctrlDate = new DateTime(CTRL_DATE);

        if ($strDate != "") {
            //対象日付
            $dtDate = new DateTime($strDate);

            $formatDate = "";
            $dateDiff = $dtDate->diff($ctrlDate);
            $years = $dateDiff->format('%y');
            $months = $dateDiff->format('%m');

            if ($years > 0) {
                $formatDate = $years.$defaultDispYear." ";
            }
            $formatDate .= $months."ヶ月";

            //対象日付がログイン日付より後の場合
            if ($dtDate > $ctrlDate) {
                $formatDate = "-".$formatDate;
            }
        }

        return $formatDate;
    }
}
