<?php

require_once('for_php7.php');

class knjd234uModel extends Model {
    var $field;
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolCode;

    function knjd234uModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "COURSE"               =>  VARS::post("COURSE"),               //コース名
                             "EDATE"                =>  VARS::post("EDATE"),                //出欠集計範囲・終了日付
                             "DATE_DIV"             =>  VARS::post("DATE_DIV"),             //出欠集計範囲（累計・学期）ラジオボタン
                             "SCHREGNO_KAIKIN6"     =>  VARS::post("SCHREGNO_KAIKIN6"),     //皆勤6ヵ年学籍番号
                             "SCHREGNO_KAIKIN3"     =>  VARS::post("SCHREGNO_KAIKIN3"),     //皆勤3ヵ年学籍番号
                             "SCHREGNO_KAIKIN1"     =>  VARS::post("SCHREGNO_KAIKIN1"),     //皆勤1ヵ年学籍番号
                             "YURYO"                =>  VARS::post("YURYO"),                //優良者
                             "YURYO"                =>  VARS::post("YURYO"),                //優良者
                             "FUSHIN"               =>  VARS::post("FUSHIN")                //不振者
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

}
?>
