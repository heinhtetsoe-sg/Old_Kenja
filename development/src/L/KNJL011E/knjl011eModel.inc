<?php

require_once('for_php7.php');

class knjl011eModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011eModel() {
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
        $this->cmd      = VARS::request("cmd");
        $this->year     = CTRL_YEAR + 1;
        $this->examno   = VARS::post("EXAMNO");
        $this->pdffile  = VARS::file("PDF_FILE");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "TESTDIV"              =>  VARS::post("TESTDIV"),              //入試区分
                             "TESTDIV1"             =>  VARS::post("TESTDIV1"),             //受験区分
                             "DESIREDIV"            =>  VARS::post("DESIREDIV"),            //出願コース
                             "SHDIV"                =>  VARS::post("SHDIV"),                //専併
                             "LOGIN_ID"             =>  VARS::post("LOGIN_ID"),             //ログインID

                             /* 志願者情報 */
                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名カナ(志願者)
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日
                             "FINSCHOOLCD"          =>  VARS::post("FINSCHOOLCD"),          //出身校（中学）
                             "FS_DAY"               =>  VARS::post("FS_DAY"),               //卒業年月
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV"),            //卒業区分
                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号
                             "ENT_MONEY_STATUS"     =>  VARS::post("ENT_MONEY_STATUS"),     //入学金支払状況
                             "RECOMMENDATION"       =>  VARS::post("RECOMMENDATION"),       //本校同窓会推薦書

                             /* 保護者情報 */
                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名カナ(保護者)
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),         //続柄
                             "GZIPCD"               =>  VARS::post("GZIPCD"),               //郵便番号(保護者)
                             "GADDRESS1"            =>  VARS::post("GADDRESS1"),            //住所(保護者)
                             "GADDRESS2"            =>  VARS::post("GADDRESS2"),            //方書(保護者)
                             "GTELNO"               =>  VARS::post("GTELNO"),               //電話番号(保護者)

                             /* 音楽専攻科 */
                             "HOPE1"                =>  VARS::post("HOPE1"),                //第一希望
                             "HOPE2"                =>  VARS::post("HOPE2"),                //第二希望
                             "HOPE"                 =>  VARS::post("HOPE"),                 //出願専攻
                             "NOMAL_SHDIV"          =>  VARS::post("NOMAL_SHDIV"),          //普通科併願

                             /* 推薦資格審査 */
                             "QUALIFIED_CONTENT"    =>  VARS::post("QUALIFIED_CONTENT"),    //資格内容
                             "QUALIFIED"            =>  VARS::post("QUALIFIED"),            //資格
                             "PRACTICE"             =>  VARS::post("PRACTICE"),             //実技有無
                             "POSSIBLE"             =>  VARS::post("POSSIBLE"),             //判定

                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //PDFアップロード
    function &getExecPdfModel() {
        //ファイル名取得
        $target = $_FILES['PDF_FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "pdf" && $kakutyousi != "PDF") {
            $this->warning = "PDFファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        $filename = DOCUMENTROOT.'/image/ENTEXAM/'.'KNJL011E_'.$this->year.'_'.$this->field["APPLICANTDIV"].'_'.$this->examno.'.pdf';
        if (!is_uploaded_file($this->pdffile['tmp_name'])) {
            $this->warning = "ファイル名又は\\nサイズ(500KB以上)等が不正です。";
        } else {
            if (move_uploaded_file($this->pdffile['tmp_name'], $filename) == FALSE){
                $this->warning = "PDFファイルの取込失敗です。";
            } else {
                $this->message = "PDFファイルの取込正常です。";
            }
        }
    }

    //PDF削除
    function getPdfDeleteModel() {
        $path_file  = 'KNJL011E_'.$this->year.'_'.$this->field["APPLICANTDIV"].'_'.$this->examno.'.pdf';
        $path_file  = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", 'KNJL011E_'.$this->year.'_'.$this->field["APPLICANTDIV"].'_'.$this->examno);
        $dir        = '/image/ENTEXAM/';

        $path_file = DOCUMENTROOT .$dir.$path_file;
        /* ファイルの存在確認 */
        if (!file_exists($path_file)) {
            $this->setWarning("MSG303","指定の受験番号のPDFデータは存在しません。");
            return false;
        }

        if (unlink($path_file)) {
            $this->message = "PDFファイルを削除しました。";
        } else {
            $this->warning = "PDFファイルの削除に失敗しました。";
        }

        return true;
    }

    //入力項目チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
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
            $this->setWarning("MSG301", "( 志願者 氏名カナ )");
            return false;
        }
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }

        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }

        //文字数チェック
        /* 志願者 */
        if (strlen($this->examno) > 7) {
            $this->setWarning("MSG915","受験番号は7バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( 氏名カナ )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["FINSCHOOLCD"]) > 7) {
            $this->setWarning("MSG915","出身校学校は7バイトまでです。");
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

        /* 保護者 */
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( 氏名カナ )は120バイトまでです。");
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

        /* 推薦資格者 */
        if (strlen($this->field["HOPE1"]) > 150) {
            $this->setWarning("MSG915","第一希望は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["HOPE2"]) > 150) {
            $this->setWarning("MSG915","第二希望は150バイトまでです。");
            return false;
        }

        /* 推薦資格者 */
        if ($this->validate_row_cnt($this->field["QUALIFIED_CONTENT"], (40 * 2)) > 4) {
            $this->setWarning("MSG915","(資格内容は4行までです)");
            return false;
        }

        //存在チェック
        $cnt = $db->getOne(knjl011eQuery::getExistdataCnt($this));
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303", '受験番号');
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl011eQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
?>