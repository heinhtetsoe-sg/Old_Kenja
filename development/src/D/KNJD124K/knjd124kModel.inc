<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd124kModel.inc 65735 2019-02-16 01:40:04Z tawada $

class knjd124kModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;
    var $gen_ed = "900100";        //総合学習コード

    /**
     * メンバ変数の初期化
     */
    function knjd124kModel()
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

        //校種
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");

        //講座コード
        if($this->cmd!="classcd"&&VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        }else{
            $this->field["CHAIRCD"] = "";
        }

        if ($this->cmd == "update") {
            if ($this->Properties["use_prg_schoolkind"] == "1") $this->field["SCHOOL_KIND"] = VARS::post("H_SCHOOL_KIND");
            $this->field["SEMESTER"]    = VARS::post("H_SEMESTER");
            $this->field["CLASSCD"]     = VARS::post("H_CLASSCD");
            $this->field["CHAIRCD"]     = VARS::post("H_CHAIRCD");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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

        knjd124kQuery::update($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
?>
