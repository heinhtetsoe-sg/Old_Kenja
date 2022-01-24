<?php

require_once('for_php7.php');

class knjl011wModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受付番号
    public $recomExamno;       //受検番号
    public $applicantdiv;      //入試制度
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl011wModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->recomExamno);
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
        $this->recomExamno  = VARS::post("RECOM_EXAMNO");
        if (strlen($this->examno) > 0) {
            $this->examno       = sprintf("%05d", $this->examno);
        }
        if (strlen($this->recomExamno) > 0) {
            $this->recomExamno  = sprintf("%05d", $this->recomExamno);
        }
        //検索ボタンを押した時は、受検番号で受付番号を取得
        if ($this->cmd == "reference" || $this->cmd == "back1" || $this->cmd == "next1") {
            $this->recomExamno       = knjl011wQuery::getReferenceExamno($this);
        }
        
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
            $this->recomExamno  = knjl011wQuery::getSendRecomExamno($this);
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("APPLICANTDIV"                   =>  VARS::post("APPLICANTDIV"),               //入試制度
                             "TESTDIV"                        =>  VARS::post("TESTDIV"),                    //入試区分
                             "TESTDIV2"                       =>  VARS::post("TESTDIV2"),                   //追検査
                             "SPORT_CD"                       =>  VARS::post("SPORT_CD"),                   //募集競技
                             "OBOEGAKI_SIGANSYA"              =>  VARS::post("OBOEGAKI_SIGANSYA"),          //覚書志願者
                             "KAIGAI_KIKOKUSEI_NADO"          =>  VARS::post("KAIGAI_KIKOKUSEI_NADO"),      //海外帰国生等
                             "TUUGAKU_KUIKIGAI_KYOKA"         =>  VARS::post("TUUGAKU_KUIKIGAI_KYOKA"),     //通学区域外許可
                             "HOSHOUNIN_TODOKE"               =>  VARS::post("HOSHOUNIN_TODOKE"),           //保証人届
                             "KENGAI_ZAIJUU"                  =>  VARS::post("KENGAI_ZAIJUU"),              //県外在住
                             "KENGAI_CHUUGAKKOU_SHUSSHIN"     =>  VARS::post("KENGAI_CHUUGAKKOU_SHUSSHIN"), //県外中学校出身
                             "KENGAI_CHUUGAKKOU_SHUSSHIN2"    =>  VARS::post("KENGAI_CHUUGAKKOU_SHUSSHIN2"), //県外中学校出身
//                             "NYUUGAKU_SIGAN_KYOKA"         =>  VARS::post("NYUUGAKU_SIGAN_KYOKA"),         //入学志願許可(13-2)
                             "EXAMCOURSE"                     =>  VARS::post("EXAMCOURSE"),                 //第１志望コース
                             "EXAMCOURSE2"                    =>  VARS::post("EXAMCOURSE2"),                //第２志望コース
                             "EXAMCOURSE_HENKOU"              =>  VARS::post("EXAMCOURSE_HENKOU"),          //第１志望コース
                             "EXAMCOURSE_HENKOU2"             =>  VARS::post("EXAMCOURSE_HENKOU2"),         //第２志望コース
                             "RECEPTDATE"                     =>  VARS::post("RECEPTDATE"),         //受付日付
                             "NAME"                           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"                      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "ERACD"                          =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"                        =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"                        =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"                        =>  VARS::post("BIRTH_D"),            //生年月日日
                             "BIRTHDAY"                       =>  VARS::post("BIRTHDAY"),           //生年月日
                             "FS_CD"                          =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_ERACD"                       =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"                       =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"                           =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"                           =>  VARS::post("FS_M"),               //卒業月
                             "FS_GRDDIV"                      =>  VARS::post("FS_GRDDIV"),          //卒業区分
                             "ZIPCD"                          =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"                       =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"                       =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"                          =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "GNAME"                          =>  VARS::post("GNAME"),              //氏名(保護者)
                             "RELATIONSHIP"                   =>  VARS::post("RELATIONSHIP"),       //続柄
                             "GKANA"                          =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"                         =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"                      =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"                      =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"                         =>  VARS::post("GTELNO"),             //電話番号(保護者)

                             "REMARK1"                        =>  VARS::post("REMARK1"),            //備考１
                             "REMARK2"                        =>  VARS::post("REMARK2")             //備考２
                            );
        //内申
        $this->field["CONFIDENTIAL_RPT01"] = VARS::post("CONFIDENTIAL_RPT01");
        $this->field["CONFIDENTIAL_RPT02"] = VARS::post("CONFIDENTIAL_RPT02");
        $this->field["CONFIDENTIAL_RPT03"] = VARS::post("CONFIDENTIAL_RPT03");
        $this->field["CONFIDENTIAL_RPT04"] = VARS::post("CONFIDENTIAL_RPT04");
        $this->field["CONFIDENTIAL_RPT05"] = VARS::post("CONFIDENTIAL_RPT05");
        $this->field["CONFIDENTIAL_RPT06"] = VARS::post("CONFIDENTIAL_RPT06");
        $this->field["CONFIDENTIAL_RPT07"] = VARS::post("CONFIDENTIAL_RPT07");
        $this->field["CONFIDENTIAL_RPT08"] = VARS::post("CONFIDENTIAL_RPT08");
        $this->field["CONFIDENTIAL_RPT09"] = VARS::post("CONFIDENTIAL_RPT09");
        $this->field["CONFIDENTIAL_RPT10"] = VARS::post("CONFIDENTIAL_RPT10");
        $this->field["TOTAL9"] = VARS::post("TOTAL9");
        $this->field["ABSENCE_DAYS1"] = VARS::post("ABSENCE_DAYS1");
        $this->field["ABSENCE_DAYS2"] = VARS::post("ABSENCE_DAYS2");
        $this->field["ABSENCE_DAYS3"] = VARS::post("ABSENCE_DAYS3");
    }

    //追加処理
    public function &getMaxExamno()
    {
        //MAX受検番号＋１
        $this->recomExamno = knjl011wQuery::getMaxRecomeExamnoQuery($this);
        //MAX受付番号＋１
        $this->examno = knjl011wQuery::getMaxExamnoQuery($this);
        return;
    }

    //入力項目チェック
    public function validateInput()
    {

        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受検番号 )");
            return false;
        }
        if (!($this->recomExamno)) {
            $this->setWarning("MSG301", "( 受付番号 )");
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
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //存在チェック
        $query = knjl011wQuery::existdata($this);
        $cnt = $db->getOne($query);
        if (strlen($cnt) && $cnt > 0 && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        if ((!strlen($cnt) || $cnt == 0) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック(RECOM_EXAMNO)
        $query = knjl011wQuery::existdataRecomExamno($this);
        $cnt = $db->getOne($query);
        if ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next") {
            if (!strlen($cnt) || $cnt == 0) {
                $this->setWarning("受付番号の変更はできません。");
                return false;
            }
        } elseif (strlen($cnt) && $cnt > 0) {
            $this->setWarning("MSG302", "( 受付番号 )");
            return false;
        }

        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915", "受検番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->recomExamno) > 5) {
            $this->setWarning("MSG915", "受付番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 120) {
            $this->setWarning("MSG915", "志願者( 氏名 )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 240) {
            $this->setWarning("MSG915", "志願者( ふりがな )は240バイトまでです。");
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
        if (strlen($this->field["GNAME"]) > 120) {
            $this->setWarning("MSG915", "保護者( 氏名 )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 240) {
            $this->setWarning("MSG915", "保護者( ふりがな )は240バイトまでです。");
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
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915", "備考１は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 60) {
            $this->setWarning("MSG915", "備考２は60バイトまでです。");
            return false;
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
            $result = knjl011wQuery::getUpdateQuery($this);
            $result = knjl011wQuery::getDelInsConfrptQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011wQuery::getUpdateQuery($this);
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
            $this->setWarning("MSG304", "( 受検番号 )");
            return false;
        }
        if ($this->recomExamno == "") {
            $this->setWarning("MSG304", "( 受付番号 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $query = knjl011wQuery::existdata($this);
        $cnt = $db->getOne($query);
        //DB切断
        Query::dbCheckIn($db);
        if (!strlen($cnt) || $cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        knjl011wQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->recomExamno = "";
        $this->setMessage("MSG201");
    }
}
