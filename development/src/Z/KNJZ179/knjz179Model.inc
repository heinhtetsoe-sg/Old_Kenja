<?php

require_once('for_php7.php');

class knjz179Model extends Model {
    var $fields      = array();
    var $cmd;
    var $hr_class;        //primary
    var $grade;
    var $term;
    var $term2;
    var $control;

    function knjz179Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::request("term") != "") {
            $this->term  = VARS::request("term");
        }
        if ($this->term == "") {
            $this->term = CTRL_YEAR;
        }
        if (VARS::request("term2") != "") {
            $this->term2 = VARS::request("term2");
        }

        $this->fields = array("GRADE"        =>  VARS::post("GRADE"),
                              "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),
                              "GRADE_CD"     =>  VARS::post("GRADE_CD"),
                              "GRADE_NAME1"  =>  VARS::post("GRADE_NAME1"),
                              "GRADE_NAME2"  =>  VARS::post("GRADE_NAME2"),
                              "GRADE_NAME3"  =>  VARS::post("GRADE_NAME3"),
                              "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        if ($this->cmd == "") {
            unset($this->term2);
            unset($this->term);
        }
        $temp = VARS::get("HR_CLASS");
        if (isset($temp)){
            $this->hr_class = VARS::get("HR_CLASS");
        }
        $temp = VARS::get("GRADE");
        if (isset($temp)){
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("init") == "1"){
            unset($this->grade);
            unset($this->hr_class);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "add" &&
            (trim($this->fields["GRADE"])       == "" ||
             trim($this->fields["SCHOOL_KIND"]) == "" ||
             trim($this->fields["GRADE_CD"])    == "" ||
             trim($this->fields["GRADE_NAME1"]) == ""
            )) {
            $this->setWarning("MSG301");
            return false;
        }
        //キーの更新チェック
        if ($this->cmd == "update" && $this->grade != $this->fields["GRADE"]) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && (!strlen($this->grade))) {
            $this->setWarning("MSG308");
            return false;
        }
        //頭ゼロ埋め
        if (strlen($this->fields["GRADE"]) == 1 ) {
            $this->fields["GRADE"] = sprintf("%02d", $this->fields["GRADE"]);
        }
        if (strlen($this->fields["GRADE_CD"]) == 1 ) {
            $this->fields["GRADE_CD"] = sprintf("%02d", $this->fields["GRADE_CD"]);
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjz179Query::getSchregRegdGdat($this->term, $this->fields["GRADE"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(学年)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->fields["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->fields["GRADE"]) > 2 ) {
            $this->setWarning("MSG915", "(学年) 2バイトまでです。");
            return false;
        } elseif (strlen($this->fields["SCHOOL_KIND"]) > 2 ) {
            $this->setWarning("MSG915", "(学校区分) 2バイトまでです。");
            return false;
        } elseif (strlen($this->fields["GRADE_CD"]) > 2 ) {
            $this->setWarning("MSG915", "(学年略称コード) 2バイトまでです。");
            return false;
        } elseif (strlen($this->fields["GRADE_NAME1"]) > 60 ) {
            $this->setWarning("MSG915", "(学年名称1) 60バイトまでです。");
            return false;
        } elseif (strlen($this->fields["GRADE_NAME2"]) > 60 ) {
            $this->setWarning("MSG915", "(学年名称2) 60バイトまでです。");
            return false;
        } elseif (strlen($this->fields["GRADE_NAME3"]) > 60 ) {
            $this->setWarning("MSG915", "(学年名称3) 60バイトまでです。");
            return false;
        }
        //○○年度のデータをコピーの時
        if($this->cmd =="copy"){
            if ($this->term == $this->term2) {
                $this->setWarning("MSG203", "同年度のコピーは出来ません。");
                return false;
            }
            /* コピー元(term2年)にデータがなければ */
            /* アラートを出して処理を中断させる    */
            $count = knjz179Query::cntSchregRegdGdatTerm2($this->term2, $this);
            if ($count == 0) {
                $this->setMessage("MSG203", "更新対象がありませんでした。");
                return false;
            }
            $count = knjz179Query::getCount($this->term, $this);
            if($count > "0"){
                $this->setMessage("MSG203", "既にデータが存在します。");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->grade = $this->fields["GRADE"];
            $this->hr_class = $this->fields["HR_CLASS"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->grade)) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->grade != $this->fields["GRADE"]) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjz179Query::getSchregRegdGdat($this->term, $this->fields["GRADE"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        knjz179Query::getDeleteQuery($this);
        unset($this->grade);
        $this->setMessage("MSG201");
    }

    /**********/
    /* INSERT */
    /**********/
    function &insert() {
        $result = knjz179Query::getInsertQuery($this);
        return $result;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &update() {
        $result = knjz179Query::getUpdateQuery($this);
        return $result;
    }

    /**********/
    /* コピー */
    /**********/
    //コピーボタン押し時の処理
    function &getInsertCopyModel() {
        //入力チェック
        if($this->validateInput()) {
            knjz179Query::getInsertCopyQuery($this->term2, $this->term, $this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
