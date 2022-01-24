<?php

require_once('for_php7.php');

class knjg045bModel extends Model {

    var $field = array();
    var $cmd;
    var $year;
    var $schoolkind;
    var $sort;
    var $schoolcd;
    var $schoolkind2;
    var $diarydate;
    var $seq;
    var $remark1_moji;
    var $remark1_gyou;
    var $remark2_moji;
    var $remark2_gyou;

    function knjg045bModel()
    {   
        common::GetControlMaster_Fnc($this->control_data); 
        unset($this->GradeClass);
    }
    function init()
    {
        if(VARS::get("PROGRAMID")){
            unset($this->year);
            unset($this->schoolkind);
            unset($this->sort);
            unset($this->schoolcd);
            unset($this->schoolkind2);
            unset($this->diarydate);
            unset($this->seq);
        }

        $this->field = array("SEQ"            =>  VARS::post("SEQ"),           //シーケンス番号
                             "SCHOOLCD"       =>  VARS::post("SCHOOLCD"),      //学校コード
                             "SCHOOL_KIND"    =>  VARS::post("SCHOOL_KIND"),   //校種
                             "DIARY_DATE"     =>  VARS::post("DIARY_DATE"),    //日付
                             "REMARK1"        =>  VARS::post("REMARK1"),       //出来事・その他連絡事項
                             "REMARK2"        =>  VARS::post("REMARK2"),       //対処・経過
                             "DIARY_DATE_DEF" => VARS::post("DIARY_DATE_DEF")  //変更前日付
                             );
        
        $this->cmd = VARS::request("cmd");
                    
        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        if (VARS::post("SCHKIND")) {
            $this->schoolkind = VARS::post("SCHKIND");
        }
        if (VARS::get("SORT")) {
            $this->sort = VARS::get("SORT");
        }
        
        if(!isset($this->sort)){
            $this->sort='DESC';
        }
        
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        
        if (VARS::get("SCHOOLCD")) {
            $this->schoolcd = VARS::get("SCHOOLCD");
        }
        if (VARS::get("SCHOOL_KIND")) {
            $this->schoolkind2 = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("DIARY_DATE")) {
            $this->diarydate = VARS::get("DIARY_DATE");
        }
        if (VARS::get("SEQ")) {
            $this->seq = VARS::get("SEQ");
        }
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->remark1_moji = 20;
        $this->remark1_gyou = 7;
        $this->remark2_moji = 30;
        $this->remark2_gyou = 7;

        $db = Query::dbCheckOut(); //DB接続
        //学期開始、終了日付
        $this->sDate = $db->getOne(knjg045bQuery::getSEdate("SDATE"));
        $this->eDate = $db->getOne(knjg045bQuery::getSEdate("EDATE"));
        Query::dbCheckIn($db);     //DB切断

    }
    function validateInput($kind){
        $diarydate = str_replace('/','-',$this->field['DIARY_DATE']);
        //必須項目チェック
        if ($kind == "update") {
            if ($this->field['SEQ'] == "") {
                $this->setWarning("MSG308");
                return false;
            }
            if ($diarydate != $this->field['DIARY_DATE_DEF']) {
                $this->setWarning("更新の場合は日付は変更できません");
                return false;
            }
        } else if($kind == "delete") {
            if ($this->field['SEQ'] == "") {
                $this->setWarning("MSG308");
                return false;
            }
        }
        if ($diarydate == "") {
            $this->setWarning("MSG301", "日付"); 
            return false;
        }
        if ($this->field['SCHOOL_KIND'] == "") {
            $this->setWarning("MSG301", "校種"); 
            return false;
        }

        return true;
    }

    //追加
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
        }
        return;
    }
    function &insert()
    {
        knjg045bQuery::getInsertQuery($this);

        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新
    function &getUpdateModel()
    {
       //入力チェック
       if ($this->validateInput("update")){
            $this->update();
        }
    }
    function &update()
    {
        knjg045bQuery::getUpdateQuery($this);

        if ( !isset($this->warning)){
           $this->setMessage("MSG201");
       }
        return;
    }
    //削除
    function &getDeleteModel()
    {
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    }
    function &delete()
    {
        $result = knjg045bQuery::getDeleteQuery($this);

        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

}
?>
