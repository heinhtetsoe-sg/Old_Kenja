<?php

require_once('for_php7.php');

class knjl052dModel extends Model {
    var $cmd;
    var $year;
    var $field = array();

    function knjl052dModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;

        $temp = VARS::request("YEAR");
        if (isset($temp)) {
            $this->year = VARS::request("YEAR");
        }

        //フィールド
        $this->field = array("SUBCLASS_CD"              =>  VARS::post("SUBCLASS_CD")              //科目
                            );

        //データセット
        for ($i = 1; $i <= 100; $i++) {
            $this->field["QUEST_FLAG-".$i]  = VARS::post("QUEST_FLAG-".$i);
            $this->field["PATTERN_CD-".$i]  = VARS::post("PATTERN_CD-".$i);
            $this->field["ANSWER1-".$i]     = VARS::post("ANSWER1-".$i);
            $this->field["POINT1-".$i]      = VARS::post("POINT1-".$i);
            $this->field["ANSWER2-".$i]     = VARS::post("ANSWER2-".$i);
            $this->field["POINT2-".$i]      = VARS::post("POINT2-".$i);
            $this->field["ANSWER3-".$i]     = VARS::post("ANSWER3-".$i);
            $this->field["POINT3-".$i]      = VARS::post("POINT3-".$i);
        }
    }

    function &getMainModel() {}

    //次年度のデータを作成
    function &getCopyYearModel() {
        $db = Query::dbCheckOut();

        $this->nextYear = $this->year + 1;
        $message = '';
        $flg = true;
        $chkCnt1 = $db->getOne(knjl052dQuery::getCheckEntexamQuesPointYMstSql($this->year));
        if ($chkCnt1 == 0) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        $chkCnt2 = $db->getOne(knjl052dQuery::getCheckEntexamQuesPointYMstSql($this->nextYear));
        if ($chkCnt2 != 0) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl052dQuery::getCopyQuery($this->year, $this->nextYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
        //空文字チェック
        if (!($this->field["SUBCLASS_CD"])) {
            $this->setWarning("MSG301", "( 科目 )");
            return false;
        }
        for ($i = 1; $i <= 100; $i++) {
            if ($this->field["QUEST_FLAG-".$i] != "") {
                //空文字チェック
                if (!($this->field["PATTERN_CD-".$i])) {
                    $this->setWarning("MSG301", "( {$i}行目、パターンコード )");
                    return false;
                }
                if (!($this->field["ANSWER1-".$i])) {
                    $this->setWarning("MSG301", "( {$i}行目、正解１番号 )");
                    return false;
                }
                if (!($this->field["POINT1-".$i])) {
                    $this->setWarning("MSG301", "( {$i}行目、正解１配点 )");
                    return false;
                }
                //パターンコードの値"1"の時のエラーチェック
                if (($this->field["PATTERN_CD-".$i] == "1") && ($this->field["ANSWER2-".$i] != "" || $this->field["POINT2-".$i] != "")) {
                    $this->setWarning("MSG901", "( {$i}行目、パターンコード「1」の時、正解２は入力出来ません )");
                    return false;
                }
                //パターンコードの値"2"の時のエラーチェック
                if (($this->field["PATTERN_CD-".$i] == "2") && ($this->field["POINT2-".$i] != "0")) {
                    $this->setWarning("MSG901", "( {$i}行目、パターンコード「2」の時、正解２配点は0を入力してください )");
                    return false;
                }
                //パターンコードの値"3"の時のエラーチェック
                if (($this->field["PATTERN_CD-".$i] == "3") && ($this->field["ANSWER2-".$i] == "" || $this->field["POINT2-".$i] == "")) {
                    $this->setWarning("MSG301", "( {$i}行目、パターンコード「3」の時、正解２は入力してください )");
                    return false;
                }
                //パターンコードの値"2"、"3"の時のエラーチェック
                if (($this->field["PATTERN_CD-".$i] == "2") || ($this->field["PATTERN_CD-".$i] == "3")) {
                    if ($this->field["ANSWER1-".$i] > $this->field["ANSWER2-".$i]) {
                        $this->setWarning("MSG901", "( {$i}行目、正解１は正解２より小さい値にしてください )");
                        return false;
                    }
                }
                //正解３の正答と配点の入力追加に伴い、パターンコードも追加されると思われる
                //この時のエラーチェック
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjl052dQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
