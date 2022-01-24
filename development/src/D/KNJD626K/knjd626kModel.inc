<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd626kModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    public function knjd626kModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "OUTPUT1"          =>  VARS::post("OUTPUT1"),          //必履修区分順に出力する
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),          //欠課時数を分数表記する
                             "HID_SCHKIND"      =>  VARS::post("HID_SCHKIND"),      //校種(画面更新前の学年選択時の校種)
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
