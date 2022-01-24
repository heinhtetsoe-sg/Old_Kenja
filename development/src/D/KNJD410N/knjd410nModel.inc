<?php

require_once('for_php7.php');

class knjd410nModel extends Model
{
    public $cmd;
    public $ctrl;
    public $year;
    public $semester;
    public $regdSemester;
    public $gakubu_school_kind = "";
    public $condition = "";
    public $groupcd = "";
    public $hukusiki_radio = "";
    public $hr_class = "";
    public $hr_class2 = "";
    public $selectdata;    //左リスト
    public $selectdataLabel;    //右リスト
    public $selectschno;

    public function knjd410nModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->semester);
            unset($this->regdSemester);
            unset($this->gakubu_school_kind);
            unset($this->condition);
            unset($this->groupcd);
            unset($this->hukusiki_radio);
            unset($this->hr_class);
            unset($this->hr_class2);
            $this->field = array();
        }
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
            if (VARS::post("SEMESTER") != "") {
                $this->semester = VARS::request("SEMESTER");
                $this->regdSemester = VARS::request("SEMESTER");
            }
        } else {
            $this->semester = "9";
            $this->regdSemester = CTRL_SEMESTER;
        }
        if (VARS::request("GAKUBU_SCHOOL_KIND") != "") {
            $this->gakubu_school_kind = VARS::request("GAKUBU_SCHOOL_KIND");
        }
        if ($this->Properties["useSpecial_Support_School"] == '1') {
            if (VARS::post("HUKUSIKI_RADIO") != "") {
                $this->hukusiki_radio = VARS::request("HUKUSIKI_RADIO");
            }
        }
        if (VARS::post("HR_CLASS") != "") {
            $this->hr_class = VARS::request("HR_CLASS");
        }
        if (VARS::post("HR_CLASS2") != "") {
            $this->hr_class2 = VARS::request("HR_CLASS2");
        }
        if (VARS::get("CONDITION") != "") {
            $this->condition = VARS::get("CONDITION");
        }
        if (VARS::get("GROUPCD") != "") {
            $this->groupcd = VARS::get("GROUPCD");
        }

        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataLabel  = VARS::post("selectdataLabel");
        $this->selectschno      = VARS::post("selectschno");
        $this->ghr_cd = VARS::post("GHR_CD"); //左クラス

        $this->cmd = VARS::request("cmd");

        //初期値設定
        $db = Query::dbCheckOut();
        //学部
        $gakubu_school_kind = $db->getOne(knjd410nQuery::getSchoolKind());
        $this->gakubu_school_kind = ($this->gakubu_school_kind) ? $this->gakubu_school_kind : $gakubu_school_kind;
        //年組コンボ切替
        $this->hukusiki_radio = ($this->hukusiki_radio != "") ? $this->hukusiki_radio : 1;
        //年組
        $hr_class = $db->getOne(knjd410nQuery::getHrClass($this));
        $this->hr_class = ($this->hr_class != "") ? $this->hr_class : $hr_class;
        //年組（特別クラス選択時）
        $hr_class2 = $db->getOne(knjd410nQuery::getHrClass2($this));
        $this->hr_class2 = ($this->hr_class2 != "") ? $this->hr_class2 : (($this->hr_class2 == "") ? '00-000' : $hr_class2);
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput($pre_year, $pre_semester)
    {
        $db = Query::dbCheckOut();
        if ($this->cmd == "update") {
            //リストよりデータを選択したか
            if (!strlen($this->condition) || !strlen($this->groupcd)) {
                $this->setWarning("MSG308", "");
                return false;
            }
            //選択した学籍番号が既に登録れているかチェック
            $query = knjd410nQuery::getSchregData($this);
            $result = $db->query($query);
            $conma = "";
            $getSchregNo = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $getSchregNo .= $conma.$row["SCHREGNO"];
                $conma = ",";
            }
            $result->free();
            
            if ($getSchregNo) {
                $this->setWarning("MSG302", "学籍番号(".$getSchregNo.")");
                return false;
            }
        } elseif ($this->cmd == "copy") {
            //存在チェック（指定年度） -- GRADE_KIND_COMP_GROUP_YMST
            $cnt_comp_this = $db->getOne(knjd410nQuery::checkCompGroupYMst($this->year, $this->semester));
            if ($cnt_comp_this == 0) {
                $this->setWarning("MSG305");
                return false;
            }

            //存在チェック（前年度） -- GRADE_KIND_SCHREG_GROUP_DAT
            $cnt_schreg_pre = $db->getOne(knjd410nQuery::checkSchregGroupDat($pre_year, $pre_semester));
            if ($cnt_schreg_pre == 0) {
                $this->setWarning("MSG917", "対象データが存在しません。");
                return false;
            }

            //存在チェック（指定年度） -- GRADE_KIND_SCHREG_GROUP_DAT
            $cnt_schreg_this = $db->getOne(knjd410nQuery::checkSchregGroupDat($this->year, $this->semester));
            if ($cnt_schreg_this > 0) {
                if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
                    $warning = "指定年度・学期にデータが存在します。";
                } else {
                    $warning = "指定年度にデータが存在します。";
                }
                $this->setWarning("MSG917", $warning);
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput("", "")) {
            knjd410nQuery::updateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        //MAX学期取得
        $maxSem = array();
        $result = $db->query(knjd410nQuery::getMaxSemester());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $maxSem[$row["YEAR"]] = $row["MAX_SEMESTER"];
        }
        Query::dbCheckIn($db);
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            //前年度・学期設定
            if (intval($this->semester - 1) > 0) {
                $pre_year     = $this->year;
                $pre_semester = intval($this->semester - 1);
            } else {
                $pre_year     = intval($this->year - 1);
                $pre_semester = $maxSem[$pre_year];
            }
        } else {
            $pre_year     = intval($this->year - 1);
            $pre_semester = "9";
        }

        if ($this->validateInput($pre_year, $pre_semester)) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knjd410nQuery::copyCompGroupDat($this, $pre_year, $pre_semester));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjd410nQuery::copyInsertQuery($this, $row));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
