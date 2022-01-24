<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd120tModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;
    var $gen_ed = "900100";        //総合学習コード
    var $usePerfect;

    /**
     * メンバ変数の初期化
     */
    function knjd120tModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        //講座コード
        if($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        }else{
            $this->field["CHAIRCD"] = "";
        }

        $this->cmd     = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        /*  更新処理 */

        //学期成績データ項目名を配列にする。
        $term = array("SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_VALUE","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_VALUE","GRAD_VALUE");

        //初期化
        $this->fields = array();
        for ($j=0; $j < get_count($term); $j++) {

            for($i=0;$i<get_count($this->data["SCHREGNO"]);$i++){

                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);

                if(isset($post_term)){

                    //出欠情報が入ってたら成績は空
                    if ($post_term == "KK" || $post_term == "KS") {
                        $this->fields[$term[$j]."_DI"][] = $post_term;
                        $this->fields[$term[$j]][]       = "";
                    } else {
                        $this->fields[$term[$j]."_DI"][] = "";
                        $this->fields[$term[$j]][]       = $post_term;
                    }

                }else{

                    $this->fields[$term[$j]][]       = $this->data[$term_name];
                    $this->fields[$term[$j]."_DI"][] = $this->attend_data[$term_name];
                }
            }
        }

        knjd120tQuery::update($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
?>
