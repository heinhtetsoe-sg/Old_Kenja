<?php

require_once('for_php7.php');

//定数定義
class knjp718Model extends Model {
    var $field = array();
    var $cmd;
    var $schooldiv;     //授業料区分
    var $grade;         //学年
    var $year;

    //メンバ変数の初期化
    function knjp718Model() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->schooldiv);
            unset($this->grade);
            unset($this->schoolKind);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND"): $this->schoolKind;

        if (strlen(VARS::get("SCHOOLDIV"))) {
            $this->schooldiv = VARS::get("SCHOOLDIV");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("REDUCTION_SEQ")) {
            $this->reduction_seq = VARS::get("REDUCTION_SEQ");
        }

        $this->field = array("YEAR"                 => $this->year,
                             "SCHOOLDIV"            => VARS::post("SCHOOLDIV"),             //授業料区分
                             "GRADE"                => VARS::post("GRADE"),                 //学年
                             "REDUCTION_ADD_MONEY1" => VARS::post("REDUCTION_ADD_MONEY1"),  //支援加算額1
                             "INCOME_LOW1"          => VARS::post("INCOME_LOW1"),           //下限額1
                             "INCOME_HIGH1"         => VARS::post("INCOME_HIGH1"),          //上限額1
                             "REDUCTION_ADD_MONEY2" => VARS::post("REDUCTION_ADD_MONEY2"),  //支援加算額2
                             "INCOME_LOW2"          => VARS::post("INCOME_LOW2"),           //下限額2
                             "INCOME_HIGH2"         => VARS::post("INCOME_HIGH2"),          //上限額2
                             "REDUCTIONREMARK"      => VARS::post("REDUCTIONREMARK"),       //備考
                             "REDUCTION_SEQ"        => $this->reduction_seq                 //連番
                             );
    }

    //入力項目チェック
    function validateInput($kind) {
        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["SCHOOLDIV"]) == 0) {
                $this->setWarning("MSG301","( 授業料区分 )");
                return false;
            }
            if (strlen($this->field["GRADE"]) == 0) {
                $this->setWarning("MSG301","( 学年 )");
                return false;
            }
            for ($i = 1; $i <= 2; $i++) {
                if (strlen($this->field["REDUCTION_ADD_MONEY".$i]) == 0) {
                    $this->setWarning("MSG301","( 支援加算額{$i})");
                    return false;
                }
            }
            //文字数チェック
            for ($i = 1; $i <= 2; $i++) {
                if (strlen($this->field["REDUCTION_ADD_MONEY".$i]) > 8) {
                    $this->setWarning("MSG915","( 支援加算額{$i}は8バイトまでです。)");
                    return false;
                }
                if (strlen($this->field["INCOME_LOW".$i]) > 8) {
                    $this->setWarning("MSG915","( 所得割下限額{$i}は8バイトまでです。)");
                    return false;
                }
                if (strlen($this->field["INCOME_HIGH".$i]) > 8) {
                    $this->setWarning("MSG915","( 所得割上限額{$i}は8バイトまでです。)");
                    return false;
                }
            }
            if (strlen($this->field["REDUCTIONREMARK"]) > 75) {
                $this->setWarning("MSG915","( 備考は75バイトまでです。)");
                return false;
            }

            //上限額または下限額が空の場合
            for ($i = 1; $i <= 2; $i++) {
                $this->field["INCOME_LOW".$i]  = $this->field["INCOME_LOW".$i] ? $this->field["INCOME_LOW".$i] : 0;
                if (strlen(trim($this->field["INCOME_HIGH".$i])) == 0) {
                    $this->field["INCOME_HIGH".$i] = 99999999;
                } elseif ($this->field["INCOME_HIGH".$i] == 0) {
                    $this->field["INCOME_HIGH".$i] = 0;
                } else{
                    $this->field["INCOME_HIGH".$i] = $this->field["INCOME_HIGH".$i];
                }
            }

            //上限額または下限額の妥当性チェック
            for ($i = 1; $i <= 2; $i++) {
                $result_chk = knjp718Query::Income_Check1($this, $kind, $i);
                if ($result_chk=="low") {
                    $this->setWarning("MSG913","( 下限額{$i}が不正です。)");
                    return false;
                } elseif ($result_chk=="high") {
                    $this->setWarning("MSG913","( 上限額{$i}が不正です。)");
                    return false;
                }
            }

            //上限額・下限額の大小(矛盾)チェック
            for ($i = 1; $i <= 2; $i++) {
                if (($this->field["INCOME_LOW".$i]) && ($this->field["INCOME_HIGH".$i]) && ($this->field["INCOME_LOW".$i] > $this->field["INCOME_HIGH".$i])) {
                    $this->setWarning("MSG913");
                    return false;
                }
            }
        }

        if (($kind == "update" || $kind == "delete") && ($this->grade != $this->field["GRADE"] || $this->schooldiv != $this->field["SCHOOLDIV"])) {
            $this->setWarning("MSG308");
            return false;
        }

        $row = knjp718Query::getRow($this, 2);      //入力項目の１レコード取得

        if (($kind == "insert") && is_array($row)) {
            $this->setWarning("MSG302","支援加算額が重複しています。");
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
            $reduction_seq = knjp718Query::getInsertQuery($this, $this->field);
            $this->schooldiv     = $this->field["SCHOOLDIV"];
            $this->grade         = $this->field["GRADE"];
            $this->reduction_seq = $reduction_seq;
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjp718Query::getUpdateQuery($this, $this->field, $this->reduction_seq);
            $this->schooldiv = $this->field["SCHOOLDIV"];
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp718Query::getDeleteQuery($this, $this->field, $this->reduction_seq);
            $this->schooldiv = "";
            $this->grade = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と今年度のデータ存在チェック
        $check = array();
        $check = knjp718Query::CheckData($this->year, $this);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjp718Query::getCopyQuery($this->year, $this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>
