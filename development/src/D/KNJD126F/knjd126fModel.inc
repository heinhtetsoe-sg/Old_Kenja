<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd126fModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;
    var $gen_ed = "900100";        //総合学習コード
    var $select;
    var $nyuryoku;
    var $type_div;

    /**
     * メンバ変数の初期化
     */
    function knjd126fModel()
    {
        $this->field = array();
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

        $this->cmd     = VARS::request("cmd");

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
            $this->field["SEMESTER"] = VARS::post("H_SEMESTER");
            $this->field["CLASSCD"] = VARS::post("H_CLASSCD");
            $this->field["CHAIRCD_SUBCLASS"] = VARS::post("H_CHAIRCD");
        }

        //入力方法選択
        if (VARS::post("SELECT")) {
            $this->select = VARS::post("SELECT");
        }

        //入力選択
        if (VARS::post("NYURYOKU")) {
            $this->nyuryoku = VARS::post("NYURYOKU");
        }

        //入力値選択
        if (VARS::post("TYPE_DIV")) {
            $this->type_div = VARS::post("TYPE_DIV");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        /*  更新処理 */

        //初期化
        $this->fields = array();
        for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {

            foreach ($this->data["STATUS"] as $code => $col) {

                $term_name = "STATUS".$code."-".$i;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {

                    $this->fields["STATUS".$code][$i] = $post_term;

                }
            }
        }

        knjd126fQuery::update($this);
        knjd126fQuery::updateRecord($this);
        $this->setMessage("MSG201");
        $this->cmd = ($this->select == "1") ? "form1" : "form2";
    }
}
?>
