<?php

require_once('for_php7.php');

class knja139dModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
	var $pattern_cd;

    function knja139dModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

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
        for ($i=0; $i < get_count($this->pattern_cd); $i++) {
            $this->fields[$i]["REMARK"] = VARS::post("REMARK_".$i);
        }

        //選択コース
        $this->field["COURSE_SELECTED"] = array();
        if (VARS::post("COURSE_SELECTED")) {
            $this->field["COURSE_SELECTED"] = explode(",", VARS::post("COURSE_SELECTED"));
        }

        //選択科目
        $this->field["SUBCLASS_SELECTED"] = array();
        if (VARS::post("SUBCLASS_SELECTED")) {
            $this->field["SUBCLASS_SELECTED"] = explode(",", VARS::post("SUBCLASS_SELECTED"));
        }

    }

    //入力項目チェック
    function validateInput() {

        if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();

            //事前チェック（指定年度） -- SCHREG_REGD_GDAT
            $grade_this = $db->getOne(knja139dQuery::checkSchregRegdGdat());
            if ($grade_this == 0) {
                $this->setWarning("MSG305", "（学年）");
                return false;
            }
            //存在チェック（処理年度） -- HTRAINREMARK_TEMP_DAT
            $this_year = CTRL_YEAR;
            $cnt_this = $db->getOne(knja139dQuery::checkHtrainremarkTempDat($this, $this_year, "cnt"));
            if ($cnt_this > 0) {
                $this->setWarning("MSG917","処理年度にデータが存在します。");
                return false;
            }
            //存在チェック（前年度） -- HTRAINREMARK_TEMP_DAT
            $pre_year = CTRL_YEAR - 1;
            $cnt_pre = $db->getOne(knja139dQuery::checkHtrainremarkTempDat($this, $pre_year, "cnt"));
            if ($cnt_pre == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
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
            if (get_count($this->pattern_cd) <= 0) {
                $this->setWarning("MSG304", "（項目数）");
                return false;
            }

            if ($this->cmd == "update") {
                //文字数チェック
                for ($i = 0; $i < get_count($this->pattern_cd); $i++) {
                    if ($this->validate_row_cnt($this->fields[$i]["REMARK"], (55 * 2)) > 3) {
                        $this->setWarning("MSG915","( 3行までです )");
                        return false;
                    }
                }

                //科目が選択されている場合、コースの選択は必須
                if (get_count($this->field["SUBCLASS_SELECTED"]) > 0) {
                    if (get_count($this->field["COURSE_SELECTED"]) <= 0) {
                        $this->setWarning("MSG304", "（コース）");
                        return false;
                    }
                }
                //コースが選択されている場合、科目の選択は必須
                if (get_count($this->field["COURSE_SELECTED"]) > 0) {
                    if (get_count($this->field["SUBCLASS_SELECTED"]) <= 0) {
                        $this->setWarning("MSG304", "（科目）");
                        return false;
                    }
                }

                //登録済みチェック
                $db = Query::dbCheckOut();
                $htrainremarkArray = array();
                //学期コース毎定型文取得
                $query = knja139dQuery::getHtrainremarkTempDataCheck($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["DATA_DIV"] != $row["DATA_DIV"]) {
                        $key  = $row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
                        $key .= "-".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                        $htrainremarkArray[$key] = $row;
                    }
                }
                $result->free();
                Query::dbCheckIn($db);
                $duplicatCheck = false;
                $duplicatMsg = "";

                for ($i=0; $i < get_count($this->field["COURSE_SELECTED"]); $i++) { 
                    for ($j=0; $j < get_count($this->field["SUBCLASS_SELECTED"]); $j++) { 
                        $key  = $this->field["COURSE_SELECTED"][$i];
                        $key .= "-".$this->field["SUBCLASS_SELECTED"][$j];

                        if (isset($htrainremarkArray[$key])) {
                            // $courseName  = "(".$htrainremarkArray[$key]["COURSECD"].$htrainremarkArray[$key]["MAJORCD"].")";
                            // $courseName .= " ".$htrainremarkArray[$key]["COURSENAME"].$htrainremarkArray[$key]["MAJORNAME"];
                            // $courseName .= "(".$htrainremarkArray[$key]["COURSECODE"].") ".$htrainremarkArray[$key]["COURSECODENAME"];
                            $courseName = " ".$htrainremarkArray[$key]["COURSENAME"].$htrainremarkArray[$key]["MAJORNAME"]." ".$htrainremarkArray[$key]["COURSECODENAME"];

                            // $subclassName = $htrainremarkArray[$key]["CLASSCD"]."-".$htrainremarkArray[$key]["SCHOOL_KIND"];
                            // $subclassName .= "-".$htrainremarkArray[$key]["CURRICULUM_CD"]."-".$htrainremarkArray[$key]["SUBCLASSCD"];
                            // $subclassName .= " ".$htrainremarkArray[$key]["SUBCLASSNAME"];
                            $subclassName = " ".$htrainremarkArray[$key]["SUBCLASSNAME"];

                            $duplicatMsg .= "【種別：{$htrainremarkArray[$key]["DATA_DIV"]}】{$courseName}：{$subclassName}"."\\r\\n";
                            $duplicatCheck = true;
                        }
                    }
                }

                if ($duplicatCheck) {
                    $this->setWarning("MSG302", $duplicatMsg);
                    return false;
                }
            }
        }

        return true;
    }

    //更新用モデル
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knja139dQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //削除用モデル
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knja139dQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();

            //前年度のデータ取得 -- HTRAINREMARK_TEMP_DAT
            $pre_year = CTRL_YEAR - 1;
            $result = $db->query(knja139dQuery::checkHtrainremarkTempDat($this, $pre_year, "list"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー -- HTRAINREMARK_TEMP_DAT
                $db->query(knja139dQuery::copyUpdateQuery($row));
            }
            $result->free();

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
