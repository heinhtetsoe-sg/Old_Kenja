<?php

require_once('for_php7.php');

//定数定義

class knjz072Model extends Model
{
    public $field      = array();
    public $cmd;
    public $subclasscd="";        //primary
    //教育課程用
    public $school_kind;
    public $curriculum_cd;
    public $year_code = "";

    //メンバ変数の初期化
    public function knjz072Model()
    {
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
                
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
                            "ELECTDIV"            =>  VARS::post("ELECTDIV"),                 //選択
                            "UPDATED"             =>  VARS::post("UPDATED"),                  //更新日付
                            "year_code"           =>  VARS::post("year_code")                 //前画面の年度情保持
                            );

        $this->cmd             = VARS::request("cmd");
        if (VARS::get("PROGRAMID")) {
            unset($this->subclasscd);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->school_kind);
                unset($this->curriculum_cd);
            }
        }
        if (VARS::get("year_code")) {
            unset($this->subclasscd);
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
        if (VARS::get("SUBCLASSCD")!="") {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::get("SCHOOL_KIND")!="") {
                $this->school_kind   = VARS::get("SCHOOL_KIND");
            }
            if (VARS::get("CURRICULUM_CD")!="") {
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //１レコード取得
        //教育委員会DB
        $row = knjz072Query::getRow($this, "", "", $this->field["SUBCLASSCD"]);
        //教育委員会DB/学校別教育委員会科目外テーブルに科目コードがあるか
        $row_board = knjz072Query::getRowSubclassAnotherDat($this);
        if ($kind=="update" || $kind=="delete") {                 //リストより選択していない
            if ($this->subclasscd=="") {
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
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            //削除不可チェック
            if ($kind=="delete" && is_array($row_board)) {
                $this->setWarning("MSG919", "学校の科目マスタに登録されています。");
                return false;
            }

            if ($kind=="delete") {
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
            }
        }
        if ($kind=="update" || $kind=="insert") {
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
            //重複チェック
            if ($this->cmd == "add" && is_array($row_board)) {
                $this->setWarning("MSG918", "(学校登録の科目コード)");
                return false;
            }
            if (strlen($this->field["SUBCLASSNAME"]) >105) {
                $this->setWarning("MSG915", "(科目名は105バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSNAME"])==0) {
                $this->setWarning("MSG301", "(科目名称)");
                return false;
            } elseif (strlen($this->field["SUBCLASSABBV"]) > 90) {
                $this->setWarning("MSG915", "(科目略称は90バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSABBV"]) > $this->set_maxabbv) {
                $this->setWarning("MSG915", "(科目略称は".$this->set_maxabbv."バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSNAME_ENG"]) >50) {
                $this->setWarning("MSG915", "(科目名称英字は50バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSABBV_ENG"]) >20) {
                $this->setWarning("MSG915", "(科目略称英字は20バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME1"]) >105) {
                $this->setWarning("MSG915", "(調査書用科目名は105バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME2"]) >60) {
                $this->setWarning("MSG915", "(通知票用科目名は60バイトまでです)");
                return false;
            } elseif (strlen($this->field["SUBCLASSORDERNAME3"]) >60) {
                $this->setWarning("MSG915", "(科目名その他３は60バイトまでです)");
                return false;
            } elseif (strlen($this->field["SHOWORDER"]) >2) {
                $this->setWarning("MSG915", "(表示順)");
                return false;
            } elseif (strlen($this->field["SHOWORDER2"]) >2) {
                $this->setWarning("MSG915", "(調査書用表示順)");
                return false;
            } elseif (strlen($this->field["SHOWORDER3"]) >2) {
                $this->setWarning("MSG915", "(通知表用表示順)");
                return false;
            }
        }
        return true;
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

        $query          = knjz072Query::getTargetTable($allTables);

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

            $existsCnt = $db->getOne(knjz072Query::checkSubclass($this, $tableName, $subclasscd));
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
            $this->delete();
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->field["SUBCLASSCD"] = "";
            }
        }
        return;
    }
    public function &delete()
    {
        if (!isset($this->subclasscd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz072Query::getDeleteQuery($this->field, $this);
        if ($result==true) {
            if (!isset($model->warning)) {
                $this->setMessage("MSG201");
            }
            $this->subclasscd ="";
        } else {
            $this->setWarning("MSG919", "年度データに登録されています。");
        }
        return $result;
    }
    public function &insert()
    {
        $result = knjz072Query::getInsertQuery($this->field, $this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz072Query::getUpdateQuery($this->field, $this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        /*        if ($result==true){
                    //更新は成功すると、新しい日付を保留
                    $row = knjz072Query::getRow($this->field["SUBCLASSCD"]);
        //            $this->field["UPDATED"] = $row["UPDATED"];
                } */
        return $result;
    }
    //変数をクリアします
    public function clean()
    {
    }
}
