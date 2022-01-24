<?php

require_once('for_php7.php');


class knjj091Model extends Model {

    var $field      = array();
    var $cmd;
    var $schregno;                  //学籍番号
    var $index;                     //連番
    var $control_data = array();    //コントロールマスタデータを保持
    var $sec_competence;            //セキュリティーチェック
    var $grade;                     //学年
    var $org_data;                  //変更前のデータ

    function knjj091Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->grade);
    }
    function init()
    {

        if (VARS::get("PROGRAMID")) {
            unset($this->schKind);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::request("cmd") != 'reset') {
            $this->field = array("COMMITTEECD"      =>  VARS::post("COMMITTEECD"),   //委員会
                                 "SEMESTER"         =>  VARS::post("SEMESTER"),      //学期
                                 "COMMITTEE_FLG"    =>  VARS::post("COMMITTEE_FLG"), //委員会区分
                                 "EXECUTIVECD"      =>  VARS::post("EXECUTIVECD"),   //役職
                                 "SEQPOST"          =>  VARS::post("SEQPOST"),       //連番
                                 "temp_cd"          =>  VARS::post("temp_cd"),       //最初のCD
                                 "UPDATED"          =>  VARS::post("UPDATED")        //更新日付
                                 );
        }

        if (VARS::request("cmd") == 'edit2') {
            $this->field2 = array("COMMITTEECD"      =>  VARS::post("COMMITTEECD"),   //委員会
                                  "SEMESTER"         =>  VARS::post("SEMESTER"),      //学期
                                  "COMMITTEE_FLG"    =>  VARS::post("COMMITTEE_FLG"), //委員会区分
                                  "EXECUTIVECD"      =>  VARS::post("EXECUTIVECD"),   //役職
                                  "SEQPOST"          =>  VARS::post("SEQPOST"),       //連番
                                  "temp_cd"          =>  VARS::post("temp_cd"),       //最初のCD
                                  "UPDATED"          =>  VARS::post("UPDATED")        //更新日付
                                  );
        }

        //list→right_list,right_list→edit
        $this->schregno         = VARS::request("SCHREGNO");
        if (VARS::request("SCHREGNO")=="")          $this->schregno         = VARS::post("SCHREGNO");

        //right_list→edit

        $this->index = VARS::request("SEQ");
        if (VARS::request("SEQ")=="")   $this->index    = VARS::post("SEQPOST");

        $this->cmd       = VARS::request("cmd");

        if(VARS::request("GRADE")) {
            $this->grade    = VARS::request("GRADE");
        }
        //校種取得
        $db = Query::dbCheckOut();
        $this->schKind = $db->getOne(knjj091Query::getSchkind($this));
        Query::dbCheckIn($db);

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck(STAFFCD,"KNJJ091");
    }
    //入力項目チェック
    function validateInput($kind)
    {
        // 権限チェック
        if($this->sec_competence != DEF_UPDATABLE && $this->sec_competence != DEF_UPDATE_RESTRICT){
            $this->setWarning("MSG300");
            return false;
        }

        //委員会、委員会区分、学期
        if ($kind == "insert" || $kind == "update") {
            if($this->field["SEMESTER"] == "" || $this->field["COMMITTEE_FLG"] == "" || $this->field["COMMITTEECD"] == "") {
                $this->setWarning("MSG301");              //必須項目が入力されていません
                return false;
            }
        }

        if ($this->schregno=="") {
            $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
            return false;
        }

        $this->isload = "0";

        if ($kind=="update" || $kind=="delete")         //更新・削除のとき
        {
            //リストより選択していない
            if ($this->field["SEQPOST"]==""){
                $this->setWarning("MSG308");
                return false;
            }
            $db = Query::dbCheckOut();
            $row = knjj091Query::getRow($this,$this->field["SEQPOST"], $db);
            Query::dbCheckIn($db);
            //キー値が変更されていますかをチェック
            if ($row["SEQ"] !=$this->field["SEQPOST"]){
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
            $cnt = knjj091Query::checkDetailExist($this);
            if ($this->cmd == "delete" && $cnt > 0){
                $this->setWarning("MSG919","（記録備考データ有）");
                return false;
            }
        }
        return true;
    }

//  /**
//   * 編集完了用のモデルを返します
//  */


//  /**
//   * 更新用のモデルを返します
//  */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        } else {
            $this->isload = "1";
        }
    }
    function &update()
    {
        $result = knjj091Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }


//  /**
//   * 追加用のモデルを返します
//   */
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
        } else {
            $this->isload = "1";
        }
        return;
    }
    function &insert()
    {
        $result = knjj091Query::getInsertQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->index = $result;
        }
        return $result;
    }


//  /**
//   * 削除用のモデルを返します
//   */
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        } else {
            $this->isload = "1";
        }
        return;
    }
    function &delete()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        $result = knjj091Query::getDeleteQuery($this);

        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        $this->field = array();

        return $result;
    }

}
?>

