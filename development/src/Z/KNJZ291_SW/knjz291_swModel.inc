<?php

require_once('for_php7.php');

class knjz291_swModel extends Model {
    var $field      = array();
    var $subField2  = array();
    var $cmd;
    var $year;
    var $staffcd;
    var $schoolCd;
    var $auth;
    var $prgid;

    function knjz291_swModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->staffcd);

            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSubmit);
        }

        $this->field = array("JOBCD"                =>  VARS::post("JOBCD"),
                             "SECTIONCD"            =>  VARS::post("SECTIONCD"),
                             "DUTYSHARECD"          =>  VARS::post("DUTYSHARECD"),
                             "DUTYSHARECD2"         =>  VARS::post("DUTYSHARECD2"),
                             "CHARGECLASSCD"        =>  VARS::post("CHARGECLASSCD"),
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

        $db = Query::dbCheckOut();
        //学校コード
        $query = knjz291_swQuery::selectSchoolCd($model);
        $this->schoolCd = $db->getOne($query);
         Query::dbCheckIn($db);
    }

   //入力項目チェック
    function validateInput() {
        //キーのチェック
        if ($this->staffcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }

        //１レコード取得
        $db  = Query::dbCheckOut();
        $query = knjz291_swQuery::getRow($this, $this->staffcd);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG203');
            return false;
        }

        //肩書きの重複チェック
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

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //更新
    function &update() {
        knjz291_swQuery::getUpdateQuery($this, $this->field);
        knjz291_swQuery::newDataUpdate($this, $this->field);
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
            $query = knjz291_swQuery::get_cnt_Classcd($this->staffcd,$this->subField2["CLASSCD"], $this);
            $cnt_G_Address = $db->getOne($query);
            if (($this->cmd =="subform1_add") && $cnt_G_Address > 0) {
                $this->setWarning("MSG302","( 同一の教科 )");
                return false;
            }
        }

        //重複エラーチェック
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $query = knjz291_swQuery::getRow2check($this->staffcd, $this->subField2["CLASSCD"], $this->subField2["SDATE"], $this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $query = knjz291_swQuery::getRow2($this->staffcd, $this->subField2["CLASSCD"], $this->subField2["SDATE"], $this);
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
        knjz291_swQuery::getInsert2Query($this->subField2, $this);
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
        knjz291_swQuery::getUpdate2Query($this);
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
        $result = knjz291_swQuery::getDelete2Query($this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }
    // 前年度コピー(職員詳細データ)のモデルを返します
    function &getCopyModel()
    {
        $result = knjz291_swQuery::getCopyDetail($this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
    }

}
?>
