<?php

require_once('for_php7.php');

class knjh540Model extends Model {
    var $field      = array();
    var $cmd;
    var $oyear;
    var $ryear;
    var $course_div;
    var $grade;
    var $proficiency_subclass_cd;

    function knjh540Model()
    {
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->ryear    = (VARS::request("RYEAR")) ? VARS::request("RYEAR") : $this->ryear;
        $this->oyear    = (VARS::request("OYEAR")) ? VARS::request("OYEAR") : $this->oyear;

        if (VARS::get("CLICK_DATA")) {
            $val = preg_split("/:/", VARS::get("CLICK_DATA"));
            $this->course_div       = $val[0];
            $this->grade            = $val[1];
            $this->proficiency_subclass_cd = $val[2];
        } else if (in_array($this->cmd, array("edit", "copy", "list", "changeOyear")) &&
                   !isset($this->warning) && !VARS::get("clear")) {
            unset($this->course_div);
            unset($this->grade);
            unset($this->proficiency_subclass_cd);
        }
        $this->field = array("COURSE_DIV"       => VARS::post("COURSE_DIV"),
                             "GRADE"            => VARS::post("GRADE"),
                             "PROFICIENCY_SUBCLASS_CD" => VARS::post("PROFICIENCY_SUBCLASS_CD"),
                             "PERFECT"          => VARS::post("PERFECT"),
                             "PASS_SCORE"       => VARS::post("PASS_SCORE")
                             );

        //初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ryear);
            unset($this->oyear);
        }
    }

    //データコピー
    function &getCopyModel()
    {
        if (!$this->copyCheck()) {
            return;
        }
        knjh540Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //コピーチェック
    function copyCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        if (0 < $db->getOne(knjh540Query::getList($this, "CNT"))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();

            $this->course_div       = $this->field["COURSE_DIV"];
            $this->grade            = $this->field["GRADE"];
            $this->proficiency_subclass_cd = $this->field["PROFICIENCY_SUBCLASS_CD"];

            $this->setMessage("MSG201");
        }
        return;
    }

    function &insert()
    {
        $result = knjh540Query::getInsertQuery($this, $this->field);
        return $result;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();

            $this->course_div       = $this->field["COURSE_DIV"];
            $this->grade            = $this->field["GRADE"];
            $this->proficiency_subclass_cd = $this->field["PROFICIENCY_SUBCLASS_CD"];

            $this->setMessage("MSG201");
        }    
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh540Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $check = array("COURSE_DIV"       => "区分",
                       "GRADE"            => "学年",
                       "PROFICIENCY_SUBCLASS_CD" => "試験科目",
                       "PERFECT"          => "満点"
                       );

        //キーのチェック
        foreach ($this->field as $key => $val) {
            if (trim($val) == "") {
                $this->setWarning("MSG301","(".$check[$key].")");
                return false;
            }
        }

        //１レコード取得
        $row = $db->getRow(knjh540Query::getList($this, "CHECK"));

        if ($this->cmd == "add") {
            //重複チェック
            if (is_array($row)) {
                $this->setWarning("MSG918");
                return false;
            }
        } else {
            if ($this->course_div != $this->field["COURSE_DIV"] ||
                $this->grade != $this->field["GRADE"] ||
                $this->proficiency_subclass_cd != $this->field["PROFICIENCY_SUBCLASS_CD"])
            {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //DELETE
    function &getDeleteModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //キーの更新チェック
        if ($this->proficiency_subclass_cd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->proficiency_subclass_cd != $this->field["PROFICIENCY_SUBCLASS_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = $db->getRow(knjh540Query::getList($this, "CHECK"));

        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        knjh540Query::getDeleteQuery($db, $this, $this->field);
        $this->proficiency_subclass_cd = "";
        $this->setMessage("MSG201");

        //DB切断
        Query::dbCheckIn($db);

    }
}
?>
