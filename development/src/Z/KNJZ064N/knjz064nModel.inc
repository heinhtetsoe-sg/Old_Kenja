<?php

require_once('for_php7.php');

class knjz064nModel extends Model
{
    public $cmd;
    public $ctrl;
    public $year;
    public $semester;
    public $regdSemester;
    public $gakubu_school_kind = "";
    public $hukusiki_radio = "";
    public $hr_class = "";
    public $hr_class2 = "";
    public $condition = "";
    public $groupcd = "";
    public $field;
    public $selectdata = "";

    public function knjz064nModel()
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
            unset($this->hukusiki_radio);
            unset($this->hr_class);
            unset($this->hr_class2);
            unset($this->condition);
            unset($this->groupcd);
            $this->field = array();
        }
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if ($this->Properties["useGradeKindCompGroupSemester"] == '1') {
            if (VARS::post("SEMESTER") != "") {
                $this->semester = VARS::request("SEMESTER");
                $this->regdSemester = VARS::request("SEMESTER");
            }
        } else {
            $this->semester = "9";
            $this->regdSemester = CTRL_SEMESTER;
        }
        if (VARS::post("GAKUBU_SCHOOL_KIND") != "") {
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

        $this->field = array("CONDITION"            =>  VARS::post("CONDITION"),        //状態区分
                             "GROUPCD"              =>  (VARS::post("GROUPCD") == "") ? "" : sprintf("%04d", VARS::post("GROUPCD")),     //グループコード
                             "GROUPNAME"            =>  VARS::post("GROUPNAME"),        //グループ名
                             "GROUPABBV"            =>  VARS::post("GROUPABBV"),        //グループ名略称
                             "GUIDANCE_PATTERN"     =>  VARS::post("GUIDANCE_PATTERN"), //指導計画帳票パターン
                            );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");

        //初期値設定
        $db = Query::dbCheckOut();
        //学部
        $gakubu_school_kind = $db->getOne(knjz064nQuery::getSchoolKind());
        $this->gakubu_school_kind = ($this->gakubu_school_kind) ? $this->gakubu_school_kind : $gakubu_school_kind;
        //年組コンボ切替
        $this->hukusiki_radio = ($this->hukusiki_radio != "") ? $this->hukusiki_radio : 1;
        //年組
        $hr_class = $db->getOne(knjz064nQuery::getHrClass($this));
        $this->hr_class = ($this->hr_class != "") ? $this->hr_class : $hr_class;
        //年組（特別クラス選択時）
        $hr_class2 = $db->getOne(knjz064nQuery::getHrClass2($this));
        $this->hr_class2 = ($this->hr_class2 != "") ? $this->hr_class2 : (($this->hr_class2 == "") ? '00-000' : $hr_class2);
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();
        if ($this->cmd == "update") {
            //必須入力チェック
            if (!$this->gakubu_school_kind || $this->hr_class == "" || $this->hr_class2 == "" || !$this->field["CONDITION"] || !$this->field["GROUPCD"] || !$this->field["GROUPNAME"]) {
                $this->setWarning("MSG304");
                $this->groupcd = "";
                return false;
            }
            //文字数チェック
            if (strlen($this->field["GROUPCD"]) > 4) {
                $this->setWarning("MSG915", "(グループコード) 4バイトまで");
                return false;
            } elseif (strlen($this->field["GROUPNAME"]) > 90) {
                $this->setWarning("MSG915", "(名称) 90バイトまで");
                return false;
            } elseif (strlen($this->field["GROUPABBV"]) > 90) {
                $this->setWarning("MSG915", "(略称) 90バイトまで");
                return false;
            }
            //更新チェック
            if ($this->groupcd != $this->field["GROUPCD"] || $this->condition != $this->field["CONDITION"]) {
                $getData = $db->getOne(knjz064nQuery::getList($this, $this->field["CONDITION"], $this->field["GROUPCD"]));
                if ($getData > 0) {
                    $this->setWarning("MSG308");
                    $this->groupcd = "";
                    return false;
                }
            }
        } elseif ($this->cmd == "copy") {
            //存在チェック（前年度）
            $cnt_preYM = $db->getOne(knjz064nQuery::checkCompGroupYMst($this, intval($this->year - 1)));
            $cnt_preD  = $db->getOne(knjz064nQuery::checkCompGroupDat($this, intval($this->year - 1)));
            if ($cnt_preYM == 0 || $cnt_preD == 0) {
                $this->setWarning("MSG917", "対象データが存在しません。");
                return false;
            }

            //存在チェック（指定年度）
            $cnt_thisYM = $db->getOne(knjz064nQuery::checkCompGroupYMst($this, $this->year));
            $cnt_thisD  = $db->getOne(knjz064nQuery::checkCompGroupDat($this, $this->year));
            if ($cnt_thisYM > 0 || $cnt_thisD > 0) {
                $this->setWarning("MSG917", "指定年度にデータが存在します。");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            //指導計画帳票パターンコンボ作成(プロパティが立っていたら非表示なので、他画面(KNJD417)で設定したDB設定値を取得)
            if ($this->Properties["unuseEduPlan_Group_GuidancePattern"] == "1") {
                $db = Query::dbCheckOut();
                //既存の登録データを取得
                $query = knjz064nQuery::getFixGuidancePattern($this);
                $this->field["GUIDANCE_PATTERN"] = $db->getOne($query);
                if ($this->field["GUIDANCE_PATTERN"] == "") {
                    //既存データが無ければ、他の類似データ(校種+状態区分が一致するデータ)から取得
                    $query = knjz064nQuery::getOtherGuidancePattern($this);
                    $this->field["GUIDANCE_PATTERN"] = $db->getOne($query);
                }
                Query::dbCheckIn($db);
            }
            knjz064nQuery::updateQuery($this);
            //更新時にグループコードをセット
            if ($this->cmd != "delete") {
                $this->condition = $this->field["CONDITION"];
                $this->groupcd = $this->field["GROUPCD"];
            } else {
                $this->field = array();
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    public function &getCopyModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $key = "";
            //前年度のデータ取得
            $query = knjz064nQuery::copyCompGroupDat($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($key !== $row["SEMESTER"].'-'.$row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"].'-'.$row["GROUPCD"]) {
                    //コピー    -- GRADE_KIND_COMP_GROUP_YMST
                    $db->query(knjz064nQuery::copyInsertQueryYMst($this, $row));
                }
                //コピー    -- GRADE_KIND_COMP_GROUP_DAT
                $db->query(knjz064nQuery::copyInsertQueryDat($this, $row));

                $key = $row["SEMESTER"].'-'.$row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"].'-'.$row["GROUPCD"];
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjz064nQuery::updateQuery($this);
            //更新時にグループコードをセット
            if ($this->selectdata) {
                $this->condition = $this->field["CONDITION"];
                $this->groupcd = $this->field["GROUPCD"];
            } else {
                $this->field = array();
            }
            $this->setMessage("MSG201");
        }
        return;
    }
}
