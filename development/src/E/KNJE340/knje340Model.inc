<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje340Model.inc 69350 2019-08-23 08:44:42Z ishii $

class knje340Model extends Model {

    var $field      = array();
    var $cmd;
    var $seq;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $mode;                      //在籍卒業モード

    function knje340Model() {
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->GradeClass);
        $this->mode = "ungrd";      //在籍モード
    }

    function init() {
        $this->field = array(
            "JOB_DATE1"         =>  VARS::post("JOB_DATE1"),            // 会社訪問日
            "HOUR_S_JOB"        =>  VARS::post("HOUR_S_JOB"),           // 会社訪問時間（自）（時）
            "MINUTE_S_JOB"      =>  VARS::post("MINUTE_S_JOB"),         // 会社訪問時間（自）（分）
            "HOUR_E_JOB"        =>  VARS::post("HOUR_E_JOB"),           // 会社訪問時間（至）（時）
            "MINUTE_E_JOB"      =>  VARS::post("MINUTE_E_JOB"),         // 会社訪問時間（至）（分）
            "STAT_CD"           =>  VARS::request("STAT_CD"),           // 会社コード
            "HOWTOEXAM"         =>  VARS::post("HOWTOEXAM"),            // 応募方法
            "HOWTOEXAM_REMARK"  =>  VARS::post("HOWTOEXAM_REMARK"),     // 応募方法・その他理由
            "JOB_REMARK"        =>  VARS::post("JOB_REMARK"),           // 会社訪問携帯品
            "JOB_CONTENT"       =>  VARS::post("JOB_CONTENT"),          // 会社訪問内容
            "JOB_THINK"         =>  VARS::post("JOB_THINK"),            // 会社訪問備考
            "JOBEX_DATE1"       =>  VARS::post("JOBEX_DATE1"),          // 入社試験日
            "HOUR_S_JOBEX"      =>  VARS::post("HOUR_S_JOBEX"),         // 入社試験時間（自）（時）
            "MINUTE_S_JOBEX"    =>  VARS::post("MINUTE_S_JOBEX"),       // 入社試験時間（自）（分）
            "HOUR_E_JOBEX"      =>  VARS::post("HOUR_E_JOBEX"),         // 入社試験時間（至）（時）
            "MINUTE_E_JOBEX"    =>  VARS::post("MINUTE_E_JOBEX"),       // 入社試験時間（至）（分）
            "JOBEX_REMARK"      =>  VARS::post("JOBEX_REMARK"),         // 入社試験携帯品
            "JOBEX_CONTENT"     =>  VARS::post("JOBEX_CONTENT"),        // 入社試験内容
            "JOBEX_THINK"       =>  VARS::post("JOBEX_THINK"),          // 入社試験感想
            "DECISION"          =>  VARS::post("DECISION"),             // 受験結果
            "PLANSTAT"          =>  VARS::post("PLANSTAT"),             // 進路状況
            "temp_cd"           =>  VARS::post("temp_cd"),              // 最初のCD

            "COMPANY_NAME"      =>  VARS::post("COMPANY_NAME"),         // 会社名
            "SHUSHOKU_ADDR"     =>  VARS::post("SHUSHOKU_ADDR"),        // 就業場所
            "TELNO"             =>  VARS::post("TELNO"),                // 電話番号
            "HAND_DATE"         =>  VARS::post("HAND_DATE"),            // 提出日

            "STAT_KIND"         =>  VARS::post("STAT_KIND"),            // 情報種別

            "UPDATED"           =>  VARS::post("UPDATED")               // 更新日付
            );

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //在籍卒業モード
        if (VARS::get("mode")){
            $this->mode = VARS::get("mode");
        }
        if (VARS::request("SCHREGNO")) {
            $this->schregno = VARS::request("SCHREGNO");
        }
        $this->seq = VARS::request("SEQ");
        if (VARS::request("SCHREGNO")=="")  $this->schregno = VARS::post("SCHREGNO");
        if (VARS::request("SEQ") == "")     $this->seq      = VARS::post("SEQ");
        $this->cmd = VARS::request("cmd");
        if (VARS::request("GrCl")) {
            $this->GradeClass = VARS::request("GrCl");
        }

        $this->sonota = VARS::request("SONOTA");
    }

    // 会社マスタ検索チェック
    function chkCompanyMst($company_cd) {
        $row = knje340Query::getCompanyMst($company_cd);
        if (!isset($row)) {
            $this->setWarning("MSG303", "該当する会社コードはありません。$company_cd");
            return false;
        }
        return true;
    }

    // 入力項目チェック
    function validateInput($kind) {
        // 必須入力項目をチェック
        if ($this->field["COMPANY_NAME"] == "") {
            if ($kind=="insert") {
                $this->setWarning("MSG301");
            } else {
                $this->setWarning("MSG308");
            }
            return false;
        }

        if ($this->schregno == "") {
            $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
            return false;
        }

        if ($kind == "delete") {
            if ($this->field["STAT_KIND"] == "1") {
                $this->setWarning("MSG919","校内選考データは、削除できません。");
                return false;
            }
        }

        $this->isload = "0";
        if ($kind == "update" || $kind == "delete") {
            // リストより選択していない
            if ($this->field["temp_cd"]==""){
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->seq == "") {
                $this->setWarning("MSG308");
                return false;
            }

            // １レコード取得
            $row = knje340Query::getOneRow($this->control_data["年度"], $this->seq, $this->schregno);

            // キー値が変更されていないか?
            if ($row["SCHREGNO"] != $this->field["temp_cd"]) {
                $this->isload = "1";
                $this->setWarning("MSG308");
                return false;
            }
            // 更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            // 更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }

        if ($kind == "update" || $kind == "insert") {
            if (trim($this->field["COMPANY_NAME"]) == "") {
                $this->setWarning("MSG301");
                return false;
            }
            if ($this->field["HOWTOEXAM"] == $this->sonota && $this->field["HOWTOEXAM_REMARK"] == "") {
                $this->setWarning("MSG301","(応募方法・その他理由)");
                return false;
            }

            // 文字数チェック
            if (strlen($this->field["COMPANY_NAME"]) > 120) {
                $this->setWarning("MSG914","(会社名) 120バイトまで");
                return false;
            }
            if (strlen($this->field["SHUSHOKU_ADDR"]) > 120) {
                $this->setWarning("MSG914","(就業場所) 120バイトまで");
                return false;
            }
            if (strlen($this->field["TELNO"]) > 16) {
                $this->setWarning("MSG914","(電話番号) 16バイトまで");
                return false;
            }
            if (strlen($this->field["HOWTOEXAM_REMARK"]) > 120) {
                $this->setWarning("MSG914","(応募方法・その他理由) 120バイトまで");
                return false;
            }
            if (strlen($this->field["JOB_REMARK"]) > 120) {
                $this->setWarning("MSG914","(会社訪問携帯品) 120バイトまで");
                return false;
            }
            if (strlen($this->field["JOBEX_REMARK"]) > 120) {
                $this->setWarning("MSG914","(入社試験携帯品) 120バイトまで");
                return false;
            }

            // 文字数チェック(改行も含む)
            if (strlen($this->field["JOB_CONTENT"]) > 242) {
                $this->setWarning("MSG914", "(会社訪問内容) 242バイトまで");
                return false;
            }
            if (strlen($this->field["JOB_THINK"]) > 486) {
                $this->setWarning("MSG914", "(会社訪問備考) 486バイトまで");
                return false;
            }
            if (strlen($this->field["JOBEX_CONTENT"]) > 242) {
                $this->setWarning("MSG914", "(入社試験内容) 242バイトまで");
                return false;
            }
            if (strlen($this->field["JOBEX_THINK"]) > 486) {
                $this->setWarning("MSG914", "(入社試験感想) 486バイトまで");
                return false;
            }
            // 行数のチェック
            if (substr_count($this->field["JOB_CONTENT"], "\n") >= 2) {
                $this->setWarning("MSG914", "(会社訪問内容) の行数が範囲を超えました。");
                return false;
            }
            if (substr_count($this->field["JOBEX_CONTENT"], "\n") >= 2) {
                $this->setWarning("MSG914", "(入社試験内容) の行数が範囲を超えました。");
                return false;
            }
            if (substr_count($this->field["JOBEX_THINK"], "\n") >= 4) {
                $this->setWarning("MSG914", "(入社試験感想) の行数が範囲を超えました。");
                return false;
            }
            // 会社マスタ検索チェック
            if ($this->field["STAT_CD"] != "") {
                $Row = knje340Query::getCompanyMst($this->field["STAT_CD"]);
                if (!isset($Row)) {
                    $this->setWarning("MSG303","該当する会社コードはありません。".$this->field["STAT_CD"]);
                    return false;
                } else {
                    $this->field["COMPANY_NAME"]    = $Row["COMPANY_NAME"];
                    $this->field["SHUSHOKU_ADDR"]   = $Row["SHUSHOKU_ADDR"];
                    $this->field["TELNO"]           = $Row["TELNO"];
                }
            }
        }

        return true;
    }

    /**
     * 編集完了用のモデルを返します
     */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
        }
        return;
    }

    /**
     * 削除用のモデルを返します
     */
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }

    function &delete() {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        $result = knje340Query::getDeleteQuery($this->control_data["年度"], $this->seq, $this->schregno);

        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        $this->field = array();

        return $result;
    }

    function &insert() {
        $max_seq = knje340Query::getMaxSeq($this->control_data["年度"]);
        $this->seq = $max_seq + 1;
        $result = knje340Query::getInsertQuery($this->field, $this->control_data["年度"], $this->seq, $this->schregno);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        $result = knje340Query::getUpdateQuery($this->field, $this->control_data["年度"], $this->seq, $this->schregno);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
?>
