<?php

require_once('for_php7.php');

class knjl501iModel extends Model
{
    public $cmd;
    public $leftYear = "";
    public $applicantdiv = "2";
    public $testdiv;
    public $field    = array();

    //科目登録可能上限（格納先テーブルのREMARKフィールド数に相当）
    public $REGIST_CLASS_LIMIT_MAX = 10;

    //入学前スケジュール項目件数
    public $SCHEDULE_BEFORE_ENTRY_MAX = 5;

    public function knjl501iModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->testdiv);
            unset($this->field);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->field = array("TESTDIV"                          => VARS::post("TESTDIV"),
                             "APPLICANTDIV"                     => $this->applicantdiv,
                             "TESTDIV_NAME"                     => VARS::post("TESTDIV_NAME"),
                             "TESTDIV_ABBV"                     => VARS::post("TESTDIV_ABBV"),
                             "TEST_DATE"                        => VARS::post("TEST_DATE"),
                             "ANNOUNCEMENT_DATE"                => VARS::post("ANNOUNCEMENT_DATE"),
                             "ENTRY_FEE"                        => VARS::post("ENTRY_FEE"),
                             "ENTRY_FEE_CLOSING_DATE"           => VARS::post("ENTRY_FEE_CLOSING_DATE"),
                             "ENTRY_FEE_CLOSING_TIME"           => VARS::post("ENTRY_FEE_CLOSING_TIME"),
                             "EQUIPMENT_COST"                   => VARS::post("EQUIPMENT_COST"),
                             "EQUIPMENT_COST_CLOSING_DATE"      => VARS::post("EQUIPMENT_COST_CLOSING_DATE"),
                             "EQUIPMENT_COST_CLOSING_TIME"      => VARS::post("EQUIPMENT_COST_CLOSING_TIME"),
                             "PAYMENT_CLOSING_DATE"             => VARS::post("PAYMENT_CLOSING_DATE")
                            );
        for ($clsNo =1; $clsNo < $this->REGIST_CLASS_LIMIT_MAX; $clsNo++) {
            $this->field["TEST_CLASS_{$clsNo}"] = VARS::post("TEST_CLASS_{$clsNo}");
        }
        for ($schdlNo =1; $schdlNo <= $this->SCHEDULE_BEFORE_ENTRY_MAX; $schdlNo++) {
            $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_NAME"]       = VARS::post("ENTRY_SCHDL_{$schdlNo}_EVNT_NAME");
            $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_DATE"]  = VARS::post("ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_DATE");
            $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_TIME"]  = VARS::post("ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_TIME");
            $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_CLOSE_TIME"] = VARS::post("ENTRY_SCHDL_{$schdlNo}_EVNT_CLOSE_TIME");
            $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_PLACE"]      = VARS::post("ENTRY_SCHDL_{$schdlNo}_EVNT_PLACE");
            $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_MAIN"]       = VARS::post("ENTRY_SCHDL_{$schdlNo}_EVNT_MAIN");
        }

        //ログイン時に選択された年度・学期の年度を取得し左ペインの初期選択年度として設定
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }

        // 左ペインの初期選択年度取得に失敗した場合はLib/Contorolerで設定されている年度の翌年を初期選択年度として設定
        $this->leftYear = ($this->leftYear == "") ? CTRL_YEAR + 1 : $this->leftYear;

        if (VARS::get("TESTDIV") != "") {
            $this->testdiv = VARS::get("TESTDIV");
        }
    }

    //コピーのモデルを返します
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();

        $nextYear = $this->leftYear + 1;
        $message = '';

        //コピー元データの存在確認
        if ($this->checkTestDivMstYear($db, $this->leftYear) < 1) {
            $message = '作成するデータが存在しません。';
        }
        //コピー先データの存在確認
        if ($this->checkTestDivMstYear($db, $nextYear) > 0) {
            $message = $message . '既に次年度のデータが作成されています。';
        }

        // DB接続を切断
        Query::dbCheckIn($db);

        if ($message != "") {
            $this->setMessage("MSG203", "\\n" . $message);
        } else {
            knjl501iQuery::copyTestDivMstQuery($this->leftYear, $nextYear, $this->applicantdiv);
            $this->setMessage("MSG201");
        }
    }

    //指定された試験年度に該当する対象の入試区分マスタレコードの件数を取得
    public function checkTestDivMstYear($db, $year, $testdiv = null)
    {
        $query = knjl501iQuery::getCheckTestDivMstSql($year, $this->applicantdiv, $testdiv);
        $result = $db->getOne($query);
        return $result;
    }

    //指定された試験年度に該当する対象の入試区分詳細シーケンスマスタレコードの件数を取得
    public function checkTestDivDetailSeqMstYear($db, $year, $testdiv = null)
    {
        $query = knjl501iQuery::getCheckTestDivDetailMstSql($year, $this->applicantdiv, $testdiv);
        $result = $db->getOne($query);
        return $result;
    }

    //指定されたフィールド値の長さをチェックする
    private function validateLength($targetFldVal, $fieldName, $minLen, $maxLen)
    {
        $len = mb_strlen($targetFldVal, 'UTF-8');

        if ($len < $minLen) {
            $this->setWarning("MSG301", "( {$fieldName} )");
            return false;
        }

        if (-1 < $maxLen && $maxLen < $len) {
            $this->setWarning("MSG915", sprintf("%s %d桁まで", $fieldName, $maxLen));
            return false;
        }

        return true;
    }

    //指定されたフィールド値が数字として設定されているかをチェックする
    private function validateNumeric($targetFldVal, $fieldName)
    {
        if (preg_match('/^[0-9]+$/', $targetFldVal) == false) {
            $this->setWarning("MSG907", "( {$fieldName} )");
            return false;
        }

        return true;
    }

    //指定されたフィールド値が日付として正しく設定されているかをチェックする
    private function validateDateFormat($targetFldVal, $fieldName)
    {
        if (strlen($targetFldVal) < 1) {
            //未入力は正常とみなす（フォーマットチェックの対象外）
            return true;
        } else {
            if (strlen($targetFldVal) < 8 /* yyyy/m/d */) {
                $this->setWarning("MSG901", "( {$fieldName} )");
                return false;
            }
        }

        if (preg_match('/^[0-9]{4}[\/]{1}[0-1]{0,1}[0-9]{1}[\/]{1}[0-3]{0,1}[0-9]{1}$/', $targetFldVal) == false) {
            $this->setWarning("MSG902", "( {$fieldName} )");
            return false;
        }

        $isValidDate = true;
        $date = DateTime::createFromFormat('Y/m/d', $targetFldVal);
        if (is_null($date)) {
            $isValidDate = false;
        } else {
            switch ($targetFldVal) {
                case $date->format('Y/m/d'):
                    //正常
                    break;
                case $date->format('Y/n/d'):
                    //正常
                    break;
                case $date->format('Y/m/j'):
                    //正常
                    break;
                case $date->format('Y/n/j'):
                    //正常
                    break;
                default:
                    $isValidDate = false;
                    break;
            }
        }
        if ($isValidDate == false) {
            $this->setWarning("MSG902", "( {$fieldName} )");
            return false;
        }

        return true;
    }

    //指定されたフィールド値が時刻（HH:MM）として正しく設定されているかをチェックする
    private function validateTimeFormat($targetFldVal, $fieldName)
    {
        if (strlen($targetFldVal) < 1) {
            //未入力は正常とみなす（フォーマットチェックの対象外）
            return true;
        } else {
            if (strlen($targetFldVal) < 5 /* HH:MM */) {
                $this->setWarning("MSG901", "( {$fieldName} )");
                return false;
            }
        }

        if (preg_match('/^[0-9]{2}[\:]{1}[0-9]{2}$/', $targetFldVal) == false) {
            $this->setWarning("MSG901", "( {$fieldName} )");
            return false;
        }

        $date = DateTime::createFromFormat('H:i', $targetFldVal);
        if (is_null($date) || $targetFldVal != $date->format('H:i')) {
            $this->setWarning("MSG901", "( {$fieldName} )");
            return false;
        }

        return true;
    }

    //指定されたフィールド値が半角文字のみで指定されているか否かを判定する
    private function checkHalfCharOnly($targetFldVal, $fieldName)
    {
        if (strlen($targetFldVal) < 1) {
            //未入力は正常とみなす（フォーマットチェックの対象外）
            return true;
        }

        if (strlen($targetFldVal) != mb_strlen($targetFldVal)) {
            $this->setWarning("MSG905", "( {$fieldName} )");
            return false;
        }

        return true;
    }

    //指定されたフィールド値が全角文字のみ（半角カナを含めない）で指定されているか否かを判定する
    private function checkWideCharOnly($targetFldVal, $fieldName)
    {
        if (strlen($targetFldVal) < 1) {
            //未入力は正常とみなす（フォーマットチェックの対象外）
            return true;
        }

        foreach (preg_split('//u', $targetFldVal, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 3 && preg_match('/^[ｦ-ﾟｰ ]+$/u', $v)) {
                $this->setWarning("MSG904", "( {$fieldName} )");
                return false;
            }
        }

        return true;
    }

    //入力項目チェック
    public function validateInput()
    {
        //ゼロサプレス
        if ($this->field["TESTDIV"] != "") {
            $this->field["TESTDIV"] = sprintf("%02d", $this->field["TESTDIV"]);
        }

        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            $flg = false;
            if ($this->testdiv != $this->field["TESTDIV"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        $chkTargets = array(
        //  項目ID                                 0:チェック対象コマンド  1:論理名            2:最小 3:最大 4:その他のチェック処理（チェック関数名）
            "TESTDIV"                     => array("add|update|delete", "入試区分ＣＤ"          , 2,   2, array("checkHalfCharOnly")),
            "TESTDIV_NAME"                => array("add|update"       , "入試区分名"            , 1,  20, array("checkWideCharOnly")),
            "TESTDIV_ABBV"                => array("add|update"       , "入試区分略称"          , 0,   6, array("checkWideCharOnly")),
            "TEST_DATE"                   => array("add|update"       , "入学試験日"            , 8,  10, array("validateDateFormat")),
            "ANNOUNCEMENT_DATE"           => array("add|update"       , "合格発表日"            , 8,  10, array("validateDateFormat")),
            "ENTRY_FEE"                   => array("add|update"       , "入学申込金"            , 1,   6, array("validateNumeric")),
            "ENTRY_FEE_CLOSING_DATE"      => array("add|update"       , "入学申込金納入締切日"   , 8,  10, array("validateDateFormat")),
            "ENTRY_FEE_CLOSING_TIME"      => array("add|update"       , "入学申込金納入締切時刻" , 5,   5, array("validateTimeFormat")),
            "EQUIPMENT_COST"              => array("add|update"       , "施設設備費"            , 1,   6, array("validateNumeric")),
            "EQUIPMENT_COST_CLOSING_DATE" => array("add|update"       , "施設設備費納入締切日"   , 8,  10, array("validateDateFormat")),
            "EQUIPMENT_COST_CLOSING_TIME" => array("add|update"       , "施設設備費納入締切時刻" , 5,   5, array("validateTimeFormat")),
            "PAYMENT_CLOSING_DATE"        => array("add|update"       , "授業料等納入締切日"     , 8,  10, array("validateDateFormat"))
        );
        for ($schdlNo =1; $schdlNo <= $this->SCHEDULE_BEFORE_ENTRY_MAX; $schdlNo++) {
            $chkTargets["ENTRY_SCHDL_{$schdlNo}_EVNT_NAME"]       = array("add|update" , "入学式までのスケジュール{$schdlNo} - 行事名"  , 0, 100, array("checkWideCharOnly"));
            $chkTargets["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_DATE"]  = array("add|update" , "入学式までのスケジュール{$schdlNo} - 日付"    , 0,  10, array("validateDateFormat"));
            $chkTargets["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_TIME"]  = array("add|update" , "入学式までのスケジュール{$schdlNo} - 時間from", 0,   5, array("validateTimeFormat"));
            $chkTargets["ENTRY_SCHDL_{$schdlNo}_EVNT_CLOSE_TIME"] = array("add|update" , "入学式までのスケジュール{$schdlNo} - 時間to"  , 0,   5, array("validateTimeFormat"));
            $chkTargets["ENTRY_SCHDL_{$schdlNo}_EVNT_PLACE"]      = array("add|update" , "入学式までのスケジュール{$schdlNo} - 会場"    , 0,  60, array("checkWideCharOnly"));
            $chkTargets["ENTRY_SCHDL_{$schdlNo}_EVNT_MAIN"]       = array("add|update" , "入学式までのスケジュール{$schdlNo} - 対象者"  , 0,  60, array("checkWideCharOnly"));
        }

        //既存件数を取得
        $db = Query::dbCheckOut(); //DB接続
        $countMst = $this->checkTestDivMstYear($db, $this->leftYear, $this->field["TESTDIV"]);  //入試区分マスタの件数
        $countMstDtlSeq = $this->checkTestDivDetailSeqMstYear($db, $this->leftYear, $this->field["TESTDIV"]); //入試区分詳細シーケンスマスタの件数
        Query::dbCheckIn($db);     //DB切断
        if ($countMst < 1 && $countMstDtlSeq < 1) {
            //指定の試験年度における入試区分が未登録である場合
            if ($this->cmd == "update" || $this->cmd == "delete") {
                //未登録に対して更新／削除は不可能
                $this->setWarning("MSG303");
                return false;
            }
        } else {
            //指定の試験年度における入試区分が登録済みである場合
            if ($this->cmd == "add") {
                //登録済みの入試区分を再度新規登録しようとしている
                $this->setWarning("MSG918", "{$setMsseage}");
                return false;
            }
        }

        foreach ($chkTargets as $id => $checkConditions) {
            if (strpos($checkConditions[0], $this->cmd) == false) {
                continue;
            }

            if (!$this->validateLength($this->field[$id], $checkConditions[1], $checkConditions[2], $checkConditions[3])) {
                return false;
            }

            if (is_array($checkConditions[4])) {
                foreach ($checkConditions[4] as $chkFncName) {
                    if (call_user_func(array($this, $chkFncName), $this->field[$id], $checkConditions[1]) == false) {
                        return false;
                    }
                }
            }
        }

        //文字数とフォーマットをチェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            /*****************
             日付反転チェック
            ******************/
            //入試日 - 合格発表日
            if (strcmp($this->field["TEST_DATE"], $this->field["ANNOUNCEMENT_DATE"]) > 0) {
                $this->setWarning("MSG913", "( 入学試験日 ～ 合格発表日 )");
                return false;
            }
            //入試日 - 入学申込金納入締切日
            if (strcmp($this->field["TEST_DATE"], $this->field["ENTRY_FEE_CLOSING_DATE"]) > 0) {
                $this->setWarning("MSG913", "( 入学試験日 ～ 入学申込金納入締切日 )");
                return false;
            }
            //入試日 - 施設設備費納入締切日
            if (strcmp($this->field["TEST_DATE"], $this->field["EQUIPMENT_COST_CLOSING_DATE"]) > 0) {
                $this->setWarning("MSG913", "( 入学試験日 ～ 施設設備費納入締切日 )");
                return false;
            }
            //入試日 - 授業料等納入締切日
            if (strcmp($this->field["TEST_DATE"], $this->field["PAYMENT_CLOSING_DATE"]) > 0) {
                $this->setWarning("MSG913", "( 入学試験日 ～ 授業料等納入締切日 )");
                return false;
            }

            /*****************
             試験科目選択状況を確認
            ******************/
            $selectCount = 0;
            for ($clsNo = 1; $clsNo <= $this->REGIST_CLASS_LIMIT_MAX; $clsNo++) {
                if (0 < strlen($this->field["TEST_CLASS_{$clsNo}"])) {
                    $selectCount++;
                }
            }
            if ($selectCount < 1) {
                //試験科目が１つも選択されていない
                $this->setWarning("MSG310", "( 入試科目 )");
                return false;
            }

            // 入学前スケジュール
            for ($schdlNo =1; $schdlNo <= $this->SCHEDULE_BEFORE_ENTRY_MAX; $schdlNo++) {
                /*****************
                 日付反転チェック
                ******************/
                //合格発表日 - 入学前スケジュール日付
                if (0 < strlen($this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_DATE"])) {
                    if (strcmp($this->field["ANNOUNCEMENT_DATE"], $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_DATE"]) > 0) {
                        $this->setWarning("MSG913", "( 合格発表日 ～ 入学式までのスケジュール{$schdlNo} - 日付 )");
                        return false;
                    }
                }
                /*****************
                時刻反転チェック
                ******************/
                if (0 < strlen($this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_TIME"]) && 0 < strlen($this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_CLOSE_TIME"])) {
                    if (strcmp($this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_TIME"], $this->field["ENTRY_SCHDL_{$schdlNo}_EVNT_CLOSE_TIME"]) > 0) {
                        $this->setWarning("MSG913", "( 入学式までのスケジュール{$schdlNo} - 時間from ～ 時間to )");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl501iQuery::updateTestDivAndDetailSeq($this->leftYear, $this->field);
            $this->testdiv      = $this->field["TESTDIV"];
            $this->setMessage("MSG201");
        }
    }

    public function getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl501iQuery::insertTestDivAndDetailSeq($this->leftYear, $this->field);
            $this->testdiv      = $this->field["TESTDIV"];
            $this->setMessage("MSG201");
        }
    }

    //DELETE
    public function getDeleteModel()
    {
        if ($this->validateInput()) {
            knjl501iQuery::deleteTestDivAndDetailSeq($this->leftYear, $this->field);
            $this->testdiv      = "";
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
}
