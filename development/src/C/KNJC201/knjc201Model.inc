<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjc201Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ
    public $staffcd;//職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjc201Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;//職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"    => VARS::post("YEAR"),  //年度
                             "GAKKI"   => VARS::post("GAKKI"), //学期
                             "OUTPUT"  => VARS::post("OUTPUT"),//ソート選択
                             "HR_NAME" => VARS::post("HR_NAME")//クラス選択
        );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
