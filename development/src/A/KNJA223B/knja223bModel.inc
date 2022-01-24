<?php

require_once('for_php7.php');

class knja223bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $schoolName;

    function knja223bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),             //年度
                             "HR_CLASS_TYPE"     =>  VARS::post("HR_CLASS_TYPE"),    //クラス方式
                             "KENSUU"            =>  VARS::post("KENSUU"),           //出力件数
                             "FORM_DIV"          =>  VARS::post("FORM_DIV"),         //フォーム
                             "KANA"              =>  VARS::post("KANA"),             //かな
                             "RYOMEN"            =>  VARS::post("RYOMEN"),           //両面
                             "GAKKI"             =>  VARS::post("GAKKI"),            //学期
                             "DATE"              =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata  = VARS::post("selectdata");

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //学校名取得
        $query = knja223bQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne($query);
        Query::dbCheckIn($db);

    }
}
?>
