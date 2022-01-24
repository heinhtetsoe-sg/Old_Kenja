<?php
class knjl412hModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $receptno;          //志願者SEQ
    public $checkrecept;
    public $field = array();           //フィールドデータ
    public $maxHopeNum;         //出願内容 最大行数
    public $maxHopeCourseNum;   //出願内容 最大希望数
    public $managementFlg;

    public function knjl412hModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->receptno);
            unset($this->year);
            unset($this->checkrecept);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->examno       = VARS::post("EXAMNO");       //志願者SEQ
        $this->receptno     = VARS::post("RECEPTNO");     //志願者SEQ
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->examno = ($this->examno) ? sprintf("%05d", $this->examno) : "";
        $this->receptno = ($this->receptno) ? sprintf("%05d", $this->receptno) : "";

        //フィールド
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試区分
                             "EXAMNO"         =>  VARS::post("EXAMNO"),             //志願者SEQ
                             "USERSEQ"        =>  VARS::post("USERSEQ"),            //ログインユーザーSEQ
                             "MAIL_LOGIN"     =>  VARS::post("MAIL_LOGIN"),         //メールアドレス ログイン
                             "MAIL_APP"       =>  VARS::post("MAIL_APP"),           //メールアドレス 申込時

                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEQ005_REAMRK1"    =>  VARS::post("SEQ005_REAMRK1"),        //宗内生
                             "SEQ005_REAMRK2"    =>  VARS::post("SEQ005_REAMRK2"),        //生徒会
                             "SEQ005_REAMRK3"    =>  VARS::post("SEQ005_REAMRK3"),        //資格
                             "SEQ005_REAMRK4"    =>  VARS::post("SEQ005_REAMRK4"),        //資格
                             "SEQ005_REAMRK5"    =>  VARS::post("SEQ005_REAMRK5"),        //資格
                             "SEQ005_REAMRK6"    =>  VARS::post("SEQ005_REAMRK6"),        //その他
                             "BIRTHDAY"       =>  VARS::post("BIRTHDAY"),           //生年月日
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "FS_CD"          =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_ERACD"       =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"       =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"           =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"           =>  VARS::post("FS_M"),               //卒業月
                             "FS_GRDDIV"      =>  VARS::post("FS_GRDDIV"),          //卒業区分
                             "ZIPCD"          =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"       =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"       =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"          =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "PRISCHOOLCD"        =>  VARS::post("PRISCHOOLCD"),        //塾
                             "PRISCHOOL_CLASS_CD" =>  VARS::post("PRISCHOOL_CLASS_CD"), //教室

                             "GNAME"          =>  VARS::post("GNAME"),              //氏名(保護者)
                             "GRELATIONSHIP"  =>  VARS::post("GRELATIONSHIP"),      //続柄
                             "GKANA"          =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"         =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"      =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"      =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "EMERGENCYTELNO" =>  VARS::post("EMERGENCYTELNO"),     //連絡先1(保護者)
                             "EMERGENCYCALL"  =>  VARS::post("EMERGENCYCALL"),      //連絡先2(保護者)
                            );

        $this->getPropertiesAll(); //プロパティファイル

        //出願内容
        $this->field["APPLICANTDIV"] = ($this->field["APPLICANTDIV"] == "") ? "1" : $this->field["APPLICANTDIV"]; //初期値設定
        $this->maxHopeNum = ($this->field["APPLICANTDIV"] == "1") ? 5 : 2; //中学の場合：5行 , それ以外の場合：2行
        $this->maxHopeCourseNum = ($this->field["APPLICANTDIV"] == "1") ? 2 : 3; //中学の場合：第２志望まで , それ以外の場合：第３志望まで
        for ($hopeNum = 1; $hopeNum <= $this->maxHopeNum; $hopeNum++) {
            $this->field["HOPE".$hopeNum."_RECEPTNO"]     = VARS::post("HOPE".$hopeNum."_RECEPTNO");    //出願内容 受験番号
            $this->field["HOPE".$hopeNum."_EXAM_TYPE"]    = VARS::post("HOPE".$hopeNum."_EXAM_TYPE");   //出願内容 受験型
            for ($hopeCourseNum = 1; $hopeCourseNum <= $this->maxHopeCourseNum; $hopeCourseNum++) {
                $field1 = "HOPE".$hopeNum."_COURSE".$hopeCourseNum;
                $field2 = "HOPE".$hopeNum."_TESTDIV".$hopeCourseNum;
                $this->field[$field1]    = VARS::post($field1); //出願内容 第１希望 ～ 第３希望 コース
                $this->field[$field2]    = VARS::post($field2); //出願内容 第１希望 ～ 第３希望 出願区分
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->receptno)) {
            $this->setWarning("MSG301", "( 志願者SEQ )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }

        //必須チェック
        if (!($this->field["USERSEQ"])) {
            $this->setWarning("MSG301", "( ログインユーザーSEQ )");
            return false;
        }
        if (!($this->field["MAIL_LOGIN"])) {
            $this->setWarning("MSG301", "( メールアドレス ログイン )");
            return false;
        }
        if (!($this->field["MAIL_APP"])) {
            $this->setWarning("MSG301", "( メールアドレス 申込時 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 志願者 ふりがな )");
            return false;
        }
        if (!($this->field["ZIPCD"])) {
            $this->setWarning("MSG301", "( 志願者 郵便番号 )");
            return false;
        }
        if (!($this->field["ADDRESS1"])) {
            $this->setWarning("MSG301", "( 志願者 住所 )");
            return false;
        }
        if (!($this->field["GNAME"])) {
            $this->setWarning("MSG301", "( 保護者 氏名 )");
            return false;
        }
        if (!($this->field["GKANA"])) {
            $this->setWarning("MSG301", "( 保護者 ふりがな )");
            return false;
        }
        if (!($this->field["GRELATIONSHIP"])) {
            $this->setWarning("MSG301", "( 保護者 続柄 )");
            return false;
        }
        if (!($this->field["GZIPCD"])) {
            $this->setWarning("MSG301", "( 保護者 郵便番号 )");
            return false;
        }
        if (!($this->field["GADDRESS1"])) {
            $this->setWarning("MSG301", "( 保護者 住所 )");
            return false;
        }

        //出願内容の必須チェック
        $hopeChk = false;
        for ($hopeNum = 1; $hopeNum <= $this->maxHopeNum; $hopeNum++) {
            $hopeReceptno = "HOPE".$hopeNum."_RECEPTNO";
            $hopeExamType = "HOPE".$hopeNum."_EXAM_TYPE";
            //受験番号、受験型 いずれかが未入力の場合
            if (!($this->field[$hopeReceptno]) || !($this->field[$hopeExamType])) {
                continue;
            }
            $hopeChk = true;
        }
        if ($hopeChk == false) {
            //1件以上入力されていない場合
            $this->setWarning("MSG301", "出願内容が未入力です。");
            return false;
        }


        //キー値変更チェック
        if ($this->receptno != $this->checkrecept) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //存在チェック
        $query = knjl412hQuery::existdata($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915", "志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915", "志願者( ふりがな )は120バイトまでです。");
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
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915", "出身学校コードは7バイトまでです。");
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
        if (strlen($this->field["GADDRESS1"]) > 90) {
            $this->setWarning("MSG915", "保護者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 90) {
            $this->setWarning("MSG915", "保護者( 方書 )は90バイトまでです。");
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
            $result = knjl412hQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
