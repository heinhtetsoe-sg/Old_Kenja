<?php
//定数定義
class knjz070kModel extends Model {
    var $field = array();
    var $cmd;
    var $exp_scd;       //費目小分類コード
    var $year;
    var $calledId;      //呼び出し元

    //メンバ変数の初期化
    function knjz070kModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->exp_scd);
            unset($this->calledId);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXPENSE_S_CD")) {
            $this->exp_scd = sprintf("%02d",VARS::get("EXPENSE_S_CD"));
        }

        $this->field = array("YEAR"             => $this->year,
                             "EXPENSE_S_CD"     => VARS::post("EXPENSE_S_CD"),     //費目小分類コード
                             "EXPENSE_S_NAME"   => VARS::post("EXPENSE_S_NAME"),   //費目小分類名称
                             "EXPENSE_S_MONEY"  => VARS::post("EXPENSE_S_MONEY"),  //費目小分類金額
                             "SEX"              => VARS::post("SEX")               //性別
                             );

        if (VARS::get("mode") != ""){
            $this->calledId = VARS::get("mode");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        if(strlen($this->field["EXPENSE_S_CD"]) == 0){
            $this->setWarning("MSG301","(費目小分類コード)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["EXPENSE_S_NAME"]) == 0){
                $this->setWarning("MSG301","(費目小分類名称)");
                return false;
            }
            if(strlen($this->field["EXPENSE_S_MONEY"]) == 0){
                $this->setWarning("MSG301","(金額)");
                return false;
            }
        } else {
            if (knjz070kQuery::getMsmst($this->field) > 0) {
                $this->setWarning("中小分類マスタ登録済み\\n削除できません。");
                return false;
            }
        }

        if(($kind=="update" || $kind=="delete") && $this->exp_scd != $this->field["EXPENSE_S_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjz070kQuery::getRow($this,2);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["EXPENSE_S_NAME"]) > 60){
            $this->setWarning("MSG915","(費目小分類名称は60バイトまでです。)");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjz070kQuery::getInsertQuery($this->field);
            $this->exp_scd = sprintf("%02d",$this->field["EXPENSE_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjz070kQuery::getUpdateQuery($this->field);
            $this->exp_scd = sprintf("%02d",$this->field["EXPENSE_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjz070kQuery::getDeleteQuery($this->field);
            $this->exp_scd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //今年度のデータが存在するか確認
        $t_year = knjz070kQuery::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjz070kQuery::CheckData($this->year - 1);
        if(!isset($pre_year)){
          $this->setWarning('MSG203', "前年データが存在しません。");
          return false;
        }

        knjz070kQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
