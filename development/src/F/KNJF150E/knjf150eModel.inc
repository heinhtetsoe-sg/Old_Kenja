<?php

require_once('for_php7.php');

class knjf150eModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $s_period;
    var $e_period;
    var $selectdata = array();

    function knjf150eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
            unset($this->semester);
            unset($this->selectdata);
        }

        $this->field = array("SEQNO"            =>  VARS::post("SEQNO"),            //直近データ
                             "FROM_DATE"        =>  VARS::post("FROM_DATE"),        //開始日付
                             "TO_DATE"          =>  VARS::post("TO_DATE"),          //終了日付
                             "FROM_PERIOD"      =>  VARS::post("FROM_PERIOD"),      //開始校時
                             "TO_PERIOD"        =>  VARS::post("TO_PERIOD"),        //終了校時
                             "INPUT_TYPE"       =>  VARS::post("INPUT_TYPE"),       //閉鎖区分
                             "DI_CD"            =>  VARS::post("DI_CD"),            //勤怠
                             "DI_REMARK_CD"     =>  VARS::post("DI_REMARK_CD"),     //症状・理由
                             "DI_REMARK"        =>  VARS::post("DI_REMARK")         //症状・理由（その他）
                            );

        $this->cmd          = VARS::request("cmd");
        $this->semester     = ($this->semester == "") ? CTRL_SEMESTER : VARS::post("SEMESTER");
        $this->selectdata   = explode(',',VARS::post("selectdata"));

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //新規チェック
        if ($this->field["SEQNO"]) {
            $this->setWarning("作成済みのデータは実行できません。");
            return false;
        }

        //キーの必須選択チェック
        $errorLabel = "";
        if ($this->field["FROM_DATE"] == "") {
            $errorLabel .= (strlen($errorLabel) ? "," : "")."開始日付";
        }
        if ($this->field["TO_DATE"] == "") {
            $errorLabel .= (strlen($errorLabel) ? "," : "")."終了日付";
        }
        if ($this->field["INPUT_TYPE"] == "") {
            $errorLabel .= (strlen($errorLabel) ? "," : "")."閉鎖区分";
        }
        if ($this->field["DI_CD"] == "") {
            $errorLabel .= (strlen($errorLabel) ? "," : "")."勤怠";
        }
        if (strlen($errorLabel)) {
            $this->setWarning("MSG301", "(".$errorLabel.")");
            return false;
        }
        if (!$this->selectdata[0]) {
            $errorLabel = ($this->field["INPUT_TYPE"] == '3') ? '年組' : '学年';
            $this->setWarning("MSG310", "(".$errorLabel.")");
            return false;
        }

        //日付チェック（学期内）
        $sdate  = $this->control["学期開始日付"][$this->semester];
        $edate  = $this->control["学期終了日付"][$this->semester];

        if (!($sdate <= $this->field["FROM_DATE"]) || !($this->field["FROM_DATE"] <= $edate)) {
            $this->warning = "学期範囲外です。(開始日付)";
            return false;
        }
        if (!($sdate <= $this->field["TO_DATE"]) || !($this->field["TO_DATE"] <= $edate)) {
            $this->warning = "学期範囲外です。(終了日付)";
            return false;
        }
        //大小チェック
        if ($this->field["TO_DATE"] < $this->field["FROM_DATE"]) {
            $this->warning = "開始日付が終了日付を超えています。";
            return false;
        }
        if ($this->field["FROM_PERIOD"] == "")  $this->field["FROM_PERIOD"]  = $this->s_period;
        if ($this->field["TO_PERIOD"] == "")    $this->field["TO_PERIOD"]    = $this->e_period;
        if (($this->field["TO_DATE"] == $this->field["FROM_DATE"]) && ($this->field["TO_PERIOD"] < $this->field["FROM_PERIOD"])) {
            $this->warning = "開始校時が終了校時を超えています。";
            return false;
        }

        //文字数チェック
        if (strlen($this->field["DI_REMARK"]) > 60) {
            $this->setWarning("MSG914", "症状・理由（その他）は全角20文字までです。");
            return false;
        }

        //時間割一覧取得
        if ($this->Properties["hibiNyuuryoku"] != 'ATTEND_DAY_DAT') {
            $this->array = knjf150eQuery::getSchChrDat($this);
            //存在チェック（時間割）
            if (!get_count($this->array)) {
                $this->setWarning("時間割がありません。");
                return false;
            }
        } else {
            $this->array = knjf150eQuery::getAttendDayDat($this);
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    //UPDATE
    function &update() {
        //MAX値取得
        $ret_seqno = knjf150eQuery::getMaxSeqno();
        $this->field["SEQNO"] = (($ret_seqno) ? $ret_seqno : 0) + 1;

        //更新
        knjf150eQuery::getInsertQuery($this);
        $this->message = "データを新規作成しました。";

        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの必須選択チェック
        if ($this->field["SEQNO"] == "") {
            $this->setWarning("MSG304", "(実行済みデータ)");
            return false;
        }

        knjf150eQuery::getDeleteQuery($this);
        unset($this->field);
        $this->selectdata = array();
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
