<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjb061Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    /**
     * メンバ変数の初期化
     */
    function knjb061Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }
    function init() {
        $this->field = array("RADIO"              =>  VARS::post("RADIO"),              //ラジオ選択データ
                             "DATE"               =>  VARS::post("DATE"),               //チェックボックス選択データ
                             "DATE2"              =>  VARS::post("DATE2"),              //チェックボックス選択データ
                             "KUBUN"              =>  VARS::post("KUBUN"),              //出力区分ラジオ
                             "GRADE_HR_CLASS1"    =>  VARS::post("GRADE_HR_CLASS1"),    //生徒コンボボックス１
                             "GRADE_HR_CLASS2"    =>  VARS::post("GRADE_HR_CLASS2"),    //生徒コンボボックス２
                             "SECTION_CD_NAME1"   =>  VARS::post("SECTION_CD_NAME1"),   //所属コンボボックス１
                             "SECTION_CD_NAME2"   =>  VARS::post("SECTION_CD_NAME2"),   //所属コンボボックス２
                             "FACCD_NAME1"        =>  VARS::post("FACCD_NAME1"),        //施設コンボボックス１
                             "FACCD_NAME2"        =>  VARS::post("FACCD_NAME2"),        //施設コンボボックス２
                             "TITLE"              =>  VARS::post("TITLE"),              //時間割コンボボックス選択データ
                             "DBNAME"             =>  VARS::post("DBNAME"),             //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
