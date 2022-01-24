<?php

require_once('for_php7.php');

class knjd126sModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;

    function knjd126sModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->field);
            unset($this->data);
            unset($this->select);
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
        if (VARS::post("SELECT")) {
            $this->select = VARS::post("SELECT");
        }

        //初期化
        $this->fields = array();
        if (get_count($this->data["STATUS"])) {
            for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {
                foreach ($this->data["STATUS"] as $code => $col) {
                    $term_name = "STATUS".$code."-".$i;
                    $post_term = VARS::post($term_name);
                    if (isset($post_term)) {
                        $this->fields["STATUS".$code][$i] = $post_term;
                    }
                    if ($code == "9") {
                        $this->fields["STATUS".$code."-HIDDEN"][$i] = VARS::post($term_name."-HIDDEN");
                    }
                }
            }
        }
    }

    //入力項目チェック
    function validateInput($viewCd, $mark) {
        if (substr($viewCd, 2) != "99" && $this->select == "2" && $mark) {
            if (!is_array($this->viewLevelMarkArray[$viewCd][$mark])) {
                return false;
            }
        } else if (substr($viewCd, 2) == "99") {
            if ($mark && !is_numeric($mark)) {
                return false;
            }
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        $errFlg = false;
        $errMsg = "";
        for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {

            foreach ($this->data["STATUS"] as $code => $col) {

                $term_name = "STATUS".$code."-".$i;
                $post_term = VARS::post($term_name);
                if (!$this->validateInput($col, $post_term)) {
                    $errMsg .= $this->data["SCHREGNO"][$i]." 観点コード：".$col."\\n";
                    $errFlg = true;
                    continue;
                }
                if (isset($post_term)) {

                    $this->fields["STATUS".$code][$i] = $post_term;

                }
            }
        }
        $this->cmd = ($this->select == "1") ? "form1" : "form2";
        if ($errFlg) {
            $this->setWarning($errMsg);
            return false;
        } else {
            knjd126sQuery::getUpdateQuery($this);
            knjd126sQuery::getUpdateRecordQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
