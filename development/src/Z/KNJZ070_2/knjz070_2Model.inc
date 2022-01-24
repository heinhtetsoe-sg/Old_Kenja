<?php

require_once('for_php7.php');

//定数定義

class knjz070_2Model extends Model
{
    public $field = array();
    public $cmd;
    public $subclasscd = "";        //primary
    //教育課程用
    public $school_kind;
    public $curriculum_cd;
    public $year_code = "";
    public $schoolName = "";

    //メンバ変数の初期化
    public function knjz070_2Model()
    {
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        $mid ="" ;
        //コードのゼロ埋め
        if (VARS::post("SUBCLASSCD") != "") {
            $mid = sprintf("%04d", VARS::post("SUBCLASSCD"));
        }

        $this->field = array("SUBCLASSCD"         =>  VARS::post("CLASSCD").$mid,             //科目コード
                            //教育課程用
                            "SCHOOL_KIND"         =>  VARS::post("SCHOOL_KIND"),              //学校校種
                            "CURRICULUM_CD"       =>  VARS::post("CURRICULUM_CD"),            //教育課程コード
                            "SUBCLASSNAME"        =>  VARS::post("SUBCLASSNAME"),             //科目名
                            "SUBCLASSABBV"        =>  VARS::post("SUBCLASSABBV"),             //科目略称
                            "SUBCLASSNAME_ENG"    =>  VARS::post("SUBCLASSNAME_ENG"),         //科目名英字
                            "SUBCLASSABBV_ENG"    =>  VARS::post("SUBCLASSABBV_ENG"),         //科目略称英字
                            "SUBCLASSORDERNAME1"  =>  VARS::post("SUBCLASSORDERNAME1"),       //科目名その他１
                            "SUBCLASSORDERNAME2"  =>  VARS::post("SUBCLASSORDERNAME2"),       //科目名その他２
                            "SUBCLASSORDERNAME3"  =>  VARS::post("SUBCLASSORDERNAME3"),       //科目名その他３
                            "SHOWORDER"           =>  VARS::post("SHOWORDER"),                //表示順
                            "SHOWORDER2"          =>  VARS::post("SHOWORDER2"),               //調査書用表示順
                            "SHOWORDER3"          =>  VARS::post("SHOWORDER3"),               //通知表用表示順
                            "SUBCLASSCD2"         =>  VARS::post("SUBCLASSCD2"),              //調査書・指導要録用科目グループコード
                            "SUBCLASSCD3"         =>  VARS::post("SUBCLASSCD3"),              //通知表用科目グループコード
                            "ELECTDIV"            =>  VARS::post("ELECTDIV"),                 //選択
                            "DETAIL001"           =>  VARS::post("DETAIL001"),                //状態区分
                            "UPDATED"             =>  VARS::post("UPDATED"),                  //更新日付
                            "year_code"           =>  VARS::post("year_code")                 //前画面の年度情保持
                            );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("year_code")) {
            unset($this->schoolName);
            unset($this->subclasscd);
            unset($this->schkind);
            unset($this->list_curriculum_cd);
            unset($this->list_classcd);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->school_kind);
                unset($this->curriculum_cd);
            }
        }
        if (VARS::get("mode") == "1") {
            unset($this->subclasscd);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->school_kind);
                unset($this->curriculum_cd);
            }
        }
        if (VARS::get("SUBCLASSCD") != "") {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }

        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::get("SCHOOL_KIND") != "") {
                $this->school_kind   = VARS::get("SCHOOL_KIND");
            }
            if (VARS::get("CURRICULUM_CD") != "") {
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
        }

        //左画面
        if (VARS::post("SCHKIND")) {
            $this->schkind = VARS::post("SCHKIND");
        }
        if (VARS::post("LIST_CURRICULUM_CD")) {
            $this->list_curriculum_cd = VARS::post("LIST_CURRICULUM_CD");
        }
        if (VARS::post("LIST_CLASSCD")) {
            $this->list_classcd = VARS::post("LIST_CLASSCD");
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //１レコード取得
        $row = knjz070_2Query::getRow($this, "", "", $this->field["SUBCLASSCD"]);
        if ($kind == "update" || $kind == "delete") {                 //リストより選択していない
            if ($this->subclasscd == "") {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (strlen($this->field["SUBCLASSCD"]) != strlen($this->subclasscd)) {
                    //キー値が変更されていますかをチェック
                    if (substr($this->field["SUBCLASSCD"], 5, 6) != $this->subclasscd) {
                        $this->setWarning("MSG308", "キー値は変更できません");
                        return false;
                    }
                } else {
                    //キー値が変更されていますかをチェック
                    if ($this->field["SUBCLASSCD"] != $this->subclasscd) {
                        $this->setWarning("MSG308", "キー値は変更できません");
                        return false;
                    }
                }
            } else {
                //キー値が変更されていますかをチェック
                if ($this->field["SUBCLASSCD"] != $this->subclasscd) {
                    $this->setWarning("MSG308", "キー値は変更できません");
                    return false;
                }
            }
        }
        if ($kind == "update" || $kind == "insert") {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (trim(substr($this->field["SUBCLASSCD"], 5, 6)) < 100) {
                    $this->setWarning("MSG301", "(科目コード)");
                    return false;
                }
            } else {
                if (trim($this->field["SUBCLASSCD"]) < 100) {
                    $this->setWarning("MSG301", "(科目コード)");
                    return false;
                }
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918", "(科目コード)");
                return false;
            }
            if (strlen($this->field["SUBCLASSNAME"]) > 105) {
                $this->setWarning("MSG915", "(科目名は105バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSNAME"]) == 0) {
                $this->setWarning("MSG301", "(科目名称)");
                return false;
            } elseif (strlen($this->field["SUBCLASSABBV"]) > 90) {
                $this->setWarning("MSG915", "(科目略称は90バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSABBV"]) > $this->set_maxabbv) {
                $this->setWarning("MSG915", "(科目略称は".$this->set_maxabbv."バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSNAME_ENG"]) > 50) {
                $this->setWarning("MSG915", "(科目名称英字は50バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSABBV_ENG"]) > 20) {
                $this->setWarning("MSG915", "(科目略称英字は20バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME1"]) > 105) {
                $this->setWarning("MSG915", "(調査書用科目名は105バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME2"]) > 105) {
                $this->setWarning("MSG915", "(通知票用科目名は105バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME3"]) > 90) {
                $this->setWarning("MSG915", "(科目名その他３は90バイトまでです)");
                return false;
            } elseif (strlen($this->field["SHOWORDER"]) > 2) {
                $this->setWarning("MSG915", "(表示順)");
                return false;
            } elseif (strlen($this->field["SHOWORDER2"]) > 2) {
                $this->setWarning("MSG915", "(調査書用表示順)");
                return false;
            } elseif (strlen($this->field["SHOWORDER3"]) > 2) {
                $this->setWarning("MSG915", "(通知表用表示順)");
                return false;
            } elseif (strlen($this->field["SUBCLASSCD2"]) > 6) {
                $this->setWarning("MSG915", "(調査書・指導要録用科目グループコード)");
                return false;
            } elseif (strlen($this->field["SUBCLASSCD3"]) > 6) {
                $this->setWarning("MSG915", "(通知表用科目グループコード)");
                return false;
            }

            //コードのゼロ埋め
            if ($this->field["SUBCLASSCD2"] != "" && strlen($this->field["SUBCLASSCD2"]) < 6) {
                $this->field["SUBCLASSCD2"] = sprintf("%06d", $this->field["SUBCLASSCD2"]);
            }
            if ($this->field["SUBCLASSCD3"] != "" && strlen($this->field["SUBCLASSCD3"]) < 6) {
                $this->field["SUBCLASSCD3"] = sprintf("%06d", $this->field["SUBCLASSCD3"]);
            }

            //存在チェック
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $row2 = knjz070_2Query::getRowCheck($this, $this->field["SUBCLASSCD"], $this->field["SUBCLASSCD2"]);
                if ($this->field["SUBCLASSCD2"] != "" && !is_array($row2)) {
                    $this->setWarning("MSG303", "(調査書・指導要録用科目グループコード)");
                    return false;
                }
                $row3 = knjz070_2Query::getRowCheck($this, $this->field["SUBCLASSCD"], $this->field["SUBCLASSCD3"]);
                if ($this->field["SUBCLASSCD3"] != "" && !is_array($row3)) {
                    $this->setWarning("MSG303", "(通知表用科目グループコード)");
                    return false;
                }
            } else {
                $row2 = knjz070_2Query::getRow($this, "", "", $this->field["SUBCLASSCD2"]);
                if ($this->field["SUBCLASSCD2"] != "" && !is_array($row2)) {
                    $this->setWarning("MSG303", "(調査書・指導要録用科目グループコード)");
                    return false;
                }
                $row3 = knjz070_2Query::getRow($this, "", "", $this->field["SUBCLASSCD3"]);
                if ($this->field["SUBCLASSCD3"] != "" && !is_array($row3)) {
                    $this->setWarning("MSG303", "(通知表用科目グループコード)");
                    return false;
                }
            }
        }
        return true;
    }
    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->subclasscd = $this->field["SUBCLASSCD"];
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $result = $this->delete();
            //教育課程対応
            if ($result == true && $this->Properties["useCurriculumcd"] == '1') {
                $this->field["SUBCLASSCD"] = "";
            }
        }
        return;
    }
    public function &delete()
    {
        if (!isset($this->subclasscd)) {
            $this->setWarning("MSG303");
            return false;
        }
        $result = knjz070_2Query::getDeleteQuery($this->field, $this);
        if ($result == true) {
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
            $this->subclasscd ="";
        } else {
            $this->setWarning("MSG919", "年度データに登録されています。");
        }

        if ($this->Properties["useCurriculumcd"] == '1') {
            list($classcd, $schkind, $subclasscd) = explode("-", $this->field["SUBCLASSCD"]);
            $targetSubCd = $classcd ."-".$schkind."-".$this->field["CURRICULUM_CD"]."-".$subclasscd;
        } else {
            $targetSubCd = $this->field["SUBCLASSCD"];
        }
        $chkRsult = $this->checkDeleteSubclass($targetSubCd);// $chkRsult => 0:削除対象の科目が他のテーブルで参照されているかの真偽値, 1:エラーメッセージ
        if ($chkRsult[0]) {
            $this->setWarning("MSG919", $chkRsult[1]);
            return false;
        }

        return $result;
    }
    //科目が別テーブルで参照されているかのチェック
    public function checkDeleteSubclass($subclasscd)
    {
        $db = Query::dbCheckOut();

        $recordTables   = array("RECORD_DAT", "RECORD_SCORE_DAT", "RECORD_SCORE_HIST_DAT");
        $rankTables     = array("RECORD_RANK_DAT", "RECORD_RANK_SDIV_DAT", "RECORD_RANK_FI_SDIV_DAT");
        $studyRecTables = array("SCHREG_STUDYREC_DAT");
        $chairTables    = array("CHAIR_DAT");
        $attendTables   = array("ATTEND_SUBCLASS_DAT");
        $allTables      = array_merge($recordTables, $rankTables, $studyRecTables, $chairTables, $attendTables);

        $query          = knjz070_2Query::getTargetTable($allTables);

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

            $existsCnt = $db->getOne(knjz070_2Query::checkSubclass($this, $tableName, $subclasscd));
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
        $result = knjz070_2Query::getInsertQuery($this->field, $this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz070_2Query::getUpdateQuery($this->field, $this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    public function clean()
    {
    }
}
