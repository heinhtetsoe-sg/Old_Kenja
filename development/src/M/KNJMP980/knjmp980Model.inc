<?php

require_once('for_php7.php');

//定数定義
class knjmp980Model extends Model {
    var $field = array();
    var $cmd;
    var $levy_l_cd;       //学校払込コード
    var $levy_bank_div;      //銀行種別

    //メンバ変数の初期化
    function knjmp980Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->levy_l_cd);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");

        if (VARS::get("LEVY_L_CD")) {
            $this->levy_l_cd = sprintf("%02d",VARS::get("LEVY_L_CD"));
        }

        $this->field = array("LEVY_L_CD"      => VARS::post("LEVY_L_CD"),         //学校払込コード
                             "LEVY_L_NAME"    => VARS::post("LEVY_L_NAME"),       //学校払込名称
                             "LEVY_L_ABBV"    => VARS::post("LEVY_L_ABBV"),       //銀行種別
                             "UPDATED"        => VARS::post("UPDATED"));
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["LEVY_L_CD"]) == 0){
            $this->setWarning("MSG301","(コード)");
            return false;
        }
        
        if ($kind == "insert" || $kind == "update") {
            if(strlen($this->field["LEVY_L_NAME"]) == 0){
                $this->setWarning("MSG301","(名称)");
                return false;
            }
        }            
        //バイト数チェック
        if (strlen($this->field["LEVY_L_NAME"]) > 90){
            $this->setWarning("MSG915","(名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["LEVY_L_ABBV"]) > 90){
            $this->setWarning("MSG915","(略称は90バイトまでです。)");
            return false;
        }
        
        if (($kind=="update" || $kind=="delete") && $this->levy_l_cd != $this->field["LEVY_L_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjmp980Query::getRow($this, $this->field["LEVY_L_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjmp980Query::getRow($this, $this->levy_l_cd);
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row2)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjmp980Query::getInsertQuery($this);
            $this->levy_l_cd = sprintf("%02d",$this->field["LEVY_L_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjmp980Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjmp980Query::getDeleteQuery($this);
            $this->levy_l_cd = "";
            $this->setMessage("MSG201");
        }
        return;
    }


    //前年度のデータをコピー
    function &getCopyModel()
    {
        //今年度のデータが存在するか確認
        $t_year = knjmp980Query::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjmp980Query::CheckData($this->year - 1);
        if(!isset($pre_year)){
          $this->setWarning('MSG203', "前年データが存在しません。");
          return false;
        }

        knjmp980Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
