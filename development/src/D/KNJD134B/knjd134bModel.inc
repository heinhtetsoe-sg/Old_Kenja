<?php

require_once('for_php7.php');

class knjd134bModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ

    public function knjd134bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "DATE_DIV"         =>  VARS::post("DATE_DIV"),         //出欠集計範囲（累計・学期）
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "EDATE"            =>  VARS::post("EDATE"),            //出欠集計終了日付
                             "FORM_SELECT"      =>  VARS::post("FORM_SELECT"),      //フォーム選択
                             "TYUI_TYOUKA"      =>  VARS::post("TYUI_TYOUKA"),      //注意・超過
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
