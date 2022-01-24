<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd124jModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;
    var $gen_ed = "900100";        //総合学習コード

    /**
     * メンバ変数の初期化
     */
    function knjd124jModel()
    {
        $this->field = array();
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

    }

    function init()
    {
        $this->cmd     = VARS::request("cmd");
        //学期コード
        $this->field["SEMESTER"] = VARS::post("SEMESTER");

        //科目コード
        $this->field["CLASSCD"] = VARS::post("CLASSCD");
        if ($this->cmd == "update") {
            $this->field["CLASSCD"] = VARS::post("H_CLASSCD");
            $this->field["SEMESTER"] = VARS::post("H_SEMESTER");
        }

        //講座コード
        if($this->cmd!="classcd"&&VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else if ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        }else{
            $this->field["CHAIRCD"] = "";
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

        knjd124jQuery::update($this);
        knjd124jQuery::updateRecord($this);//---2006/03/14
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
?>
