<?php

require_once('for_php7.php');

//定数定義
class knjl030qModel extends Model {
    var $field = array();
    var $cmd;
    var $applicantdiv;
    var $recno;
    var $year;

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->recno);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::get("RECNO");
        if (isset($temp)) {
            $this->recno = VARS::get("RECNO");
        }

        $this->field = array("ENTEXAMYEAR"      =>  VARS::post("year"),             //入試年度
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "RECNO"            =>  VARS::post("RECNO"),            //自動発番キー
                             "EXAMNO_FROM"      =>  VARS::post("EXAMNO_FROM"),      //受験番号（開始）
                             "EXAMNO_TO"        =>  VARS::post("EXAMNO_TO"),        //受験番号（終了）
                             "TESTDIV0"         =>  VARS::post("TESTDIV0"),         //入試区分
                             "EXAMCOURSE"       =>  VARS::post("EXAMCOURSE"),       //コース区分
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分２
                             "EXAMHALLCD"       =>  VARS::post("EXAMHALLCD"),       //試験会場
                             "REMARK_DIV"       =>  VARS::post("REMARK_DIV"),       //備考
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput($kind) {
        //必須チェック
        if (strlen($this->field["ENTEXAMYEAR"]) == 0) {
            $this->setWarning("MSG301","( 入試年度 )");
            return false;
        }
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }

        if ($kind == "insert" || $kind == "update") {
            //必須チェック
            if (strlen($this->field["EXAMNO_FROM"]) == 0) {
                $this->setWarning("MSG301","( 開始受験番号 )");
                return false;
            }
            if (strlen($this->field["EXAMNO_TO"]) == 0) {
                $this->setWarning("MSG301","( 終了受験番号 )");
                return false;
            }
            if (strlen($this->field["TESTDIV0"]) == 0) {
                $this->setWarning("MSG301","( 入試区分 )");
                return false;
            }
            if (strlen($this->field["EXAMCOURSE"]) == 0) {
                $this->setWarning("MSG301","( コース区分 )");
                return false;
            }
            if (strlen($this->field["TESTDIV"]) == 0) {
                $this->setWarning("MSG301","( 入試区分２ )");
                return false;
            }
            if (strlen($this->field["EXAMHALLCD"]) == 0) {
                $this->setWarning("MSG301","( 試験会場 )");
                return false;
            }

            //文字数チェック
            if (strlen($this->field["EXAMNO_FROM"]) > 5) {
                $this->setWarning("MSG915","( 開始受験番号は5バイトまでです。)");
                return false;
            }
            if (strlen($this->field["EXAMNO_TO"]) > 5) {
                $this->setWarning("MSG915","( 終了受験番号は5バイトまでです。)");
                return false;
            }

            //ゼロ埋め
            $this->field["EXAMNO_FROM"] = sprintf("%05d",$this->field["EXAMNO_FROM"]);
            $this->field["EXAMNO_TO"]   = sprintf("%05d",$this->field["EXAMNO_TO"]);

            //番号の大小チェック
            if ($this->field["EXAMNO_FROM"] > $this->field["EXAMNO_TO"]) {
                $this->setWarning("MSG916","受験番号が前後しています。");
                return false;
            }

            //受験番号の重複チェック
            $recno = ($kind == "update") ? $this->field["RECNO"] : "";
            $db = Query::dbCheckOut();
            $cnt1 = $db->getOne(knjl030qQuery::checkExamno($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $recno, $this->field["EXAMNO_FROM"]));
            $cnt2 = $db->getOne(knjl030qQuery::checkExamno($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $recno, $this->field["EXAMNO_TO"]));
            $cnt3 = $db->getOne(knjl030qQuery::checkExamno($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $recno, $this->field["EXAMNO_FROM"], $this->field["EXAMNO_TO"]));
            Query::dbCheckIn($db);

            if ($cnt1 + $cnt2 + $cnt3 > 0) {
                $this->setWarning("MSG918","( 受験番号 )");
                return false;
            }
        }

        if ($kind == "update" || $kind == "delete") {
            //キー値が変更されているかをチェック
            if ($this->field["ENTEXAMYEAR"]     != $this->year ||
                $this->field["APPLICANTDIV"]    != $this->applicantdiv ||
                $this->field["RECNO"]           != $this->recno) {
                $this->setWarning("MSG308");
                return false;
            }

            //１レコード取得
            $db = Query::dbCheckOut();
            $chk = $db->getOne(knjl030qQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["RECNO"]));
            Query::dbCheckIn($db);

            //存在チェック
            if (strlen($chk) == 0) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl030qQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            $recno = knjl030qQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
            $this->recno = $recno;
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl030qQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel() {
        $db = Query::dbCheckOut();

        $nextYear = $this->year + 1;
        $message = '';
        $flg = true;
        $array = $db->getCol(knjl030qQuery::getNameMst($nextYear, "L003"));
        if (!in_array($this->applicantdiv, $array)) {
            $message .= '次年度に入試制度が存在しません。';
            $flg = false;
        }
        if (!checkYearData($db, $this->year, $this->applicantdiv, "this")) {
            $message .= '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkYearData($db, $nextYear, $this->applicantdiv, "next")) {
            $message .= '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl030qQuery::getCopyQuery($this->year, $nextYear, $this->applicantdiv);
            $db->query($query);
            $this->setMessage("MSG201");
            $this->year = $nextYear;
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        Query::dbCheckIn($db);
    }
}

//対象年度のデータチェック
function checkYearData($db, $year, $applicantdiv, $flg) {
    $query = knjl030qQuery::checkEntexamNoDat($year, $applicantdiv);
    $result = $db->getOne($query);
    if ($flg == "this") {
        if ($result > 0) return true;
    } else {
        if ($result == 0) return true;
    }
    return false;
}
?>
