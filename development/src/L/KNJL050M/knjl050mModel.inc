<?php

require_once('for_php7.php');

class knjl050mModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $receptno;            //受験番号
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl050mModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->receptno);
            unset($this->year);
            unset($this->checkexam);
        }

        $this->cmd = VARS::request("cmd");
        if (VARS::request("kakutei") && $this->cmd != '') {
            $this->kakutei = true;
        } else {
            unset($this->kakutei);
        }
        $this->year = CTRL_YEAR + 1;
        $this->receptno = VARS::post("RECEPTNO");

        if (strlen($this->receptno)) {
            $this->receptno = sprintf("%05d", $this->receptno);
        }

        //フィールド
        $this->field = array("TESTPAPERCD"  => VARS::post("TESTPAPERCD"),  //科目詳細
                             "EXAMHALLCD"   => VARS::post("EXAMHALLCD"),   //会場
                             "SCORE"        => VARS::post("SCORE"),        //得点
                            );
    }


    //入力項目チェック
    function validateInput()
    {
        //必須チェック
        if (!($this->receptno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl050mQuery::getDeleteInsertQuery($this);
        }
        if ($this->cmd == 'first_search') {
            unset($this->receptno);
        }

        return;
    }
}
?>