<?php

require_once('for_php7.php');

class knjc036Model extends Model {

    var $cmd;
    var $field = array();
    var $semester;
    var $hr_class;

    function knjc036Model()
    {
        $this->field = array();
    }

    function init()
    {
        $this->cmd               = VARS::request("cmd");
        $this->semester = VARS::post("SEMESTER"); //対象学期
        $this->hr_class = VARS::post("HR_CLASS"); //対象学級

        /* 編集対象データを取得 */
        $this->field = array();
        $this->field["SCHREGNO"]    = VARS::post("SCHREGNO"); //学籍番号
        $this->field["LATE_COR"]    = VARS::post("LATE_COR" .$this->semester); //遅刻訂正
        $this->field["KEKKA_COR"]   = VARS::post("KEKKA_COR".$this->semester); //欠課訂正
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /* 入力項目チェック */
    function validateInput()
    {
        if(!isset($this->field["SCHREGNO"])){
            $this->setMessage("MSG304");
            return false;
        }

        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $result = knjc036Query::getUpdateQuery($this);
        return $result;
    }
}
?>
