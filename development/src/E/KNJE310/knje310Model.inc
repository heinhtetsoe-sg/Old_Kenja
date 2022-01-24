<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje310Model.inc 69350 2019-08-23 08:44:42Z ishii $

class knje310Model extends Model {

    var $field      = array();
    var $cmd;
    var $year;
    var $seq;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $mode;                      //在籍卒業モード

    function knje310Model() {
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->GradeClass);
        $this->mode = "ungrd";      //在籍モード
    }

    function init() {

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array(
            "TOROKU_DATE"       =>  VARS::post("TOROKU_DATE"),          // 登録日
            "SCHOOL_SORT"       =>  VARS::post("SCHOOL_SORT"),          // 分類
            "STAT_CD"           =>  VARS::request("STAT_CD"),           // 学校・会社コード
            "STAT_NAME"         =>  VARS::post("STAT_NAME"),            // 学校・会社名
            "BUNAME"            =>  VARS::post("BUNAME"),               // 学部名
            "JUKEN_HOWTO"       =>  VARS::post("JUKEN_HOWTO"),          // 受験方法
            "RECOMMEND"         =>  VARS::post("RECOMMEND"),            // 推薦基準
            "SENKOU_KAI"        =>  VARS::post("SENKOU_KAI"),           // 校内選考会
            "SENKOU_FIN"        =>  VARS::post("SENKOU_FIN"),           // 校内選考結果
            "SENKOU_NO"         =>  VARS::post("SENKOU_NO"),            // 求人番号
            "REMARK"            =>  VARS::post("REMARK"),               // 備考
            "DECISION"          =>  VARS::post("DECISION"),             // 受験結果
            "PLANSTAT"          =>  VARS::post("PLANSTAT"),             // 進路状況
            "ATTEND"            =>  VARS::post("ATTEND"),               // 欠席点
            "AVG"               =>  VARS::post("AVG"),                  // 評定平均値点
            "TEST"              =>  VARS::post("TEST"),                 // 統一テスト
            "SEISEKI"           =>  VARS::post("SEISEKI"),              // 総合点
            "temp_cd"           =>  VARS::post("temp_cd"),              // 最初のCD
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
        $this->year = VARS::request("YEAR");
        if (VARS::request("SCHREGNO")=="")  $this->schregno = VARS::post("SCHREGNO");
        if (VARS::request("SEQ") == "")     $this->seq      = VARS::post("SEQ");
        if (VARS::request("YEAR") == "")    $this->year     = VARS::post("YEAR");
        $this->cmd = VARS::request("cmd");
        if (VARS::request("GrCl")) {
            $this->GradeClass = VARS::request("GrCl");
        }
    }

    // 学校・会社マスタ検索チェック
    function chkCollegeOrCompanyMst($stat_cd, $school_sort) {
        $row = knje310Query::getCollegeOrCompanyMst($stat_cd, $school_sort);
        $Msg = ("04" < $school_sort) ? "会社" : "学校";
        if (!isset($row)) {
            $this->setWarning("MSG303", "該当する".$Msg."コードはありません。$stat_cd");
            return false;
        }
        return true;
    }

    // 入力項目チェック
    function validateInput($kind) {
        // 必須入力項目をチェック
        if ($this->field["STAT_NAME"] == "") {
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
            $row = knje310Query::getOneRow($this->year, $this->seq, $this->schregno);

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
            if (trim($this->field["STAT_NAME"]) == "") {
                $this->setWarning("MSG301");
                return false;
            }

            // 文字数チェック
            if (strlen($this->field["STAT_NAME"]) > 120) {
                $this->setWarning("MSG914","(学校名・会社名) 120バイトまで");
                return false;
            }
            if (strlen($this->field["BUNAME"]) > 120) {
                $this->setWarning("MSG914","(学部名) 120バイトまで");
                return false;
            }
            if (strlen($this->field["RECOMMEND"]) > 120) {
                $this->setWarning("MSG914","(推薦基準) 120バイトまで");
                return false;
            }
            if (strlen($this->field["REMARK"]) > 60) {
                $this->setWarning("MSG914","(備考) 60バイトまで");
                return false;
            }

            // 学校・会社マスタ検索チェック
            if ($this->field["STAT_CD"] != "") {
                $Row = knje310Query::getCollegeOrCompanyMst($this->field["STAT_CD"], $this->field["SCHOOL_SORT"]);
                $Msg = ("04" < $this->field["SCHOOL_SORT"]) ? "会社" : "学校";
                if(!isset($Row)) {
                    $this->setWarning("MSG303","該当する".$Msg."コードはありません。".$this->field["STAT_CD"]);
                    return false;
                } else {
                    $this->field["STAT_NAME"]   = $Row["STAT_NAME"];
//                    if ($Msg == "学校") $this->field["BUNAME"] = $Row["BUNAME"];
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

        $result = knje310Query::getDeleteQuery($this->year, $this->seq, $this->schregno);

        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        $this->field = array();

        return $result;
    }

    function &insert() {
        $max_seq = knje310Query::getMaxSeq($this->control_data["年度"]);
        $this->seq = $max_seq + 1;
        $result = knje310Query::getInsertQuery($this->field, $this->control_data["年度"], $this->seq, $this->schregno);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        $this->year = $this->control_data["年度"];
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        $result = knje310Query::getUpdateQuery($this->field, $this->year, $this->seq, $this->schregno);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
?>
