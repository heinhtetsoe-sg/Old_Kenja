<?php

require_once('for_php7.php');


class knjb0053Model extends Model {
    var $field        = array();
    var $cmd;
    var $selectCode;            //GETより取得
    var $year;

    function knjb0053Model()
    {
    }

    function init()
    {
        $this->field = array("RIREKI_CODE"    =>  VARS::post("RIREKI_CODE"),
                             "SELECT_NAME"    =>  VARS::post("SELECT_NAME"),
                             "SELECT_DATE"    =>  VARS::post("SELECT_DATE")
                            );
        $this->cmd  = VARS::request("cmd");

        //年度（初期値）
        $this->year = ($this->year == "") ? CTRL_YEAR + 1 : $this->year;
        if (VARS::request("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if(VARS::get("RIREKI_CODE")){
            $this->rirekiCode = VARS::get("RIREKI_CODE");
        }

        if(VARS::get("PROGRAMID")){
            $this->clean();
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        //入力チェック
        if (trim($this->field["RIREKI_CODE"]) == "") {
            $this->setWarning('MSG301', "(コード)");
            return false;
        } else if(trim($this->field["SELECT_NAME"]) == "") {
            $this->setWarning('MSG301', "(項目名)");
            return false;
        } else if(trim($this->field["SELECT_DATE"]) == "") {
            $this->setWarning('MSG301', "(登録日付)");
            return false;
        } else if(strlen($this->field["SELECT_NAME"]) > 60 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(項目名は60バイトまでです。)");
            return false;
        }
        //項目コードの０埋め
        $this->field["RIREKI_CODE"] = sprintf("%02d", $this->field["RIREKI_CODE"]);
        //選択チェック
        if($this->cmd == "update" || $this->cmd == "delete"){
            if($this->rirekiCode != $this->field["RIREKI_CODE"]){
                $this->setWarning('MSG308',"(コード)");
                return false;
            }
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        
        $setYear = $this->year;
        $beforeYear = $this->year - 1;
        $nowData = "";
        $beforeData = "";
        
        //今年度データチェック
        $nowData = knjb0053Query::getCountData($setYear);
        if ($nowData > 0) {
            $this->setWarning('MSG203',"今年度は既にデータが存在します。");
            return false;
        }
        //前年度データチェック
        $beforeData = knjb0053Query::getCountData($beforeYear);
        if ($beforeData == 0) {
            $this->setWarning('MSG303',"(前年度)");
            return false;
        }

        knjb0053Query::getCopyQuery($this);
        $this->setMessage('MSG201');
        return;
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->setMessage('MSG201');
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage('MSG201');
        }
    }

    //インサート時の内部メソッド
    function &insert()
    {
        knjb0053Query::getInsertUpdateQuery($this);
        $this->rirekiCode = $this->field["RIREKI_CODE"];
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjb0053Query::getInsertUpdateQuery($this);
        $this->rirekiCode = $this->field["RIREKI_CODE"];
    }

    //デリート時の内部メソッド
    function &delete()
    {
        knjb0053Query::getDeleteQuery($this);
        unset($this->field);
    }
    
    //変数をクリアします
    function clean()
    {
        unset($this->rirekiCode);
    }
}
?>
