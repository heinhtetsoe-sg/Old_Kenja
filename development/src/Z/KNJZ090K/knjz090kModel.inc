<?php

require_once('for_php7.php');

//定数定義
class knjz090kModel extends Model {
    var $field = array();
    var $cmd;
    var $prefecturescd; //都道府県コード
    var $grade;         //学年
    var $reductionmoney_1;//軽減額1
    var $reductionmoney_2;//軽減額2
    var $year;

    //メンバ変数の初期化
    function knjz090kModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->prefecturescd);
            unset($this->grade);
            unset($this->reductionmoney_1);
            unset($this->income_low1);
            unset($this->income_high1);
            unset($this->reductionmoney_2);
            unset($this->income_low2);
            unset($this->income_high2);
        }

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PREFECTURESCD")) {
            $this->prefecturescd = VARS::get("PREFECTURESCD");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("REDUCTION_SEQ")) {
            $this->reduction_seq = VARS::get("REDUCTION_SEQ");
        }
        if (VARS::get("REDUCTIONMONEY_1")) {
            $this->reductionmoney_1 = VARS::get("REDUCTIONMONEY_1");
        }
        if (VARS::get("INCOME_LOW1")) {
            $this->income_low1 = VARS::get("INCOME_LOW1");
        } elseif (!VARS::get("INCOME_LOW1")) {
            $this->income_low1 = 0;
        }
        if (VARS::get("INCOME_HIGH1")) {
            $this->income_high1 = VARS::get("INCOME_HIGH1");
        }
        if (VARS::get("REDUCTIONMONEY_2")) {
            $this->reductionmoney_2 = VARS::get("REDUCTIONMONEY_2");
        }
        if (VARS::get("INCOME_LOW2")) {
            $this->income_low2 = VARS::get("INCOME_LOW2");
        } elseif (!VARS::get("INCOME_LOW2")) {
            $this->income_low2 = 0;
        }
        if (VARS::get("INCOME_HIGH2")) {
            $this->income_high2 = VARS::get("INCOME_HIGH2");
        }

        $this->field = array("YEAR"             => $this->year,
                             "PREFECTURESCD"    => VARS::post("PREFECTURESCD"),     //都道府県コード
                             "GRADE"            => VARS::post("GRADE"),             //学年
                             "REDUCTIONMONEY_1" => VARS::post("REDUCTIONMONEY_1"),  //軽減額1
                             "INCOME_LOW1"      => VARS::post("INCOME_LOW1"),       //下限額1
                             "INCOME_HIGH1"     => VARS::post("INCOME_HIGH1"),      //上限額1
                             "INCOME_SIBLINGS1" => VARS::post("INCOME_SIBLINGS1"),  //兄弟姉妹1
                             "INCOME_RANK1"     => VARS::post("INCOME_RANK1"),      //ランク1
                             "REDUCTIONMONEY_2" => VARS::post("REDUCTIONMONEY_2"),  //軽減額2
                             "INCOME_LOW2"      => VARS::post("INCOME_LOW2"),       //下限額2
                             "INCOME_HIGH2"     => VARS::post("INCOME_HIGH2"),      //上限額2
                             "INCOME_SIBLINGS2" => VARS::post("INCOME_SIBLINGS2"),  //兄弟姉妹2
                             "INCOME_RANK2"     => VARS::post("INCOME_RANK2"),      //ランク2
                             "REDUCTIONREMARK"  => VARS::post("REDUCTIONREMARK"),   //備考
                             "REDUCTION_SEQ"    => $this->reduction_seq             //連番
                             );
    }

    //入力項目チェック
    function validateInput($kind) {
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
            if (strlen($this->field["REDUCTIONMONEY_1"]) == 0) {
                $this->setWarning("MSG301","( 支援額 )");
                return false;
            }
            if (strlen($this->field["REDUCTIONMONEY_2"]) == 0) {
                $this->setWarning("MSG301","( 支援額 )");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["REDUCTIONMONEY_1"]) > 8) {
                $this->setWarning("MSG915","( 支援額は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["INCOME_LOW1"]) > 8) {
                $this->setWarning("MSG915","( 課税総所得下限額1は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["INCOME_HIGH1"]) > 8) {
                $this->setWarning("MSG915","( 課税総所得上限額1は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REDUCTIONMONEY_2"]) > 8) {
                $this->setWarning("MSG915","( 支援額は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["INCOME_LOW2"]) > 8) {
                $this->setWarning("MSG915","( 課税総所得下限額2は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["INCOME_HIGH2"]) > 8) {
                $this->setWarning("MSG915","( 課税総所得上限額2は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REDUCTIONREMARK"]) > 75) {
                $this->setWarning("MSG915","( 備考は75バイトまでです。)");
                return false;
            }

            //上限額または下限額が空の場合
            //ランクが04:C2の時はNULLで登録できるようにする
            for ($inComeRange = 1; $inComeRange <= 2; $inComeRange++) {
                if ($this->field["INCOME_RANK{$inComeRange}"] !== '04') {
                    $this->field["INCOME_LOW{$inComeRange}"]  = $this->field["INCOME_LOW{$inComeRange}"] ? $this->field["INCOME_LOW{$inComeRange}"] : 0;
                    if (strlen(trim($this->field["INCOME_HIGH{$inComeRange}"]))==0) {
                        $this->field["INCOME_HIGH{$inComeRange}"] = 99999999;
                    } elseif ($this->field["INCOME_HIGH{$inComeRange}"] == 0) {
                        $this->field["INCOME_HIGH{$inComeRange}"] = 0;
                    } else{
                        $this->field["INCOME_HIGH{$inComeRange}"] = $this->field["INCOME_HIGH{$inComeRange}"];
                    }

                    //上限額または下限額の妥当性チェック
                    //reduction_seqがNULLのときにセット
                    if ($this->reduction_seq == "" || $this->field["REDUCTION_SEQ"] == "") {
                        $reduction_seq = knjz090kQuery::getReduction_seq($this, $this->field);
                        $this->field["REDUCTION_SEQ"] = $reduction_seq;
                        $this->reduction_seq = $reduction_seq;
                    }

                    //上限額・下限額の大小(矛盾)チェック
                    if (($this->field["INCOME_LOW{$inComeRange}"]) && ($this->field["INCOME_HIGH{$inComeRange}"]) && ($this->field["INCOME_LOW{$inComeRange}"] > $this->field["INCOME_HIGH{$inComeRange}"])) {
                        $this->setWarning("MSG913");
                        return false;
                    }
                }
            }
        }

        if (($kind=="update" || $kind=="delete") && ($this->prefecturescd != $this->field["PREFECTURESCD"] ||
                                                    $this->grade != $this->field["GRADE"])) {
            $this->setWarning("MSG308");
            return false;
        }
        //reduction_seqがNULLのときにセット
        if ($this->reduction_seq == "" || $this->field["REDUCTION_SEQ"] == "") {
            $reduction_seq = knjz090kQuery::getReduction_seq($this, $this->field);
            $this->field["REDUCTION_SEQ"] = $reduction_seq;
            $this->reduction_seq = $reduction_seq;
        }
        $row = knjz090kQuery::getRow($this,2);      //入力項目の１レコード取得
        $cntch = knjz090kQuery::getCntch($this);    //整合性

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
            $reduction_seq = knjz090kQuery::getInsertQuery($this->field);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];
            $this->reductionmoney_1 = $this->field["REDUCTIONMONEY_1"];
            $this->reductionmoney_2 = $this->field["REDUCTIONMONEY_2"];
            $this->income_low1      = $this->field["INCOME_LOW1"];
            $this->income_high1     = $this->field["INCOME_HIGH1"];
            $this->income_low2      = $this->field["INCOME_LOW2"];
            $this->income_high2     = $this->field["INCOME_HIGH2"];
            
            $this->reduction_seq    = $reduction_seq;
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz090kQuery::getUpdateQuery($this->field,$this->reduction_seq);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];
            $this->reductionmoney_1 = $this->field["REDUCTIONMONEY_1"];
            $this->reductionmoney_2 = $this->field["REDUCTIONMONEY_2"];
            $this->income_low1      = $this->field["INCOME_LOW1"];
            $this->income_high1     = $this->field["INCOME_HIGH1"];
            $this->income_low2      = $this->field["INCOME_LOW2"];
            $this->income_high2     = $this->field["INCOME_HIGH2"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz090kQuery::getDeleteQuery($this->field,$this->reduction_seq);
            $this->prefecturescd    = "";
            $this->grade            = "";
            $this->reductionmoney_1 = "";
            $this->reductionmoney_2 = "";
            $this->income_low1      = "";
            $this->income_high1     = "";
            $this->income_low2      = "";
            $this->income_high2     = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と今年度のデータ存在チェック
        $check = array();
        $check = knjz090kQuery::CheckData($this->year);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjz090kQuery::getCopyQuery($this->year);
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>
