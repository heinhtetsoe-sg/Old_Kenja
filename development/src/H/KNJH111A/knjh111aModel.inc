<?php

require_once('for_php7.php');

class knjh111aModel extends Model
{
    public $field        = array();
    public $cmd;
    public $schregno;
    public $regddate;      //登録日付
    public $subclasscd;    //科目コード
    public $condition;     //学修・併修
    public $seq;           //回数
    public $name;          //生徒氏名
//※教育課程対応
    public $classcd;

    public function knjh111aModel()
    {
    }

    public function init()
    {
        //クッキー(?)の値をクリアする
        if (!VARS::get("EXP_YEAR") && !VARS::get("REGDDATE") && $this->cmd == "edit") {
            unset($this->regddate);
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("REGDDATE"      =>  str_replace("/", "-", VARS::post("REGDDATE")), //取得日付
                             "CLASSCD"       =>  VARS::post("CLASSCD"),                 //※教育課程対応
                             "SUBCLASSCD"    =>  VARS::post("SUBCLASSCD"),              //科目コード(実際は使っていないが、テーブルにフィールドが存在する為変数としては残している)
                             "CONDITION_DIV" =>  VARS::post("CONDITION_DIV"),           //学修・併修
                             "QUALIFIED_CD"  =>  VARS::post("QUALIFIED_CD"),            //資格コード
                             "RANK"          =>  VARS::post("RANK"),                    //級・段位
                             "PROMOTER"      =>  VARS::post("PROMOTER"),                //主催
                             "CERTIFICATE"   =>  VARS::post("CERTIFICATE"),             //資格証書
                             "CONTENTS"      =>  VARS::post("CONTENTS"),                //資格内容
                             "SEQ"           =>  VARS::post("SEQ"),                     //回数
                             "selectdata"    =>  VARS::post("selectdata"),              //選択した生徒(リストtoリスト)
                             "REMARK"        =>  VARS::post("REMARK"),                  //備考
                             "HOBBY_SCORE"   =>  VARS::post("HOBBY_SCORE"),             //得点
                             "SCHREGNO"      =>  VARS::post("SCHREGNO"),                //学籍番号
                             "UPDATED"       =>  VARS::post("UPDATED"));
        $this->field["check"] = array(VARS::post("RCHECK0"),
                                      VARS::post("RCHECK1"),
                                      VARS::post("RCHECK2"),
                                      VARS::post("RCHECK3"),
                                      VARS::post("RCHECK4"));
        $this->field["check_all"] = VARS::post("RCHECK5");

        $this->cmd = VARS::request("cmd");

        if ($this->cmd != 'qualifiedCd' || $this->cmd != 'conditionDiv') {
            $this->field2 = $this->field;
        }

        if ($this->cmd == 'clear') {
            $this->field = $this->field2;
        }

        if (VARS::get("SCHREGNO")    !="") {
            $this->schregno    = VARS::get("SCHREGNO");
        }
        if (VARS::get("subclasscd")  !="") {
            $this->subclasscd  = VARS::get("subclasscd");
        }
        //※教育課程対応
        if (VARS::get("classcd")!="") {
            $this->classcd = VARS::get("classcd");
        }

        if (VARS::get("condition")       !="") {
            $this->condition   = substr(VARS::get("condition"), 0, 1);
        }
        if (VARS::get("year")            !="") {
            $this->year        = VARS::get("year");
        }
        if (VARS::get("seq")             !="") {
            $this->seq         = VARS::get("seq");
        }
        if (VARS::get("NAME")            !="") {
            $this->name        = VARS::get("NAME");
        }
        if (VARS::get("REGDDATE")        !="") {
            $this->regddate    = VARS::request("REGDDATE");
        }
        if (VARS::get("GRADE")           !="") {
            $this->grade       = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS")        !="") {
            $this->hr_class    = VARS::get("HR_CLASS");
        }
        if (VARS::get("SHIKAKU_CD")      !="") {
            $this->shikakuCd   = VARS::get("SHIKAKU_CD");
        }
        if (VARS::get("TEST_CD")         !="") {
            $this->testCd      = VARS::get("TEST_CD");
        }
        if (VARS::get("MANAGEMENT_FLG")  !="") {
            $this->sendMaFlg   = VARS::get("MANAGEMENT_FLG");
        }

        $this->field["ANNUAL"] = $this->grade;

        //年組コンボが変更されたときキー値を初期化する
        if (VARS::get("init")!="") {
            unset($this->year);
            unset($this->regddate);
            unset($this->subclasscd);
            unset($this->condition);
            unset($this->seq);
            unset($this->schregno);
            unset($this->name);
            //※教育課程対応
            unset($this->classcd);
            //$this->managementFlgが"1"の時使用
            unset($this->shikakuCd);
            unset($this->testCd);
            unset($this->sendMaFlg);
        }

        $this->getPropertiesAll(); //プロパティファイル

        /*********************/
        /* 一括更新処理処理1 */
        /*********************/
        if ($this->cmd == 'replace_update1') {
            $this->replace_data  = array();
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["check"]   = array(VARS::post("RCHECK0"),
                                                   publicS::post("RCHECK1"),
                                                   publicS::post("RCHECK2"),
                                                   publicS::post("RCHECK3"),
                                                   publicS::post("RCHECK4"),
                                                   publicS::post("RCHECK5"));
            $this->replace_data["check_all"] = VARS::post("RCHECK6");
            if (VARS::post("RCHECK0")=="1") {
                $this->replace_data["REGDDATE"]      = VARS::post("REGDDATE");
            }
            if (VARS::post("RCHECK1")=="1") {
                $this->replace_data["CONDITION_DIV"] = VARS::post("CONDITION_DIV");
            }
            if (VARS::post("RCHECK2")=="1") {
                $this->replace_data["QUALIFIED_CD"]  = VARS::post("QUALIFIED_CD");
            }
            if (VARS::post("RCHECK2")=="1") {
                $this->replace_data["RANK"]          = VARS::post("RANK");
            }
            if (VARS::post("RCHECK3")=="1") {
                $this->replace_data["CERTIFICATE"]   = VARS::post("CERTIFICATE");
            }
            if (VARS::post("RCHECK4")=="1") {
                $this->replace_data["HOBBY_SCORE"]   = VARS::post("HOBBY_SCORE");
            }
            if (VARS::post("RCHECK5")=="1") {
                $this->replace_data["REMARK"]        = VARS::post("REMARK");
            }
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if ($this->field["REGDDATE"]=="") {
            if ($kind=="insert") {
                    $this->setWarning("MSG301", "(取得日付)");
            } else {
                    $this->setWarning("MSG308", "(取得日付)");
            }
            return false;
        }
        if ($this->schregno=="") {
            $this->setWarning("MSG304", "(左より生徒を選択してから行ってください)");
            return false;
        }

        if ($this->Properties["useQualifiedManagementFlg"] == '1' && $this->managementFlg == "1") {
            if ($kind == "update" || $kind == "delete") {
                if ($this->sendMaFlg == "") {
                    $this->setWarning("MSG304", "(一覧よりデータを指定してください。)");
                    return false;
                }
                if ($this->regddate == "") {
                    $this->setWarning("MSG308");
                    return false;
                }
                if (str_replace("/", "-", $this->regddate) != $this->field["REGDDATE"]) {
                    $this->setWarning("MSG308", "キー値は変更できません");
                    return;
                }
                if (str_replace("/", "-", $this->shikakuCd) != $this->field["QUALIFIED_CD"]) {
                    $this->setWarning("MSG308", "キー値は変更できません");
                    return;
                }
                if (str_replace("/", "-", $this->testCd) != $this->field["RANK"]) {
                    $this->setWarning("MSG308", "キー値は変更できません");
                    return;
                }
            }

            if ($kind == "update" || $kind == "insert") {
                if (trim($this->field["REGDDATE"]) == "") {
                    $this->setWarning("MSG301", "(取得日付)");
                    return false;
                }
                if (trim($this->field["QUALIFIED_CD"]) == "") {
                    $this->setWarning("MSG301", "(資格名称)");
                    return false;
                }
                if (trim($this->field["RANK"]) == "") {
                    $this->setWarning("MSG301", "(級・段位)");
                    return false;
                }

                //１レコード取得
                $db = Query::dbCheckOut();
                $query = knjh111aQuery::getSchregQualifiedTestDat($this, "insert");
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                Query::dbCheckIn($db);

                if ($kind == "update" && !is_array($row)) {
                    $this->setWarning("MSG303");
                    return false;
                }
                //重複チェック
                if ($kind == "insert" && is_array($row)) {
                    $this->setWarning("MSG918");
                    return false;
                }
            }
        } else {
            if ($kind=="update" || $kind=="delete") {
                if ($this->seq=="") {
                    $this->setWarning("MSG304", "(一覧よりデータを指定してください。)");
                    return false;
                }
                if ($this->regddate == "") {
                    $this->setWarning("MSG308");
                    return false;
                }

                //１レコード取得
                $db = Query::dbCheckOut();
                $query = knjh111aQuery::getSchregQualifiedHobbyDat($this);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                Query::dbCheckIn($db);

                //更新日付チェック1
                if ($this->cmd == "update" && $row["UPDATED"] == "") {
                    $this->setWarning("MSG303");
                    return false;
                }
                //更新日付チェック2
                if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
                    $this->setWarning("MSG302");
                    return false;
                }
            }

            if ($kind=="update" || $kind=="insert") {
                if (trim($this->field["REGDDATE"]) == "") {
                    $this->setWarning("MSG301", "(取得日付)");
                    return false;
                }

                if (strlen($this->field["CONTENTS"]) >90 ) {
                    $this->setWarning("MSG915", "(その他内容は90バイトまでです)");
                    return false;
                }
                if (strlen($this->field["REMARK"]) >90 ) {
                    $this->setWarning("MSG915", "(備考は90バイトまでです)");
                    return false;
                }
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput("update")) {
            $this->seq = $this->update();
        }
    }

    public function &getInsertModel()
    {
        if ($this->validateInput("insert")) {
            $seq = $this->insert();
            $this->regddate   = $this->field["REGDDATE"];
            $this->subclasscd = $this->field["SUBCLASSCD"];
            //※教育課程対応
            $this->classcd = $this->field["CLASSCD"];

            $this->condition  = $this->field["CONDITION_DIV"];
            $this->seq        = $seq;
        }
    }

    public function &getDeleteModel()
    {
        if ($this->validateInput("delete")) {
            $this->delete();
        }
    }

    public function &delete()
    {
        if ($this->regddate=="") {
            $this->setWarning("MSG308");
            return;
        }
        //キー値が変更されているとエラー
        if (str_replace("/", "-", $this->regddate) != $this->field["REGDDATE"] || $this->subclasscd != $this->field["SUBCLASSCD"]) {
            $this->setWarning("MSG308", "取得日付または科目は変更できません");
            return;
        }
        $result = knjh111aQuery::getDeleteQuery($this->regddate, $this->schregno, $this->subclasscd, $this->seq, $this->year, $this);
        $this->setMessage("MSG201");
        $this->field = array();
    }

    //追加
    public function &insert()
    {
        $seq = knjh111aQuery::getInsertQuery($this, $this->field, $this->schregno);
        $this->setMessage("MSG201");
        return $seq;
    }

    //更新
    public function &update()
    {
        $seq = knjh111aQuery::getUpdateQuery($this, $this->field, $this->schregno, $this->seq);
        $this->setMessage("MSG201");
        return $seq;
    }

    /*****************/
    /* 一括更新処理1 */
    /*****************/
    public function &ReplaceModel1()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        $selectdata = explode(",", $this->replace_data["selectdata"]);
        foreach ($selectdata as $val) {
            knjh111aQuery::getReplaceInsertQuery1($this, $val);
        }
        $this->setMessage("MSG201");
    }
}
