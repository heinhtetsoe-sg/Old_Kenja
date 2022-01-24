<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc043tModel extends Model
{
    public $field      = array();
    public $cmd;
    public $useTestCountflg;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjc043tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
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
                             "ONEDAY_ATTEND_CHECK"   =>  VARS::post("ONEDAY_ATTEND_CHECK"), //1日出欠判定チェック
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
    public function clean()
    {
    }
}
