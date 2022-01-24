<?php

require_once('for_php7.php');

class knjd126rModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;

    function knjd126rModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->field);
            unset($this->data);
        }

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティーファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");

        //学期コード
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        $this->field["SEMESTER2"] = VARS::post("SEMESTER2");

        //講座コード
        if($this->field["CLASSCD"] == VARS::post("CLASSCD") && VARS::post("CHAIRCD")){
            $this->field["CHAIRCD_SUBCLASS"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD_SUBCLASS"] = "";
        }

        //科目コード
        $this->field["CLASSCD"] = VARS::post("CLASSCD");
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        if ($this->cmd == "update") {
            if ($this->Properties["use_prg_schoolkind"] == "1") $this->field["SCHOOL_KIND"] = VARS::post("H_SCHOOL_KIND");
            $this->field["SEMESTER"] = VARS::post("H_SEMESTER");
            $this->field["CLASSCD"] = VARS::post("H_CLASSCD");
            $this->field["CHAIRCD_SUBCLASS"] = VARS::post("H_CHAIRCD");
        }

        //初期化
        $this->fields = array();
        if (get_count($this->data["SCORE"])) {
            for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {
                foreach ($this->data["SCORE"] as $code => $col) {
                    $term_name = "SCORE".$code."-".$i;
                    $post_term = VARS::post($term_name);
                    if (isset($post_term)) {
                        $this->fields["SCORE".$code][$i] = $post_term;
                    }
                    if ($code == "9") {
                        $this->fields["SCORE".$code."-HIDDEN"][$i] = VARS::post($term_name."-HIDDEN");
                    }
                }
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {

            foreach ($this->data["SCORE"] as $code => $col) {

                $term_name = "SCORE".$code."-".$i;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {

                    $this->fields["SCORE".$code][$i] = $post_term;

                }
            }
        }

        knjd126rQuery::getUpdateQuery($this);
        knjd126rQuery::getUpdateRecordQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
