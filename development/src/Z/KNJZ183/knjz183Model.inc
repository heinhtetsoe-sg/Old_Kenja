<?php

require_once('for_php7.php');

class knjz183Model extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;
    var $control;
    var $grade;
    var $grd_sch_kind;
    var $group_cd;
    var $course_major;

    function knjz183Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->grade);
            unset($this->grd_sch_kind);
            unset($this->group_cd);
            unset($this->course_major);
            unset($this->subclass);
            unset($this->rate);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd      = VARS::request("cmd");
        $this->grade    = VARS::request("GRADE");

        $this->field = array("COURSE_MAJOR"     =>  VARS::post("COURSE_MAJOR"),
                             "GROUP_CD"         =>  VARS::post("GROUP_CD"),
                             "SUBCLASS"         =>  VARS::post("SUBCLASS"),
                             "RATE"             =>  VARS::post("RATE")
                             );

        if (VARS::get("cmd") == "edit") {
            $this->course_major = VARS::get("COURSE_MAJOR");
            $this->group_cd     = VARS::get("GROUP_CD");
            $this->subclass     = VARS::get("SUBCLASS");
            $this->rate         = VARS::get("RATE");
        }

        if (VARS::get("cmd") == "edit" || $this->cmd == "reset") {
            $this->field["COURSE_MAJOR"]= $this->course_major;
            $this->field["GROUP_CD"]    = $this->group_cd;
            $this->field["SUBCLASS"]    = $this->subclass;
            $this->field["RATE"]        = $this->rate;
        }

        if ($this->cmd == "grade") {
            unset($this->course_major);
            unset($this->group_cd);
            unset($this->subclass);
            unset($this->rate);
            unset($this->field);
        }

        //初期値設定
        if (!$this->grd_sch_kind) {
            $db = Query::dbCheckOut();
            $result = $db->query(knjz183Query::getGrade());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->grd_sch_kind[$row["VALUE"]] = $row["SCHOOL_KIND"];
            }
            $result->free();
            Query::dbCheckIn($db);
        }
    }

    //データコピー
    function &getCopyModel() {
        if (!$this->copyCheck()) {
            return;
        }

        knjz183Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //コピーチェック
    function copyCheck() {
        $db = Query::dbCheckOut();

        $lastyear = CTRL_YEAR - 1;

        if (0 < $db->getOne(knjz183Query::checkdata($lastyear))) {
            $this->setWarning('MSG203', "データが既に存在しています。");
            return false;
        }
        if (0 == $db->getOne(knjz183Query::checkdata($lastyear, "LAST_DATA"))) {
            $this->setWarning('MSG203', "コピー対象データがありません。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->grade) == "") {
            $this->setWarning('MSG301', "( 学年 )");
            return false;
        }
        if ($this->schoolkind === 'H') {
            if (trim($this->field["GROUP_CD"]) == "") {
                $this->setWarning('MSG301', "( コースグループ )");
                return false;
            }
        } else {
            if (trim($this->field["COURSE_MAJOR"]) == "") {
                $this->setWarning('MSG301', "( コース )");
                return false;
            }
        }
        if (trim($this->field["SUBCLASS"]) == "") {
            $this->setWarning('MSG301', "( 科目 )");
            return false;
        }

        if ($this->cmd == "insert" || $this->cmd == "update" ) {
            //必須チェック
            if (trim($this->field["RATE"]) == "") {
                $this->setWarning('MSG301', "( 割合 )");
                return false;
            }
            //範囲チェック
            if (strlen($this->field["RATE"]) > 3 || $this->field["RATE"] == 0 || $this->field["RATE"] > 100) {
                $this->setWarning('MSG916', "( 割合 )");
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            //変更チェック
            if ($this->schoolkind === 'H') {
                if ($this->group_cd != $this->field["GROUP_CD"]) {
                    $this->setWarning('MSG308',"( コースグループ )");
                    return false;
                }
            } else {
                if ($this->course_major != $this->field["COURSE_MAJOR"]) {
                    $this->setWarning('MSG308',"( コース )");
                    return false;
                }
            }
            if ($this->subclass != $this->field["SUBCLASS"]) {
                $this->setWarning('MSG308',"( 科目 )");
                return false;
            }
        }

        //データ件数取得
        $db = Query::dbCheckOut();
        $existsCnt = $db->getOne(knjz183Query::getExistsCnt($this));
        Query::dbCheckIn($db);

        //存在チェック
        if ($this->cmd == "insert" && $existsCnt > 0) {
            $this->setWarning('MSG302');
            return false;
        } else if (($this->cmd == "update" || $this->cmd == "delete") && $existsCnt == 0) {
            $this->setWarning('MSG303');
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz183Query::getUpdateQuery($this);

            $this->course_major = $this->field["COURSE_MAJOR"];
            $this->group_cd     = $this->field["GROUP_CD"];
            $this->subclass     = $this->field["SUBCLASS"];
            $this->rate         = $this->field["RATE"];

            $this->setMessage('MSG201');
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz183Query::getDeleteQuery($this);

            unset($this->course_major);
            unset($this->group_cd);
            unset($this->subclass);
            unset($this->rate);
            unset($this->field);

            $this->setMessage('MSG201');
        }
    }
}
?>
