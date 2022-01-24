<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb060aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjb060aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }
    function init()
    {               
        $this->field = array("RADIO"            =>  VARS::post("RADIO"),     //時間割種別ラジオ
                            "KUBUN"             =>  VARS::post("KUBUN"),     //出力区分ラジオ
                            "DATE"              =>  VARS::post("DATE"),     //指定日付テキスト１
                            "DATE2"             =>  VARS::post("DATE2"),     //指定日付テキスト２
                            "SECTION_CD_NAME1"  =>  VARS::post("SECTION_CD_NAME1"),     //所属コンボボックス１
                            "SECTION_CD_NAME2"  =>  VARS::post("SECTION_CD_NAME2"),     //所属コンボボックス２
                            "GRADE_HR_CLASS1"   =>  VARS::post("GRADE_HR_CLASS1"),     //学年／組コンボボックス１
                            "GRADE_HR_CLASS2"   =>  VARS::post("GRADE_HR_CLASS2"),     //学年／組コンボボックス２
                            "GRADE_HR_CLASS3"   =>  VARS::post("GRADE_HR_CLASS3"),     //学年／組コンボボックス３
                            "GRADE_HR_CLASS4"   =>  VARS::post("GRADE_HR_CLASS4"),     //学年／組コンボボックス４
                            "TITLE"             =>  VARS::post("TITLE"),     //時間割コンボボックス選択データ
                            "CHECK"             =>  VARS::post("CHECK"),     //授業が無い「校時」を詰める、詰めないのチェックボックス
                            "TEST_CHECK"        =>  VARS::post("TEST_CHECK"),//「テスト時間割のみ出力」チェックボックス---2006/11/01
                             "SUBCLASS_CHAIR_DIV" =>  VARS::post("SUBCLASS_CHAIR_DIV"), //出力項目(科目名・講座名)
                            "STAFF_CHECK"       =>  VARS::post("STAFF_CHECK"),      //「職員は正担任（MAX職員番号）のみ出力」チェックボックス
                            "NO_CLASS_CHECK"    =>  VARS::post("NO_CLASS_CHECK"),   //「クラス名は出力しない」チェックボックス
                            "DBNAME"          =>  VARS::post("DBNAME"),          //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
