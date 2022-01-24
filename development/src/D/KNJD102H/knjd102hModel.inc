<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd102hModel extends Model {
    var $field      = array();
    var $select_data      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $testTable;

    /**
     * メンバ変数の初期化
     */
    function knjd102hModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト名
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "RANK_BASE"        =>  VARS::post("RANK_BASE"),        //順位基準
                             "RANK_DIV"         =>  VARS::post("RANK_DIV"),         //総合順位
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
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
