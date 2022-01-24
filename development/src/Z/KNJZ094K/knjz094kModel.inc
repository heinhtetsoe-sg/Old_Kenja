<?php

require_once('for_php7.php');

//定数定義
class knjz094kModel extends Model {
    var $field = array();
    var $cmd;
    var $grade;         //学年
    var $reductionmoney;//基準額
    var $reductionAddMoney;//支援加算額
    var $year;

    //メンバ変数の初期化
    function knjz094kModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->grade);
            unset($this->reductionmoney);
            unset($this->reductionAddMoney);
            unset($this->income_low);
            unset($this->income_high);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");

        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("REDUCTION_SEQ")) {
            $this->reduction_seq = VARS::get("REDUCTION_SEQ");
        }
        if (VARS::get("REDUCTIONMONEY")) {
            $this->reductionmoney = VARS::get("REDUCTIONMONEY");
        }
        if (VARS::get("INCOME_LOW")) {
            $this->income_low = VARS::get("INCOME_LOW");
        } elseif (!VARS::get("INCOME_LOW")) {
            $this->income_low = 0;
        }
        if (VARS::get("INCOME_HIGH")) {
            $this->income_high = VARS::get("INCOME_HIGH");
        }

        $this->field = array("YEAR"                 => $this->year,
                             "GRADE"                => VARS::post("GRADE"),                 //学年
                             "REDUCTIONMONEY"       => VARS::post("REDUCTIONMONEY"),        //基準額
                             "INCOME_LOW"           => VARS::post("INCOME_LOW"),            //下限額1
                             "INCOME_HIGH"          => VARS::post("INCOME_HIGH"),           //上限額1
                             "REDUCTIONREMARK"      => VARS::post("REDUCTIONREMARK"),       //備考
                             "REDUCTION_SEQ"        => $this->reduction_seq                 //連番
                             );
    }

    //入力項目チェック
    function validateInput($kind) {
        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["GRADE"]) == 0) {
                $this->setWarning("MSG301","( 学年 )");
                return false;
            }
            if (strlen($this->field["REDUCTIONMONEY"]) == 0) {
                $this->setWarning("MSG301","( 基準額)");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["REDUCTIONMONEY"]) > 8) {
                $this->setWarning("MSG915","( 基準額は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["INCOME_LOW"]) > 8) {
                $this->setWarning("MSG915","( 課税総所得下限額は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["INCOME_HIGH"]) > 8) {
                $this->setWarning("MSG915","( 課税総所得上限額は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REDUCTIONREMARK"]) > 75) {
                $this->setWarning("MSG915","( 備考は75バイトまでです。)");
                return false;
            }

            //上限額または下限額が空の場合
            $this->field["INCOME_LOW"]  = $this->field["INCOME_LOW"] ? $this->field["INCOME_LOW"] : 0;
            if (strlen(trim($this->field["INCOME_HIGH"])) == 0) {
                $this->field["INCOME_HIGH"] = 99999999;
            } elseif ($this->field["INCOME_HIGH"] == 0) {
                $this->field["INCOME_HIGH"] = 0;
            } else{
                $this->field["INCOME_HIGH"] = $this->field["INCOME_HIGH"];
            }

            //上限額または下限額の妥当性チェック
            $result_chk = knjz094kQuery::Income_Check1($this,$kind);
            if ($result_chk=="low") {
                $this->setWarning("MSG913","( 下限額1が不正です。)");
                return false;
            } elseif ($result_chk=="high") {
                $this->setWarning("MSG913","( 上限額1が不正です。)");
                return false;
            }

            //上限額・下限額の大小(矛盾)チェック
            if (($this->field["INCOME_LOW"]) && ($this->field["INCOME_HIGH"]) && ($this->field["INCOME_LOW"] > $this->field["INCOME_HIGH"])) {
                $this->setWarning("MSG913");
                return false;
            }
        }

        if (($kind == "update" || $kind == "delete") && $this->grade != $this->field["GRADE"]) {
            $this->setWarning("MSG308");
            return false;
        }

        $row = knjz094kQuery::getRow($this, 2);      //入力項目の１レコード取得
        $cntch = knjz094kQuery::getCntch($this);    //整合性

        if (($kind == "insert") && is_array($row)) {
            $this->setWarning("MSG302","支援基準額が重複しています。");
            return false;

        //支援基準額重複
        } elseif (($kind == "insert" || $kind == "update") && $cntch != 0) {
            $this->setWarning("MSG302","支援基準額が重複しています。");
            return false;
        }

        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            $reduction_seq = knjz094kQuery::getInsertQuery($this->field);
            $this->grade             = $this->field["GRADE"];
            $this->reductionmoney    = $this->field["REDUCTIONMONEY"];
            $this->income_low        = $this->field["INCOME_LOW"];
            $this->income_high       = $this->field["INCOME_HIGH"];
            $this->reduction_seq     = $reduction_seq;
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz094kQuery::getUpdateQuery($this->field,$this->reduction_seq);
            $this->grade             = $this->field["GRADE"];
            $this->reductionmoney    = $this->field["REDUCTIONMONEY"];
            $this->income_low        = $this->field["INCOME_LOW"];
            $this->income_high       = $this->field["INCOME_HIGH"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz094kQuery::getDeleteQuery($this->field, $this->reduction_seq);
            $this->grade             = "";
            $this->reductionmoney    = "";
            $this->reductionAddMoney = "";
            $this->income_low        = "";
            $this->income_high       = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と今年度のデータ存在チェック
        $check = array();
        $check = knjz094kQuery::CheckData($this->year);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjz094kQuery::getCopyQuery($this->year);
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>
