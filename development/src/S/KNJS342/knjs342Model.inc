<?php

require_once('for_php7.php');

class knjs342Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ

    public function knjs342Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("HR_CLASS_TYPE"    => VARS::post("HR_CLASS_TYPE"),     //クラス方式
                             "GAKUNEN_KONGOU"   => VARS::post("GAKUNEN_KONGOU"),    //学年混合チェックボックス
                             "GRADE_HR_CLASS"   => VARS::post("GRADE_HR_CLASS"),    //対象クラス
                             "TARGET_MONTH"     => VARS::post("TARGET_MONTH"),      //対象月
                             "TARGET_DAY"       => VARS::post("TARGET_DAY"),        //対象日
                             "FORM_SELECT"      => VARS::post("FORM_SELECT"),       //15行で選択
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //変数をクリアします
    public function clean()
    {
    }
}
