<?php

require_once('for_php7.php');

//メニュー画面
class knjb0100Model extends model
{
    var $cmd;               //一覧表示時のcmd
    var $field;             //現在フィールド
    var $field2;            //最初のフィールド
    var $ObjYear;           //入試年度
    var $pdata;             //校時ＣＤ
    var $data = array();                //
    var $error_flg = false; //入力エラーフラグ

    function knjb0100Model()
    {
        $this->ObjYear = CTRL_YEAR;
    }

    function init()
    {
        $this->pdata = 0;
        $this->cmd    = VARS::request("cmd");
        $this->field = array("SUBCD"        => VARS::post("SUBCD"),
                             "STATE"        => VARS::post("STATE"),
                             "PERIOD_CD"    => VARS::post("PERIOD_CD"));

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {

    }

    //入力項目チェック
    function validateInput()
    {
        $this->error_flg = false;
        $checdata = array();
        $checcnt  = array();
        $checary  = array();
        $s = 0;
        for ($i = 0; $i < get_count($this->field["STATE"]); $i++){
            if ($this->field["STATE"][$i] == 1) {
                $checdata[] = $this->field["PERIOD_CD"][$s];
                $s++;
            }else if ($this->field["STATE"][$i] == 2) {
                $s++;
            }
        }
        //独占の校時CDが重複していれば、ERRを返す。
        if (get_count($checdata) != get_count(array_count_values($checdata))){
            $this->setWarning("MSG918");
            $this->error_flg = true;
            return false;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &update()
    {
        $result = knjb0100Query::getUpdateQuery($this);
        $this->field = array("SUBCD"        => VARS::post("SUBCD"),
                             "STATE"        => VARS::post("STATE"),
                             "PERIOD_CD"    => VARS::post("PERIOD_CD"));
        return $result;
    }

    function &delete()
    {
        $result = knjb0100Query::getDeleteQuery($this);
        return $result;
    }
}
?>
