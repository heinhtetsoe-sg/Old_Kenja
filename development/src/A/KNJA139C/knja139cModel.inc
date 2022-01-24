<?php

require_once('for_php7.php');

class knja139cModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $pattern_cd;

    public function knja139cModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }

        //学期
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        //学年
        $this->field["GRADE"] = VARS::post("GRADE");
        //種別
        $this->field["DATA_DIV"] = VARS::post("DATA_DIV");
        //データ数
        $this->field["DATA_CNT"] = VARS::post("DATA_CNT");

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->pattern_cd); $i++) {
            $this->fields[$i]["REMARK"] = VARS::post("REMARK_".$i);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();

            //事前チェック（指定年度） -- SCHREG_REGD_GDAT
            $grade_this = $db->getOne(knja139cQuery::checkSchregRegdGdat());
            if ($grade_this == 0) {
                $this->setWarning("MSG305", "（学年）");
                return false;
            }
            //存在チェック（処理年度） -- HTRAINREMARK_TEMP_DAT
            $this_year = CTRL_YEAR;
            $cnt_this = $db->getOne(knja139cQuery::checkHtrainremarkTempDat($this, $this_year, "cnt"));
            if ($cnt_this > 0) {
                $this->setWarning("MSG917", "処理年度にデータが存在します。");
                return false;
            }
            //存在チェック（前年度） -- HTRAINREMARK_TEMP_DAT
            $pre_year = CTRL_YEAR - 1;
            $cnt_pre = $db->getOne(knja139cQuery::checkHtrainremarkTempDat($this, $pre_year, "cnt"));
            if ($cnt_pre == 0) {
                $this->setWarning("MSG917", "対象データが存在しません。");
                return false;
            }

            Query::dbCheckIn($db);
        } else {
            //必須入力チェック
            if ($this->field["GRADE"] == "") {
                $this->setWarning("MSG304", "（学年）");
                return false;
            }
            if ($this->field["DATA_DIV"] == "") {
                $this->setWarning("MSG304", "（種別）");
                return false;
            }

            //文字数チェック
            if ($this->cmd == "update") {
                for ($i = 0; $i < get_count($this->pattern_cd); $i++) {
                    if ($this->validate_row_cnt($this->fields[$i]["REMARK"], (55 * 2)) > 3) {
                        $this->setWarning("MSG915", "( 3行までです )");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //更新用モデル
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knja139cQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //削除用モデル
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knja139cQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //コピー処理
    public function &getCopyModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();

            //前年度のデータ取得 -- HTRAINREMARK_TEMP_DAT
            $pre_year = CTRL_YEAR - 1;
            $result = $db->query(knja139cQuery::checkHtrainremarkTempDat($this, $pre_year, "list"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー -- HTRAINREMARK_TEMP_DAT
                $db->query(knja139cQuery::copyUpdateQuery($row));
            }
            $result->free();

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
