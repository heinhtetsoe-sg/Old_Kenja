<?php

require_once('for_php7.php');

class knjz221Model extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $semester;
    var $credit;

    function knjz221Model() {}

    function init() {
        if (VARS::get("PROGRAMID") || VARS::get("ini")) {
            $this->year = "";
            $this->semester = "";
            $this->credit = "";
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->year = CTRL_YEAR;
        if (VARS::request("YEAR"))      $this->year         = VARS::request("YEAR");
        if (VARS::get("SEMESTER"))      $this->semester     = VARS::get("SEMESTER");
        if (VARS::get("CREDIT"))        $this->credit       = VARS::get("CREDIT");
        if (VARS::post("SEMESTER"))     $this->semester     = VARS::post("SEMESTER");
        if (VARS::post("CREDIT"))       $this->credit       = VARS::post("CREDIT");
        if (VARS::post("maxCredit"))    $this->maxCredit    = VARS::post("maxCredit");

        for ($score=0; $score <= 10; $score++) {
            $this->field["KEKKA_LOW".$score]    = VARS::post("KEKKA_LOW".$score);
            $this->field["KEKKA_HIGH".$score]   = VARS::post("KEKKA_HIGH".$score);
        }

        $this->cmd = VARS::request("cmd");
    }

    //チェック
    function validateInput() {
        //リストより選択したかチェック
        if ($this->semester == "" || $this->credit == "") {
            $this->setWarning("MSG308");
            return false;
        }

        //値チェック
        $val = "";
        $err_score = array();
        $errFlg = false;
        for ($score=10; $score >= 0; $score--) {
            if ($this->field["KEKKA_LOW".$score] == "" && $this->field["KEKKA_HIGH".$score] != "") {
                $err_score[0][] = $score;
                $errFlg = true;
            } else if ($this->field["KEKKA_LOW".$score] != "" && $this->field["KEKKA_HIGH".$score] == "") {
                $err_score[0][] = $score;
                $errFlg = true;
            } else if ($this->field["KEKKA_LOW".$score] != "" && $this->field["KEKKA_HIGH".$score] != "" && $this->field["KEKKA_LOW".$score] > $this->field["KEKKA_HIGH".$score]) {
                $err_score[1][] = $score;
                $errFlg = true;
            } else if ($this->field["KEKKA_LOW".$score] != "" && $val != "" && $val > $this->field["KEKKA_LOW".$score]) {
                $err_score[2][] = $score;
                $errFlg = true;
            }

            if ($this->field["KEKKA_HIGH".$score] != "") $val = $this->field["KEKKA_HIGH".$score];
        }

        $msg1 = "";
        $msg2 = array();
        if ($errFlg) {
            if (get_count($err_score[0]) > 0) $msg1   = "出席点(".implode('点,',$err_score[0])."点)：値が未入力";
            if (get_count($err_score[1]) > 0) $msg2[] = "出席点(".implode('点,',$err_score[1])."点)：上限値が下限値より小さい";
            if (get_count($err_score[2]) > 0) $msg2[] = "出席点(".implode('点,',$err_score[2])."点)：高い方の出席点より小さい";

            if (strlen($msg1) > 0) {
                $this->setWarning("MSG901",$msg1);
            } else {
                $this->setWarning("MSG901",implode("\\n", $msg2));
            }
            return false;
        }

        return true;
    }

    //更新のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &update() {
        knjz221Query::getInsertQuery($this);
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        knjz221Query::getCopyQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
