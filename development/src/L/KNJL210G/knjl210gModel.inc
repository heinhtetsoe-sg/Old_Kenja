<?php

require_once('for_php7.php');

class knjl210gModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl210gModel()
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
        $this->field = array("APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV"              =>  VARS::post("TESTDIV"),            //入試区分
                             "GRADE"                =>  VARS::post("GRADE"),              //学年
                             "EXAMCOURSE"           =>  VARS::post("EXAMCOURSE"),         //第１志望
                             "EXAMCOURSE2"          =>  VARS::post("EXAMCOURSE2"),        //第２志望
                             "SHDIV2"               =>  VARS::post("SHDIV2"),             //専併区分
                             "RECEPTDATE"           =>  VARS::post("RECEPTDATE"),         //受付日付

                             "EXAM_PAY_DIV"         =>  VARS::post("EXAM_PAY_DIV"),       //受験料・入金方法
                             "EXAM_PAY_DATE"        =>  VARS::post("EXAM_PAY_DATE"),      //受験料・入金日
                             "EXAM_PAY_CHAK_DATE"   =>  VARS::post("EXAM_PAY_CHAK_DATE"), //受験料・着金日

                             "SPECIAL_REASON_DIV"   =>  VARS::post("SPECIAL_REASON_DIV"), //特別措置者
                             "GANSHO_YUUSOU"        =>  VARS::post("GANSHO_YUUSOU"),      //願書郵送
                             "NAME"                 =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"                  =>  VARS::post("SEX"),                //性別
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),           //生年月日
                             "FS_CD"                =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_ERACD"             =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"             =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"                 =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"                 =>  VARS::post("FS_M"),               //卒業月
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV"),          //卒業区分
                             "ZIPCD"                =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"                =>  VARS::post("TELNO"),              //電話番号(志願者)

                             "SHDIV"                =>  VARS::post("SHDIV"),              //専併区分
                             "REMARK3_016"          =>  VARS::post("REMARK3_016"),        //延期願チェックボックス
                             "REMARK1_016"          =>  VARS::post("REMARK1_016"),        //併願校名
                             "REMARK2_016"          =>  VARS::post("REMARK2_016"),        //合格発表日
                             "REMARK1_029"          =>  VARS::post("REMARK1_029"),        //特待希望チェックボックス
                             "REMARK2_029"          =>  VARS::post("REMARK2_029"),        //特待希望コンボ
                             "REMARK10_029"         =>  VARS::post("REMARK10_029"),       //特待希望テキストボックス

                             "GNAME"                =>  VARS::post("GNAME"),              //氏名(保護者)
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),       //続柄
                             "GKANA"                =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"               =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"            =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"            =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"               =>  VARS::post("GTELNO"),             //電話番号(保護者)
                             "EMERGENCYCALL"        =>  VARS::post("EMERGENCYCALL"),      //緊急連絡先(保護者)

                             "REMARK6_018"          =>  VARS::post("REMARK6_018"),        //本校卒業日
                             "REMARK5_018"          =>  VARS::post("REMARK5_018"),        //本校卒業コースコンボ
                             "REMARK7_018"          =>  VARS::post("REMARK7_018"),        //在籍フラグコンボ
                             "REMARK1_018"          =>  VARS::post("REMARK1_018"),        //氏名(本校卒業・在校生)
                             "REMARK3_018"          =>  VARS::post("REMARK3_018"),        //続柄コンボ
                             "REMARK10_032"         =>  VARS::post("REMARK10_032")        //志望理由
                            );

        //DB接続
        $db = Query::dbCheckOut();

        $this->l008Arr = array();
        $query = knjl210gQuery::getNameCd($this->year, "L008");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setVal = $row["VALUE"];
            $this->field["CONFIDENTIAL_RPT".$setVal] = VARS::post("CONFIDENTIAL_RPT".$setVal);
            $this->l008Arr[$setVal] = $row["ABBV1"];
        }
        $this->field["TOTAL5"]        = VARS::post("TOTAL5");
        $this->field["TOTAL_ALL"]     = VARS::post("TOTAL_ALL");
        $this->field["ABSENCE_DAYS3"] = VARS::post("ABSENCE_DAYS3");

        //DB切断
        Query::dbCheckIn($db);

        //志望理由
        $this->getPro["REMARK10_032"]["moji"] = 74;
        $this->getPro["REMARK10_032"]["gyou"] = 7;
    }

    //追加処理
    public function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl210gQuery::getMaxExamnoQuery($this);
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
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->field["SHDIV"])) {
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        if (!($this->field["EXAMCOURSE"])) {
            $this->setWarning("MSG301", "( 第１志望 )");
            return false;
        }
        if (!($this->field["RECEPTDATE"])) {
            $this->setWarning("MSG301", "( 受付日付 )");
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
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }
        if (strlen($this->field["GNAME"]) != 0 && strlen($this->field["GZIPCD"]) == 0) {
            $this->setWarning("MSG301", "( 保護者郵便番号 )");
            return false;
        }
        if (strlen($this->field["GNAME"]) != 0 && strlen($this->field["GADDRESS1"]) == 0) {
            $this->setWarning("MSG301", "( 保護者住所 )");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //存在チェック
        $query = knjl210gQuery::existdata($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (isset($row["EXAMNO"]) && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        if (!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }

        //文字数チェック
        if (strlen($this->examno) > 8) {
            $this->setWarning("MSG915", "受験番号は8バイトまでです。");
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
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["EMERGENCYCALL"]) > 14) {
            $this->setWarning("MSG915", "保護者( 緊急連絡先 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1_016"]) > 150) {
            $this->setWarning("MSG915", "併願校名は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK10_029"]) > 1500) {
            $this->setWarning("MSG915", "特待希望理由は1500バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK6_018"]) > 4) {
            $this->setWarning("MSG915", "本校卒業・在籍生情報( 卒業年 )は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1_018"]) > 60) {
            $this->setWarning("MSG915", "本校卒業・在籍生情報( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK10_032"]) > 789) {
            $this->setWarning("MSG915", "志望理由は789バイトまでです。");
            return false;
        }

        //行数チェック
        $chkArr = array();
        $chkArr[] = array("fieldName" => "REMARK10_032",      "j_Title" => "志望理由");

        foreach ($chkArr as $key => $valArr) {
            if ($this->validate_row_cnt($this->field["{$valArr["fieldName"]}"], ($this->getPro["{$valArr["fieldName"]}"]["moji"])) > $this->getPro["{$valArr["fieldName"]}"]["gyou"]) {
                $this->setWarning("MSG915", "({$valArr["j_Title"]}は{$this->getPro["{$valArr["fieldName"]}"]["gyou"]}行までです)");
                return false;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjl210gQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl210gQuery::getUpdateQuery($this);
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
        //DB接続
        $db = Query::dbCheckOut();
        $query = knjl210gQuery::existdata($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //DB切断
        Query::dbCheckIn($db);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        knjl210gQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
}
