<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje151jModel extends Model {
    
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
    function knje151jModel()
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

        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "CLASSCD"          =>  VARS::post("CLASSCD")           //教科
                            );

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //講座コード
        if($this->field["CLASSCD"] == VARS::post("CLASSCD") && VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        //科目コード
        $this->field["CLASSCD"] = VARS::post("CLASSCD");
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

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

        knje151jQuery::update($this);
        knje151jQuery::updateRecord($this);
        $this->setMessage("MSG201");
    }
}
?>
