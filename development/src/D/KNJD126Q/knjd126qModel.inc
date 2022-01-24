<?php

require_once('for_php7.php');

class knjd126qModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;

    function knjd126qModel() {
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
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期（観点データ用）
                             "SEMESTER2"        =>  VARS::post("SEMESTER2"),        //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD")        //科目
                            );

        if ($this->cmd == "update") {
            $this->field["SEMESTER"]        = VARS::post("H_SEMESTER");
            $this->field["GRADE_HR_CLASS"]  = VARS::post("H_GRADE_HR_CLASS");
            $this->field["SUBCLASSCD"]      = VARS::post("H_SUBCLASSCD");
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

        knjd126qQuery::getUpdateQuery($this);
        knjd126qQuery::getUpdateRecordQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
