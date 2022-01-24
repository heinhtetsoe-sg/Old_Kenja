<?php

require_once('for_php7.php');


class knjz060_2Model extends Model
{
    public $field      = array();
    public $cmd;
    public $classcd = "";        //primary
    //教育課程対応
    public $school_kind = "";

    public function knjz060_2Model()
    {
    }

    public function init()
    {
        $this->field = array("CLASSCD"          =>  VARS::post("CLASSCD"),
                             //教育課程用
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),           //学校種別
                             
                             "CLASSNAME"        =>  VARS::post("CLASSNAME"),
                             "CLASSABBV"        =>  VARS::post("CLASSABBV"),
                             "CLASSNAME_ENG"    =>  VARS::post("CLASSNAME_ENG"),
                             "CLASSABBV_ENG"    =>  VARS::post("CLASSABBV_ENG"),
                             "CLASSORDERNAME1"  =>  VARS::post("CLASSORDERNAME1"),
                             "CLASSORDERNAME2"  =>  VARS::post("CLASSORDERNAME2"),
                             "CLASSORDERNAME3"  =>  VARS::post("CLASSORDERNAME3"),
                             "SUBCLASSES"       =>  VARS::post("SUBCLASSES"),
                             "SHOWORDER"        =>  VARS::post("SHOWORDER"),
                             "SHOWORDER2"       =>  VARS::post("SHOWORDER2"),
                             "SHOWORDER3"       =>  VARS::post("SHOWORDER3"),
                             "SHOWORDER4"       =>  VARS::post("SHOWORDER4"),
                             "ELECTDIV"         =>  VARS::post("ELECTDIV"),
                             "SPECIALDIV"       =>  VARS::post("SPECIALDIV"),
                             "SEQ001"           =>  VARS::post("SEQ001"),
                             "SEQ001_REMARK2"   =>  VARS::post("SEQ001_REMARK2"),        //状態区分
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        if (VARS::post("SCHKIND")) {
            $this->schkind = VARS::post("SCHKIND");
        }

        $this->cmd             = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        if (VARS::get("mode") == "1") {
            unset($this->classcd);
            unset($this->schkind);
        }
        $temp = VARS::get("CLASSCD");
        if (isset($temp)) {
            $this->classcd      = VARS::get("CLASSCD");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1' || $this->Properties["use_prg_schoolkind"] == "1") {
                $this->school_kind   = VARS::get("SCHOOL_KIND");
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //キーのチェック
        if (trim($this->field["CLASSCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(教科コード)");
            return false;
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            //キーのチェック
            if (trim($this->field["SCHOOL_KIND"]) == "" && $this->cmd == "add") {
                $this->setWarning("MSG301", "(学校校種)");
                return false;
            }
        }
        if ($this->classcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->classcd != $this->field["CLASSCD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if ($this->school_kind == "" && $this->cmd == "update") {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->cmd == "update" && $this->field["SCHOOL_KIND"] != $this->school_kind) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
        }
        //教科コードのゼロ埋め
        if (strlen($this->field["CLASSCD"]) < 2) {
            $this->field["CLASSCD"] = sprintf("%02d", $this->field["CLASSCD"]);
        }
        //１レコード取得
        $row = knjz060_2Query::getRow($this->field["CLASSCD"], $this, $this->field["SCHOOL_KIND"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(教科コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["CLASSNAME"]) > 90) {
            $this->setWarning("MSG915", "(教科名) 90バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSABBV"]) > 90) {
            $this->setWarning("MSG915", "(教科略称) 90バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSABBV"]) > $this->set_maxabbv) {
            $this->setWarning("MSG915", "(教科略称) ".$this->set_maxabbv."バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSNAME_ENG"]) > 40) {
            $this->setWarning("MSG915", "(教科名英字) 40バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSABBV_ENG"]) > 30) {
            $this->setWarning("MSG915", "(教科略称英字) 30バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSORDERNAME1"]) > 60) {
            $this->setWarning("MSG915", "(調査書用教科名) 60バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSORDERNAME2"]) > 60) {
            $this->setWarning("MSG915", "(教科名その他２) 60バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSORDERNAME3"]) > 60) {
            $this->setWarning("MSG915", "(教科名その他３) 60バイトまで");
            return false;
        } elseif (strlen($this->field["SUBCLASSES"]) > 2) {
            $this->setWarning("MSG915", "(科目数) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER"]) > 2) {
            $this->setWarning("MSG915", "(表示順) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER2"]) > 2) {
            $this->setWarning("MSG915", "(調査書用表示順) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER3"]) > 2) {
            $this->setWarning("MSG915", "(通知表用表示順) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER4"]) > 2) {
            $this->setWarning("MSG915", "(成績一覧用表示順) 2バイトまで");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            //教育課程用追加
            $this->classcd = $this->field["CLASSCD"];
            if ($this->Properties["useCurriculumcd"] == '1' || $this->Properties["use_prg_schoolkind"] == "1") {
                $this->schkind = ($this->schkind != "99") ? $this->field["SCHOOL_KIND"]: "99";
            }
            $this->setMessage("MSG201");
        }
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->classcd = $this->field["CLASSCD"];
            //教育課程用追加
            if ($this->Properties["useCurriculumcd"] == '1' || $this->Properties["use_prg_schoolkind"] == "1") {
                $this->school_kind = $this->field["SCHOOL_KIND"];
                $this->schkind = ($this->schkind != "99") ? $this->field["SCHOOL_KIND"]: "99";
            }
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->classcd == "") {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if ($this->classcd != $this->field["CLASSCD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            //キーの更新チェック
            if ($this->school_kind == "") {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["SCHOOL_KIND"] != $this->school_kind) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
        }
        $row = knjz060_2Query::getRow($this->classcd, $this, $this->school_kind);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        if (knjz060_2Query::IsExistingSubclass($this->classcd, $this, $this->school_kind)) {
            $this->setWarning("MSG919", "科目登録がされています。");
            return;
        }
        if (knjz060_2Query::IsExisting($this->classcd, $this, $this->school_kind)) {
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }

        $targetClsCd = $this->field["CLASSCD"] ."-". $this->field["SCHOOL_KIND"];
        $chkRsult = $this->checkDeleteClsCd($targetClsCd);// $chkRsult => 0:削除対象の教科が他のテーブルで参照されているかの真偽値, 1:エラーメッセージ
        if ($chkRsult[0]) {
            $this->setWarning("MSG919", $chkRsult[1]);
            return false;
        }

        knjz060_2Query::getDeleteQuery($this->field, $this, $this->school_kind);
        if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
            knjz060_2Query::detailDeleteQuery($this->field, $this, $this->school_kind, "001");
        }
        //$this->classcd="";
        if ($this->Properties["useCurriculumcd"] == '1' || $this->Properties["use_prg_schoolkind"] == "1") {
            $this->schkind = ($this->schkind != "99") ? $this->field["SCHOOL_KIND"]: "99";
        }
        $this->setMessage("MSG201");
    }

    //教科が別テーブルで参照されているかのチェック
    public function checkDeleteClsCd($classcd)
    {
        $db = Query::dbCheckOut();

        $recordTables   = array("RECORD_DAT", "RECORD_SCORE_DAT", "RECORD_SCORE_HIST_DAT");
        $rankTables     = array("RECORD_RANK_DAT", "RECORD_RANK_SDIV_DAT", "RECORD_RANK_FI_SDIV_DAT");
        $studyRecTables = array("SCHREG_STUDYREC_DAT");
        $chairTables    = array("CHAIR_DAT");
        $attendTables   = array("ATTEND_SUBCLASS_DAT");
        $allTables      = array_merge($recordTables, $rankTables, $studyRecTables, $chairTables, $attendTables);

        $query          = knjz060_2Query::getTargetTable($allTables);

        $existFlg = false;
        $checkArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tableName = $row["TABLE_NAME"];
            $typeSort =
            $typeName = "";
            if (in_array($tableName, $recordTables)) {
                $typeSort = 1;
                $typeName = "成績";
            }
            if (in_array($tableName, $rankTables)) {
                $typeSort = 2;
                $typeName = "序列後の成績";
            }
            if (in_array($tableName, $studyRecTables)) {
                $typeSort = 3;
                $typeName = "指導要録用成績";
            }
            if (in_array($tableName, $chairTables)) {
                $typeSort = 4;
                $typeName = "講座";
            }
            if (in_array($tableName, $attendTables)) {
                $typeSort = 5;
                $typeName = "科目別出欠";
            }

            $existsCnt = $db->getOne(knjz060_2Query::checkClsCd($this, $tableName, $classcd));
            $existFlg = ($existsCnt > 0) ? true : $existFlg;
            if ($existsCnt > 0) {
                $checkArray[$typeSort] =  $typeName;
            }
        }
        $result->free();

        $msg = "\\r\\n";
        for ($i = 1; $i <= 5; $i++) {
            if (!isset($checkArray[$i])) {
                continue;
            }

            $typeName = $checkArray[$i];
            if ($typeName) {
                $msg .= "{$typeName}データに登録されています。";
                $msg .= "\\r\\n";
            }
        }
        Query::dbCheckIn($db);

        return array($existFlg, $msg);
    }

    public function &insert()
    {
        knjz060_2Query::getInsertQuery($this->field, $this, $this->school_kind);
        if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
            $this->detailDelIns("001");
        }
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        knjz060_2Query::getUpdateQuery($this->field, $this, $this->school_kind);
        if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
            $this->detailDelIns("001");
        }
    }

    //DETAIL_MST更新
    public function &detailDelIns($classSeq)
    {
        knjz060_2Query::detailDeleteQuery($this->field, $this, $this->school_kind, $classSeq);
        if ($this->field["SEQ".$classSeq]) {
            knjz060_2Query::detailInsertQuery($this->field, $this, $this->school_kind, $classSeq);
        }
    }

    //変数をクリアします
    public function clean()
    {
    }
}
