<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje330Model.inc 69350 2019-08-23 08:44:42Z ishii $

class knje330Model extends Model {

    var $field      = array();
    var $cmd;
    var $seq;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $mode;                      //在籍卒業モード

    function knje330Model() {
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->GradeClass);
        $this->mode = "ungrd";      //在籍モード
    }

    function init() {
        $this->field = array(
            "STAT_DATE1"        =>  VARS::post("STAT_DATE1"),           // 試験日
            "HOUR_S"            =>  VARS::post("HOUR_S"),               // 試験時間（自）（時）
            "MINUTE_S"          =>  VARS::post("MINUTE_S"),             // 試験時間（自）（分）
            "HOUR_E"            =>  VARS::post("HOUR_E"),               // 試験時間（至）（時）
            "MINUTE_E"          =>  VARS::post("MINUTE_E"),             // 試験時間（至）（分）
            "STAT_DATE2"        =>  VARS::post("STAT_DATE2"),           // ２次試験日
            "STAT_CD"           =>  VARS::request("STAT_CD"),           // 学校コード
            "HOWTOEXAM"         =>  VARS::post("HOWTOEXAM"),            // 応募方法
            "HOWTOEXAM_REMARK"  =>  VARS::post("HOWTOEXAM_REMARK"),     // 応募方法・その他理由
            "CONTENTEXAM"       =>  VARS::post("CONTENTEXAM"),          // 試験内容
            "REASONEXAM"        =>  VARS::post("REASONEXAM"),           // 受験理由
            "THINKEXAM"         =>  VARS::post("THINKEXAM"),            // 備考
            "DECISION"          =>  VARS::post("DECISION"),             // 受験結果
            "PLANSTAT"          =>  VARS::post("PLANSTAT"),             // 進路状況
            "temp_cd"           =>  VARS::post("temp_cd"),              // 最初のCD

            "SCHOOL_NAME"       =>  VARS::post("SCHOOL_NAME"),          // 学校名
            "BUNAME"            =>  VARS::post("BUNAME"),               // 学部名
            "SCHOOL_SORT"       =>  VARS::post("SCHOOL_SORT"),          // 学校分類
            "AREA_NAME"         =>  VARS::post("AREA_NAME"),            // 所在地
            "TELNO"             =>  VARS::post("TELNO"),                // 電話番号
            "HAND_DATE"         =>  VARS::post("HAND_DATE"),            // 提出日

            "STAT_KIND"         =>  VARS::post("STAT_KIND"),            // 情報種別

            "UPDATED"           =>  VARS::post("UPDATED")               // 更新日付
            );

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
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->sonota = VARS::request("SONOTA");
    }

    // 学校マスタ検索チェック
    function chkCollegeMst($school_cd) {
        $row = knje330Query::getCollegeMst($school_cd);
        if (!isset($row)) {
            $this->setWarning("MSG303", "該当する学校コードはありません。$school_cd");
            return false;
        }
        return true;
    }

    // 入力項目チェック
    function validateInput($kind) {
        // 必須入力項目をチェック
        if ($this->field["SCHOOL_NAME"] == "") {
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
            $row = knje330Query::getOneRow($this->control_data["年度"], $this->seq, $this->schregno);

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
            if (trim($this->field["SCHOOL_NAME"]) == "") {
                $this->setWarning("MSG301");
                return false;
            }
            if ($this->field["HOWTOEXAM"] == $this->sonota && $this->field["HOWTOEXAM_REMARK"] == "") {
                $this->setWarning("MSG301","(応募方法・その他理由)");
                return false;
            }

            // 文字数チェック
            if (strlen($this->field["SCHOOL_NAME"]) > 120) {
                $this->setWarning("MSG914","(学校名) 120バイトまで");
                return false;
            }
            if (strlen($this->field["BUNAME"]) > 120) {
                $this->setWarning("MSG914","(学部名) 120バイトまで");
                return false;
            }
            if (strlen($this->field["TELNO"]) > 16) {
                $this->setWarning("MSG914","(電話番号) 16バイトまで");
                return false;
            }
            if (strlen($this->field["AREA_NAME"]) > 30) {
                $this->setWarning("MSG914","(所在地) 30バイトまで");
                return false;
            }
            if (strlen($this->field["HOWTOEXAM_REMARK"]) > 120) {
                $this->setWarning("MSG914","(応募方法・その他理由) 120バイトまで");
                return false;
            }
            if (strlen($this->field["CONTENTEXAM"]) > 120) {
                $this->setWarning("MSG914","(試験内容) 120バイトまで");
                return false;
            }

            // 文字数チェック(改行も含む)
            if(strlen($this->field["REASONEXAM"]) > 242){
                $this->setWarning("MSG914", "(受験理由) 242バイトまで");
                return false;
            }
            if(strlen($this->field["THINKEXAM"]) > 486){
                $this->setWarning("MSG914", "(備考) 486バイトまで");
                return false;
            }
            // 行数のチェック
            if (substr_count($this->field["REASONEXAM"], "\n") >= 2){
                $this->setWarning("MSG914", "(受験理由) の行数が範囲を超えました。");
                return false;
            }
            // 学校マスタ検索チェック
            if ($this->field["STAT_CD"] != "") {
                $Row = knje330Query::getCollegeMst($this->field["STAT_CD"]);
                if(!isset($Row)) {
                    $this->setWarning("MSG303","該当する学校コードはありません。".$this->field["STAT_CD"]);
                    return false;
                } else {
                    $this->field["SCHOOL_NAME"] = $Row["SCHOOL_NAME"];
                    $this->field["SCHOOL_SORT"] = $Row["SCHOOL_SORT"];
                    $this->field["AREA_NAME"]   = $Row["AREA_NAME"];
                    $this->field["TELNO"]       = $Row["TELNO"];
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

        $result = knje330Query::getDeleteQuery($this->control_data["年度"], $this->seq, $this->schregno);

        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        $this->field = array();

        return $result;
    }

    function &insert() {
        $max_seq = knje330Query::getMaxSeq($this->control_data["年度"]);
        $this->seq = $max_seq + 1;
        $result = knje330Query::getInsertQuery($this->field, $this->control_data["年度"], $this->seq, $this->schregno);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        $result = knje330Query::getUpdateQuery($this->field, $this->control_data["年度"], $this->seq, $this->schregno);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
?>
