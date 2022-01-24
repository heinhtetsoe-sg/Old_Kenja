<?php

require_once('for_php7.php');

class knjb0059Model extends Model {

    var $cmd;
    var $groupcd = "";
    var $ctrl;
    var $field = array();
    var $patterncd = "";
    var $patternname = "";
    var $selectdata = "";
    var $curriculum_cd = "";
    var $subclassArray = array();
    var $year;
    var $gradeCourse;

    function knjb0059Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->gradeCourse);
            unset($this->groupcd);
            unset($this->patternname);
            unset($this->field["PATTERN_CD"]);
            unset($this->field["PATTERN_NAME"]);
        }

        $this->field = array("PATTERN_CD"     => VARS::post("PATTERN_CD"),      //履修コード
                             "PATTERN_NAME"   => VARS::post("PATTERN_NAME"),    //履修名称
                             "selectdata"     => VARS::post("selectdata"),      //履修パターン科目
                             "temp_cd"        => VARS::post("GROUPCD"),         //最初のCD
                             "UPDATED"        => VARS::post("UPDATED")          //更新日付
                            );

        $this->cmd = VARS::request("cmd");

        //年度（初期値）
        $this->year = ($this->year == "") ? CTRL_YEAR + 1 : $this->year;
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::post("YEAR");
        }
        //学年、コース
        if (VARS::post("GRADE_COURSE") != "") {
            $this->gradeCourse = VARS::post("GRADE_COURSE");
        }
        if (VARS::get("PATTERN_CD") != "") {
            $this->groupcd = VARS::get("PATTERN_CD");
            $this->field["PATTERN_CD"] = $this->groupcd;
        }

        if (VARS::get("PATTERN_NAME") != "") {
            $this->patternname = VARS::get("PATTERN_NAME");
            $this->field["PATTERN_NAME"] = $this->patternname;
        }

        $this->updData = array();
        foreach ($this->subclassArray as $key => $subclassData) {
            if (VARS::post("PATTERN_CHK".$subclassData) == "1") {
                $this->updData[$subclassData]["PATTERN_CHK"] = VARS::post("PATTERN_CHK".$subclassData);
            }
        }

        if (VARS::post("cmd") == "edit") {
            if (VARS::post("PATTERN_CD") != "") {
                $this->patterncd   = VARS::post("PATTERN_CD");
            }

            if (VARS::post("PATTERN_NAME") != "") {
                $this->patternname = VARS::post("PATTERN_NAME");
            }

        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind) {

        $cmd = $this->cmd;

        //必須項目
        if ($this->gradeCourse == "") {
            $this->setWarning('MSG301',"(課程学科コース)");
            return false;
        }
        
        //入力コードがない
        if ($this->field["PATTERN_CD"] == "") {
            $this->setWarning('MSG901',"データを入力してください。(履修コード)");
            return false;
        }

        //コードのゼロ埋め
        if (strlen($this->field["PATTERN_CD"]) < 2) {
            $this->pattern_cd = $this->field["PATTERN_CD"] = sprintf("%02d",$this->field["PATTERN_CD"]);
        }

        //リストから選択していない
        if ($this->field["temp_cd"] == "") {
            if ($kind == "delete") {
                $this->setWarning('MSG308');
                return false;
            }
            //新規登録処理
            //$cmd = "add";
        }

        //選択コードと入力コードが異なる
        if ($this->field["temp_cd"] != $this->field["PATTERN_CD"]) {
            if ($kind == "delete") {
                $this->setWarning('MSG308');
                return false;
            }
            //新規登録処理
            //$cmd = "add";
        }

        if ($kind != "delete") {
            $db = Query::dbCheckOut();
            $query = knjb0059Query::getPatternRow($this, $this->field["PATTERN_CD"]);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            //重複チェック
            if ($cmd == "add" && is_array($Row)) {
                $this->setWarning('MSG308');
                return false;
            }

            //チェックデータ有無
            if (get_count($this->updData) == 0) {
                $this->setWarning('科目を選択して下さい。');
                return false;
            }

            //更新日付チェック1
            if ($cmd == "update" && $Row["UPDATED"] == "") {
                $this->setWarning('MSG303');
                return false;
            }

            //履修名称
            if (strlen($this->field["PATTERN_NAME"]) == 0) { 
                $this->setWarning('MSG901',"履修名称を入力してください。");
                return false;
            }

            //バイト数チェック
            foreach (array("PATTERN_NAME"  => array(90,"(商品名称)")) as $key => $val) {
                if (strlen($this->field[$key]) > $val[0]) {
                    $this->setWarning("MSG915",$val[1]); return false;
                }
            }

            $this->cmd = $cmd;
        }

        return true;
    }

    //更新
    function &getUpdateModel() {

        if ($this->validateInput("update")) {
            switch ($this->cmd) {
                //新規登録
                case "add" :
                    knjb0059Query::InsertQuery($this, $this->field);
                    knjb0059Query::InsertDatQuery($this, $this->field, $this->updData);
                    $this->groupcd = $this->field["PATTERN_CD"];
                    break;
                //変更処理
                case "update" :
                    knjb0059Query::UpdateQuery($this, $this->field);
                    knjb0059Query::DeleteDatQuery($this, $this->field["PATTERN_CD"]);
                    knjb0059Query::InsertDatQuery($this, $this->field, $this->updData);
                    break;
            }

            $this->setMessage("MSG201");
        }
    }

    //削除
    function getDeleteModel() {
        if ($this->validateInput("delete")) {
            knjb0059Query::DeleteQuery($this, $this->field);
            knjb0059Query::DeleteDatQuery($this, $this->field["PATTERN_CD"]);
            $this->setMessage("MSG201");
            unset($this->groupcd);
            unset($this->patternname);
            unset($this->field["PATTERN_CD"]);
            unset($this->field["PATTERN_NAME"]);
        }
    }

    //COPY
    function getCopyModel() {
        knjb0059Query::CopyQuery($this);
        $this->setMessage("MSG201");
        unset($this->groupcd);
    }
}
?>
