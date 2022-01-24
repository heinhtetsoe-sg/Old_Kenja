<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}
class knjz174Model extends Model {
        var $field       = array();
        var $cmd;
        var $staffcd;   //セキュリティー用スタッフコード

    /**
     * メンバ変数の初期化
     */
    function knjz174Model()
    {
        $this->sepa = "";
        $this->level = "";
    }
    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSchoolKind);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth         = VARS::get("SEND_AUTH");
            $this->sendPrgid        = VARS::get("SEND_PRGID");
            $this->sendSchoolKind   = VARS::get("SEND_SCHOOLKIND");
        } else if (!$this->sendPrgid) {
            $this->sendSchoolKind   = SCHOOLKIND;
        }
        $this->field = array("MONTHCD"      => VARS::post("MONTHCD"),
                             "COPY_KEY"     => VARS::post("COPY_KEY"),
                             "H_GRADEHR"    => VARS::post("H_GRADEHR"),
                             "HR_CLASS_DIV" => VARS::post("HR_CLASS_DIV")
        );

        $this->gradeHr = preg_split("/,/", $this->field["H_GRADEHR"]);
        if (!is_array($this->gradeHr)) {
            $this->gradeHr = array();
        }
        foreach ($this->gradeHr as $key => $val) {
            $this->field["LESSON".$val] =  VARS::post("LESSON".$val);
        }
        //権限
        $this->auth = ($this->sendAuth == "") ? AUTHORITY : $this->sendAuth;
        $this->cmd = VARS::request("cmd");

        if (get_count($this->fields["AUTO_CHECK"]) > 0) {
            foreach($this->fields["AUTO_CHECK"] as $key => $val) {
                $this->fields["AUTO_CHECK"][$key] = VARS::post("AUTO_CHECK".$key);
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();

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
        return false;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        knjz174Query::getInsertQuery($this);
        return true;
    }
        
    /**
     * 編集完了用のモデルを返します
    */
    function &getCopyModel()
    {
        //入力チェック
        knjz174Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return false;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
