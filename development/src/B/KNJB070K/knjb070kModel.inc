<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb070kModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    public function knjb070kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "DATA_DIV"             =>  VARS::post("DATA_DIV"),             //テスト区分ラジオ
                             "OUT_DIV"              =>  VARS::post("OUT_DIV"),              //考査の出力区分ラジオ
                             "TESTCD"               =>  VARS::post("TESTCD"),               //考査コンボ
                             "PROFICIENCYDIV"       =>  VARS::post("PROFICIENCYDIV"),       //データ種別コンボ
                             "PROFICIENCYCD"        =>  VARS::post("PROFICIENCYCD"),        //テスト名称コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年コンボ
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
