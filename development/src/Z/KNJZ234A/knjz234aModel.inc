<?php

require_once('for_php7.php');

class knjz234aModel extends Model
{
    public $field = array();
    public $cmd;
    public $chair_group_cd;
    public $resetchair_group_cd;
    public $subclasscd;
    public $reset_subclasscd;
    public $test_cd;
    public $resettest_cd;
    public $chair_group_name;
    public $chair_group_abbv;
    public $chairselect;
    //教育課程用
    public $classcd;
    public $school_kind;
    public $curriculum_cd;
    //コースコード、学年
    public $coursename;
    public $course_pk;
    public $coursecode;
    public $coursecd;
    public $majorcd ;
    public $grade ;

    //メンバ変数の初期化
    public function knjz234aModel()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->coursename);
            unset($this->course_pk);
            unset($this->coursecode);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->grade);
            unset($this->sendDiv);

            unset($this->chair_group_cd);
            unset($this->resetchair_group_cd);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->classcd);
                unset($this->school_kind);
                unset($this->curriculum_cd);
            }
            unset($this->subclasscd);
            unset($this->reset_subclasscd);
            unset($this->test_cd);
            unset($this->resettest_cd);
            unset($this->chair_group_name);
            unset($this->chair_group_abbv);
            unset($this->chairselect);

            unset($this->target_seme);
            unset($this->refer_seme);
            unset($this->selectTestCd);
        }

        //コースコンボ用
        if (VARS::post("COURSENAME") != "") {
            $this->coursename     = VARS::post("COURSENAME");
            $this->course_pk      = explode(" ", VARS::post("COURSENAME"));
            $this->coursecode     = $this->course_pk[0];
            $this->coursecd       = $this->course_pk[1];
            $this->majorcd        = $this->course_pk[2];
            $this->grade          = $this->course_pk[3];
        }

        //リンクより取得
        if (VARS::get("COURSECD_SET") !="") {
            $this->coursecd_set   = VARS::get("COURSECD_SET");
            $this->getcourse_pk   = explode(" ", VARS::get("COURSECD_SET"));
            $this->getcoursecode  = $this->getcourse_pk[0];
            $this->getcoursecd    = $this->getcourse_pk[1];
            $this->getmajorcd     = $this->getcourse_pk[2];
            $this->getgrade       = $this->getcourse_pk[3];

            $this->chair_group_cd   = VARS::get("CHAIR_GROUP_CD");
            $this->test_cd          = VARS::get("TEST_CD");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd       = VARS::get("CLASSCD");
                $this->school_kind   = VARS::get("SCHOOL_KIND");
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
            $this->subclasscd       = VARS::get("SUBCLASSCD");
            $this->sendDiv          = VARS::get("sendDiv");
            if ($this->sendDiv === 'ALL') {
                $this->chair_group_cd = "";
            }
        }

        //フィールドより取得
        $this->field = array("CHAIR_GROUP_CD"      =>  VARS::post("CHAIR_GROUP_CD"),
                             "CHAIR_GROUP_NAME"    =>  VARS::post("CHAIR_GROUP_NAME"),
                             "CHAIR_GROUP_ABBV"    =>  VARS::post("CHAIR_GROUP_ABBV"),
                             "TEST_CD"             =>  VARS::post("TEST_CD")
                             );

        if (VARS::request("TEST_CD")) {
            $this->selectTestCd = VARS::request("TEST_CD");
        }
        $this->field['TEST_CD']=$this->selectTestCd;

        if ($this->cmd == 'init') {
            $this->field = array("CHAIR_GROUP_CD"      =>  '',
                                 "CHAIR_GROUP_NAME"    =>  '',
                                 "CHAIR_GROUP_ABBV"    =>  '',
                                 "TEST_CD"             =>  ''
            );
        }

        if ($this->Properties["useKoteiTestCd"] === '1') {
            if ($this->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $this->field['TEST_CD'] = '000000';
            } else {
                $this->field['TEST_CD'] = '0000';
            }
        }

        if (VARS::request("TARGET_SEME")) {
            $this->target_seme = VARS::request("TARGET_SEME");
        }
        if (VARS::post("REFER_SEME")) {
            $this->refer_seme  = VARS::post("REFER_SEME");
        }

        //取消用
        if (VARS::get("CHAIR_GROUP_CD")) {
            $this->resetchair_group_cd = VARS::get("CHAIR_GROUP_CD");
            unset($this->field["CHAIR_GROUP_NAME"]);
            unset($this->field["CHAIR_GROUP_ABBV"]);
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::get("CLASSCD")) {
                $this->reset_classcd = VARS::get("CLASSCD");
            }
            if (VARS::get("SUBCLASSCD")) {
                $this->reset_school_kind = VARS::get("SCHOOL_KIND");
            }
            if (VARS::get("CURRICULUM_CD")) {
                $this->reset_curriculum_cd = VARS::get("CURRICULUM_CD");
            }
        }
        if (VARS::get("SUBCLASSCD")) {
            $this->reset_subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("TEST_CD")) {
            $this->resettest_cd = VARS::get("TEST_CD");
        }

        //更新
        if ($this->cmd == "update" || $this->cmd == "insert") {
            $this->chairselect = VARS::post("chairselect");
        } elseif ($this->cmd == "clear") {
            $this->field["CHAIR_GROUP_CD"] = $this->resetchair_group_cd;
            unset($this->field["CHAIR_GROUP_NAME"]);
            unset($this->field["CHAIR_GROUP_ABBV"]);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd       = $this->reset_classcd;
                $this->school_kind   = $this->reset_school_kind;
                $this->curriculum_cd = $this->reset_curriculum_cd;
            }
            $this->subclasscd       = $this->reset_subclasscd;
            $this->test_cd          = $this->resettest_cd;
            unset($this->chair_group_name);
            unset($this->chair_group_abbv);
        }
    }

    //コピーのモデルを返します
    public function &getCopyModel()
    {
        if ($this->target_seme == $this->refer_seme) {
            $this->setWarning("MSG300", "同学期のコピーは出来ません。");
            return false;
        }
        $cnt = "";
        $cnt = knjz234aQuery::countChairGroupMst($this->target_seme);
        if ($cnt > "0") {
            $this->warning = "対象学期にデータが存在するのでコピーは出来ません。";
            return false;
        }
        knjz234aQuery::getInsertCopyQuery($this);
        $this->setMessage("MSG201");
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if (trim($this->field["CHAIR_GROUP_CD"]) == "") {
            $this->setWarning("MSG301", "(講座グループコード)");
            return false;
        }
        if (trim($this->field["CHAIR_GROUP_NAME"]) == "") {
            $this->setWarning("MSG301", "(講座グループ名称)");
            return false;
        }
        if (trim($this->field["CHAIR_GROUP_ABBV"]) == "") {
            $this->setWarning("MSG301", "(講座グループ略称)");
            return false;
        }
        //テスト種別コードの設定
        if ($this->Properties["useKoteiTestCd"] !== '1') {
            if (trim($this->field["TEST_CD"]) == "") {
                $this->setWarning("MSG301", "(テスト種別)");
                return false;
            }
        }
        //文字数チェック
        if ($this->Properties["useKoteiTestCd"] === '1') {
            if (strlen($this->field["CHAIR_GROUP_CD"]) > 3) {
                $this->setWarning("MSG914", "(講座グループコード) 3バイトまで");
                return false;
            }
            if (sprintf("%06d", $this->field["CHAIR_GROUP_CD"]) === '000000') {
                $this->setWarning("MSG901", "講座グループコードは0より大きい値をセットして下さい。");
                return false;
            }
        } else {
            if (strlen($this->field["CHAIR_GROUP_CD"]) > 6) {
                $this->setWarning("MSG914", "(講座グループコード) 6バイトまで");
                return false;
            }
        }
        if (strlen($this->field["CHAIR_GROUP_NAME"]) > 60) {
            $this->setWarning("MSG914", "(講座グループ名称) 60バイトまで");
            return false;
        }
        if (strlen($this->field["CHAIR_GROUP_ABBV"]) > 60) {
            $this->setWarning("MSG914", "(講座グループ略称) 60バイトまで");
            return false;
        }
        return true;
    }

    //追加
    public function &getInsertModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->insCheck($db)) {
            return;
        }
        //入力チェック
        if ($this->validateInput()) {
            //講座マスタ削除
            $db->query(knjz234aQuery::delChairMQuery($this));
            //講座マスタ
            $db->query(knjz234aQuery::makeInsChairMQuery($this));
            //講座グループ削除
            $db->query(knjz234aQuery::delChairGQuery($this));
            //講座グループ
            knjz234aQuery::chairGInstQuery($db, $this->chairselect, $this);
            //更新後セット
            //テスト種別コードの設定
            if ($this->Properties["useKoteiTestCd"] === '1') {
                $this->chair_group_cd = sprintf("%06d", $this->field["CHAIR_GROUP_CD"]);
            } else {
                $this->chair_group_cd = $this->field["CHAIR_GROUP_CD"];
            }
            $this->test_cd = $this->field["TEST_CD"];
        }

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //追加チェック
    public function insCheck($db)
    {
        $year_sem = CTRL_YEAR.$this->target_seme;
        if (0 < $db->getOne(knjz234aQuery::checkChairGroupD($this, $year_sem, "INS"))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }

        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->updCheck($db)) {
            return;
        }
        //入力チェック
        if ($this->validateInput()) {
            //講座グループ削除
            $db->query(knjz234aQuery::delChairMQuery($this));
            //講座マスタ
            $db->query(knjz234aQuery::makeInsChairMQuery($this));
            //講座グループ削除
            $db->query(knjz234aQuery::delChairGQuery($this));
            //講座グループ新規
            knjz234aQuery::chairGInstQuery($db, $this->chairselect, $this);
            //更新後セット
            //テスト種別コードの設定
            if ($this->Properties["useKoteiTestCd"] === '1') {
                $this->chair_group_cd = sprintf("%06d", $this->field["CHAIR_GROUP_CD"]);
            } else {
                $this->chair_group_cd = $this->field["CHAIR_GROUP_CD"];
            }
            $this->test_cd = $this->field["TEST_CD"];
        }

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //更新チェック
    public function updCheck($db)
    {
        if ($this->chair_group_cd == "") {
            $this->setWarning("MSG303");
            return false;
        }
        $year_sem = CTRL_YEAR.$this->target_seme;
        if (0 == $db->getOne(knjz234aQuery::checkChairGroupD($this, $year_sem, "UPD"))) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //削除
    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->updCheck($db)) {
            return;
        }
        //グループデータ
        $db->query(knjz234aQuery::delChairGQuery($this));

        $year_sem = CTRL_YEAR.$this->target_seme;
        if (0 == $db->getOne(knjz234aQuery::checkChairGroupD($this, $year_sem, "DEL"))) {
            $db->query(knjz234aQuery::delChairMQuery($this));
        }
        //フィールド初期化
        $this->field = array();
        $this->chair_group_cd = "";
        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }
}
