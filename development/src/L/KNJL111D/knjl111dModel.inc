<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');
class knjl111dModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $desirediv;         //志願区分
    var $testdiv;           //入試区分
    var $transferedFlg;     //転籍フラグ
    var $shdiv;
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl111dModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd = VARS::request("cmd");
        $this->year         = VARS::post("YEAR");
        $this->applicantdiv = 1;//1固定
        $this->shdiv        = 1;//1固定
        $this->semester     = CTRL_SEMESTER;
        $this->testdiv      = VARS::post("TESTDIV");
        $this->desirediv    = VARS::post("DESIREDIV");
        $this->examno       = VARS::post("EXAMNO");
        $this->pdffile      = VARS::file("PDF_FILE");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->testdiv      = VARS::get("SEND_TESTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("DESIREDIV"            =>  VARS::post("DESIREDIV"),            //志願区分
                             "TESTDIV"              =>  VARS::post("TESTDIV"),              //入試区分
                             "VISIT_NO"             =>  VARS::post("VISIT_NO"),             //事前相談番号

                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名かな(志願者)
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "ERACD"                =>  VARS::post("ERACD"),                //元号（生年月日）
                             "BIRTH_Y"              =>  VARS::post("BIRTH_Y"),              //年
                             "BIRTH_M"              =>  VARS::post("BIRTH_M"),              //月
                             "BIRTH_D"              =>  VARS::post("BIRTH_D"),              //日
                             "FS_CD"                =>  VARS::post("FINSCHOOLCD"),          //出身校（中学）
                             "FS_ERACD"             =>  VARS::post("FS_ERACD"),             //卒業元号
                             "FS_Y"                 =>  VARS::post("FS_Y"),                 //卒業年
                             "FS_M"                 =>  VARS::post("FS_M"),                 //卒業月
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV"),            //卒業区分
                             "REMARK8_033"          =>  VARS::post("REMARK8_033"),          //転編入学照会日
                             "REMARK2_033"          =>  VARS::post("REMARK2_033"),          //出身校（高校）コード
                             "REMARK3_033"          =>  VARS::post("REMARK3_033"),          //出身校（高校）課程
                             "REMARK4_033"          =>  VARS::post("REMARK4_033"),          //出身校（高校）学科
                             "REMARK5_033"          =>  VARS::post("REMARK5_033"),          //出身校（高校）学年
                             "REMARK6_033"          =>  VARS::post("REMARK6_033"),          //出身校（高校）状況
                             "REMARK9_033"          =>  VARS::post("REMARK9_033"),          //調査書照会日
                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号

                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名かな(保護者)
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),         //続柄
                             "GZIPCD"               =>  VARS::post("GZIPCD"),               //郵便番号(保護者)
                             "GADDRESS1"            =>  VARS::post("GADDRESS1"),            //住所(保護者)
                             "GADDRESS2"            =>  VARS::post("GADDRESS2"),            //方書(保護者)
                             "GTELNO"               =>  VARS::post("GTELNO"),               //電話番号(保護者)
                             "GTELNO2"              =>  VARS::post("GTELNO2"),              //携帯電話(保護者)

                            );

        //DB接続
        $db = Query::dbCheckOut();
        $this->transferedFlg = $db->getOne(knjl111dQuery::getTrabsferedFlg($this));
        //DB切断
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->field["DESIREDIV"])) {
            $this->setWarning("MSG301", "( 志願区分 )");
            return false;
        }
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 志願者 氏名かな )");
            return false;
        }
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }

        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //必須チェック(転籍フラグが立っているとき)
        if ($this->transferedFlg == "1") {
            if ($this->field["REMARK2_033"] == "" || $this->field["REMARK3_033"] == "" || $this->field["REMARK4_033"] == "" || 
                $this->field["REMARK5_033"] == "" || $this->field["REMARK6_033"] == "") {
                $this->setWarning("MSG301", "( 出身校（高等学校）の情報が入力されていません。 )");
                return false;
            }
        }

        //文字数チェック
        //志願者
        if (strlen($this->examno) > 3) {
            $this->setWarning("MSG915","受験番号は3バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( 氏名かな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身校（中学）は7バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2_033"]) > 7) {
            $this->setWarning("MSG915","出身校（高校）は7バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK4_033"]) > 150) {
            $this->setWarning("MSG915","出身校（高校）学科は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK5_033"]) > 150) {
            $this->setWarning("MSG915","出身校（高校）学年は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 150) {
            $this->setWarning("MSG915","志願者( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 150) {
            $this->setWarning("MSG915","志願者( 方書 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        //保護者
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( 氏名かな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS1"]) > 150) {
            $this->setWarning("MSG915","保護者( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 150) {
            $this->setWarning("MSG915","保護者( 方書 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915","保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO2"]) > 14) {
            $this->setWarning("MSG915","保護者( 携帯電話 )は14バイトまでです。");
            return false;
        }

        //重複チェック
        $cnt = $db->getOne(knjl111dQuery::getExistdataCnt($this));
        if ($cnt > 0 && $this->cmd == "add") {
            $this->setWarning("MSG302", '受験番号');
            return false;
        }

        //存在チェック
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303", '受験番号');
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjl111dQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl111dQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
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
        $cnt = $db->getOne(knjl111dQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl111dQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>