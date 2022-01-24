<?php
class knjl063iModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $testdiv;           //入試区分
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl063iModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->examno       = VARS::post("EXAMNO");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "TESTDIV_ABBV"         =>  VARS::post("TESTDIV_ABBV"),         //入試区分
                             "CLEAR_FLG"            =>  VARS::post("CLEAR_FLG"),            //照合したRECOM_EXAMNOを削除するかのフラグ
                             "RECOM_EXAMNO"         =>  VARS::post("RECOM_EXAMNO"),         //併願受験番号

                            //志願者情報
                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名カナ(志願者)
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "FS_CD"                =>  VARS::post("FINSCHOOLCD"),          //出身学校コード
                             "FS_ERACD"             =>  VARS::post("FS_ERACD"),             //卒業年元号コード
                             "FS_Y"                 =>  VARS::post("FS_Y"),                 //卒業年
                             "FS_M"                 =>  VARS::post("FS_M"),                 //卒業月
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV"),            //卒業区分
                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号(志願者)
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所(志願者)
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書(志願者)
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号(志願者)
                            //保護者情報
                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名カナ(保護者)
                             "GZIPCD"               =>  VARS::post("GZIPCD"),               //郵便番号(保護者)
                             "GADDRESS1"            =>  VARS::post("GADDRESS1"),            //住所(保護者)
                             "GADDRESS2"            =>  VARS::post("GADDRESS2"),            //方書(保護者)
                             "GTELNO"               =>  VARS::post("GTELNO"),               //電話番号(保護者)
                             "GTELNO2"              =>  VARS::post("GTELNO2"),              //携帯番号(保護者) ※中等部のみ
                            //送付先情報 ※高等部のみ
                            "SEND_ZIPCD"            =>  VARS::post("SEND_ZIPCD"),           //郵便番号(送付先)
                            "SEND_ADDRESS1"         =>  VARS::post("SEND_ADDRESS1"),        //住所(送付先)
                            "SEND_ADDRESS2"         =>  VARS::post("SEND_ADDRESS2"),        //方書(送付先)
                            "SEND_TELNO"            =>  VARS::post("SEND_TELNO"),           //電話番号(送付先)
                            //その他 ※高等部のみ
                            "RANK"                  =>  VARS::post("RANK"),                 //郵便番号(送付先)
                            "SCHOOLCD1"             =>  VARS::post("SCHOOLCD1"),            //住所(送付先)
                            "SCHOOLCD2"             =>  VARS::post("SCHOOLCD2"),            //方書(送付先)
                            );

        //ゼロ埋め
        $this->field["FS_Y"] = ($this->field["FS_Y"] != "") ? sprintf("%02d", $this->field["FS_Y"]) : "";
        $this->field["FS_M"] = ($this->field["FS_M"] != "") ? sprintf("%02d", $this->field["FS_M"]) : "";

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    public function validateInput()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 志願者 カナ )");
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
        if (!($this->field["ZIPCD"])) {
            $this->setWarning("MSG301", "( 志願者郵便番号 )");
            return false;
        }
        if (!($this->field["ADDRESS1"])) {
            $this->setWarning("MSG301", "( 志願者住所 )");
            return false;
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 4) {
            $this->setWarning("MSG915", "受験番号は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915", "志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 60) {
            $this->setWarning("MSG915", "志願者( カナ )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 150) {
            $this->setWarning("MSG915", "志願者( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 150) {
            $this->setWarning("MSG915", "志願者( 方書 )は150バイトまでです。");
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
        if (strlen($this->field["GKANA"]) > 60) {
            $this->setWarning("MSG915", "保護者( カナ )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS1"]) > 150) {
            $this->setWarning("MSG915", "保護者( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 150) {
            $this->setWarning("MSG915", "保護者( 方書 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO2"]) > 14) {
            $this->setWarning("MSG915", "保護者( 携帯電話 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["SEND_ADDRESS1"]) > 150) {
            $this->setWarning("MSG915", "送付先( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["SEND_ADDRESS2"]) > 150) {
            $this->setWarning("MSG915", "送付先( 方書 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["SEND_TELNO"]) > 14) {
            $this->setWarning("MSG915", "送付先( 電話番号 )は14バイトまでです。");
            return false;
        }

        if ($this->field["ADDRESS1"] != "") {
            $allZenkakuMoji = mb_convert_kana($this->field["ADDRESS1"], "RNASKHC");
            if (strlen($allZenkakuMoji) != strlen($this->field["ADDRESS1"])) {
                $this->setWarning("MSG901", "志願者( 住所 )は半角不可です。");
                return false;
            }
        }
        if ($this->field["ADDRESS2"] != "") {
            $allZenkakuMoji = mb_convert_kana($this->field["ADDRESS2"], "RNASKHC");
            if (strlen($allZenkakuMoji) != strlen($this->field["ADDRESS2"])) {
                $this->setWarning("MSG901", "志願者( 方書 )は半角不可です。");
                return false;
            }
        }
        if ($this->field["GADDRESS1"] != "") {
            $allZenkakuMoji = mb_convert_kana($this->field["GADDRESS1"], "RNASKHC");
            if (strlen($allZenkakuMoji) != strlen($this->field["GADDRESS1"])) {
                $this->setWarning("MSG901", "保護者( 住所 )は半角不可です。");
                return false;
            }
        }
        if ($this->field["GADDRESS2"] != "") {
            $allZenkakuMoji = mb_convert_kana($this->field["GADDRESS2"], "RNASKHC");
            if (strlen($allZenkakuMoji) != strlen($this->field["GADDRESS2"])) {
                $this->setWarning("MSG901", "保護者( 方書 )は半角不可です。");
                return false;
            }
        }
        if ($this->field["SEND_ADDRESS1"] != "") {
            $allZenkakuMoji = mb_convert_kana($this->field["SEND_ADDRESS1"], "RNASKHC");
            if (strlen($allZenkakuMoji) != strlen($this->field["SEND_ADDRESS1"])) {
                @
                $this->setWarning("MSG901", "送付先( 住所 )は半角不可です。");
                return false;
            }
        }
        if ($this->field["SEND_ADDRESS2"] != "") {
            $allZenkakuMoji = mb_convert_kana($this->field["SEND_ADDRESS2"], "RNASKHC");
            if (strlen($allZenkakuMoji) != strlen($this->field["SEND_ADDRESS2"])) {
                $this->setWarning("MSG901", "送付先( 方書 )は半角不可です。");
                return false;
            }
        }

        $cnt = $db->getOne(knjl063iQuery::getExistdataCnt($this));
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
            $result = knjl063iQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        if ($this->examno == "") {
            $this->setWarning("MSG304", "( 受験番号 )");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knjl063iQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl063iQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
}
