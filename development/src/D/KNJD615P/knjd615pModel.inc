<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd615pModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjd615pModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "NOT_EMPTY_LINE"   =>  VARS::post("NOT_EMPTY_LINE"),   //チェックボックス選択データ
                             "OUTPUT_ORDER"     =>  VARS::post("OUTPUT_ORDER"),     //チェックボックス選択データ
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
