<?php

require_once('for_php7.php');

class knjf100aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjf100aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->field = array("DATE1"                =>  VARS::post("DATE1"),                  //日付FROM
                             "DATE2"                =>  VARS::post("DATE2"),                  //日付TO
                             "SORT_SELECTED_HIDDEN" =>  VARS::post("SORT_SELECTED_HIDDEN"),   //ソート順
                             "NAIKA"                =>  VARS::post("NAIKA"),                  //出力する帳票(内科)
                             "GEKA"                 =>  VARS::post("GEKA"),                   //出力する帳票(外科)
                             "KENKO_SODAN"          =>  VARS::post("KENKO_SODAN"),            //出力する帳票(健康相談活動)
                             "SONOTA"               =>  VARS::post("SONOTA"),                 //出力する帳票(その他)
                             "SEITO_IGAI"           =>  VARS::post("SEITO_IGAI"),             //出力する帳票(生徒以外)
                             "CHECK1"               =>  VARS::post("CHECK1"),                 //改ページ(クラスごと)
                             "CHECK2"               =>  VARS::post("CHECK2"),                 //改ページ(来室種別ごと)
                             "YEAR"                 =>  VARS::post("YEAR"),                   //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),               //学期
                             "SEME_DATE"            =>  VARS::post("SEME_DATE"),              //学期期間
                             "DBNAME"               =>  VARS::post("DBNAME"),                 //DB変更に備えて
                             );

        if ($this->field["SORT_SELECTED_HIDDEN"]) {
            $this->field["SORT_SELECTED_HIDDEN"] = explode(',', $this->field["SORT_SELECTED_HIDDEN"]);
        } else {
            $this->field["SORT_SELECTED_HIDDEN"] = array('TYPE','NEN_KUMI_BAN','VISIT_DATE');
        }
        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
