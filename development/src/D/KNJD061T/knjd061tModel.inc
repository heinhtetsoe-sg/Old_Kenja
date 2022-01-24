<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd061tModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $testTable;

    /**
     * メンバ変数の初期化
     */
    function knjd061tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "RECORD_DIV"       =>  VARS::post("RECORD_DIV"),       //素点・評価のラジオボタン
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //単位保留の科目には＊を表示する
                             "ASSESS"           =>  VARS::post("ASSESS"),           //評定平均
                             "HYOTEI"           =>  VARS::post("HYOTEI"),           //不振の基準評定
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "ATTEND"           =>  VARS::post("ATTEND"),           //出欠集計範囲ラジオボタン
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計範囲（開始日付）
                             "DATE"             =>  VARS::post("DATE"),             //出欠集計範囲（終了日付）
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                     );

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
