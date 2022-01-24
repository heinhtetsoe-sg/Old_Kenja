<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja170kModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knja170kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->field = array("YEAR"            =>  VARS::post("YEAR"),           //年度
                            "CHANGE"           =>  VARS::post("CHANGE"),         //印刷指定(法定/個人指定/実)
                            "GAKUNEN_KONGOU"   =>  VARS::post("GAKUNEN_KONGOU"), //学年混合
                            "OUTPUT"           =>  VARS::post("OUTPUT"),         //学期
                            "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")  //年組
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
