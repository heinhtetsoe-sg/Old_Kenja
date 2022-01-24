<?php

require_once('for_php7.php');

class knjl611aModel extends Model
{

    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $testdiv;           //入試区分
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl611aModel()
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
            unset($this->fubanMukou);
        }
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = "1";
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
                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名かな(志願者)
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "FS_CD"                =>  VARS::post("FINSCHOOLCD"),          //出身学校コード
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV"),            //卒業区分
                             "BASE005_R1"           =>  VARS::post("BASE005_R1"),           //特別活動優遇
                             "BASE005_R2"           =>  VARS::post("BASE005_R2"),           //帰国生優遇
                             "BASE005_R3"           =>  VARS::post("BASE005_R3"),           //英検取得級
                             "BASE005_R4"           =>  VARS::post("BASE005_R4"),           //入学金延納希望
                             "BASE005_R5"           =>  VARS::post("BASE005_R5"),           //受験校1
                             "BASE005_R6"           =>  VARS::post("BASE005_R6"),           //受験校2
                             "BASE005_R7"           =>  VARS::post("BASE005_R7"),           //受験校3

                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号(志願者)
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所(志願者)
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書(志願者)
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号(志願者)
                             "EMAIL"                =>  VARS::post("EMAIL"),                //メールアドレス(志願者)
                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名かな(保護者)
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),         //続柄(保護者)
                             "GTELNO"               =>  VARS::post("GTELNO"),               //電話番号(保護者)
                             "EMERGENCYTELNO"       =>  VARS::post("EMERGENCYTELNO"),       //緊急連絡先(保護者)

                             "PRISCHOOLCD"          =>  VARS::post("PRISCHOOLCD"),          //塾
                             "PRISCHOOL_CLASS_CD"   =>  VARS::post("PRISCHOOL_CLASS_CD"),   //教室

                             "BASE009_R1"           =>  VARS::post("BASE009_R1"),           //備考1
                             "BASE009_R2"           =>  VARS::post("BASE009_R2"),           //備考2
                             "BASE009_R3"           =>  VARS::post("BASE009_R3"),           //備考3
                             "BASE009_R4"           =>  VARS::post("BASE009_R4"),           //備考4
                             "BASE009_R5"           =>  VARS::post("BASE009_R5")            //備考5
                            );
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //受験番号取得
    public function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl611aQuery::getMaxExamnoQuery($this);
        return;
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
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
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
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915", "志願者( ふりがな )は120バイトまでです。");
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
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915", "保護者( ふりがな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["EMERGENCYTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 緊急連絡先 )は14バイトまでです。");
            return false;
        }
        $cnt = $db->getOne(knjl611aQuery::getExistdataCnt($this));
        //存在チェック
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }
    
    //追加処理
    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            $result = knjl611aQuery::getUpdateQuery($this, "INSERT");
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl611aQuery::getUpdateQuery($this);
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
        $cnt = $db->getOne(knjl611aQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl611aQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
}
?>