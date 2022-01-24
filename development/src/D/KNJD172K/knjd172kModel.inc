<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd172kModel extends Model {
    var $field      = array();
    var $select_data      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd172kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DATE2"            =>  VARS::post("DATE2"),            //作成日付 NO001
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト名
                             "OUTPUT"       =>  VARS::post("OUTPUT"),               //出力種類 NO001
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
//                           "MEN"              =>  VARS::post("MEN"),              //印刷面
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

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
