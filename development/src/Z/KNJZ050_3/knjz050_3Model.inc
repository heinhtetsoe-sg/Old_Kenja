<?php

require_once('for_php7.php');


class knjz050_3Model extends Model {
    var $field      = array();
    var $cmd;
    var $year;
    var $majorcd = "";        //primary
    var $coursecd = "";

    function knjz050_3Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),
                             "SCHOOLDIV"        =>  VARS::post("SCHOOLDIV")
        );

        $this->cmd         = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->coursecd);
            unset($this->majorcd);
            //前画面のyearをセット
            $this->year      = VARS::get("SEND_YEAR");
        }
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->leftyear      = VARS::get("YEAR");
            $this->coursecd      = VARS::get("COURSECD");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd      = VARS::get("MAJORCD");
        }
    }
    //入力項目チェック
    function validateInput()
    {
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
    //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz050_3Query::getUpdateQuery($this, $this->field);
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
