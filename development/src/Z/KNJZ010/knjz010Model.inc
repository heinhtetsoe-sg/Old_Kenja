<?php

require_once('for_php7.php');

/**
定数定義
*/
class knjz010Model extends Model {
    var $field      = array();
    var $cmd;
    var $isload;
    /**
     * メンバ変数の初期化
     */
    function knjz010Model()
    {
        $this->cmd = "";
    }
    function init()
    {   
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),                   //年度
                             "SEMESTER"          =>  VARS::post("SEMESTER"),               //学期
                             "CTRL_DATE"         =>  VARS::post("CTRL_DATE"),              //日付
                             "ATTEND_CTRL_DATE"  =>  VARS::post("ATTEND_CTRL_DATE"),       //出欠制御日付
                             "ATTEND_TERM"       =>  VARS::post("ATTEND_TERM"),            //出欠期間
                             "PWDVALIDTERM"      =>  VARS::post("PWDVALIDTERM"),           //パスワード有効期限
                             "IMAGEPATH"         =>  VARS::post("IMAGEPATH"),              //パス
                             "EXTENSION"         =>  VARS::post("EXTENSION"),              //拡張子
                             "MESSAGE"           =>  VARS::post("MESSAGE"),                //メッセージ
                             "UPDATED"           =>  VARS::post("UPDATED")
                             );
    }
    //入力項目チェック
    function validateInput()
    {
        if (!strlen($this->field["YEAR"])) {
            $this->setWarning("MSG305","( 年度 )");
            return false;
        }
        if (knjz010Query::semesterRow()<1) {
            $this->setWarning("MSG305","( 学期 )");
            return false;
        }
        if (!strlen($this->field["CTRL_DATE"])) {
            $this->setWarning("MSG301","( 年月日 )");
            return false;
        }
        if (!strlen($this->field["ATTEND_CTRL_DATE"])) {
            $this->setWarning("MSG301","( 出欠制御年月日 )");
            return false;
        }
        if (strlen($this->field["IMAGEPATH"]) && !preg_match("/^[\/a-zA-Z0-9]*$/", $this->field["IMAGEPATH"])) {
            $this->setWarning("MSG901","( パス )");
            return false;
        }
        if (!preg_match("/^[\/a-zA-Z0-9]*$/", $this->field["EXTENSION"])) {
            $this->setWarning("MSG901","( 拡張子 )");
            return false;
        }
        if (strlen($this->field["MESSAGE"]) > 2898 ) {
            $this->setWarning("MSG914","( メッセージは2898バイトまでです )");
            return false;  
        }
                
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
        }    
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz010Query::getUpdateQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201","登録された年度、学期は再ログイン時に有効になります。");
        }
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
