<?php

require_once('for_php7.php');

class knjz391Model extends Model {
    var $cmd;
    var $year;
    var $course_cd;

    function knjz391Model() {}

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->course_cd);
            $this->field = array();
        }

        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }

        $this->field = array("SCHEDULE_CD"      =>  VARS::post("SCHEDULE_CD"),
                             "COURSE_CD"        =>  VARS::post("COURSE_CD") ? sprintf("%02d", VARS::post("COURSE_CD")) : "",
                             "BUS_NAME"         =>  VARS::post("BUS_NAME")
                            );

        if (VARS::get("COURSE_CD") != "") {
            $this->course_cd = VARS::get("COURSE_CD");
        }

        //時間帯コード
        $this->schedule = array();
        $this->schedule[1] = "前半";
        $this->schedule[2] = "後半";

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if ($this->year == "") {
            $this->setWarning("MSG301", "（年度）");
            return false;
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            //必須チェック
            if ($this->field["SCHEDULE_CD"] == "") {
                $this->setWarning("MSG301", "（時間帯）");
                return false;
            }
            if ($this->field["COURSE_CD"] == "") {
                $this->setWarning("MSG301", "（コード）");
                return false;
            }

            if ($this->cmd == "update") {
                if ($this->field["BUS_NAME"] == "") {
                    $this->setWarning("MSG301", "（名称）");
                    return false;
                }

                //文字数チェック
                if (strlen($this->field["COURSE_CD"]) > 2 ) {
                    $this->setWarning("MSG914","(コードは2バイトまでです)");
                    return false;
                }
                if ($this->validate_row_cnt($this->field["BUS_NAME"], (10 * 2)) > 1) {
                    $this->setWarning("MSG915","(名称は10文字までです)");
                    return false;
                }
            }

            //存在チェック
            $db = Query::dbCheckOut();
            $exists = $db->getOne(knjz391Query::getChildcareBusYmst($this, $this->field["COURSE_CD"]));
            Query::dbCheckIn($db);

            if ($exists) {
                //リストから選択したかチェック
                if ($this->field["COURSE_CD"] != $this->course_cd) {
                    $this->setWarning("MSG308", "（コード）");
                    return false;
                }
            //データ有無チェック（削除）
            } else if ($this->cmd == "delete" && !$exists) {
                $this->setWarning("MSG303");
                return false;
            }

        } else if ($this->cmd == "copy") {
            //前年度データ存在チェック
            $db = Query::dbCheckOut();
            $exists = $db->getOne(knjz391Query::getPreYearData($this, "cnt"));
            Query::dbCheckIn($db);

            if ($exists == 0) {
                $this->setWarning("MSG917", "対象データが存在しません。");
                return false;
            }

        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz391Query::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->setCmd("edit");

            $this->course_cd    = $this->field["COURSE_CD"];
        } else {
            $this->setCmd("check");
        }

        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz391Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {

        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knjz391Query::getPreYearData($this, "get"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjz391Query::copyInsertQuery($this, $row));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");

            unset($this->course_cd);
            $this->field = array();
        }
        return;
    }
}
?>
