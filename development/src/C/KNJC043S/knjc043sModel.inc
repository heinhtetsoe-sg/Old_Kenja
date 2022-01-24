<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc043sModel extends Model {
    var $field      = array();
    var $cmd;
    var $useTestCountflg;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjc043sModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"            =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),       //学期
                             "SEME_DATE"        =>  VARS::post("SEME_DATE"),      //学期
                             "DATE1"            =>  VARS::post("DATE1"),          //印刷範囲開始日
                             "DATE2"            =>  VARS::post("DATE2"),          //印刷範囲終了日
                             "OUTPUT1"          =>  VARS::post("OUTPUT1"),        //チェックボックス
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),        //チェックボックス
                             "OUTPUT3"          =>  VARS::post("OUTPUT3"),        //チェックボックス
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),        //チェックボックス
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),        //チェックボックス
                             "SHR_SYUREI"       =>  VARS::post("SHR_SYUREI"),     //チェックボックス「SHR」「終礼」の科目を出力しない
                             "RADIO1"           =>  VARS::post("RADIO1"),         //ラジオ NO003
                             "CHECK1"           =>  VARS::post("CHECK1"),         //チェックボックス NO003
                             "CHECK2"           =>  VARS::post("CHECK2"),         //校時別科目一覧表チェックボックス
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"), //年組
                             "HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),  //クラス方式選択 (1:法定クラス 2:実クラス 3:統計学級)
                             "DBNAME"           =>  VARS::post("DBNAME"),         //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->Properties["useTestCountflg"] = ($this->Properties["useTestCountflg"]) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG";
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
