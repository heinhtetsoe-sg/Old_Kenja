<?php

require_once('for_php7.php');

class knjb104aModel extends Model
{
    public $cmd;
    public $year;
    public $year2;
    public $semester;
    public $semester2;
    public $gyou;
    public $retu;
    public $exp_year;
    public $exp_semester;
    public $exp_faccd;
    public $exp_chaircd;
    public $exp_facname;
    public $exp_chairname;
    public $notuse;

    public function knjb104aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            $this->year = CTRL_YEAR;
            $this->year2 = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
            $this->semester2 = CTRL_SEMESTER;
            $this->gyou = 7;
            $this->retu = 7;
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->exp_faccd);
            unset($this->exp_chaircd);
            unset($this->exp_facname);
            unset($this->exp_chairname);
            unset($this->notuse);
        } else {
            if (VARS::request("cmd") == 'list') {
                $this->year = VARS::request("YEAR");
                $this->year2 = VARS::request("YEAR2");
                $this->semester = VARS::request("SEMESTER");
                $this->semester2 = VARS::request("SEMESTER2");
            }
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::request("exp_year")!="") {
            $this->exp_year = VARS::request("exp_year");
            if ($this->cmd != 'update') {
                $this->gyou = 7;
                $this->retu = 7;
            }
        }

        if (VARS::request("exp_semester")!="") {
            $this->exp_semester = VARS::request("exp_semester");
        }

        if (VARS::request("exp_faccd")!="") {
            $this->exp_faccd = VARS::request("exp_faccd");
        }

        if (VARS::request("exp_chaircd")!="") {
            $this->exp_chaircd = VARS::request("exp_chaircd");
        }
        if (VARS::request("exp_facname")!="") {
            $this->exp_facname = VARS::request("exp_facname");
        }

        if (VARS::request("exp_chairname")!="") {
            $this->exp_chairname = VARS::request("exp_chairname");
        }
        $this->notuse = array();
        if (VARS::request("notuse")!="") {
            $this->notuse = explode(',', VARS::request("notuse"));
        }

        if ($this->cmd == 'gyouretu') {
            $this->gyou = VARS::post("GYOU");
            $this->retu = VARS::post("RETU");
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjb104aQuery::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //前年度コピー
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knjb104aQuery::hdatCount($this, $this->year, $this->semester));
        Query::dbCheckIn($db);
        //入力チェック
        if ($this->year == '' ||  $this->year2 == '' || $this->semester == '' ||  $this->semester2 == '') {
            $this->setWarning("MSG300", "コピーするためには参照と対象の年度と学期を指定する必要があります。");
            return false;
        } elseif ($this->year == $this->year2 && $this->semester == $this->semester2) {
            $this->setWarning("MSG300", "同年度、同学期のコピーは出来ません。");
            return false;
        } elseif ($cnt > 0) {
            $this->warning="対象年度にデータが存在します。コピーできません。";
            return false;
        } else {
            knjb104aQuery::copyQuery($this, $this->year, $this->year2, $this->semester, $this->semester2);
            $this->setMessage("MSG201");
        }
        return;
    }
}
