<?php

require_once('for_php7.php');

class knjx_anotherModel extends Model
{
    public $cmd;
    public $schregno;
    public $seq;
    public $year;
    public $getPrgId;
    public $auth;

    public function knjx_anotherModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("SEND_AUTH")) {
            unset($this->seq);
            $this->year         = VARS::request("YEAR") ? VARS::request("YEAR") : CTRL_YEAR;
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->schregno     = VARS::get("SEND_SCHREGNO");
            $this->sendAuth     = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");
        $this->schregno = VARS::get("SCHREGNO") ? VARS::get("SCHREGNO") : $this->schregno;

        $this->year = VARS::request("YEAR") ? VARS::request("YEAR") : $this->year;

        $this->seq = VARS::get("SEQ") ? VARS::get("SEQ") : $this->seq;

        //フィールド
        $this->field = array("SCHREGNO"            => VARS::post("SCHREGNO"),            //学籍番号
                             "SEQ"                 => VARS::post("SEQ"),                 //SEQ
                             "STUDENT_DIV"         => VARS::post("STUDENT_DIV"),         //前籍校学生区分
                             "FINSCHOOLCD"         => VARS::post("FINSCHOOLCD"),         //学校コード
                             "MAJOR_NAME"          => VARS::post("MAJOR_NAME"),          //学科名称
                             "REGD_S_DATE"         => VARS::post("REGD_S_DATE"),         //在学期間開始日
                             "REGD_E_DATE"         => VARS::post("REGD_E_DATE"),         //在学期間終了日
                             "PERIOD_MONTH_CNT"    => VARS::post("PERIOD_MONTH_CNT"),    //期間月数
                             "ABSENCE_CNT"         => VARS::post("ABSENCE_CNT"),         //休学月数
                             "MONTH_CNT"           => VARS::post("MONTH_CNT"),           //月数
                             "ENT_FORM"            => VARS::post("ENT_FORM"),            //入学形態
                             "REASON"              => VARS::post("REASON"),              //事由
                             "ANOTHER_SPORT"       => VARS::post("ANOTHER_SPORT"),       //スポーツ振興センター
                             "UPDATED"             => VARS::post("UPDATED")              //更新日付
                            );
        $this->getPropertiesAll();
        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if ($kind=="update" || $kind=="delete") {
            $db = Query::dbCheckOut();
            $query = knjx_anotherQuery::getAnotherSchoolData($this->field["SCHREGNO"], $this->field["SEQ"]);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            //キー値が変更されていますかをチェック
            if ($this->field["SCHREGNO"] != $this->schregno) {
                $this->setWarning('MSG308');
                return false;
            }
            if ($this->field["SEQ"] != $this->seq) {
                $this->setWarning('MSG308');
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $Row["UPDATED"] == "") {
                $this->setWarning('MSG303');
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $Row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning('MSG307');
                return false;
            }
        }
        if ($kind!="delete") {
            //空文字チェック
            $strnotchk = array("SCHREGNO"            => "( 学籍番号 )",
                               "STUDENT_DIV"         => "( 前籍校学生区分 )",
                               "FINSCHOOLCD"         => "( 学校コード )",
                               "MAJOR_NAME"          => "( 学科名称 )",
                               "REGD_S_DATE"         => "( 在学期間開始日 )",
                               "REGD_E_DATE"         => "( 在学期間終了日 )",
                               "ENT_FORM"            => "( 入学形態 )"
                        );

            foreach ($strnotchk as $key => $val) {
                if (strlen($this->field[$key]) <= 0) {
                    $this->setWarning("MSG301", $val);
                    return false;
                }
            }

            //在籍期間矛盾チェック
            if ($this->field["REGD_S_DATE"] > $this->field["REGD_E_DATE"]) {
                $this->setWarning('MSG916', "( 在籍期間 )");
                return false;
            }
            //学校コード存在チェック
            $db = Query::dbCheckOut();
            if ($db->getOne(knjx_anotherQuery::getFinschoolcd($this)) == 0) {
                $this->setWarning("MSG901", "( 学校コード )");
                return false;
            }
            Query::dbCheckIn($db);

            //新規データ在籍期間チェック
            if ($kind == "insert") {
                $db = Query::dbCheckOut();
                $query = knjx_anotherQuery::getAnotherSchoolCheck(
                    $this->field["SCHREGNO"],
                    $this->field["REGD_S_DATE"],
                    $this->field["REGD_E_DATE"]
                );
                $cnt = $db->getOne($query);
                Query::dbCheckIn($db);

                if ($cnt > 0) {
                    $this->setWarning('MSG918', "( 在籍期間 )");
                    return false;
                }
            }
            //更新データ在籍期間チェック
            if ($kind == "update") {
                $db = Query::dbCheckOut();
                $query = knjx_anotherQuery::getAnotherSchoolCheck(
                    $this->field["SCHREGNO"],
                    $this->field["REGD_S_DATE"],
                    $this->field["REGD_E_DATE"],
                    $this->field["SEQ"]
                );
                $cnt = $db->getOne($query);
                Query::dbCheckIn($db);

                if ($cnt > 0) {
                    $this->setWarning('MSG918', "( 在籍期間 )");
                    return false;
                }
            }

            //期間月数、月数の整合性チェック
            if (intval($this->field["PERIOD_MONTH_CNT"]) < 1) {
                $this->setWarning("MSG901", "( 期間月数 )");
                return false;
            }
            if (intval($this->field["MONTH_CNT"]) < 1) {
                $this->setWarning("MSG901", "( 月数 )");
                return false;
            }

            //バイト数チェック
            $finschoolcdbyte = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 11);
            $strlenchk = array("FINSCHOOLCD"         => array($finschoolcdbyte,"( 学校コード )"),
                               "MAJOR_NAME"          => array(120,"( 学科名称 )"),
                               "ABSENCE_CNT"         => array(2,"( 休学月数 ))"),
                               "REASON"              => array(150,"( 事由 ))")
                 );

            foreach ($strlenchk as $key => $val) {
                if (strlen($this->field[$key]) > $val[0]) {
                    $this->setWarning("MSG915", $val[1]);
                    return false;
                }
            }
        }
        return true;
    }

    //追加処理
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $result = knjx_anotherQuery::getInsertQuery($this);
            $this->seq = $result;
            if (strlen($this->field["ANOTHER_SPORT"])) {
                knjx_anotherQuery::getUpdateSchregBaseDetailMst($this);
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $result = knjx_anotherQuery::getUpdateQuery($this);
            if (strlen($this->field["ANOTHER_SPORT"])) {
                knjx_anotherQuery::getUpdateSchregBaseDetailMst($this);
            }
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            if ($this->seq == "") {
                $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
                return false;
            }

            knjx_anotherQuery::getDeleteQuery($this);
            $this->seq = "";
            $this->setMessage("MSG201");
        }
    }
}
