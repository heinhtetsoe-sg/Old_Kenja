<?php

require_once('for_php7.php');

class knjf102aModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ

    public function knjf102aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "NAIKA"            =>  VARS::post("NAIKA"),            //内科
                             "GEKA"             =>  VARS::post("GEKA"),             //外科
                             "KENKO_SODAN"      =>  VARS::post("KENKO_SODAN"),      //健康相談
                             "SONOTA"           =>  VARS::post("SONOTA"),           //その他
                             "SEITO_IGAI"       =>  VARS::post("SEITO_IGAI"),       //生徒以外
                             "CHECK1"           =>  VARS::post("CHECK1"),           //来室種別ごとの改ページあり
                             "DATE1"            =>  VARS::post("DATE1"),            //来室日付範囲（開始）
                             "DATE2"            =>  VARS::post("DATE2"),            //体質日付範囲（終了）
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
