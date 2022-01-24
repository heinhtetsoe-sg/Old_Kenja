<?php

require_once('for_php7.php');

class knje370gModel extends Model {
    var $field = array();
    var $cmd;

    function knje370gModel() {}

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("DISP"             =>  VARS::post("DISP"),             //表示選択
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV"),       //選考分類
                             "HYOTEI"           =>  VARS::post("HYOTEI"),           //評定の処理設定
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

        // //まなびの記録
        // $this->remark_moji = 50;
        // $this->remark_gyou = 36;
    }
}
?>
