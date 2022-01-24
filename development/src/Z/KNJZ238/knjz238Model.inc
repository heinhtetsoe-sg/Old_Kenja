<?php

require_once('for_php7.php');

class knjz238Model extends Model {
    var $field      = array();
    var $cmd;
    var $special_group_cd;
    var $subclasscd;

    function knjz238Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SPECIAL_GROUP_CD"   =>  VARS::post("SPECIAL_GROUP_CD"),
                             "SPECIAL_GROUP_NAME" =>  VARS::post("SPECIAL_GROUP_NAME"),
                             "SPECIAL_GROUP_ABBV" =>  VARS::post("SPECIAL_GROUP_ABBV"),
                             "SUBCLASSCD"         =>  VARS::post("SUBCLASSCD"),
                             "MINUTES"            =>  VARS::post("MINUTES")
                             );

        $this->cmd          = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //教育課程対応 SCHOOL_KIND、CURRICULUM_CDの値セット
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->field["CLASSCD"] = substr($this->field["SUBCLASSCD"],0,2);
            $this->field["SCHOOL_KIND"] = substr($this->field["SUBCLASSCD"],3,1);
            $this->field["CURRICULUM_CD"] = substr($this->field["SUBCLASSCD"],5,1);
        }

        //教育課程対応 キーをリセット
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::request("cmd") == '') {
                unset($this->special_group_cd);
                unset($this->subclasscd);
            }
            if (VARS::post("cmd") == "list") {
                unset($this->special_group_cd);
                unset($this->subclasscd);
            }
        }
        
        if (VARS::get("SEND_FLG") == "1"){
            $this->special_group_cd = VARS::get("SPECIAL_GROUP_CD");
            $this->subclasscd       = VARS::get("SUBCLASSCD");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd       = VARS::get("CLASSCD");
                $this->school_kind   = VARS::get("SCHOOL_KIND");
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
        } else {
            unset($this->grade);
            unset($this->hr_class);
        }
        
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::get("cmd") == "edit" || $this->cmd == "reset"){
                $this->field["CLASSCD"]         = $this->classcd;
                $this->field["SCHOOL_KIND"]     = $this->school_kind;
                $this->field["CURRICULUM_CD"]   = $this->curriculum_cd;
                $this->field["SUBCLASSCD"]      = $this->subclasscd;

                /*$this->field["SPECIAL_GROUP_CD"]   = $special_group_cd;
                $this->field["SPECIAL_GROUP_NAME"] = $special_group_cd;
                $this->field["SPECIAL_GROUP_ABBV"] = $special_group_cd;
                $this->field["MINUTES"]            = $special_group_cd;*/
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "add" &&
            (trim($this->field["SPECIAL_GROUP_CD"]) == "" ||
             trim($this->field["SUBCLASSCD"]) == ""
            )) {
            $this->setWarning("MSG301");
            return false;
        }
        //キーの更新チェック
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if ($this->cmd == "update" && ($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"] || $this->subclasscd != substr($this->field["SUBCLASSCD"],7,6))) {
                $this->setWarning("MSG308");
                return false;
            }
        } else {
            if ($this->cmd == "update" && ($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"] || $this->subclasscd != $this->field["SUBCLASSCD"])) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        if ($this->cmd == "update" && (!strlen($this->special_group_cd) || (!strlen($this->subclasscd)))) {
            $this->setWarning("MSG308");
            return false;
        }
        //頭ゼロ埋め
        if (strlen($this->field["SPECIAL_GROUP_CD"]) <= 3) {
            $this->field["SPECIAL_GROUP_CD"] = sprintf("%03d", $this->field["SPECIAL_GROUP_CD"]);
        }
        //DATのチェック (UPDATE)
        $db = Query::dbCheckOut();
        $query = knjz238Query::getAttendSubclassSpecialDat($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //DATのチェック (INSERT)
        $db = Query::dbCheckOut();
        $query = knjz238Query::getAttendSubclassSpecialDat_insert($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (strlen($this->field["SPECIAL_GROUP_CD"]) > 3 ) {
                $this->setWarning("MSG915", "(特活グループコード) 3バイトまでです。");
                return false;
            } elseif (strlen($this->field["SPECIAL_GROUP_NAME"]) > 60 ) {
                $this->setWarning("MSG915", "(特活グループ名称) 60バイトまでです。");
                return false;
            } elseif (strlen($this->field["SPECIAL_GROUP_ABBV"]) > 9 ) {
                $this->setWarning("MSG915", "(特活グループ名称) 9バイトまでです。");
                return false;
            } elseif (strlen(substr($this->field["SUBCLASSCD"],7,6)) > 6 ) {
                $this->setWarning("MSG915", "(科目コード) 6バイトまでです。");
                return false;
            } elseif (strlen($this->field["MINUTES"]) > 3 ) {
                $this->setWarning("MSG915", "(時間(分)) 3バイトまでです。");
                return false;
            }
        } else {
            if (strlen($this->field["SPECIAL_GROUP_CD"]) > 3 ) {
                $this->setWarning("MSG915", "(特活グループコード) 3バイトまでです。");
                return false;
            } elseif (strlen($this->field["SPECIAL_GROUP_NAME"]) > 60 ) {
                $this->setWarning("MSG915", "(特活グループ名称) 60バイトまでです。");
                return false;
            } elseif (strlen($this->field["SPECIAL_GROUP_ABBV"]) > 9 ) {
                $this->setWarning("MSG915", "(特活グループ名称) 9バイトまでです。");
                return false;
            } elseif (strlen($this->field["SUBCLASSCD"]) > 6 ) {
                $this->setWarning("MSG915", "(科目コード) 6バイトまでです。");
                return false;
            } elseif (strlen($this->field["MINUTES"]) > 3 ) {
                $this->setWarning("MSG915", "(時間(分)) 3バイトまでです。");
                return false;
            }
        }

        //前年度のデータをコピーの時
        if($this->cmd =="copy"){
            /* コピー元(term2年)にデータがなければ */
            /* アラートを出して処理を中断させる    */
            $db = Query::dbCheckOut();
            $year = CTRL_YEAR - 1;
            $query = knjz238Query::cntAttendSubclassSpecialDat($this, $year);
            $count = $db->getOne($query);
            if ($count == 0) {
                $this->setMessage("MSG203", "更新対象がありませんでした。");
                return false;
            }
            $year = CTRL_YEAR;
            $query = knjz238Query::cntAttendSubclassSpecialDat($this, $year);
            $count = $db->getOne($query);
            if($count > "0"){
                $this->setMessage("MSG203", "既にデータが存在します。");
                return false;
            }
            Query::dbCheckIn($db);
        }
        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->special_group_cd = $this->field["SPECIAL_GROUP_CD"];
                $this->subclasscd       = substr($this->field["SUBCLASSCD"], 7, 6);
            } else {
                $this->special_group_cd = $this->field["SPECIAL_GROUP_CD"];
                $this->subclasscd       = $this->field["SUBCLASSCD"];
            }
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->special_group_cd) || !strlen($this->subclasscd)) {
            $this->setWarning("MSG308");
            return false;
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if ($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"] || $this->subclasscd != substr($this->field["SUBCLASSCD"],7,6)) {
                $this->setWarning("MSG308");
                return false;
            }
        } else {
            if ($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"] || $this->subclasscd != $this->field["SUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        //１レコード取得
        $db    = Query::dbCheckOut();
        $query = knjz238Query::getAttendSubclassSpecialDat($this);
        $row   = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        knjz238Query::getDeleteQuery($this);
        unset($this->special_group_cd);
        unset($this->subclasscd);
        $this->setMessage("MSG201");
    }

    /* INSERT */
    function &insert() {
        if ($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"]) {
            $db = Query::dbCheckOut();
            $query = knjz238Query::getAttendSubclassSpecialMst($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (!is_array($row)) {
                knjz238Query::getInsertMstQuery($this);
            }
        }
        if ($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"] || $this->subclasscd != $this->field["SUBCLASSCD"]) {
            knjz238Query::getInsertDatQuery($this);
        }
    }

    /* UPDATE */
    function &update() {
        knjz238Query::getUpdateMstQuery($this);
        knjz238Query::getUpdateDatQuery($this);
    }

    /**********/
    /* コピー */
    /**********/
    //コピーボタン押し時の処理
    function &getInsertCopyModel() {
        //入力チェック
        if($this->validateInput()) {
            knjz238Query::getInsertCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
