<?php

require_once('for_php7.php');

//定数定義
class knjp708Model extends Model {
    var $field = array();
    var $cmd;
    var $reductionDivCd;

    //メンバ変数の初期化
    function knjp708Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->reductionDivCd);
            unset($this->schoolKind);
        }

        $this->cmd = VARS::request("cmd");

        $this->year       = CTRL_YEAR;
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        if (VARS::get("REDUCTION_DIV_CD")) {
            $this->reductionDivCd = sprintf("%02d",VARS::get("REDUCTION_DIV_CD"));
        }

        $this->field = array("REDUCTION_DIV_CD"     => VARS::post("REDUCTION_DIV_CD"),
                             "REDUCTION_DIV_NAME"   => VARS::post("REDUCTION_DIV_NAME"),
                             "SCHOLARSHIP"          => VARS::post("SCHOLARSHIP"),
                             "SEMESTER_DIV"         => VARS::post("SEMESTER_DIV"),
                             "REDUCTION_DIV"        => VARS::post("REDUCTION_DIV"),
                             "MONEY_DIV"            => VARS::post("MONEY_DIV"), 
                             "REDUCTION_TIMING"     => VARS::post("REDUCTION_TIMING")
                             );

        //各減免対象項目の入力値を保持
        $db = Query::dbCheckOut();
        if (is_array($this->rowIdArr)) {
            foreach($this->rowIdArr as $rowId) {
                $this->field["MONEY_DIV".$rowId]       = VARS::post("MONEY_DIV".$rowId);
                if ($this->field["MONEY_DIV".$rowId] == "1") {
                    $this->field["NUMERATOR".$rowId]   = VARS::post("NUMERATOR".$rowId);
                    $this->field["DENOMINATOR".$rowId] = VARS::post("DENOMINATOR".$rowId);
                    $this->field["MONEY".$rowId] = "";
                } else if ($this->field["MONEY_DIV".$rowId] == "2") {
                    $this->field["NUMERATOR".$rowId]   = "";
                    $this->field["DENOMINATOR".$rowId] = "";
                    $this->field["MONEY".$rowId]       = VARS::post("MONEY".$rowId);
                }
            }
        }
        Query::dbCheckIn($db);

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["REDUCTION_DIV_CD"]) == 0) {
            $this->setWarning("MSG301","(コード)");
            return false;
        }

        if ($kind == "insert" || $kind == "update") {
            if (strlen($this->field["REDUCTION_DIV_NAME"]) == 0) {
                $this->setWarning("MSG301","(減免種別名)");
                return false;
            }
            if (strlen($this->field["SCHOLARSHIP"]) == 0) {
                $this->setWarning("MSG301","(特待区分)");
                return false;
            }
        }
        //バイト数チェック
        if (strlen($this->field["REDUCTION_DIV_NAME"]) > 45) {
            $this->setWarning("MSG915","(減免種別名は45バイトまでです。)");
            return false;
        }
        if (($kind=="update" || $kind=="delete") && $this->reductionDivCd != $this->field["REDUCTION_DIV_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjp708Query::getRow($this, $this->field["REDUCTION_DIV_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjp708Query::getRow($this, $this->reductionDivCd );
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row2)) {
            $this->setWarning("MSG303");
            return false;
        }

        //減免対象項目が一つも入力されていない場合
        if (is_array($this->rowIdArr) &&get_count($this->rowIdArr) > 0) {
            $chkFlg = false;
            foreach($this->rowIdArr as $rowId) {
                if ($this->field["MONEY_DIV".$rowId] == "1") {
                    if(trim($this->field["NUMERATOR".$rowId]) && trim($this->field["DENOMINATOR".$rowId])) {
                        $chkFlg = true;
                        break;
                    }
                } else if ($this->field["MONEY_DIV".$rowId] == "2") {
                    if(trim($this->field["MONEY".$rowId])) {
                        $chkFlg = true;
                        break;
                    }
                }
            }
            if (!$chkFlg) {
                $this->setWarning("MSG304", "入力項目の設定を1件以上してください。");
                return false;
            }
        }

        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjp708Query::getInsertQuery($this, $this->field);
            $this->reductionDivCd = sprintf("%02d",$this->field["REDUCTION_DIV_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjp708Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp708Query::getDeleteQuery($this, $this->field);
            $this->reductionDivCd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp708Query::CheckData($this, $this->year);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp708Query::CheckData($this, $this->year - 1);
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp708Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
