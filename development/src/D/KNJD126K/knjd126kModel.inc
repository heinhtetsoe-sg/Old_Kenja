<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd126kModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;
    var $select;

    var $nyuryoku;
    var $type_div;

    var $nonVisualViewCd;
    var $setGyou;
    var $setmojicnt;
    
    var $electdiv;

    /**
     * メンバ変数の初期化
     */
    function knjd126kModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->select);
            unset($this->nyuryoku);
            unset($this->type_div);
            unset($this->nonVisualViewCd);
        }

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期（観点データ用）
                             "SEMESTER2"        =>  VARS::post("SEMESTER2"),        //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND")       //校種
                            );

        if ($this->cmd == "update" || $this->cmd == "form2_update") {
            if ($this->Properties["use_prg_schoolkind"] == "1") $this->field["SCHOOL_KIND"] = VARS::post("H_SCHOOL_KIND");
            $this->field["SEMESTER"]        = VARS::post("H_SEMESTER");
            $this->field["GRADE_HR_CLASS"]  = VARS::post("H_GRADE_HR_CLASS");
            $this->field["SUBCLASSCD"]      = VARS::post("H_SUBCLASSCD");
        }

        if (VARS::post("SELECT")) {
            $this->select = VARS::post("SELECT");
        }
        if (VARS::post("NYURYOKU")) {
            $this->nyuryoku = VARS::post("NYURYOKU");
        }
        if (VARS::post("TYPE_DIV")) {
            $this->type_div = VARS::post("TYPE_DIV");
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        
        if (VARS::post("SET_STATUS9_GYOU")) {
            $this->setGyou = VARS::post("SET_STATUS9_GYOU");
        }
        if (VARS::post("SET_STATUS9_MOJICNT")) {
            $this->setmojicnt = VARS::post("SET_STATUS9_MOJICNT");
        }
        if (VARS::post("CODE8INPUT")) {
            $this->code8InputFlg = VARS::post("CODE8INPUT");
        }
        if (VARS::post("ELECTDIV")) {
            $this->electdiv = VARS::post("ELECTDIV");
        }
    }

    //入力項目チェック
    function validateInput() {
        for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["STATUS9"][$i], ($this->setmojicnt * 2)) > $this->setGyou) {
                $this->setWarning("MSG915","(学習内容と様子は全角{$this->setmojicnt}文字{$this->setGyou}行までです)");
                return false;
            }
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {

            foreach ($this->data["STATUS"] as $code => $col) {

                $term_name = "STATUS".$code."-".$i;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {

                    $this->fields["STATUS".$code][$i] = $post_term;

                }
            }
        }
        if (!$this->validateInput()) {
            return;
        }

        knjd126kQuery::update($this);
        knjd126kQuery::updateRecord($this);
        $this->setMessage("MSG201");
    }
}
?>