<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje151Model extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $term;
    public $select;

    public $nyuryoku;
    public $type_div;


    /**
     * メンバ変数の初期化
     */
    public function knje151Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->select);
            unset($this->nyuryoku);
            unset($this->type_div);
            unset($this->nonVisualVal);
        }

        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "CLASSCD"          =>  VARS::post("CLASSCD")           //教科
                            );

        $this->cmd = VARS::request("cmd");


        if (VARS::post("SELECT")) {
            $this->select = VARS::post("SELECT");
        }
        if (VARS::post("NYURYOKU")) {
            $this->nyuryoku = VARS::post("NYURYOKU");
        }
        if (VARS::post("TYPE_DIV")) {
            $this->type_div = VARS::post("TYPE_DIV");
        }
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    /* プロパティが立った時のSTATUS値の変換関数（テーブル参照時使用） */
    public function convertSelStatus($status)
    {
        if ($this->Properties["useJviewStatus_NotHyoji_D028_Sub"] == "02") {
            if (is_null($status)) {
                $status = 'F';
            } elseif ($status == $this->nonVisualVal) {
                $status = "";
            }
        }
        return $status;
    }

    /* プロパティが立った時のSTATUS値の変換関数(テーブル更新時使用） */
    public function convertUpdStatus($status)
    {
        if ($this->Properties["useJviewStatus_NotHyoji_D028_Sub"] == "02") {
            if ($status == 'F') {
                $status = "";
            } elseif ($status == "") {
                $status = $this->nonVisualVal;
            }
        }
        return $status;
    }

    //更新処理
    public function &getUpdateModel()
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

        knje151Query::update($this);
        knje151Query::updateRecord($this);
        $this->setMessage("MSG201");
    }
}
