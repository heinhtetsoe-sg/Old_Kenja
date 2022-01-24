<?php

require_once('for_php7.php');

class knjmp971Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $select_data = array();
    var $select_opt  = array();    //セレクトデータ
    var $selectleft;

    function knjmp971Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "LIMIT_DATE"       =>  VARS::post("LIMIT_DATE"),
                             "TOKUSOKU_DATE"    =>  VARS::post("TOKUSOKU_DATE"),
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                            );
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->selectleft = VARS::post("selectleft");
                            
        //初期値
        if ($cmd == "") {
            $this->field["LIMIT_DATE"] = ($this->field["LIMIT_DATE"]) ? $this->field["LIMIT_DATE"] : str_replace("-","/",CTRL_DATE);
            $this->field["PRINT_DATE"] = ($this->field["PRINT_DATE"]) ? $this->field["PRINT_DATE"] : str_replace("-","/",CTRL_DATE);
        }
        if ($this->cmd != 'read') {
            $this->select_opt = array();
        }
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }
    function &update()
    {
        $result = knjmp971Query::getUpdateQuery($this);

        return $result;
    }
}
?>
