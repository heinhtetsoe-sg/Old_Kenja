<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja250Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knja250Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "GRAD"              =>  VARS::post("GRAD"),             //異動区分（卒業）
                            "MOVE"              =>  VARS::post("MOVE"),             //異動区分（転学）
                            "DROP"              =>  VARS::post("DROP"),             //異動区分（退学）
                            "FOREIGN"           =>  VARS::post("FOREIGN"),          //異動区分（留学）
                            "HOLI"              =>  VARS::post("HOLI"),             //異動区分（休学）
                            "SUSPEND"           =>  VARS::post("SUSPEND"),          //異動区分（出停）NO001
                            "ADMISSION"         =>  VARS::post("ADMISSION"),        //異動区分（編入）NO001
                            "MOVINGIN"          =>  VARS::post("MOVINGIN"),         //異動区分（転入）NO002
                            "REMOVE"            =>  VARS::post("REMOVE"),           //異動区分（除籍）
                            "GAKUNEN"           =>  VARS::post("GAKUNEN"),          //学年
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll(); //使用するテーブルを取得
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
