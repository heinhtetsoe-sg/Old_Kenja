<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd280cModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $select;
    var $selectLabel;

    /**
     * メンバ変数の初期化
     */
    function knjd280cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "CLASSCD_SET"      =>  VARS::post("CLASSCD_SET"),       //教科
                             "MAJOR"            =>  VARS::post("MAJOR"),             //学科
                             "DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->select = VARS::post("select");
        $this->selectLabel = VARS::post("selectLabel");
        $this->getPropertiesAll();

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>

