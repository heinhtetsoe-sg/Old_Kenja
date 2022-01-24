<?php

require_once('for_php7.php');

//定数定義
class knjxcommi_detailModel extends Model
{
    public $field = array();
    public $cmd;
    public $programid = "";
    public $schregno = "";
    public $seq = "";
    public $date = "";
    public $detail_seq = "";

    //メンバ変数の初期化
    public function knjxcommi_detailModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->programid);
            unset($this->schregno);
            unset($this->seq);
            unset($this->date);
            unset($this->detail_seq);
        }

        $this->field = array("SEQ"              =>  VARS::post("SEQ"),
                             "DETAIL_DATE"      =>  VARS::post("DETAIL_DATE"),
                             "DETAIL_SEQ"       =>  VARS::post("DETAIL_SEQ"),
                             "DETAIL_REMARK"    =>  VARS::post("DETAIL_REMARK"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        if (VARS::get("SEQ")) {
            $this->seq = VARS::get("SEQ");
        }
        if (VARS::get("DETAIL_DATE")) {
            $this->date = VARS::get("DETAIL_DATE");
        }
        if (VARS::get("DETAIL_SEQ")) {
            $this->detail_seq = VARS::get("DETAIL_SEQ");
        }

        if (VARS::get("PROGRAMID")) {
            $this->programid = VARS::get("PROGRAMID");
        }
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("SCHKIND")) {
            $this->schKind = VARS::get("SCHKIND");
        }

        $this->cmd = VARS::request("cmd");
    }
    
    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();

        //必須項目チェック
        if (!$this->schregno) {
            $this->setWarning("生徒を指定してください。");
            return false;
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            //リストより選択していない
            if (!$this->seq || !$this->date || !$this->detail_seq) {
                $this->setWarning("MSG308");
                return false;
            }
            //キー変更
            if (($this->seq != $this->field["SEQ"]) ||
               ($this->date != $this->field["DETAIL_DATE"]) ||
               ($this->detail_seq != $this->field["DETAIL_SEQ"])) {
                $this->setWarning("MSG308");
                return false;
            }
            //必須項目チェック
            if (!$this->field["DETAIL_REMARK"]) {
                $this->setWarning("MSG301", "　　（記録備考）");
                return false;
            }
        }

        if ($this->cmd == "add") {
            //必須項目チェック
            if (!$this->field["SEQ"]) {
                $this->setWarning("MSG301", "　　（委員会）");
                return false;
            }
            if (!$this->field["DETAIL_DATE"]) {
                $this->setWarning("MSG301", "　　（日付）");
                return false;
            }
            if (!$this->field["DETAIL_REMARK"]) {
                $this->setWarning("MSG301", "　　（記録備考）");
                return false;
            }
            //日付チェック
            $sdate = CTRL_YEAR.'/04/01';
            $edate = (CTRL_YEAR+1).'/03/31';
            if ($this->field["DETAIL_DATE"] < $sdate || $edate < $this->field["DETAIL_DATE"]) {
                $this->setWarning("MSG901", "(".$sdate."～".$edate.")");
                return false;
            }
        }

        //桁数チェック
        if (strlen($this->field["DETAIL_REMARK"]) > 45) {
            $this->setWarning("MSG914", "(記録備考は45バイトまでです)");
            return false;
        }

        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    public function &delete()
    {
        knjxcommi_detailQuery::getDeleteQuery($this);

        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->seq = $this->date = $this->detail_seq = array();
            $this->field = array();
        }
        return;
    }

    //INSERT・UPDATE時の内部メソッド
    public function &insert()
    {
        $detail_seq = knjxcommi_detailQuery::getInsertQuery($this);

        if (!isset($this->warning)) {
            $this->seq = $this->field["SEQ"];
            $this->date = $this->field["DETAIL_DATE"];
            $this->detail_seq = $detail_seq;
            $this->setMessage("MSG201");
        }
        return;
    }
}
