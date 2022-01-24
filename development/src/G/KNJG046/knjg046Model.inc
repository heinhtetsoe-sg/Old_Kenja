<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg046Model extends Model
{
    public $field = array();
    public $cmd;
    public $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjg046Model()
    {
    }

    public function init()
    {
        $this->field = array("DATE_FROM"        =>  VARS::post("DATE_FROM"),    //開始日付
                             "DATE_TO"          =>  VARS::post("DATE_TO"),      //終了日付
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
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
