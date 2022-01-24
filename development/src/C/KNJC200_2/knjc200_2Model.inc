<?php

require_once('for_php7.php');

class knjc200_2Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $hr_name;
    var $s_period;
    var $e_period;
    var $arr_period;
    var $selectdata = array();

    function knjc200_2Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHREGNO"         =>  VARS::request("SCHREGNO"),      //生徒
                             "SEQNO"            =>  VARS::post("SEQNO"),            //直近データ
                             "CONTACTERDIV"     =>  VARS::post("CONTACTERDIV"),     //連絡元
                             "CONTACTER"        =>  VARS::post("CONTACTER"),        //連絡元その他
                             "FROMDATE"         =>  VARS::post("FROMDATE"),         //開始日付
                             "TODATE"           =>  VARS::post("TODATE"),           //終了日付
                             "FROMPERIOD"       =>  VARS::post("FROMPERIOD"),       //開始校時
                             "TOPERIOD"         =>  VARS::post("TOPERIOD"),         //終了校時
                             "SONOTA"           =>  VARS::post("SONOTA"),           //その他
                             "CALLBACK"         =>  VARS::post("CALLBACK"),         //返電
                             "DI_CD"            =>  VARS::post("DI_CD"),            //勤怠
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        if (VARS::get("hr_name")) $this->hr_name = VARS::get("hr_name");

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = explode(',',VARS::post("selectdata"));
    }

    //入力項目チェック
    function validateInput()
    {
        //キーの必須選択チェック
        if ($this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG310", "(生徒)");
            return false;
        }
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
        if ($this->field["FROMPERIOD"] == "")   $this->field["FROMPERIOD"]  = $this->s_period;
        if ($this->field["TOPERIOD"] == "")     $this->field["TOPERIOD"]    = $this->e_period;
        if (($this->field["TODATE"] == $this->field["FROMDATE"]) && ($this->field["TOPERIOD"] < $this->field["FROMPERIOD"])) {
            $this->warning = "開始校時が終了校時を超えています。";
            return false;
        }
        //文字数チェック
        if (strlen($this->field["CONTACTER"]) > 90) {
            $this->setWarning("MSG915","( 連絡元その他は90バイトまでです )");
            $this->field["CONTACTERDIV"] = "2";
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
            $this->selectdata  = explode(',',VARS::get("selectdata"));
            return false;
        }

        return true;
    }

    //追加および更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }

    //UPDATE（直近データが「なし」の場合、追加。「なし」以外の場合、更新。）
    function &update()
    {
        if ($this->field["SEQNO"] == "") {
            $ret_seqno = knjc200_2Query::getMaxSeqno();
            $this->field["SEQNO"] = $ret_seqno;//受付番号Max値をセット
            knjc200_2Query::getInsertQuery($this);
            $this->message = "データを新規作成しました。";
        } else {
            knjc200_2Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの必須選択チェック
        if ($this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG308", "(生徒)");
            return false;
        }
        if ($this->field["SEQNO"] == "") {
            $this->setWarning("MSG310", "(この生徒の登録済みデータ)");
            return false;
        }

        $ret_val = knjc200_2Query::getDeleteQuery($this);
        unset($this->field["SEQNO"]);
        $this->message = "データを削除しました。";
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
