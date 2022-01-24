<?php

require_once('for_php7.php');

//メニュー画面
class knjm250Model extends model
{
    var $cmd;                           //一覧表示時のcmd
    var $ObjYear;                       //年度
    var $sub;                           //講座コード+科目コード
    var $repcntall;                     //回数
    var $setdata = array();             //表示データ配列
    var $kaisu = array();               //回数
    var $error_flg = false; //入力エラーフラグ

    function knjm250Model()
    {
        $this->ObjYear = CTRL_YEAR;
    }

    function init()
    {
        $this->cmd    = VARS::request("cmd");

        $this->ObjYear    = VARS::request("YEAR");

        $this->sub = VARS::post("SELSUB");

        if ($this->cmd != "reset" ){
            for ($ini = 0 ; $ini < $this->repcntall ; $ini++){
                $this->setdata["REPDIV"][$ini] = VARS::post("REPDIV".$ini);
                $this->setdata["ENDDAY"][$ini] = VARS::post("ENDDAY".$ini);
                $this->setdata["BASEDAY"][$ini] = VARS::post("BASEDAY".$ini);
                $this->setdata["REMARK"][$ini] = VARS::post("REMARK".$ini);
            }
        }
        $this->getPropertiesAll(); //プロパティファイル
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
        $this->setdata = array();               //表示データ配列
    }

    //入力項目チェック
    function validateInput()
    {
        $this->error_flg = false;
        $checdata = array();
        $checcnt  = array();
        $checary  = array();
        $s = 0;

        for ($i = 0; $i < $this->repcntall ; $i++){

            $this->setdata["REPDIV"][$i] = VARS::post("REPDIV".$i);
            $this->setdata["ENDDAY"][$i] = VARS::post("ENDDAY".$i);
            $this->setdata["BASEDAY"][$i] = VARS::post("BASEDAY".$i);
            $this->setdata["REMARK"][$i] = VARS::post("REMARK".$i);
            if ($this->setdata["REPDIV"][$i] == "" || $this->setdata["BASEDAY"][$i] == ""){
                $this->setWarning("レポート区分、提出基準日は必須です。");
                return false;
            }
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
        $result = knjm250Query::getUpdateQuery($this);

        return $result;
    }

    function &delete()
    {
        $result = knjm250Query::getDeleteQuery($this);
        return $result;
    }
}
?>
