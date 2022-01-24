<?php

require_once('for_php7.php');


class knjz403Model extends Model {
    var $field      = array();
    var $cmd;
    var $grade;       //primary
    var $code;        //primary
    var $lenval;      //観点の文字列の長さ(byte)

    function knjz403Model()
    {
        ;
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            $this->grade = "";
            $this->code = "";
        }
    
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),
                             "CODE"             =>  VARS::post("CODE"),
                             "CODENAME"         =>  VARS::post("CODENAME"),
                             "VIEWNAME"         =>  VARS::post("VIEWNAME"),
                             "STUDYREC_CODE"    =>  VARS::post("STUDYREC_CODE"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd   = VARS::request("cmd");

        //リンク用から取得した科目
        if (VARS::get("GRADE")!="") {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("CODE")!="") {
            $this->code    = VARS::get("CODE");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if ($this->lenval = $this->Properties["knjz403useResizeViewName"]) {
            $this->lenval = $this->Properties["knjz403useResizeViewName"];
        } else {
            $this->lenval = 50;
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["CODE"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(行動の記録コード)");
            return false;
        }

        if ($this->code == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->cmd == "update" && ($this->code != $this->field["CODE"] && $this->grade != $this->field["GRADE"])) {
            $this->setWarning("MSG308");
            return;
        }

        //１レコード取得
        $row = knjz403Query::getRow($this->field["GRADE"], $this->field["CODE"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(行動の記録コード)");
            return false;
        }

        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }

        //文字数チェック    
        if (strlen($this->field["CODENAME"]) > 30 ) {
            $this->setWarning("MSG914", "(行動の記録名称) 30バイトまで");
            return false;
        }
        if (strlen($this->field["VIEWNAME"]) > ($this->lenval*3) ) {
            $this->setWarning("MSG914", "(観点名称) ".($this->lenval*3)."バイトまで".strlen($this->field["VIEWNAME"]));
            return false;
        } 

        //文字数チェック    
        if (strlen($this->field["CODENAME"]) <= 0 ) {
            $this->setWarning("MSG301", "(行動の記録名)");
            return false;
        }

        return true;
    }

    //次年度コピー用のモデルを返します
    function &getCopyModel()
    {
        $cntselected = knjz403Query::cntThisOrder($this);
        $cntlastorder = knjz403Query::cntLastOrder($this);
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntlastorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz403Query::getCopyQuery($this);
            if ( !isset($model->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->grade = $this->field["GRADE"];
            $this->code  = $this->field["CODE"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->code == "") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->code != $this->field["CODE"] || $this->grade != $this->field["GRADE"]) {
            $this->setWarning("MSG308");
            return;
        }

         $row = knjz403Query::getRow($this->grade, $this->code);

        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }

        knjz403Query::getDeleteQuery($this->field);
        $this->grade = "";
        $this->code = "";
        $this->setMessage("MSG201");
    }

    function &copy()
    {
        $result = knjz403Query::getCopyQuery($this->field);
    }

    function &insert()
    {
        $result = knjz403Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz403Query::getUpdateQuery($this->field);
    }

    function clean()
    {
    }
}
?>
