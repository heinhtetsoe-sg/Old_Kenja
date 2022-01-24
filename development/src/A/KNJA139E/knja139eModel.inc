<?php

require_once('for_php7.php');

class knja139eModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $pattern_cd;

    public function knja139eModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }

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
        for ($i=0; $i < get_count($this->pattern_cd); $i++) {
            $this->fields[$i]["REMARK"] = VARS::post("REMARK_".$i);
            $this->fields[$i]["FROM_SCORE"] = VARS::post("FROM_SCORE_".$i);
            $this->fields[$i]["TO_SCORE"] = VARS::post("TO_SCORE_".$i);
        }
    }

    //入力項目チェック
    public function validateInput()
    {

        if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();

            //事前チェック（指定年度） -- SCHREG_REGD_GDAT
            $grade_this = $db->getOne(knja139eQuery::checkSchregRegdGdat());
            if ($grade_this == 0) {
                $this->setWarning("MSG305", "（学年）");
                return false;
            }
            //存在チェック（処理年度） -- HTRAINREMARK_TEMP_SCORE_MST
            $this_year = CTRL_YEAR;
            $cnt_this = $db->getOne(knja139eQuery::checkHtrainremarkTempScoreMst($this, $this_year, "cnt"));
            if ($cnt_this > 0) {
                $this->setWarning("MSG917", "処理年度にデータが存在します。");
                return false;
            }
            //存在チェック（前年度） -- HTRAINREMARK_TEMP_SCORE_MST
            $pre_year = CTRL_YEAR - 1;
            $cnt_pre = $db->getOne(knja139eQuery::checkHtrainremarkTempScoreMst($this, $pre_year, "cnt"));
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

            if ($this->cmd == "update") {
                for ($i = 0; $i < get_count($this->pattern_cd); $i++) {
                    if (!isset($this->fields[$i]["FROM_SCORE"])) {
                        $this->setWarning("MSG301", "下限");
                        return false;
                    }
                    //空かどうか？
                    if ($this->fields[$i]["FROM_SCORE"] == "") {
                        $this->setWarning("MSG301", "下限");
                        return false;
                    }
     
                    //数字かどうか？
                    if (!is_numeric($this->fields[$i]["FROM_SCORE"]) && !$this->fields[$i]["FROM_SCORE"] == 0) {
                        $this->setWarning("MSG907", "下限");
                        return false;
                    }

                    // 0 or 1 or 100 かどうか？
                    if ($this->fields[$i]["FROM_SCORE"] < 0) {
                        $this->setWarning("MSG913");
                        return false;
                    } elseif ($this->fields[$i]["FROM_SCORE"] >= 100) {
                        $this->setWarning("MSG913", "下限は100を超えてはいけません。");
                        return false;
                    }

                    if ($this->fields[$i]["TO_SCORE"] < 0) {
                        $this->setWarning("MSG913");
                        return false;
                    } elseif ($this->fields[$i]["TO_SCORE"] > 100) {
                        $this->setWarning("MSG913", "上限は100までです。");
                        return false;
                    }

                    //値大小チェック
                    $chkLevel = $i + 1;
                    if ($this->field["DATA_CNT"] >= $chkLevel) {
                        if (!($this->fields[$chkLevel]["FROM_SCORE"] < $this->fields[$i]["FROM_SCORE"])) {
                            $this->setWarning("MSG913");
                            return false;
                        }
                    }

                    if ($this->fields[$i]["FROM_SCORE"] > $this->fields[$i]["TO_SCORE"]) {
                        $this->setWarning("MSG913");
                        return false;
                    }

                    //文字数チェック
                    if ($this->validate_row_cnt($this->fields[$i]["REMARK"], (32 * 2)) > 2) {
                        $this->setWarning("MSG915", "( 2行までです )");
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
            knja139eQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //削除用モデル
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knja139eQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //コピー処理
    public function &getCopyModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();

            //前年度のデータ取得 -- HTRAINREMARK_TEMP_SCORE_MST
            $pre_year = CTRL_YEAR - 1;
            $result = $db->query(knja139eQuery::checkHtrainremarkTempScoreMst($this, $pre_year, "list"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー -- HTRAINREMARK_TEMP_SCORE_MST
                $db->query(knja139eQuery::copyUpdateQuery($row));
            }
            $result->free();

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
