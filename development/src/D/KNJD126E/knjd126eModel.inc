<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd126eModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;
    var $select;

    var $nyuryoku;
    var $type_div;


    /**
     * メンバ変数の初期化
     */
    function knjd126eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->select);
            unset($this->nyuryoku);
            unset($this->type_div);
        }

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイル
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期（観点データ用）
                             "SEMESTER2"        =>  VARS::post("SEMESTER2"),        //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD")        //科目
                            );

        if ($this->cmd == "update" || $this->cmd == "form2_update") {
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
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
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

        knjd126eQuery::update($this);
        knjd126eQuery::updateRecord($this);
        $this->setMessage("MSG201");
    }
}
?>
