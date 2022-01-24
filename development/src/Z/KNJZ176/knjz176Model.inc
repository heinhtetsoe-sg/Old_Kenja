<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz176Model extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjz176Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass = VARS::post("GRADE_HR_CLASS");

        $this->selectdata    = VARS::post("selectdata");    //対象者一覧
        $this->selectdata2   = VARS::post("selectdata2");   //生徒一覧

        $this->cmd         = VARS::request("cmd");

    }

    function validateInput()
    {
        //指定学期に１件でもあれば、エラーメッセージを表示。
        $dataCnt = knjz176Query::getDetailExists($this);
        if (0 < $dataCnt) {
            $this->setWarning("MSG300", "指定学期に１件でもあれば、コピーできません。"); //"MSG203" or "MSG300"
            return false;
        }

        return true;
    }

    function &getCopyModel()
    {
        if ($this->validateInput()){
            knjz176Query::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getUpdateModel()
    {
        $this->update();
    }

    function &update()
    {
        knjz176Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return $result;
    }
}
?>
