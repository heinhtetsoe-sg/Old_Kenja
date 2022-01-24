<?php

require_once('for_php7.php');

//定数定義
class knjl004aModel extends Model
{
    public $field = array();
    public $cmd;
    public $applicantdiv;      //primary
    public $honordiv;          //primary
    public $year;              //入試年度
    public $NextYear;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->honordiv);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::get("PATTERN_CD");
        if (isset($temp)) {
            $this->patternCd = VARS::get("PATTERN_CD");
        }
        $temp = VARS::get("TOKUTAI_SELECT");
        if (isset($temp)) {
            $this->tokutaiSelect = VARS::get("TOKUTAI_SELECT");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                   //入試年度
                             "APPLICANTDIV"     =>  $this->applicantdiv,           //受験校種
                             "PATTERN_CD"       =>  VARS::post("PATTERN_CD"),      //取扱票種類
                             "TOKUTAI_SELECT"   =>  VARS::post("TOKUTAI_SELECT"),  //特待生、特待生以外
                             "PATTERN_NAME"     =>  VARS::post("PATTERN_NAME"),    //取扱票種類名
                             "PREF_NAME"        =>  VARS::post("PREF_NAME"),       //都道府県
                             "ACCOUNT_NUMBER1"  =>  VARS::post("ACCOUNT_NUMBER1"), //口座番号１
                             "ACCOUNT_NUMBER2"  =>  VARS::post("ACCOUNT_NUMBER2"), //口座番号２
                             "ACCOUNT_NUMBER3"  =>  VARS::post("ACCOUNT_NUMBER3"), //口座番号３
                             "MEMBER"           =>  VARS::post("MEMBER"),          //加入者
                             "SUBJECT"          =>  VARS::post("SUBJECT"),         //件名
                             "COMMUNICATION"    =>  VARS::post("COMMUNICATION"),   //通信欄
                             "TRANSFER_MONEY"   =>  VARS::post("TRANSFER_MONEY"),  //金額
                             );

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //必須チェック
        if ($this->year == "") {
            $this->setWarning("MSG301", "( 入試年度 )");
            return false;
        }
        if ($this->field["APPLICANTDIV"] == "") {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if ($this->field["PATTERN_CD"] == "") {
            $this->setWarning("MSG301", "( 取扱票種類 )");
            return false;
        }

        //１レコード取得
        $query = knjl004aQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["PATTERN_CD"], $this->field["TOKUTAI_SELECT"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["PATTERN_CD"]) > 3) {
            $this->setWarning("MSG915", "( 取扱票種類は3バイトまでです。)");
            return false;
        }
        if (strlen($this->field["PATTERN_NAME"]) > 30) {
            $this->setWarning("MSG915", "( 取扱票種類名は30バイトまでです。)");
            return false;
        }
        if (strlen($this->field["PREF_NAME"]) > 12) {
            $this->setWarning("MSG915", "( 都道府県は12バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ACCOUNT_NUMBER1"]) > 5) {
            $this->setWarning("MSG915", "( 口座番号1は5バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ACCOUNT_NUMBER2"]) > 1) {
            $this->setWarning("MSG915", "( 口座番号2は1バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ACCOUNT_NUMBER3"]) > 7) {
            $this->setWarning("MSG915", "( 口座番号3は7バイトまでです。)");
            return false;
        }
        if (strlen($this->field["MEMBER"]) > 30) {
            $this->setWarning("MSG915", "( 加入者名は30バイトまでです。)");
            return false;
        }
        if (strlen($this->field["SUBJECT"]) > 24) {
            $this->setWarning("MSG915", "( 件名は24バイトまでです。)");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 360) {
            $this->setWarning("MSG915", "( 通信欄は360バイトまでです。)");
            return false;
        }
        if (strlen($this->field["TRANSFER_MONEY"]) > 8) {
            $this->setWarning("MSG915", "( 金額は8バイトまでです。)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl004aQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl004aQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        $this->patternCd = $this->field["PATTERN_CD"];
        $this->tokutaiSelect = $this->field["TOKUTAI_SELECT"];
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl004aQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    public function &getCopyYearModel()
    {
        $this->nextYear = $this->year + 1;

        //今年度のデータが存在するか確認
        $t_year = knjl004aQuery::CheckData($this->year);
        if (!isset($t_year)) {
            $this->setWarning('MSG203', "今年度のデータが存在しません。");
            return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjl004aQuery::CheckData($this->nextYear);
        if (isset($pre_year)) {
            $this->setWarning('MSG203', "既に次年度のデータが作成されています。");
            return false;
        }

        knjl004aQuery::getCopyQuery($this->year, $this->nextYear);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
