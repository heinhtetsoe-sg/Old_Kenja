<?php

require_once('for_php7.php');


class knjj030Model extends Model {

    var $field      = array();
    var $cmd;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $sec_competence;            //セキュリティーチェック
    var $clubcd;    //部クラブコード
    var $enterdate; //入部日付
    var $regddate;                  //登録日付
    
    function knjj030Model()
    {   
        common::GetControlMaster_Fnc($this->control_data); 
        unset($this->GradeClass);
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->enterdate);
            unset($this->GradeClass);
            unset($this->grade);
            unset($this->schKind);
            unset($this->sec_competence);
        }

        $this->field = array(
                    /*-------         club_history_dat テーブル用        --------------*/
                             "SCHREGNO"      =>  VARS::post("SCHREGNO"),        //学籍番号
                             "CLUBCD"        =>  VARS::post("CLUBCD"),          //部クラブコード
                             "SDATE"         =>  VARS::post("SDATE"),           //入部日付
                             "EDATE"         =>  VARS::post("EDATE"),           //退部日付
                             "EXECUTIVECD"   =>  VARS::post("EXECUTIVECD"),     //役職区分
                             "REMARK"        =>  VARS::post("REMARK"),          //備考
                             "UPDATED"       =>  VARS::post("UPDATED"),         //更新日付

                             "temp_cd"       =>  VARS::post("temp_cd")          //最初のCD
                             );
                            
        if (VARS::request("SCHREGNO")){
            $this->schregno   = VARS::request("SCHREGNO");
        }

        //スクロールバーを指定行に移動させるべき時はそのままclubcdを保持したいのでcmdがlistの時はclubcdの値を変えない
        if (VARS::request("cmd") != 'list') {
            $this->clubcd       = VARS::request("CLUBCD");
            if (VARS::request("CLUBCD")=="")    $this->clubcd       = VARS::post("CLUBCD");
        }
        $this->enterdate    = VARS::request("SDATE");
        if (VARS::request("SCHREGNO")=="")  $this->schregno     = VARS::post("SCHREGNO");
        if (VARS::request("SDATE")=="")     $this->enterdate    = VARS::post("SDATE");
        $this->cmd       = VARS::request("cmd");
        if(VARS::request("GrCl")) {
            $this->GradeClass = VARS::request("GrCl");
        }

        if (VARS::get("GRADE")){
            $this->grade   = VARS::get("GRADE");
        }
        $db = Query::dbCheckOut();
        $this->schKind = $db->getOne(knjj030Query::getSchkind($this));
        Query::dbCheckIn($db);

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck(STAFFCD,"KNJJ030");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {    
        // 権限チェック
        if($this->sec_competence != DEF_UPDATABLE && $this->sec_competence != DEF_UPDATE_RESTRICT){
            $this->setWarning("MSG300");
            return false;
        }

        //必須入力項目をチェック
        if ($this->field["CLUBCD"]=="" || $this->field["SDATE"]=="") {
            if ($kind=="insert") {
                $this->setWarning("MSG301");
            } else {
                $this->setWarning("MSG308");
            }
            return false;
        }
        if ($this->schregno=="") {
            $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
            return false;
        } 
        //１レコード取得
        $row = knjj030Query::getRowSdate($model, $this->field["SCHREGNO"],$this->field["CLUBCD"],$this->field["SDATE"]); /* NO001 */
        $this->isload = "0";
        if ($kind=="update" || $kind=="delete") {        
            //リストより選択していない
            if ($this->field["temp_cd"]==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($row["SCHREGNO"] !=$this->field["temp_cd"]){
                $this->isload = "1";
                $this->setWarning("MSG308");
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == ""){
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }

            //記録備考データ存在チェック
            $cnt = knjj030Query::chkDetailExist($this, $kind);
            //更新
            if($kind == "update" && $cnt > 0){
                $this->setWarning("MSG901","（退部日付：記録備考データ有）");
                return false;
            }
            //削除
            if($kind == "delete" && $cnt > 0){
                $this->setWarning("MSG919","（記録備考データ有）");
                return false;
            }

        }
        if ($kind=="update" || $kind=="insert") {
            //重複チェック
            $histcheck = knjj030Query::getClubHistory_Dat_chk($this); /* NO001 */
            if ($this->cmd == "add" && is_array($histcheck)){          /* NO001 */
                $this->setWarning("MSG918", "(学籍番号/氏名),(部/クラブ),(入部日付)");
                return false;
            }
            //桁数チェック
            if (strlen($this->field["REMARK"]) >60 ) {
                $this->setWarning("MSG914","(備考は60バイトまでです)");
                return false;  
            }
            //入部日付と退部日付の大小チェック
            if($this->field["EDATE"] && ($this->field["SDATE"] > $this->field["EDATE"])){
                $this->setWarning("MSG901","(入部/退部日付の大小)");
                return false;  
            }
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }    
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
        }
        return;
    }

    /**
     * 削除用のモデルを返します
     */
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    } 
    function &delete()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }
        
        $result = knjj030Query::getDeleteQuery($this, $this->field,$this->schregno);
        
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        $this->field = array();
        
        return $result;
    }

    function &insert()
    {
        $result = knjj030Query::getInsertQuery($this, $this->field,$this->schregno);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjj030Query::getUpdateQuery($this, $this->field,$this->schregno);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

}
?>
