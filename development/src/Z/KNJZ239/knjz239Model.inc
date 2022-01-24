<?php

require_once('for_php7.php');

class knjz239Model extends Model {
    var $field      = array();
    var $cmd;
    var $year;
    var $grade;
    var $groupcd;
    var $selectdata;

    function knjz239Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("GRADE"        =>  VARS::post("GRADE"),
                             "COURSEMAJOR"  =>  VARS::post("COURSEMAJOR"),
                             "COURSECODE"   =>  VARS::post("COURSECODE"),
                             "GROUPCD"      =>  VARS::post("GROUPCD"),
                             "NAME"         =>  VARS::post("NAME"),
                             "ABBV"         =>  VARS::post("ABBV"),
                             "JOUGEN"       =>  VARS::post("JOUGEN"),
                             "KAGEN"        =>  VARS::post("KAGEN"),
                             "CLASSCD"      =>  VARS::post("CLASSCD"),
                             "SEND_FLG"     =>  VARS::get("SEND_FLG"),
                             "GROUP_SELECT" =>  VARS::post("GROUP_SELECT")
                             );
        //初期値
        $this->field["GROUP_SELECT"] = ($this->field["GROUP_SELECT"] == "") ? "1" : $this->field["GROUP_SELECT"];

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        unset($this->selectdata);

        if (VARS::post("selectdata") != NULL) {
            $this->selectdata = explode(',', VARS::post("selectdata"));
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }

        if ($this->cmd == "reset") {
            $this->field["CLASSCD"] = 'all';
        }

        if ($this->cmd == '') {
            unset($this->year);
            unset($this->grade);
            unset($this->groupcd);
            unset($this->courseMajor);
            unset($this->courseCode);
        }

        if (VARS::get("SEND_FLG") == "1"){
            $this->grade   = VARS::get("GRADE");
            $this->groupcd = VARS::get("GROUPCD");
            $this->courseMajor = VARS::get("COURSEMAJOR");
            $this->courseCode = VARS::get("COURSECODE");
            $this->field["GRADE"] = $this->grade;
            $this->field["COURSEMAJOR"] = $this->courseMajor;
            $this->field["COURSECODE"] = $this->courseCode;
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "add" &&
            (trim($this->field["GRADE"]) == "" ||
             trim($this->field["GROUPCD"]) == ""
            )) {
            $this->setWarning("MSG301"); //入力必須の項目が入力されていません。
            return false;
        }
        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->groupcd != $this->field["GROUPCD"] || $this->grade != $this->field["GRADE"])) {
            $this->setWarning("MSG308"); //更新・削除はリストよりデータを選択してから行ってください。
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && (!strlen($this->groupcd) || (!strlen($this->grade)))) {
            $this->setWarning("MSG308"); //更新・削除はリストよりデータを選択してから行ってください。
            return false;
        }
        //頭ゼロ埋め
        if (strlen($this->field["GROUPCD"]) <= 3) {
            $this->field["GROUPCD"] = sprintf("%03d", $this->field["GROUPCD"]);
        }
        //追加処理不可（GROUPCDが600以上）
        if ($this->cmd == "add" && $this->field["GROUPCD"] >= 600) {
            $this->setWarning("MSG914", "(選択グループコード) 600未満");
            return false;
        }

        //DATのチェック (UPDATE)
        $db = Query::dbCheckOut();
        $query = knjz239Query::getSubclassCompSelectDat($this);

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //更新対象がなければエラー
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303"); //データは存在していません。
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918"); //入力したコードは既に存在します。
            return false;
        }
        if ($this->cmd == "delete") {
            //履修選択科目登録データチェック(SUBCLASS_STD_SELECT_DAT)
            $getCount = $db->getOne(knjz239Query::countSubclassStdSelectDat($this));
            if ($getCount > 0) {
                $this->setWarning("MSG203", "対象の選択グループコードは履修選択科目登録データに登録されているため、削除できません。");
                return false;
            }
            return true;
        }
        Query::dbCheckIn($db);

        //文字数チェック
        if (strlen($this->field["GROUPCD"]) > 3 ) {
            $this->setWarning("MSG915", "(選択グループコード) 3バイトまでです。");
            return false;
        } elseif (strlen($this->field["NAME"]) > 60 ) {
            $this->setWarning("MSG915", "(選択名称) 60バイトまでです。");
            return false;
        } elseif (strlen($this->field["ABBV"]) > 9 ) {
            $this->setWarning("MSG915", "(選択略称名) 9バイトまでです。");
            return false;
        } elseif (strlen($this->field["JOUGEN"]) > 2 ) {
            $this->setWarning("MSG915", "(科目数上限) 2バイトまでです。");
            return false;
        } elseif (strlen($this->field["KAGEN"]) > 2 ) {
            $this->setWarning("MSG915", "(科目数下限) 2バイトまでです。");
            return false;
        }

        //前年度のデータをコピーの時
        if($this->cmd =="copy"){
            /* 前年度にデータがなければ            */
            /* アラートを出して処理を中断させる    */
            $db = Query::dbCheckOut();
            $year = $this->year - 1;
            $query = knjz239Query::cntSubclassCompSelectDat($year);
            $count = $db->getOne($query);
            if ($count == 0) {
                $this->setMessage("MSG203", "更新対象がありませんでした。");
                return false;
            }
            /* 今年度にデータがあれば              */
            /* アラートを出して処理を中断させる    */
            $year = $this->year;
            $query = knjz239Query::cntSubclassCompSelectDat($year);
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
        if ($this->field["GROUPCD"] >= 600) {
            $this->field["GRADE"]       = $this->grade;
            $this->field["COURSEMAJOR"] = $this->courseMajor;
            $this->field["COURSECODE"]  = $this->courseCode;
        }
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->grade   = $this->field["GRADE"];
            $this->groupcd = $this->field["GROUPCD"];
            $this->setMessage("MSG201"); //データを更新しました。
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->grade   = $this->field["GRADE"];
            $this->groupcd = $this->field["GROUPCD"];
            $this->setMessage("MSG201"); //データを更新しました。
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->field["GROUPCD"] >= 600) {
            $this->field["GRADE"]       = $this->grade;
            $this->field["COURSEMAJOR"] = $this->courseMajor;
            $this->field["COURSECODE"]  = $this->courseCode;
        }
        if ($this->validateInput()) {
            $this->delete();
            unset($this->groupcd);
            unset($this->grade);
            unset($this->field);
            unset($this->selectdata);
            $this->setMessage("MSG201");
        }
    }

    /* DELETE */
    function &delete() {
        knjz239Query::getDeleteDatQuery($this);
        knjz239Query::getDeleteMstQuery($this);
    }

    /* INSERT */
    function &insert() {
        knjz239Query::getInsertMstQuery($this);
        foreach ($this->selectdata as $subclasscd) {
            knjz239Query::getInsertDatQuery($this, $subclasscd);
        }
    }

    /* UPDATE */
    function &update() {
        if ($this->field["GROUPCD"] < 600) {
            $this->delete();
            $this->insert();
        } else {
            knjz239Query::getUpdateMstQuery($this);
            $this->field["SEND_FLG"] = '1';
        }
    }

    /**********/
    /* コピー */
    /**********/
    //コピーボタン押し時の処理
    function &getInsertCopyModel() {
        //入力チェック
        if($this->validateInput()) {
            knjz239Query::getInsertCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
