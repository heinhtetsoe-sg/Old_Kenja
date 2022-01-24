<?php

require_once('for_php7.php');

class knja226bModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード

    public function knja226bModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("GRADE"    =>  VARS::post("GRADE"),  //学年
                             "SDATE"    =>  VARS::post("SDATE"),  //対象日(開始)
                             "EDATE"    =>  VARS::post("EDATE"),  //対象日(終了)
                             "OUTPUT"   =>  VARS::post("OUTPUT"), //出欠状況出力範囲
                             "DI_CD1"   =>  VARS::post("DI_CD1"), //公欠
                             "DI_CD2"   =>  VARS::post("DI_CD2"), //出停
                             "DI_CD3"   =>  VARS::post("DI_CD3"), //忌引
                             "DBNAME"   =>  VARS::post("DBNAME")  //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }
}
?>
