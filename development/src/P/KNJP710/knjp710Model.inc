<?php

require_once('for_php7.php');

//定数定義
class knjp710Model extends Model {
    var $field = array();
    var $cmd;
    var $prefecturescd; //都道府県コード
    var $grade;         //学年
    var $year;

    //メンバ変数の初期化
    function knjp710Model() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->prefecturescd);
            unset($this->grade);
        }

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;
        $this->reductionTarget = VARS::request("REDUCTION_TARGET") ? VARS::request("REDUCTION_TARGET") : "1";

        if (VARS::get("PREFECTURESCD")) {
            $this->prefecturescd = VARS::get("PREFECTURESCD");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("REDUCTION_SEQ")) {
            $this->reduction_seq = VARS::get("REDUCTION_SEQ");
        }

        $this->field = array("YEAR"                 => $this->year,
                             "PREFECTURESCD"        => VARS::post("PREFECTURESCD"),         //都道府県コード
                             "GRADE"                => VARS::post("GRADE"),                 //学年
                             "MONEY_DIV1"           => VARS::post("MONEY_DIV1"),            //金額区分1
                             "REDUCTIONMONEY_1"     => VARS::post("REDUCTIONMONEY_1"),      //補助額1
                             "INCOME_LOW1"          => VARS::post("INCOME_LOW1"),           //下限額1
                             "INCOME_HIGH1"         => VARS::post("INCOME_HIGH1"),          //上限額1
                             "BURDEN_CHARGE_FLG1"   => VARS::post("BURDEN_CHARGE_FLG1"),    //学校負担1
                             "INCOME_RANK1"         => VARS::post("INCOME_RANK1"),          //ランク1
                             "MONEY_DIV2"           => VARS::post("MONEY_DIV2"),            //金額区分2
                             "REDUCTIONMONEY_2"     => VARS::post("REDUCTIONMONEY_2"),      //補助額2
                             "INCOME_LOW2"          => VARS::post("INCOME_LOW2"),           //下限額2
                             "INCOME_HIGH2"         => VARS::post("INCOME_HIGH2"),          //上限額2
                             "BURDEN_CHARGE_FLG2"   => VARS::post("BURDEN_CHARGE_FLG2"),    //学校負担2
                             "INCOME_RANK2"         => VARS::post("INCOME_RANK2"),          //ランク2
                             "REDUCTIONREMARK"      => VARS::post("REDUCTIONREMARK"),       //備考
                             "REDUCTION_SEQ"        => $this->reduction_seq                 //連番
                             );

        for ($num = 1; $num <= 2; $num++) {
            if ($this->field["MONEY_DIV".$num] == "1") {
                $this->field["NUMERATOR".$num]   = VARS::post("NUMERATOR".$num);
                $this->field["DENOMINATOR".$num] = VARS::post("DENOMINATOR".$num);
            } else if ($this->field["MONEY_DIV".$num] == "2" || $this->field["MONEY_DIV".$num] == "3") {
                $this->field["NUMERATOR".$num]   = "";
                $this->field["DENOMINATOR".$num] = "";
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        if ($this->reductionTarget == "1") {
            $checkCnt = 2;
        } else if ($this->reductionTarget == "2") {
            $checkCnt = 1;
        }
        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["YEAR"]) < 4) {
                $this->setWarning("MSG301","( 年度 )");
                return false;
            }
            if (strlen($this->field["PREFECTURESCD"]) == 0) {
                $this->setWarning("MSG301","( 都道府県コード )");
                return false;
            }
            if (strlen($this->field["GRADE"]) == 0) {
                $this->setWarning("MSG301","( 学年 )");
                return false;
            }
            for ($num = 1; $num <= $checkCnt; $num++) {
                $num_msg = $this->getNumMsg($num);
                if ($this->field["MONEY_DIV".$num] == "1") {
                    if (strlen($this->field["NUMERATOR".$num]) == 0) {
                        $this->setWarning("MSG301","( 補助額{$num_msg}(割合) )");
                        return false;
                    }
                    if (strlen($this->field["DENOMINATOR".$num]) == 0) {
                        $this->setWarning("MSG301","( 補助額{$num_msg}(割合) )");
                        return false;
                    }
                } else if ($this->field["MONEY_DIV".$num] == "2") {
                    if (strlen($this->field["REDUCTIONMONEY_".$num]) == 0) {
                        $this->setWarning("MSG301","( 補助額{$num_msg}(絶対額) )");
                        return false;
                    }
                } else if ($this->field["MONEY_DIV".$num] == "3") {
                    if (strlen($this->field["REDUCTIONMONEY_".$num]) == 0) {
                        $this->setWarning("MSG301","( 補助額{$num_msg}(保護者負担) )");
                        return false;
                    }
                }
                if (strlen($this->field["INCOME_RANK".$num]) == 0) {
                    $this->setWarning("MSG301","( ランク{$num_msg} )");
                    return false;
                }
            }

            //文字数チェック
            for ($num = 1; $num <= $checkCnt; $num++) {
                $num_msg = $this->getNumMsg($num);
                if ($this->field["MONEY_DIV".$num] == "1") {
                    if (strlen($this->field["NUMERATOR".$num]) > 3) {
                        $this->setWarning("MSG915","( 補助額{$num_msg}(割合)は3バイトまでです。)");
                        return false;
                    }
                    if (strlen($this->field["DENOMINATOR".$num]) > 3) {
                        $this->setWarning("MSG915","( 補助額{$num_msg}(割合)は3バイトまでです。)");
                        return false;
                    }
                } else if ($this->field["MONEY_DIV".$num] == "2") {
                    if (strlen($this->field["REDUCTIONMONEY_".$num]) > 8) {
                        $this->setWarning("MSG915","( 補助額{$num_msg}(絶対額)は8バイトまでです。)");
                        return false;
                    }
                } else if ($this->field["MONEY_DIV".$num] == "3") {
                    if (strlen($this->field["REDUCTIONMONEY_".$num]) > 8) {
                        $this->setWarning("MSG915","( 補助額{$num_msg}(保護者負担)は8バイトまでです。)");
                        return false;
                    }
                }
                if (strlen($this->field["INCOME_LOW".$num]) > 8) {
                    $this->setWarning("MSG915","( 所得割下限額{$num_msg}は8バイトまでです。)");
                    return false;
                }
                if (strlen($this->field["INCOME_HIGH".$num]) > 8) {
                    $this->setWarning("MSG915","( 所得割上限額{$num_msg}は8バイトまでです。)");
                    return false;
                }
            }
            if (strlen($this->field["REDUCTIONREMARK"]) > 75) {
                $this->setWarning("MSG915","( 備考は75バイトまでです。)");
                return false;
            }

            //上限額または下限額が空の場合
            for ($i = 1; $i <= $checkCnt; $i++) {
                $this->field["INCOME_LOW".$i]  = $this->field["INCOME_LOW".$i] ? $this->field["INCOME_LOW".$i] :0;
                if (strlen(trim($this->field["INCOME_HIGH".$i]))==0) {
                    $this->field["INCOME_HIGH".$i] = 99999999;
                } elseif ($this->field["INCOME_HIGH".$i] == 0) {
                    $this->field["INCOME_HIGH".$i] = 0;
                } else{
                    $this->field["INCOME_HIGH".$i] = $this->field["INCOME_HIGH".$i];
                }
            }

            //上限額または下限額の妥当性チェック
            //reduction_seqがNULLのときにセット
            if ($this->reduction_seq == "" || $this->field["REDUCTION_SEQ"] == "") {
                $reduction_seq = knjp710Query::getReduction_seq($this, $this->field);
                $this->field["REDUCTION_SEQ"] = $reduction_seq;
                $this->reduction_seq = $reduction_seq;
            }

            //上限額・下限額の大小(矛盾)チェック
            for ($i = 1; $i <= $checkCnt; $i++) {
                if (strlen($this->field["INCOME_LOW".$i]) && strlen($this->field["INCOME_HIGH".$i]) && ($this->field["INCOME_LOW".$i] > $this->field["INCOME_HIGH".$i])) {
                    $this->setWarning("MSG913","( 上限額{$i}と下限額{$i}の大小が不正です。)");
                    return false;
                }
            }
        }

        if (($kind=="update" || $kind=="delete") &&
            ($this->prefecturescd != $this->field["PREFECTURESCD"] || $this->grade != $this->field["GRADE"])) {
            $this->setWarning("MSG308");
            return false;
        }
        //reduction_seqがNULLのときにセット
        if ($this->reduction_seq == "" || $this->field["REDUCTION_SEQ"] == "") {
            $reduction_seq = knjp710Query::getReduction_seq($this, $this->field);
            $this->field["REDUCTION_SEQ"] = $reduction_seq;
            $this->reduction_seq = $reduction_seq;
        }
        $row = knjp710Query::getRow($this, 2);     //入力項目の１レコード取得

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
            $reduction_seq = knjp710Query::getInsertQuery($this, $this->field);
            $this->prefecturescd = $this->field["PREFECTURESCD"];
            $this->grade         = $this->field["GRADE"];

            $this->reduction_seq  = $reduction_seq;
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjp710Query::getUpdateQuery($this, $this->field, $this->reduction_seq);
            $this->prefecturescd = $this->field["PREFECTURESCD"];
            $this->grade         = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp710Query::getDeleteQuery($this, $this->field, $this->reduction_seq);
            $this->prefecturescd = "";
            $this->grade         = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と今年度のデータ存在チェック
        $check = array();
        $check = knjp710Query::CheckData($this->year, $this);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjp710Query::getCopyQuery($this->year, $this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //補助区分の値によって項目末尾の数字の有無を変える
    function getNumMsg($num) {
        if ($this->reductionTarget == "1") {
            $num_msg = $num;
        } else if ($this->reductionTarget == "2") {
            $num_msg = "";
        }
        return $num_msg;
    }

    //変数をクリアします
    function clean() {}
}
?>
