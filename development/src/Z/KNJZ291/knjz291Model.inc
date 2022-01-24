<?php

require_once('for_php7.php');

class knjz291Model extends Model {
    var $field      = array();
    var $subField2  = array();
    var $cmd;
    var $year;
    var $staffcd;
    var $auth;
    var $prgid;
    var $control_data = array();    //コントロールマスタデータを保持

    function knjz291Model()
    {
    }

    function init() {
        common::GetControlMaster_Fnc($this->control_data);
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->staffcd);

            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSubmit);
        }

        $this->field = array("STAFFCD"              =>  VARS::post("STAFFCD"),
                             "EDBOARD_STAFFCD"      =>  VARS::post("EDBOARD_STAFFCD"),
                             "EDBOARD_TORIKOMI_FLG" =>  VARS::post("EDBOARD_TORIKOMI_FLG"),
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

                             "NAME_SDATE"           =>  VARS::post("NAME_SDATE"),
                             "FROM_DATE"            =>  VARS::post("FROM_DATE"),
                             "FROM_DIV"             =>  VARS::post("FROM_DIV"),
                             "FROM_SCHOOLCD"        =>  VARS::post("FROM_SCHOOLCD"),
                             "FROM_COURSECD"        =>  VARS::post("FROM_COURSECD"),
                             "TO_DATE"              =>  VARS::post("TO_DATE"),
                             "TO_DIV"               =>  VARS::post("TO_DIV"),
                             "TO_SCHOOLCD"          =>  VARS::post("TO_SCHOOLCD"),
                             "TO_COURSECD"          =>  VARS::post("TO_COURSECD"),
                             "ADDRESS_SDATE"        =>  VARS::post("ADDRESS_SDATE"),
                             "POSITIONCD1"          =>  VARS::post("POSITIONCD1"),
                             "POSITIONCD2"          =>  VARS::post("POSITIONCD2"),
                             "POSITIONCD3"          =>  VARS::post("POSITIONCD3"),
                             "POSITIONCD_NUM"       =>  VARS::post("POSITIONCD_NUM"),
                             "POSITIONCD1_MANAGER"  =>  VARS::post("POSITIONCD1_MANAGER"),
                             "POSITION1_EXT"        =>  VARS::post("POSITION1_EXT"),
                             "POSITIONCD2_MANAGER"  =>  VARS::post("POSITIONCD2_MANAGER"),
                             "POSITION2_EXT"        =>  VARS::post("POSITION2_EXT"),
                             "POSITIONCD3_MANAGER"  =>  VARS::post("POSITIONCD3_MANAGER"),
                             "POSITION3_EXT"        =>  VARS::post("POSITION3_EXT"),
                             "CHARGENURSEOFF"       =>  VARS::post("CHARGENURSEOFF"),
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             );

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::post("YEAR")){
            $this->year = VARS::post("YEAR");
        }

        if (VARS::get("mode") == "1"){
            unset($this->staffcd);
        }

        if (VARS::get("STAFFCD")){
            $this->staffcd  = VARS::get("STAFFCD");
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendPrgid    = VARS::get("SEND_PRGID");
            $this->sendSubmit   = VARS::get("SEND_SUBMIT");
        }

        if (VARS::file("FILE_2")) {
            $this->file_2 = VARS::file("FILE_2");
        }

        $this->auth = ($this->sendAuth == "") ? AUTHORITY : $this->sendAuth;
        $this->prgid = ($this->sendPrgid == "") ? PROGRAMID : $this->sendPrgid;

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
                                 "SDATE"            =>  str_replace("/","-",VARS::post("SDATE")),
                                 "EDATE"            =>  str_replace("/","-",VARS::post("EDATE"))
                                 );

        if (VARS::get("CLASSCD")) {
            $this->classcd          = VARS::get("CLASSCD");
            //教育課程用
            $this->school_kind      = VARS::get("SCHOOL_KIND");
            $this->curriculum_cd    = VARS::get("CURRICULUM_CD");
        }
        if (VARS::get("SDATE")){
            $this->sdate = VARS::get("SDATE");
        }
        if (VARS::get("EDATE")){
            $this->edate = VARS::get("EDATE");
        }

        $this->positionArray[1] = explode(",", $this->field["POSITION1_EXT"]);
        $this->positionArray[2] = explode(",", $this->field["POSITION2_EXT"]);
        $this->positionArray[3] = explode(",", $this->field["POSITION3_EXT"]);
    }

   //入力項目チェック
    function validateInput() {

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
            $this->setWarning('MSG901',"(職員コード)");
            return false;
        }
        if (trim($this->field["STAFFNAME"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(職員氏名)");
            return false;
        }
        if (trim($this->field["STAFFNAME_SHOW"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(職員氏名表示用)");
            return false;
        }
        if (trim($this->field["FROM_DATE"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(赴任(入)日付)");
            return false;
        }
        if (trim($this->field["FROM_DIV"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(赴任区分)");
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
        if (strlen($this->field["STAFFCD"]) < $setsize){
            $this->field["STAFFCD"] = sprintf($zero_ume, $this->field["STAFFCD"]);
        }

        //１レコード取得
        $query = knjz291Query::getRow($this, $this->field["STAFFCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG203');
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
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

        //県職員コードチェック
        if ($this->cmd == "update") {
            if ($this->sendSubmit == "1" && $this->field["EDBOARD_STAFFCD"] != "") {
                //県側職員テーブルのSTAFFCD存在チェック
                $query = knjz291Query::chkEdboardStaffcd($this->field["EDBOARD_STAFFCD"]);
                $kenStaffcnt = $db2->getOne($query);
                //学校側職員テーブルのEDBOARD_STAFFCD存在チェック
                $query = knjz291Query::chkEdboardStaffcd($this->field["EDBOARD_STAFFCD"], "GAKKOU");
                $gakkouStaffcnt = $db->getOne($query);
                $query = knjz291Query::chkEdboardStaffcd($this->field["EDBOARD_STAFFCD"], $this->field["STAFFCD"]);
                $honinStaffcnt = $db->getOne($query);
                if ($kenStaffcnt == 0){
                    $this->setWarning('MSG901',"県職員コード(教育委員会未登録)");
                    return;
                } else if ($gakkouStaffcnt > 0 && $honinStaffcnt == 0) {
                    $this->setWarning('MSG901',"県職員コード(登録済み)");
                    return;
                }
            }
        }
        if ($this->cmd == "add") {
            if ($this->sendSubmit == "") {
                //学校別教職員外テーブル存在チェック
                $query = knjz291Query::chkStaffAnotherDat($this->field["STAFFCD"]);
                $another = $db->getRow($query);
                if (is_array($another)){
                    $this->setWarning('MSG918',"学校に登録済み");
                    return;
                }
            } else {
                if ($this->iinkai == "") {
                    //重複チェック（教育委員会）
                    $query = knjz291Query::getRow($this, $this->field["STAFFCD"]);
                    $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                    if (is_array($row)){
                        $this->setWarning('MSG918',"教育委員会に登録済み");
                        return;
                    }

                    //教育委員会用学校コード存在チェック
                    if ($this->edboard_schoolcd == ""){
                        $this->setWarning('MSG305',"教育委員会用学校コード未登録");
                        return;
                    }
                }
            }

            //有効開始日の必須入力チェック
            if (!$this->staffcd && !$this->iinkai) {
                if (trim($this->field["NAME_SDATE"]) == "") {
                    $this->setWarning('MSG301',"(氏名有効開始日)");
                    return false;
                }
                if (trim($this->field["ADDRESS_SDATE"]) == "") {
                    $this->setWarning('MSG301',"(住所有効開始日)");
                    return false;
                }
            }
        }

        //文字型チェック
        foreach(array("STAFFTELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "STAFFFAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }

        //桁数チェック
        if (strlen($this->field["STAFFCD"]) > $setsize) {
            $this->setWarning("MSG915","(職員コード) ".$setsize."バイトまで");
            return false;
        } else if (strlen($this->field["STAFFNAME"]) > 60) {
            $this->setWarning("MSG915","(職員氏名) 60バイトまで");
            return false;
        } else if (strlen($this->field["STAFFNAME_SHOW"]) > 15) {
            $this->setWarning("MSG915","(職員氏名表示用) 15バイトまで");
            return false;
        } else if (strlen($this->field["STAFFNAME_KANA"]) > 120) {
            $this->setWarning("MSG915","(職員氏名かな) 120バイトまで");
            return false;
        } else if (strlen($this->field["STAFFNAME_ENG"]) > 60) {
            $this->setWarning("MSG915","(職員氏名英字) 60バイトまで");
            return false;
        } else if (strlen($this->field["STAFFNAME_REAL"]) > 120) {
            $this->setWarning("MSG915","(戸籍氏名) 120バイトまで");
            return false;
        } else if (strlen($this->field["STAFFNAME_KANA_REAL"]) > 240) {
            $this->setWarning("MSG915","(戸籍氏名かな) 240バイトまで");
            return false;
        } else if (strlen($this->field["STAFFZIPCD"]) > 8) {
            $this->setWarning("MSG915","(郵便番号) 8バイトまで");
            return false;
        } else if (strlen($this->field["STAFFADDR1"]) > 90) {
            $this->setWarning("MSG915","(住所1) 90バイトまで");
            return false;
        } else if (strlen($this->field["STAFFADDR2"]) > 90) {
            $this->setWarning("MSG915","(住所2) 90バイトまで");
            return false;
        } else if (strlen($this->field["STAFFTELNO"]) > 14) {
            $this->setWarning("MSG915","(電話番号) 14バイトまで");
            return false;
        } else if (strlen($this->field["STAFFFAXNO"]) > 14) {
            $this->setWarning("MSG915","(FAX番号) 14バイトまで");
            return false;
        } else if (strlen($this->field["STAFFE_MAIL"]) > 50) {
            $this->setWarning("MSG915","(E-Mail) 50バイトまで");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->staffcd = $this->field["STAFFCD"];
            $this->setMessage('MSG201');
            $this->year = "ALL";
        }
    }

    //追加
    function &insert() {
        knjz291Query::getInsertQuery($this, $this->field);
        knjz291Query::newDataUpdate($this, $this->field);
        knjz291Query::edWorkUpd($this, $this->field);

        if ($this->sendSubmit != "") {
            knjz291Query::insertStaffAnotherDat($this, $this->field);
        }

        if (!$this->staffcd && !$this->iinkai) {
            knjz291Query::insertRirekiData($this->field);
        }
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //更新
    function &update() {
        knjz291Query::getUpdateQuery($this, $this->field);
        knjz291Query::newDataUpdate($this, $this->field);
        knjz291Query::edWorkUpd($this, $this->field);
    }

    //削除処理
    function &getDeleteModel() {
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
        $query = knjz291Query::getRow($this, $this->staffcd);
        $row1 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!is_array($row1)){
            $this->setWarning('MSG303');
            return;
        }

        //年度データ存在チェック
        $query = knjz291Query::IsExisting($this->staffcd);
        $row2 = $db->getRow($query);
        if (is_array($row2)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }

        if ($this->sendSubmit == "") {
            //学校別教職員外テーブル存在チェック
            $query = knjz291Query::chkStaffAnotherDat($this->staffcd);
            $another = $db2->getRow($query);
            if (is_array($another)){
                $this->setWarning('MSG919',"学校に登録されています。");
                return;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //削除
        knjz291Query::getDeleteQuery($this, $this->staffcd);
        if ($this->kyouikuIinkai > 0) {
            knjz291Query::edWorkDel($this, $this->field);
        }
        $this->setMessage('MSG201');
        $this->staffcd = "";
    }

    /********************/
    /* 資格教科登録画面 */
    /********************/
    //入力項目チェック
    function validateInputSubform() {
        //キーのチェック
        if (trim($this->staffcd) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301');
            return false;
        }
        if (trim($this->subField2["CLASSCD"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301',"(教科)");
            return false;
        }
        if (trim($this->subField2["SDATE"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301',"(開始日付)");
            return false;
        }
        if (trim($this->subField2["EDATE"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301',"(終了日付)");
            return false;
        }

        //更新,削除時にリストから選択しているかをチェック
        if ($this->sdate  == "" && ($this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG308');
            return false;
        }

        //開始日付と終了日付の日付の前後関係をチェック
        if ($this->cmd =="subform1_add" || $this->cmd =="subform1_update") {
            $SDATE  = date("Y-m-d",strtotime(str_replace("-","/",$this->subField2["SDATE"])));
            $EDATE  = date("Y-m-d",strtotime(str_replace("-","/",$this->subField2["EDATE"])));

            if ($EDATE <= $SDATE) {
                $this->setWarning("MSG916","( 有効期間 )");
                return false;
            }
        }

        $db = Query::dbCheckOut();

        //重複教科エラーチェック
        if ($this->cmd =="subform1_add") {
            $query = knjz291Query::get_cnt_Classcd($this->staffcd,$this->subField2["CLASSCD"], $this);
            $cnt_G_Address = $db->getOne($query);
            if (($this->cmd =="subform1_add") && $cnt_G_Address > 0) {
                $this->setWarning("MSG302","( 同一の教科 )");
                return false;
            }
        }

        //重複エラーチェック
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $query = knjz291Query::getRow2check($this->staffcd, $this->subField2["CLASSCD"], $this->subField2["SDATE"], $this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $query = knjz291Query::getRow2($this->staffcd, $this->subField2["CLASSCD"], $this->subField2["SDATE"], $this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        Query::dbCheckIn($db);

        if ($this->cmd == "subform1_add" && is_array($Row)){
            $this->setWarning('MSG302');
            return false;
        }
        
        if (($this->cmd == "subform1_update" || $this->cmd == "subform1_delete") && !is_array($Row)){
            $this->setWarning('MSG303');
            return false;
        }
        
        return true;
    }

    //追加処理
    function &getInsertSubformModel() {
        //入力チェック
        if ($this->validateInputSubform()){
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
    function &insert2() {
        knjz291Query::getInsert2Query($this->subField2, $this);
    }

    //更新処理
    function &getUpdateSubformModel() {
        //入力チェック
        if ($this->validateInputSubform()){
            $this->update2();
            $this->setMessage('MSG201');
        }    
    }
    //更新
    function &update2() {
        knjz291Query::getUpdate2Query($this);
        if (!isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteSubformModel() {
        //入力チェック
        if ($this->validateInputSubform()){
            $this->delete2();
            $this->setMessage('MSG201');
        }
    }
    //削除
    function &delete2() {
        $result = knjz291Query::getDelete2Query($this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    /**
     * 前年度コピー(職員詳細データ)のモデルを返します
    */
    function &getCopyModel()
    {
        $result = knjz291Query::getCopyDetail($this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
    }

    function &getExecModel()
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
            if (move_uploaded_file($this->file_2['tmp_name'], $filename) == FALSE){
                $this->warning = "顔写真の取込失敗です。";
            } else {
                $this->message = "顔写真の取込正常です。";
            }
        }
    }


}
?>
