<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf071Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjf071Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "EYE_MOJI"         =>  VARS::post("EYE_MOJI"),     //統計対象１
                             "EYE_KUBUN"        =>  VARS::post("EYE_KUBUN"),    //統計対象２
                             "AGE_NOZOKU"       =>  VARS::post("AGE_NOZOKU"),   //異年令を除く
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
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
