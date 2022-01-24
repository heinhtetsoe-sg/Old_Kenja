<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf070Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjf070Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "CHECK1"           =>  VARS::post("CHECK1"),       //視力検査の統計
                             "EYESIGHT"         =>  VARS::post("EYESIGHT"),     //統計対象データ選択
                             "CHECK2"           =>  VARS::post("CHECK2"),       //尿検査の統計
                             "CHECK3"           =>  VARS::post("CHECK3"),       //貧血検査の統計
                             "CHECK4"           =>  VARS::post("CHECK4"),       //歯科検査の統計
                             "CHECK5"           =>  VARS::post("CHECK5"),       //歯科検査他の統計
                             "NOT_AGE"          =>  VARS::post("NOT_AGE"),      //異年令を除く
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
    function clean()
    {
    }
}
?>
