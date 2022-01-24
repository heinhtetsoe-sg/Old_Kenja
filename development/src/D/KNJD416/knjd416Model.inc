<?php

require_once('for_php7.php');

class knjd416Model extends Model {
    var $cmd;
    var $field = array();
    var $selectdata;
    var $type;

    function knja150Model() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->selectdata);
            $this->field = array();
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SCHREGNO"         =>  VARS::post("SCHREGNO")          //学籍番号
                             );

        $this->type = VARS::request("type");   //画面タイプ 0:基本 1:学年別 2:個人別
        $this->cmd = VARS::request("cmd");

        //対象指導
        $this->selectdata   = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput() {
//        //必須チェック
//        if (!strlen($this->field["GRADE"])) {
//            $this->setWarning("MSG310", "( 学年 )");
//            return false;
//        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd416Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
            unset($this->selectdata);
        }
    }
    
    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $pre_year = CTRL_YEAR - 1;
        $year = CTRL_YEAR;
        $preYear_cnt = $db->getOne(knjd416Query::getCopyData($pre_year, "cnt"));
        //前年度データチェック
        if ($preYear_cnt == 0) {
            $this->setWarning("MSG203", "前年度データがありません。");
            return false;  
        }
        Query::dbCheckIn($db);
        knjd416Query::getCopyQuery($year, $pre_year);
        $this->setMessage("MSG201");
    }

}
?>
