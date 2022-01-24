<?php

require_once('for_php7.php');

class knjd141aModel extends Model
{
    public $cmd;
    public $field;
    public $appointed_day;
    public $lesson;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public function knjd141aModel()
    {
        $this->field = array();
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み

        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->field);
        }
        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        //編集対象データを取得
        $this->field["sansyouYear"]           = VARS::post("sansyouYear");
        $this->field["sansyouSemester"]       = VARS::post("sansyouSemester");
        $this->field["sansyouKousa"]          = VARS::post("sansyouKousa");
        $this->field["sansyouKamoku"]         = VARS::post("sansyouKamoku");
        $this->field["taisyouSemester"]       = VARS::post("taisyouSemester");
        $this->field["taisyouKousa"]          = VARS::post("taisyouKousa");
        $this->field["taisyouKamoku"]         = VARS::post("taisyouKamoku");

        if ($this->field["taisyouKousa"] != '') {
            $this->testkindcd = substr($this->field["taisyouKousa"], 0, 2);
            $this->testitemcd = substr($this->field["taisyouKousa"], 2, 2);
            $this->scoreDiv = substr($this->field["taisyouKousa"], 4, 2);
        }
        if ($this->field["taisyouKamoku"] != '') {
            $ary = explode('-', $this->field["taisyouKamoku"]);
            $this->classcd = $ary[0];
            $this->schoolKind = $ary[1];
            $this->curriculumCd = $ary[2];
            $this->subclasscd = $ary[3];
        }
        if ($this->field["sansyouKousa"] != '') {
            $this->sansyouTestkindcd = substr($this->field["sansyouKousa"], 0, 2);
            $this->sansyouTestitemcd = substr($this->field["sansyouKousa"], 2, 2);
            $this->sansyouScoreDiv = substr($this->field["sansyouKousa"], 4, 2);
        }
        if ($this->field["sansyouKamoku"] != '') {
            $ary = explode('-', $this->field["sansyouKamoku"]);
            $this->sansyouClasscd = $ary[0];
            $this->sansyouSchoolKind = $ary[1];
            $this->sansyouCurriculumCd = $ary[2];
            $this->sansyouSubclasscd = $ary[3];
        }

        if (VARS::get("PROGRAMID")) {
            $this->field["taisyouSemester"] = CTRL_SEMESTER;
        }
        $this->chaircds = explode(',', VARS::post("chaircds"));
        $this->maxcnt = intval(VARS::post("maxcnt"));

        $this->data = array();
        for ($i = 0; $i < $this->maxcnt; $i++) {
            $this->data[] =array('CHAIRCD' => $this->chaircds[$i], 'ATTENTION_SCORE' => VARS::post('ATTENTION_SCORE-' . $i));
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        for ($i = 0; $i < get_count($this->data); $i++) {
            $val = $this->data[$i]['ATTENTION_SCORE'];
            if ($val != ''  && (!is_numeric($val) || intval($val)<0 || intval($val)>100)) {
                $this->setWarning("MSG916");
                return false;
            }
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //編集対象データを更新
    public function &update()
    {
        $result = knjd141aQuery::getDeleteQuery($this);
        $result = knjd141aQuery::getUpdateQuery($this);
        return $result;
    }

    //コピー処理
    public function &getCopyModel()
    {
        if ($this->field["sansyouYear"] =='' ||
            $this->field["sansyouSemester"] =='' ||
            $this->field["sansyouKousa"] =='' ||
            $this->field["sansyouKamoku"] ==''
        ) {
            $this->warning = "参照考査が全て選択されていません。";
            return false;
        }
        if ($this->field["taisyouSemester"] =='' ||
            $this->field["taisyouKousa"] =='' ||
            $this->field["taisyouKamoku"] ==''
        ) {
            $this->warning = "対象考査が全て選択されていません。";
            return false;
        }

        $db = Query::dbCheckOut();
        $result = $db->query(knjd141aQuery::selectQuery2($this));
        $flg = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $flg = true;
        }
        if (!$flg) {
            $this->warning = "参照考査に登録データがありません。";
            return false;
        }
        $result = $db->query(knjd141aQuery::selectQuery3($this));
        $flg = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['YEAR'] != '') {
                $flg = true;
            }
        }
        if ($flg) {
            $this->warning = "対象考査に登録データが既に存在します。";
            return false;
        }
        Query::dbCheckIn($db);
        if ($this->copy()) {
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピーメイン処理
    public function &copy()
    {
        $result = knjd141aQuery::getCopyQuery($this);
        return true;
    }
}
