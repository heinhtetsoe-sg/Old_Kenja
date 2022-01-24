<?php
class knja233dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限

    function knja233dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        $this->year = CTRL_YEAR;

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "FRM_PATERN"       =>  VARS::post("FRM_PATERN"),       //帳票種類
                             "KANA_PRINT"       =>  VARS::post("KANA_PRINT"),       //ふりがな出力
                             "KENSUU"           =>  VARS::post("KENSUU"),           //出力件数
                             "GRD_NAME_NASI"    =>  VARS::post("GRD_NAME_NASI"),    //除籍者名前無し
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>