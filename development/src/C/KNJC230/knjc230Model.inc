<?php

require_once('for_php7.php');

class knjc230Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;
    public $hr_name;
    public $s_period;
    public $e_period;
    public $arr_period;
    public $selectdata = array();
    public $diCd;

    public function knjc230Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SCHKIND"          =>  VARS::request("SCHKIND"),       //校種
                             "CLUB"             =>  VARS::request("CLUB"),          //クラブ
                             "FROMDATE"         =>  VARS::post("FROMDATE"),         //開始日付
                             "TODATE"           =>  VARS::post("TODATE"),           //終了日付
                             "FROMPERIOD"       =>  VARS::post("FROMPERIOD"),       //開始校時
                             "TOPERIOD"         =>  VARS::post("TOPERIOD"),         //終了校時
                             "SONOTA"           =>  VARS::post("SONOTA"),           //その他
                             "DI_CD"            =>  VARS::post("DI_CD"),            //勤怠
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        if (VARS::get("hr_name")) {
            $this->hr_name = VARS::get("hr_name");
        }

        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(',', VARS::post("selectdata"));
        $this->selectStudent = explode(',', VARS::post("selectStudent"));
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //キーの必須選択チェック
        if ($this->field["FROMDATE"] == "") {
            $this->setWarning("MSG301", "(開始日付)");
            return false;
        }
        if ($this->field["TODATE"] == "") {
            $this->setWarning("MSG301", "(終了日付)");
            return false;
        }
        //年度内チェック
        $sdate = CTRL_YEAR ."/04/01";
        $edate = CTRL_YEAR + 1 ."/03/31";
        if (!($sdate <= $this->field["FROMDATE"]) || !($this->field["FROMDATE"] <= $edate)) {
            $this->warning = "処理年度範囲外です。(開始日付)";
            return false;
        }
        if (!($sdate <= $this->field["TODATE"]) || !($this->field["TODATE"] <= $edate)) {
            $this->warning = "処理年度範囲外です。(終了日付)";
            return false;
        }
        //大小チェック
        if ($this->field["TODATE"] < $this->field["FROMDATE"]) {
            $this->warning = "開始日付が終了日付を超えています。";
            return false;
        }
        if (($this->field["TODATE"] == $this->field["FROMDATE"]) && ($this->field["TOPERIOD"] < $this->field["FROMPERIOD"])) {
            $this->warning = "開始校時が終了校時を超えています。";
            return false;
        }
        if ($this->field["FROMPERIOD"] == "") {
            $this->field["FROMPERIOD"]  = $this->s_period;
        }
        if ($this->field["TOPERIOD"] == "") {
            $this->field["TOPERIOD"]    = $this->e_period;
        }
        if (($this->field["TODATE"] == $this->field["FROMDATE"]) && ($this->field["TOPERIOD"] < $this->field["FROMPERIOD"])) {
            $this->warning = "開始校時が終了校時を超えています。";
            return false;
        }
        //項目数チェック
        if (get_count($this->selectdata) > 10) {
            $this->warning = "症状・理由は10項目までです。";
            return false;
        }
        
        //その他のバイト数チェック
        if (strlen($this->field["SONOTA"]) > 30) {
            $this->setWarning("MSG914", "その他は30byteまでです。");
            $this->selectdata  = explode(',', VARS::get("selectdata"));
            return false;
        }

        return true;
    }

    //追加および更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    //UPDATE（直近データが「なし」の場合、追加。「なし」以外の場合、更新。）
    public function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->selectStudent as $key => $schregNo) {
            $query = knjc230Query::getMaxSeqno();
            $max_seqno = $db->getOne($query);
            knjc230Query::getInsertQuery($db, $this, $schregNo, $max_seqno);
            $this->message = "データを新規作成しました。";
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
