<?php

require_once('for_php7.php');

class knjz234Model extends Model {

    var $cmd;
    var $chair_group_cd;
    var $resetchair_group_cd;
    var $subclasscd;
    var $reset_subclasscd;
    var $test_cd;
    var $resettest_cd;
    var $chair_group_name;
    var $chairselect;
    //教育課程用
    var $classcd;
    var $school_kind;
    var $curriculum_cd;

    //メンバ変数の初期化
    function knjz234Model()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();

        //初期化
        if (VARS::get("PROGRAMID")) {
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
            unset($this->chairselect);
        }

        $this->chair_group_cd   = VARS::request("CHAIR_GROUP_CD");
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->classcd       = VARS::request("CLASSCD");
            $this->school_kind   = VARS::request("SCHOOL_KIND");
            $this->curriculum_cd = VARS::request("CURRICULUM_CD");
        }
        $this->subclasscd       = VARS::request("SUBCLASSCD");
        $this->test_cd          = VARS::request("TEST_CD");
        $this->chair_group_name = VARS::request("CHAIR_GROUP_NAME");

        //取消用
        if (VARS::get("CHAIR_GROUP_CD")) {
            $this->resetchair_group_cd = VARS::get("CHAIR_GROUP_CD");
            unset($this->chair_group_name);
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
        } else if ($this->cmd == "clear") {
            $this->chair_group_cd   = $this->resetchair_group_cd;
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd       = $this->reset_classcd;
                $this->school_kind   = $this->reset_school_kind;
                $this->curriculum_cd = $this->reset_curriculum_cd;
            }
            $this->subclasscd       = $this->reset_subclasscd;
            $this->test_cd          = $this->resettest_cd;
            unset($this->chair_group_name);
        }
    }

    //データコピー
    function &getCopyModel()
    {
        if (!$this->copyCheck()) {
            return;
        }
        knjz234Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //コピーチェック
    function copyCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $year_sem = $db->getOne(knjz234Query::getMaxYearSem());

        if (0 < $db->getOne(knjz234Query::checkChairGroupM($this, $year_sem))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }

        if (0 == $db->getOne(knjz234Query::checkChairGroupM($this, $year_sem, "LAST_DATA"))) {
            $this->setWarning("コピー対象データがありません。");
            return false;
        }

        if (0 == $db->getOne(knjz234Query::checkChairGroupD($model, $year_sem, "COPY"))) {
            $this->setWarning("コピー対象データがありません。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //追加
    function &getInsertModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->insCheck($db)) {
            return;
        }

        //講座マスタ
        $db->query(knjz234Query::delChairMQuery($this));

        //講座マスタ
        $db->query(knjz234Query::makeInsChairMQuery($this));

        //講座グループ
        knjz234Query::chairGInstQuery($db, $this->chairselect, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //追加チェック
    function insCheck($db)
    {
        $year_sem = CTRL_YEAR.CTRL_SEMESTER;
        if (0 < $db->getOne(knjz234Query::checkChairGroupD($this, $year_sem, "INS"))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->updCheck($db)) {
            return;
        }

        //講座グループ削除
        $db->query(knjz234Query::delChairMQuery($this));
        //講座マスタ
        $db->query(knjz234Query::makeInsChairMQuery($this));

        //講座グループ削除
        $db->query(knjz234Query::delChairGQuery($this));
        //講座グループ新規
        knjz234Query::chairGInstQuery($db, $this->chairselect, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //更新チェック
    function updCheck($db)
    {
        $year_sem = CTRL_YEAR.CTRL_SEMESTER;
        if (0 == $db->getOne(knjz234Query::checkChairGroupD($this, $year_sem, "UPD"))) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //削除
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //グループデータ
        $db->query(knjz234Query::delChairGQuery($this));

        $year_sem = CTRL_YEAR.CTRL_SEMESTER;
        if (0 == $db->getOne(knjz234Query::checkChairGroupD($this, $year_sem, "DEL"))) {
            $db->query(knjz234Query::delChairMQuery($this));
        }

        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }
}
?>
