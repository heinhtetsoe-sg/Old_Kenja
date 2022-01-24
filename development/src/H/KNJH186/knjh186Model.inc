<?php

require_once('for_php7.php');
class knjh186Model extends Model {
    var $cmd;
    var $schregno;
    var $name;
    var $date;
    var $field = array();

    function knjh186Model() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->date);
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("CARE_DATE"                =>  VARS::post("CARE_DATE"),
                             "FARE_CD"                  =>  VARS::post("FARE_CD"),
                             "PICK_UP"                  =>  VARS::post("PICK_UP"),
                             "REMARK"                   =>  VARS::post("REMARK"),
                             "EXTRACURRICULAR_CLASS"    =>  VARS::post("EXTRACURRICULAR_CLASS")
                             );

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->date);
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("CARE_DATE") != "") {
            $this->date = VARS::get("CARE_DATE");
        }

        //預かり費用セット
        $db = Query::dbCheckOut();
        $this->fare_array = array();
        $this->fare_array[999]  = "未設定";
        $query = knjh186Query::getFareList();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->fare_array[$row["FARE_CD"]] = number_format($row["FARE"]).'円';
        }
        $result->free();
        Query::dbCheckIn($db);

        //テキスト入力
        $this->txt_array = array();
        $this->txt_array["PICK_UP"]   = array("gyo" => 3, "moji" => 30, "title" => "お迎え");
        $this->txt_array["REMARK"]    = array("gyo" => 3, "moji" => 30, "title" => "備考");
        $this->txt_array["EXTRACURRICULAR_CLASS"] = array("gyo" => 1, "moji" => 30, "title" => "課外教室");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304", "( 園児 )");
            return false;
        }
        if (trim($this->field["CARE_DATE"]) == "") {
            $this->setWarning("MSG301", "( 預かり日付 )");
            return false;
        }
        if ($this->cmd == "update" && ($this->field["FARE_CD"] == "" || $this->field["FARE_CD"] == "999")) {
            $this->setWarning("MSG301", "未設定以外を選択してください。\\n( 金額 )");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $exists = $db->getOne(knjh186Query::getChildcareDat($this, $this->field["CARE_DATE"]));
        Query::dbCheckIn($db);

        if ($exists) {
            //リストから選択したかチェック
            if ($this->field["CARE_DATE"] != $this->date) {
                $this->setWarning("MSG308");
                return false;
            }
        //データ有無チェック（削除）
        } else if ($this->cmd == "delete" && !$exists) {
            $this->setWarning("MSG303");
            return false;
        }

        //文字数チェック
        foreach ($this->txt_array as $key => $val) {
            if ($this->validate_row_cnt($this->field[$key], ($val["moji"] * 2)) > $val["gyo"]) {
                if ($val["gyo"] == 1) {
                    $this->setWarning("MSG915","({$val["title"]}は{$val["moji"]}文字までです)");
                } else {
                    $this->setWarning("MSG915","({$val["title"]}は{$val["gyo"]}行までです)");
                }
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjh186Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
            $this->date = $this->field["CARE_DATE"];
            $this->field = array();
        }
        return;
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjh186Query::getDeleteQuery($this, $this->field);
            $this->setMessage("MSG201");
            $this->field = array();
        }
        return;
    }
}
?>
