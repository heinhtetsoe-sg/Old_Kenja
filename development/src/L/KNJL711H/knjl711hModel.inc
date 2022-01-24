<?php
class knjl711hModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantDiv;      //学校種別
    public $testDiv;           //入試区分
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl711hModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examNo);
            unset($this->applicantDiv);
            unset($this->testDiv);
            unset($this->kindDiv);
            unset($this->year);
            unset($this->checkexam);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd          = VARS::request("cmd");
        $this->year         = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantDiv = VARS::post("APPLICANTDIV");
        $this->testDiv      = VARS::post("TESTDIV");
        $this->kindDiv      = VARS::post("KINDDIV");
        $this->examNo       = VARS::post("EXAMNO");
        
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantDiv = VARS::get("SEND_APPLICANTDIV");
            $this->examNo       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array(
                            //志願者情報
                            "NAME"                 =>  VARS::post("NAME"),                 //氏名
                            "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名カナ
                            "SEX"                  =>  VARS::post("SEX"),                  //性別
                            "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日
                            "FS_CD"                =>  VARS::post("FINSCHOOLCD"),          //出身学校コード
                            "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号
                            "TELNO"                =>  VARS::post("TELNO"),                //電話番号
                            "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所
                            "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書
                            //保護者情報
                            "GNAME"                =>  VARS::post("GNAME"),                //氏名
                            "GKANA"                =>  VARS::post("GKANA"),                //氏名カナ
                            "GTELNO"               =>  VARS::post("GTELNO"),               //緊急連絡先
                            //資格
                            "QUALIFIED_ENG_CD"     => VARS::post("QUALIFIED_ENG_CD"),       //資格1
                            "QUALIFIED_KANJI_CD"   => VARS::post("QUALIFIED_KANJI_CD"),     //資格2
                            "QUALIFIED_MATH_CD"    => VARS::post("QUALIFIED_MATH_CD"),      //資格3
                            "QUALIFIED_READING_CD" => VARS::post("QUALIFIED_READING_CD"),   //資格4
                            "ATHLETIC_PRIZE_CD"    => VARS::post("ATHLETIC_PRIZE_CD"),        //運動
                            "CULTURE_PRIZE_CD"     => VARS::post("CULTURE_PRIZE_CD"),       //文化
                            "SCH_COUNCIL_PRIZE_CD" => VARS::post("SCH_COUNCIL_PRIZE_CD"),   //生徒会
                            "PLUS_POINT_TOTAL"     => VARS::post("PLUS_POINT_TOTAL"),       //加点
                            //その他
                            "BIKOU_REMARK"         => VARS::post("BIKOU_REMARK"),       //備考
                            "CONCURRENT_APP_FLG"   => VARS::post("CONCURRENT_APP_FLG"), //重複出願（同時）
                            "MULTI_APP_FLG"        => VARS::post("MULTI_APP_FLG"),      //重複出願（複数）
                            "OVER_AGE_FLG"         => VARS::post("OVER_AGE_FLG"),       //年齢超過
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    public function validateInput()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->examNo)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->applicantDiv)) {
            $this->setWarning("MSG301", "( 学校種別 )");
            return false;
        }
        if (!($this->testDiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->kindDiv)) {
            $this->setWarning("MSG301", "( 入試種別 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }
        if (!($this->field["BIRTHDAY"])) {
            $this->setWarning("MSG301", "( 生年月日 )");
            return false;
        }

        //キー値変更チェック
        if ($this->examNo != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //文字数チェック
        if (strlen($this->examNo) > 4) {
            $this->setWarning("MSG915", "受験番号は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915", "志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915", "志願者( カナ )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 90) {
            $this->setWarning("MSG915", "志願者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 90) {
            $this->setWarning("MSG915", "志願者( 方書 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 8) {
            $this->setWarning("MSG915", "出身学校コードは8バイトまでです。");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915", "志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915", "保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915", "保護者( カナ )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 緊急連絡先 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIKOU_REMARK"]) > 120) {
            $this->setWarning("MSG915", "備考は120バイトまでです。");
            return false;
        }

        $cnt = $db->getOne(knjl711hQuery::getExistdataCnt($this));
        //存在チェック
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl711hQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        if ($this->examNo == "") {
            $this->setWarning("MSG304", "( 受験番号 )");
            return false;
        }
        if ($this->examNo != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knjl711hQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl711hQuery::getDeleteQuery($this);
        $this->examNo = "";
        $this->setMessage("MSG201");
    }
}
