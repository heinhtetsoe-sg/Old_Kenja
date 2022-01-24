<?php

require_once('for_php7.php');

class knjh211Model extends Model
{

    public $field      = array();
    public $cmd;
    public $schregno;
    public $control_data = array();    //コントロールマスタデータを保持
    public $sec_competence;            //セキュリティーチェック
    public $domicd;                    //寮コード
    public $enterdate;                 //入寮日
    public $regddate;                  //登録日付
    
    public function knjh211Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->GradeClass);
    }
    public function init()
    {
        $this->field = array(
                    /*-------         domitory_history_dat テーブル用        --------------*/
                             "SCHREGNO"      =>  VARS::post("SCHREGNO"),        //学籍番号
                             "DOMI_CD"       =>  VARS::post("DOMI_CD"),         //寮コード
                             "DOMI_ENTDAY"   =>  VARS::post("DOMI_ENTDAY"),     //入寮日
                             "DOMI_OUTDAY"   =>  VARS::post("DOMI_OUTDAY"),     //退寮日
                             "UPDATED"       =>  VARS::post("UPDATED"),         //更新日付

                             "temp_cd"       =>  VARS::post("temp_cd")          //最初のCD
                             );
                            
        if (VARS::request("SCHREGNO")) {
            $this->schregno   = VARS::request("SCHREGNO");
        }

        //スクロールバーを指定行に移動させるべき時はそのままdomicdを保持したいのでcmdがlistの時はdomicdの値を変えない
        if (VARS::request("cmd") != 'list') {
            $this->domicd       = VARS::request("DOMI_CD");
            if (VARS::request("DOMI_CD")=="") {
                $this->domicd       = VARS::post("DOMI_CD");
            }
        }
        $this->enterdate    = VARS::request("DOMI_ENTDAY");
        if (VARS::request("SCHREGNO")=="") {
            $this->schregno     = VARS::post("SCHREGNO");
        }
        if (VARS::request("DOMI_ENTDAY")=="") {
            $this->enterdate    = VARS::post("DOMI_ENTDAY");
        }
        $this->cmd       = VARS::request("cmd");
        if (VARS::request("GrCl")) {
            $this->GradeClass = VARS::request("GrCl");
        }

        if (VARS::get("GRADE")) {
            $this->grade   = VARS::get("GRADE");
        }

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck(STAFFCD, "KNJH211");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        // 権限チェック
        if ($this->sec_competence != DEF_UPDATABLE && $this->sec_competence != DEF_UPDATE_RESTRICT) {
            $this->setWarning("MSG300");
            return false;
        }

        //必須入力項目をチェック
        if ($this->field["DOMI_CD"]=="" || $this->field["DOMI_ENTDAY"]=="") {
            if ($kind=="insert") {
                $this->setWarning("MSG301");
            } else {
                $this->setWarning("MSG308");
            }
            return false;
        }
        if ($this->schregno=="") {
            $this->setWarning("MSG304", "(左より生徒を選択してから行ってください)");
            return false;
        }
        //１レコード取得
        $row = knjh211Query::getRowSdate($model, $this->field["SCHREGNO"], $this->field["DOMI_CD"], $this->field["DOMI_ENTDAY"]); /* NO001 */
        $this->isload = "0";
        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->field["temp_cd"]=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($row["SCHREGNO"] != $this->field["temp_cd"]) {
                $this->isload = "1";
                $this->setWarning("MSG308");
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
            //削除
            if ($kind == "delete" && $cnt > 0) {
                $this->setWarning("MSG919", "（記録備考データ有）");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            //キーチェック
            $histcheck = knjh211Query::getDomitoryHistory_key_chk($this); /* NO001 */
            if ($this->cmd == "add" && is_array($histcheck)) {          /* NO001 */
                $this->setWarning("MSG918", "(学籍番号/氏名),(寮コード),(入寮日)");
                return false;
            }
            //重複日チェック
            $histcheck = knjh211Query::getDomitoryHistory_Dat_chk($this); /* NO001 */
            if ($this->cmd == "add" && is_array($histcheck)) {          /* NO001 */
                $this->setWarning("MSG918", "(学籍番号/氏名),(寮コード),(入寮日)");
                return false;
            }
            //入寮日と退寮日の大小チェック
            if ($this->field["DOMI_OUTDAY"] && ($this->field["DOMI_ENTDAY"] > $this->field["DOMI_OUTDAY"])) {
                $this->setWarning("MSG901", "(入寮/退寮日の大小)");
                return false;
            }
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
        }
        return;
    }

    /**
     * 削除用のモデルを返します
     */
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }
    public function &delete()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }
        
        $result = knjh211Query::getDeleteQuery($this, $this->field, $this->schregno);
        
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        $this->field = array();
        
        return $result;
    }

    public function &insert()
    {
        $result = knjh211Query::getInsertQuery($this, $this->field, $this->schregno);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    public function &update()
    {
        $result = knjh211Query::getUpdateQuery($this, $this->field, $this->schregno);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
