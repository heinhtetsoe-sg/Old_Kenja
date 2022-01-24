<?php
class knje371bModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $field = array();
    public function knje371bModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            $field = array();
            common::GetControlMaster_Fnc($this->control);
            unset($this->schoolCd);
            unset($this->facultyCd);
            unset($this->departmentCd);
            unset($this->subclassAllCd);
            unset($this->grade);
            unset($this->qualifiedCd);
        }

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->year     = CTRL_YEAR;
        $this->lastYear = $this->year - 1;
        $this->semester = CTRL_SEMESTER;

        $this->field = array(//共通
                            "SCHOOL_CD"                =>     VARS::post("SCHOOL_CD"),
                            "FACULTYCD"                =>     VARS::post("FACULTYCD"),
                            "DEPARTMENTCD"             =>     VARS::post("DEPARTMENTCD"),

                            //メイン
                            "COURSE_CONDITION_FLG"     =>     VARS::post("COURSE_CONDITION_FLG"),
                            "SUBCLASS_CONDITION_FLG"   =>     VARS::post("SUBCLASS_CONDITION_FLG"),
                            "QUALIFIED_CONDITION_FLG"  =>     VARS::post("QUALIFIED_CONDITION_FLG"),
                            "SCHOOL_NAME"              =>     VARS::post("SCHOOL_NAME"),
                            "FACULTYNAME"              =>     VARS::post("FACULTYNAME"),
                            "DEPARTMENTNAME"           =>     VARS::post("DEPARTMENTNAME"),

                            //履修コース画面
                            "COURSECODE"               =>     VARS::post("COURSECODE"),

                            //履修科目要件画面
                            "SUBCLASSCD"               =>     VARS::post("SUBCLASSCD"),
                            "GRADE1_FLG"               =>     VARS::post("GRADE1_FLG"),
                            "GRADE2_FLG"               =>     VARS::post("GRADE2_FLG"),
                            "GRADE3_FLG"               =>     VARS::post("GRADE3_FLG"),
                            "COURSECODE2"              =>     VARS::post("COURSECODE2"),
                            "REQUIRED_FLG"             =>     VARS::post("REQUIRED_FLG"),
                            "TRANSFER_FLG"             =>     VARS::post("TRANSFER_FLG"),
                            "COMEBACK_FLG"             =>     VARS::post("COMEBACK_FLG"),

                            //資格要件画面
                            "VALID_S_DATE"             =>     VARS::post("VALID_S_DATE"),
                            "CONDITION_DIV"            =>     VARS::post("CONDITION_DIV"),
                            "QUALIFIED_CD"             =>     VARS::post("QUALIFIED_CD"),
                            "CONDITION_RANK"           =>     VARS::post("CONDITION_RANK"),
                            "CONDITION_SCORE"          =>     VARS::post("CONDITION_SCORE"),
                            );

        //共通
        $temp = VARS::request("SUBCLASS_NUM");
        if (isset($temp)) {
            $this->field["SUBCLASS_NUM"]  = VARS::request("SUBCLASS_NUM");
        }
        $temp = VARS::request("SCHOOL_CD");
        if (isset($temp)) {
            $this->schoolCd       = VARS::request("SCHOOL_CD");
        }
        $temp = VARS::request("FACULTYCD");
        if (isset($temp)) {
            $this->facultyCd      = VARS::request("FACULTYCD");
        }
        $temp = VARS::request("DEPARTMENTCD");
        if (isset($temp)) {
            $this->departmentCd   = VARS::request("DEPARTMENTCD");
        }

        //サブ画面(履修科目要件)
        $temp = VARS::get("SUBCLASSCD");
        if (isset($temp)) {
            $this->subclassAllCd   = VARS::get("SUBCLASSCD");
        }

        //サブ画面(資格要件)
        $temp = VARS::get("QUALIFIED_CD");
        if (isset($temp)) {
            $this->qualifiedCd   = VARS::get("QUALIFIED_CD");
        }
    }

    /**********メイン画面処理*************/

    //更新処理(メイン)
    public function &getUpdateModelMain($kind)
    {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInputMain($db, $kind)) {
            $db->autoCommit(false);

            //削除処理(新規追加の場合はサブテーブルのデータも削除)
            //メイン
            $query = knje371bQuery::getDeleteQueryMain($this->field);
            $db->query($query);

            if ($kind == "insert") {
                //コース
                $this->deleteSub($db, "subCourse");
                //科目
                $this->deleteSub($db, "subSubclass");
                //資格
                $this->deleteSub($db, "subQualified");
            }

            //追加
            //メイン
            $query = knje371bQuery::getInsertQueryMain($this->field);
            $db->query($query);

            $db->commit(); // トランザクションをコミットする。
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
    }

    //削除処理(メイン)
    public function &getDeleteModelMain()
    {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInputMain($db, "delete")) {
            $db->autoCommit(false);

            //サブテーブルから削除
            $this->deleteSub($db, "subCourse"); //コース
            $this->deleteSub($db, "subSubclass"); //科目
            $this->deleteSub($db, "subQualified"); //資格

            //メインテーブル削除
            $query = knje371bQuery::getDeleteQueryMain($this->field);
            $db->query($query);

            $db->commit(); // トランザクションをコミットする。
            $this->setMessage("MSG202");
        }
        Query::dbCheckIn($db);
    }

    //入力項目チェック(メイン)
    public function validateInputMain($db, $kind)
    {
        //必須チェック
        if ($kind != "copy") {
            if ($this->field["SCHOOL_CD"] == "") {
                $this->setWarning("MSG301", "(大学コード)");
                return false;
            } elseif ($this->field["FACULTYCD"] == "") {
                $this->setWarning("MSG301", "(学部コード)");
                return false;
            }
            //コードのゼロ埋め
            if (strlen($this->field["SCHOOL_CD"]) < 8) {
                $this->field["SCHOOL_CD"] = sprintf("%08d", $this->field["SCHOOL_CD"]);
            }
            if (strlen($this->field["FACULTYCD"]) < 3) {
                $this->field["MAJORBANKCD"] = sprintf("%03d", $this->field["FACULTYCD"]);
            }
            if (strlen($this->field["DEPARTMENTCD"]) < 3) {
                $this->field["DEPARTMENTCD"] = sprintf("%03d", $this->field["DEPARTMENTCD"]);
            }
        }

        //キー値変更チェック
        if (($kind == "update" || $kind == "delete") && (!strlen($this->schoolCd) || !strlen($this->facultyCd) || !strlen($this->departmentCd))) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if (($kind == "update" || $kind == "delete") && ($this->schoolCd != $this->field["SCHOOL_CD"] || $this->facultyCd != $this->field["FACULTYCD"] || $this->departmentCd != $this->field["DEPARTMENTCD"])) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }

        //文字数チェック
        if ($kind == "update" || $kind == "insert") {
            if (strlen($this->field["SCHOOL_CD"]) > 8) {
                $this->setWarning("MSG915", "(大学コード) 8バイトまで");
                return false;
            } elseif (strlen($this->field["FACULTYCD"]) > 3) {
                $this->setWarning("MSG915", "(学部コード) 3バイトまで");
                return false;
            } elseif (strlen($this->field["DEPARTMENTCD"]) > 3) {
                $this->setWarning("MSG915", "(学科コード) 3バイトまで");
                return false;
            }
        }

        //１レコード取得
        $query = knje371bQuery::getListMain($this, $this->year, "validate");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($kind == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG302");
            return false;
        }

        if ($kind == "copy") {
            $query = knje371bQuery::getListMain($this, $this->year, "copy");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($row)) {
                $this->setWarning("MSG917", "処理年度にデータが存在します。");
                return false;
            }

            $query = knje371bQuery::getListMain($this, $this->lastYear, "copy");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!is_array($row)) {
                $this->setWarning("MSG917", "対象データが存在しません。");
                return false;
            }
        }

        return true;
    }


    /**********サブ画面処理*************/

    //更新処理(サブ)
    public function &getUpdateModelSub($kind, $subGamen)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $flg = false;
        //入力チェック
        if ($this->validateInputSub($db, $kind, $subGamen)) {
            //更新(DELETE INSERT)
            $this->deleteSub($db, $subGamen);
            $this->insertSub($db, $subGamen);
            $flg = true;
        }
        //必要科目数更新
        if ($subGamen == "subSubclass") {
            $query = knje371bQuery::getUpdateQueryMain($this->field);
            $db->query($query);
            $flg = true;
        }

        if ($flg) {
            $this->setMessage("MSG201");
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //削除処理(サブ)
    public function &getDeleteModelSub($kind, $subGamen)
    {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInputSub($db, "delete", $subGamen)) {
            $db->autoCommit(false);
            //更新(DELETE)
            $this->deleteSub($db, $subGamen);

            $db->commit(); // トランザクションをコミットする。
            $this->setMessage("MSG202");
        }
        Query::dbCheckIn($db);
    }

    //INSERT(サブ)
    public function &insertSub($db, $subGamen)
    {
        //コース
        $query = "";
        if ($subGamen == "subCourse") {
            $query = knje371bQuery::getInsertQuery2($this->field);
            $db->query($query);
        //科目
        } elseif ($subGamen == "subSubclass") {
            $query = knje371bQuery::getInsertQuery3($this->field);
            $db->query($query);
            $this->subclassAllCd = $this->field["SUBCLASSCD"];
        //資格
        } elseif ($subGamen == "subQualified") {
            $query = knje371bQuery::getInsertQuery4($this->field);
            $db->query($query);
            $this->qualifiedCd = $this->field["QUALIFIED_CD"];
        }
    }

    //DELETE(サブ)
    public function &deleteSub($db, $subGamen)
    {
        //コース
        $query = "";
        if ($subGamen == "subCourse") {
            $query = knje371bQuery::getDeleteQuery2($this->field);
        //科目
        } elseif ($subGamen == "subSubclass") {
            $query = knje371bQuery::getDeleteQuery3($this->field);
            $this->subclassAllCd = "";
        //資格
        } elseif ($subGamen == "subQualified") {
            $query = knje371bQuery::getDeleteQuery4($this->field);
            $this->qualifiedCd = "";
        }
        $db->query($query);
    }

    //入力項目チェック(サブ画面)
    public function validateInputSub($db, $kind, $subGamen)
    {
        //コース
        if ($subGamen == "subCourse") {
            return $this->validateInput2($db, $kind);
        //科目
        } elseif ($subGamen == "subSubclass") {
            return $this->validateInput3($db, $kind);
        //資格
        } elseif ($subGamen == "subQualified") {
            return $this->validateInput4($db, $kind);
        }
        return false;
    }

    //入力項目チェック(コース)
    public function validateInput2($db, $kind)
    {
        //必須チェック
        if ($this->field["SCHOOL_CD"] == "") {
            $this->setWarning("MSG301", "(大学コード)");
            return false;
        } elseif ($this->field["FACULTYCD"] == "") {
            $this->setWarning("MSG301", "(学部コード)");
            return false;
        } elseif ($this->field["DEPARTMENTCD"] == "") {
            $this->setWarning("MSG301", "(学科コード)");
            return false;
        } elseif ($this->field["COURSECODE"] == "") {
            $this->setWarning("MSG301", "(履修コース)");
            return false;
        }

        return true;
    }

    //入力項目チェック(科目)
    public function validateInput3($db, $kind)
    {

        //必須チェック
        if ($this->field["SUBCLASSCD"] != "") {
            //キー値変更チェック
            if (($kind == "update" || $kind == "delete") && (!strlen($this->subclassAllCd))) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            if (($kind == "update" || $kind == "delete") && ($this->subclassAllCd != $this->field["SUBCLASSCD"])) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return;
            }

            if (strlen($this->field["SUBCLASS_NUM"]) > 1) {
                $this->setWarning("MSG915", "(必要科目数) 1バイトまで");
                return false;
            }
            //必須チェック
            if ($this->field["GRADE1_FLG"] == "" && $this->field["GRADE2_FLG"] == "" && $this->field["GRADE3_FLG"] == "") {
                $this->setWarning("MSG301", "(対象学年を1つ以上設定してください。)");
                return false;
            }

            //文字サイズチェック
            if (strlen($this->field["SUBCLASS_NUM"]) > 1) {
                $this->setWarning("MSG915", "(必要科目数) 1バイトまで");
                return false;
            }

            //１レコード取得
            $query = knje371bQuery::getList3($this, "validate", $this->field["SUBCLASSCD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($kind == "update" && !is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
            //重複チェック
            if ($kind == "insert" && is_array($row)) {
                $this->setWarning("MSG302");
                return false;
            }

            return true;
        }

        return false;
    }

    //入力項目チェック(資格)
    public function validateInput4($db, $kind)
    {
        //必須チェック
        if ($this->field["VALID_S_DATE"] == "") {
            $this->setWarning("MSG301", "(取得有効開始日付)");
            return false;
        } elseif ($this->field["QUALIFIED_CD"] == "") {
            $this->setWarning("MSG301", "(資格)");
            return false;
        } elseif ($this->field["CONDITION_RANK"] == "") {
            $this->setWarning("MSG301", "(級・段位)");
            return false;
        } elseif ($this->field["CONDITION_SCORE"] == "") {
            $this->setWarning("MSG301", "(得点)");
            return false;
        }

        //キー値変更チェック
        if (($kind == "update" || $kind == "delete") && (!strlen($this->qualifiedCd))) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if (($kind == "update" || $kind == "delete") && ($this->qualifiedCd != $this->field["QUALIFIED_CD"])) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }

        //文字数チェック
        if ($kind == "update" || $kind == "insert") {
            if (strlen($this->field["VALID_S_DATE"]) > 10) {
                $this->setWarning("MSG915", "(取得有効開始日) 10バイトまで");
                return false;
            } elseif (strlen($this->field["CONDITION_SCORE"]) > 3) {
                $this->setWarning("MSG915", "(得点) 3バイトまで");
                return false;
            }
        }

        //１レコード取得
        $query = knje371bQuery::getList4($this, "validate");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($kind == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG302");
            return false;
        }

        return true;
    }

    /**********コピー処理*************/

    //前年度コピー処理
    public function getCopyModelMain()
    {
        $db = Query::dbCheckOut();
        if ($this->validateInputMain($db, "copy")) {
            $db->autoCommit(false);

            /***コピー(メイン)***/
            //カラム取得
            $columns = array();
            $columns = knje371bQuery::getColmun($db, "COLLEGE_RECOMMEND_CONDITION_MAIN_DAT");
            //INSERT
            $query  = " INSERT INTO COLLEGE_RECOMMEND_CONDITION_MAIN_DAT ";
            $query .= " SELECT ";
            $sep = "";
            foreach ($columns as $column) {
                $copyCol = $column;
                if ($column == "YEAR") {
                    $copyCol = "'{$this->year}'";
                } elseif ($column == "REGISTERCD") {
                    $copyCol = "'".STAFFCD."'";
                } elseif ($column == "UPDATED") {
                    $copyCol = "sysdate()";
                }
                $query .= $sep.$copyCol;
                $sep = ",";
            }
            $query .= " FROM ";
            $query .= "     COLLEGE_RECOMMEND_CONDITION_MAIN_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR ='{$this->lastYear}' ";
            $db->query($query);

            /***コピー(サブ)***/
            $subTableArray = array();
            $subTableArray[] = "COLLEGE_RECOMMEND_CONDITION_COURSE_DAT";
            $subTableArray[] = "COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT";
            $subTableArray[] = "COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT";

            foreach ($subTableArray as $subTable) {
                $subColumns = knje371bQuery::getColmun($db, $subTable);
                $this->copySub($db, $subColumns, $subTable);
            }

            $db->commit();
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
        return;
    }


    public function copySub($db, $columns, $subTable)
    {
        //INSERT
        $query  = " INSERT INTO {$subTable} ";
        $query .= " SELECT ";
        $sep = "";
        foreach ($columns as $column) {
            $copyCol = $column;
            if ($column == "YEAR") {
                $copyCol = "'{$this->year}'";
            } elseif ($column == "REGISTERCD") {
                $copyCol = "'".STAFFCD."'";
            } elseif ($column == "UPDATED") {
                $copyCol = "sysdate()";
            }
            $query .= $sep.$copyCol;
            $sep = ",";
        }
        $query .= " FROM ";
        $query .= "     {$subTable} SUB ";
        $query .= " WHERE ";
        $query .= "     YEAR ='{$this->lastYear}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "        SELECT ";
        $query .= "          'X' ";
        $query .= "        FROM ";
        $query .= "          COLLEGE_RECOMMEND_CONDITION_MAIN_DAT MAIN ";
        $query .= "        WHERE ";
        $query .= "          MAIN.YEAR = '{$this->lastYear}' ";
        $query .= "          AND MAIN.SCHOOL_CD = SUB.SCHOOL_CD ";
        $query .= "          AND MAIN.FACULTYCD = SUB.FACULTYCD ";
        $query .= "          AND MAIN.DEPARTMENTCD = SUB.DEPARTMENTCD ";
        $query .= "     ) ";

        $db->query($query);
    }
}
