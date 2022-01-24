<?php

require_once('for_php7.php');

class knje400aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $year;
    var $docNumber;
    var $selectdata;

    function knje400aModel() {}
    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->docNumber);
            unset($this->selectdata);
            $this->field = array();
        }
    
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("DOC_NUMBER"           =>  VARS::post("DOC_NUMBER"),           //文書番号
                             "NOTICE_TITLE"         =>  VARS::post("NOTICE_TITLE"),         //通達文のタイトル
                             "NOTICE_MESSAGE"       =>  VARS::post("NOTICE_MESSAGE"),       //通達文
                             "VIEWING_PERIOD_FROM"  =>  VARS::post("VIEWING_PERIOD_FROM"),  //掲載期間(開始)
                             "VIEWING_PERIOD_TO"    =>  VARS::post("VIEWING_PERIOD_TO"),    //掲載期間(終わり)
                             "SUBMISSION_DATE"      =>  VARS::post("SUBMISSION_DATE"),      //提出期限
                             "REQUEST_ANSWER_FLG"   =>  VARS::post("REQUEST_ANSWER_FLG"),   //回答要
                             "REQUEST_ANSWER_PRG"   =>  VARS::post("REQUEST_ANSWER_PRG"),   //回答要PRG
                             "WRITING_DATE"         =>  VARS::post("WRITING_DATE"),         //作成日
                             "TRANSMISSION_DATE"    =>  VARS::post("TRANSMISSION_DATE"),    //通達日(hidden)
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );
        //パラメータ取得
        $this->cmd = VARS::request("cmd");
        $this->year = VARS::request("YEAR");
        if (VARS::get("SEND_DOC_NUMBER") != "") {
            $this->docNumber = VARS::get("SEND_DOC_NUMBER");
        }

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function clean() {}
    
    /* 入力項目チェック */
    function validateInput()
    {
        if ($this->cmd === 'update' || $this->cmd === 'notify' || $this->cmd === 'delete') {
            if(!isset($this->docNumber)){
                $this->setMessage("MSG304");
                return false;
            }
        }
        if ($this->cmd != 'notify' && $this->cmd != 'delete') {
            //文字数チェック
            if (strlen($this->field["NOTICE_TITLE"]) > 100) {
                $this->setWarning("MSG914", "(タイトル) 100バイトまで");
                return false;
            } else if (strlen($this->field["NOTICE_MESSAGE"]) > 2898){ 
                $this->setWarning("MSG914", "(通達文) 2898バイトまで");
                return false;
            } else if (strlen($this->field["REQUEST_ANSWER_FLG"]) == 1 && $this->field["REQUEST_ANSWER_PRG"] == ""){ 
                $this->setWarning("MSG301", "回答要の場合は、調査報告を指定して下さい。");
                return false;
            }
        }
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        //チェック処理
        if (!$this->validateInput()) {
            return;
        }
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //追加
        if ($this->cmd === 'add') {
            $this->maxNumber = $db->getOne(knje400aQuery::getMaxNumber($this));
            $this->maxNumber = $this->maxNumber + 1;
            knje400aQuery::getInsertMainQuery($db, $this);
            knje400aQuery::getUpdateSchoolQuery($db, $this->selectdata, $this);
        //更新
        } else if ($this->cmd === 'update') {
            knje400aQuery::getUpdateMainQuery($db, $this);
            knje400aQuery::getUpdateSchoolQuery($db, $this->selectdata, $this);
        //学校通達
        } else {
            knje400aQuery::getUpdateMainQuery($db, $this);
        }
        
        $db->commit();
        Query::dbCheckIn($db);
        
        $this->setMessage("MSG201");
        //更新後に各値をセット
        if ($this->cmd === 'add') {
            $this->docNumber = $this->maxNumber;
            $this->field["DOC_NUMBER"] = $this->maxNumber;
        } else {
            if ($this->cmd === 'notify') {
                $this->field["TRANSMISSION_DATE"] = date("Y-m-d");
            }
            $this->field["DOC_NUMBER"] = $this->docNumber;
        }
        return;
    }
    
    /* 削除処理 */
    function &getDeleteModel()
    {
        //チェック処理
        if (!$this->validateInput()) {
            return;
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        knje400aQuery::getDeleteMainQuery($db, $this);
        knje400aQuery::getDeleteSchoolQuery($db, $this);
        
        $db->commit();
        Query::dbCheckIn($db);
        
        $this->setMessage("MSG201");
        //削除後にNULLをセット
        $this->field = array();
        $this->docNumber = "";
        return;
    }
    
}
?>
