<?php

require_once('for_php7.php');

//定数定義
class knjl030eModel extends Model {
    var $field      = array();
    var $cmd;
    var $entexamyear;
    var $applicantdiv;
    var $testdiv;
    var $examhall_type;

    var $examhallcd;

    var $NextYear;

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->entexamyear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhall_type);
            unset($this->examhallcd);
        }

        $temp = VARS::request("ENTEXAMYEAR");
        if (isset($temp)) {
            $this->entexamyear = VARS::request("ENTEXAMYEAR");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::request("TESTDIV");
        }
        $temp = VARS::request("EXAMHALL_TYPE");
        if (isset($temp)) {
            $this->examhall_type = VARS::request("EXAMHALL_TYPE");
        }

        $this->field = array("EXAMHALLCD"           =>  VARS::post("EXAMHALLCD"),           //会場コード
                             "EXAMHALL_NAME"        =>  VARS::post("EXAMHALL_NAME"),        //会場名
                             "APP_HOLD"             =>  VARS::post("APP_HOLD")              //入試制度（保持用）
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("EXAMHALLCD");
        if (isset($temp)) {
            $this->examhallcd       = VARS::get("EXAMHALLCD");
        }

        //初期値がセットされていない場合
        if ($this->applicantdiv == "" || $this->testdiv == "") {
            $db = Query::dbCheckOut();
            $appNamespare       = $db->getOne(knjl030eQuery::getNameSpare2($this, "L003"));
            $testNamespare      = $db->getOne(knjl030eQuery::getNameSpare2($this, "L004"));
            $this->applicantdiv = $db->getOne(knjl030eQuery::getNameMstSet($this, "L003", $appNamespare));
            $this->testdiv      = $db->getOne(knjl030eQuery::getNameMstSet($this, "L004", $testNamespare));
            Query::dbCheckIn($db);
        }
        if ($this->examhall_type == "") $this->examhall_type  = 1;
    }

    //入力項目チェック
    function validateInput($kind) {

        $hissu  = ($kind == 'delete') ? '' : 'hissu2';
        $change = ($kind == 'update' || $kind == 'delete') ? 'change' : '';
        $double1 = (substr($this->cmd, -1) == '1') ? 'RowH' : '';

        //チェック項目      項目 = 項目名 / サイズ / 必須チェック / サイズチェック / 変更チェック / 重複(存在)チェック
        $checkArray = array();
        $checkArray["ENTEXAMYEAR"]          = array('入試年度',             4,  'hissu1',   '',     '',     '');
        $checkArray["APPLICANTDIV"]         = array('入試制度',             1,  'hissu1',   '',     '',     '');
        $checkArray["TESTDIV"]              = array('入試区分',             2,  'hissu1',   '',     '',     '');
        $checkArray["EXAMHALL_TYPE"]        = array('会場区分',             1,  'hissu1',   '',     '',     '');
        $checkArray["EXAMHALLCD"]           = array('会場コード',           4,  'hissu2',   'size',     $change,    $double1);
        $checkArray["EXAMHALL_NAME"]        = array('会場名',              18,  $hissu,     'size',     '',         '');

        //データ取得
        $db = Query::dbCheckOut();
        $RowH = $db->getRow(knjl030eQuery::getEntexamHallYdat($this, $this->field["EXAMHALLCD"]), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //入力項目の各種チェック
        $hissuErr = $sizeErr = $changeErr = $doubleErr = $existsErr = array();
        foreach ($checkArray as $field => $val) {
            list ($label, $size, $hissuFlg, $sizeFlg, $changeFlg, $doubleFlg) = $val;

            //必須チェック
            if ($hissuFlg) {
                $fieldname = mb_strtolower($field);
                $value = (substr($hissuFlg, -1) == '1') ? $this->$fieldname : $this->field[$field];
                if (!strlen($value)) {
                    $hissuErr[] = '　・'.$label;
                }
            }
            //サイズチェック
            if ($sizeFlg) {
                $fieldname = mb_strtolower($field);
                if (strlen($this->field[$field]) > $size) {
                    $sizeErr[] = $label.'：'.$size.'バイトまで';
                }
            }
            //変更チェック
            if ($changeFlg) {
                $fieldname = mb_strtolower($field);
                if ($this->$fieldname !== $this->field[$field]) {
                    $changeErr[] = '　・'.$label;
                }
            }
            //重複チェック
            if ($doubleFlg) {
                $Row = $$doubleFlg;
                if ($kind == 'add') {
                    if (is_array($Row)) {
                        $doubleErr[] = '　・'.$label;
                    }
                } else {
                    if (!is_array($Row)) {
                        $existsErr[] = '　・'.$label;
                    }
                }
            }
        }
        //エラーメッセージ表示（必須チェック）
        if (get_count($hissuErr)) {
            $hissuList = implode("\\n", $hissuErr);
            $this->setWarning("MSG301", $hissuList);
            return false;
        }
        //エラーメッセージ表示（サイズチェック）
        if (get_count($sizeErr)) {
            $sizeList = implode("\\n", $sizeErr);
            $this->setWarning("MSG915", $sizeList);
            return false;
        }
        //エラーメッセージ表示（キー値変更チェック）
        if (get_count($changeErr)) {
            $changeList = implode("\\n", $changeErr);
            $this->setWarning("MSG308", $changeList);
            return false;
        }
        //エラーメッセージ表示（重複チェック）
        if (get_count($doubleErr)) {
            $doubleList = implode("\\n", $doubleErr);
            $this->setWarning("MSG918", $doubleList);
            return false;
        }
        //エラーメッセージ表示（存在チェック）
        if (get_count($existsErr)) {
            $existsList = implode("\\n", $existsErr);
            $this->setWarning("MSG303", $existsList);
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl030eQuery::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("add")) {
            knjl030eQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        $this->examhallcd       = $this->field["EXAMHALLCD"];
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl030eQuery::getDeleteQuery($this, $this->field);
            $this->setMessage("MSG201");
            unset($this->field);
        }
        return;
    }

    //次年度のデータを作成
    function &getCopyYearModel() {
        $db = Query::dbCheckOut();

        $this->nextYear = $this->entexamyear + 1;
        $message = '';
        $flg = true;
        if (!checkThisYear($db, $this->entexamyear)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if (!checkNameMstNextYear($db, $this->nextYear)) {
            $message = $message . '次年度の名称マスタ（L003・L004）が存在しません。';
            $flg = false;
        }
        if ($flg) {
            //次年度コピー
            $query = knjl030eQuery::getCopyQuery($db, $this->entexamyear, $this->nextYear);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        Query::dbCheckIn($db);
    }

    //変数をクリアします
    function clean()
    {
    }
}

//今年度データチェック
function checkThisYear($db, $year) {
    $result = $db->getOne(knjl030eQuery::getExists2("ENTEXAM_HALL_YDAT", $year));
    $ret = ($result > 0) ? true : false;
    return $ret;
}

//次年度データチェック
function checkNextYear($db, $nextYear) {
    $result1 = $db->getOne(knjl030eQuery::getExists2("ENTEXAM_HALL_YDAT", $nextYear));
    $ret = ($result1 == 0) ? true : false;
    return $ret;
}

//次年度データチェック（名称マスタ）
function checkNameMstNextYear($db, $nextYear) {
    $result1 = $db->getOne(knjl030eQuery::getExists2("V_NAME_MST", $nextYear, "L003"));
    $result2 = $db->getOne(knjl030eQuery::getExists2("V_NAME_MST", $nextYear, "L004"));
    $ret = ($result1 > 0 && $result2 > 0) ? true : false;
    return $ret;
}
?>
