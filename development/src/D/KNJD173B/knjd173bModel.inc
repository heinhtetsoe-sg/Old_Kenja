<?php

require_once('for_php7.php');

class knjd173bModel extends Model {
    var $field       = array();
    var $select_data = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable; //テーブル名

    /**
     * メンバ変数の初期化
     */
    function knjd173bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                   //職員コード
    }

    function init() {
        $this->field = array("TAISYOBI"         =>  VARS::post("TAISYOBI"),         //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "TEST_ITEM"        =>  VARS::post("TEST_ITEM"),        //テスト項目
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),          //認定単位・委員会・部活動の表記なし
                             "OUTPUT3"          =>  VARS::post("OUTPUT3"),          //遅刻回数・早退回数の表記なし
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //「総合的な学習の時間」所見出力
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),          //「奉仕」所見出力
                             "OUTPUT6"          =>  VARS::post("OUTPUT6"),          //「通信欄」所見出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
    }

    /**
     * 変数をクリアします
     */
    function clean() {}
}
?>
