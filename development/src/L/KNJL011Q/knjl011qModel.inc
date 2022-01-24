<?php

require_once('for_php7.php');

class knjl011qModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011qModel()
    {
    }

    function init()
    {
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
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->examno       = VARS::post("EXAMNO");
        if (SCHOOLKIND == "P") {
            $this->nameMstTest = "LP24";
        } else if (SCHOOLKIND == "J") {
            $this->nameMstTest = "L024";
        } else {
            $this->nameMstTest = "L004";
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("KUBUN"            =>  VARS::post("KUBUN"),            //受験区分(表示のみ)
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "GENERAL_FLG"      =>  VARS::post("GENERAL_FLG"),      //基準テスト対象者
                             "DORMITORY_FLG"    =>  VARS::post("DORMITORY_FLG"),    //入寮希望
                             "SCHOLAR_KIBOU"    =>  VARS::post("SCHOLAR_KIBOU"),    //スカラー希望
                             "SHDIV"            =>  VARS::post("SHDIV"),            //専併区分
                             "SH_SCHOOLCD1"     =>  VARS::post("SH_SCHOOLCD1"),     //志望校1
                             "SH_SCHOOLCD2"     =>  VARS::post("SH_SCHOOLCD2"),     //志望校2
                             "SH_SCHOOLCD3"     =>  VARS::post("SH_SCHOOLCD3"),     //志望校3
                             "SH_SCHOOLCD4"     =>  VARS::post("SH_SCHOOLCD4"),     //志望校4
                             "SH_GAKKAMEI1"     =>  VARS::post("SH_GAKKAMEI1"),     //志望校1学科名
                             "SH_GAKKAMEI2"     =>  VARS::post("SH_GAKKAMEI2"),     //志望校2学科名
                             "SH_GAKKAMEI3"     =>  VARS::post("SH_GAKKAMEI3"),     //志望校3学科名
                             "SH_GAKKAMEI4"     =>  VARS::post("SH_GAKKAMEI4"),     //志望校4学科名
                             "SAT_GROUPCD"          =>  VARS::post("SAT_GROUPCD"),          //団体コード
                             "JIZEN_BANGOU"         =>  VARS::post("JIZEN_BANGOU"),         //実践模試受験番号
                             "SIMAI_NAME1"          =>  VARS::post("SIMAI_NAME1"),          //兄弟情報(年組・氏名)
                             "SIMAI_NAME2"          =>  VARS::post("SIMAI_NAME2"),          //兄弟情報(年組・氏名)
                             "SIMAI_SCHOOL_KIND1"   =>  VARS::post("SIMAI_SCHOOL_KIND1"),   //兄弟情報(校種)
                             "SIMAI_SCHOOL_KIND2"   =>  VARS::post("SIMAI_SCHOOL_KIND2"),   //兄弟情報(校種)

                             "NAME"             =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"        =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"              =>  VARS::post("SEX"),                //性別
                             "BIRTHDAY"         =>  VARS::post("BIRTHDAY"),           //生年月日
                             "FS_CD"            =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_ERACD"         =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"         =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"             =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"             =>  VARS::post("FS_M"),               //卒業月
                             "FS_GRDDIV"        =>  VARS::post("FS_GRDDIV"),          //卒業区分
                             "FAMILY_REGISTER"  =>  VARS::post("FAMILY_REGISTER"),    //本籍
                             "ZIPCD"            =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ABSENCE2"         =>  VARS::post("ABSENCE2"),           //5年出欠
                             "ABSENCE3"         =>  VARS::post("ABSENCE3"),           //6年出欠
                             "RECOM_EXAMNO"     =>  VARS::post("RECOM_EXAMNO"),       //推薦受験番号
                             "ADDRESS1"         =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"         =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"            =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "GNAME"            =>  VARS::post("GNAME"),              //氏名(保護者)
                             "RELATIONSHIP"     =>  VARS::post("RELATIONSHIP"),       //続柄
                             "GKANA"            =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"           =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"        =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"        =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"           =>  VARS::post("GTELNO"),             //電話番号(保護者)
                             "GJOB"             =>  VARS::post("GJOB"),               //職業(保護者)
                             "REMARK1"          =>  VARS::post("REMARK1")             //備考(保護者)
                            );
    }

    //入力項目チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        //受験番号範囲設定にある項目
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
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
        if (SCHOOLKIND == "H" && !strlen($this->field["BIRTHDAY"])) {
            $this->setWarning("MSG301", "( 生年月日 )");
            return false;
        }
        if (SCHOOLKIND == "H" && !strlen($this->field["ZIPCD"])) {
            $this->setWarning("MSG301", "( 郵便番号 )");
            return false;
        }
        if (SCHOOLKIND == "H" && !strlen($this->field["ADDRESS1"])) {
            $this->setWarning("MSG301", "( 住所 )");
            return false;
        }
        if (SCHOOLKIND == "H" && !strlen($this->field["TELNO"])) {
            $this->setWarning("MSG301", "( 電話番号 )");
            return false;
        }
        if (SCHOOLKIND == "H" && !strlen($this->field["FS_CD"])) {
            $this->setWarning("MSG301", "( 出身学校 )");
            return false;
        }
        if (SCHOOLKIND == "H" && (!strlen($this->field["FS_Y"]) || !strlen($this->field["FS_M"]) || !strlen($this->field["FS_GRDDIV"]))) {
            $this->setWarning("MSG301", "( 卒業年月 )");
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
        //文字数チェック
        if (strlen($this->examno) > $this->examNoLength) {
            $this->setWarning("MSG915","受験番号は{$this->examNoLength}バイトまでです。");
            return false;
        }
        if (strlen($this->field["RECOM_EXAMNO"]) > 5) {
            $this->setWarning("MSG915","推薦受験番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( ふりがな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 90) {
            $this->setWarning("MSG915","志願者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 90) {
            $this->setWarning("MSG915","志願者( 方書 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身学校コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( ふりがな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS1"]) > 90) {
            $this->setWarning("MSG915","保護者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 90) {
            $this->setWarning("MSG915","保護者( 方書 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915","保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GJOB"]) > 150) {
            $this->setWarning("MSG915","保護者( 職業 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 150) {
            $this->setWarning("MSG915","保護者( 備考 )は150バイトまでです。");
            return false;
        }
        //半角カナ
        if (mbereg("[｡-ﾟ]", $this->field["ADDRESS1"])) {
            $this->setWarning("MSG901","志願者( 住所 )半角カナは入力できません");
            return false;
        }
        if (mbereg("[｡-ﾟ]", $this->field["ADDRESS2"])) {
            $this->setWarning("MSG901","志願者( 方書 )半角カナは入力できません");
            return false;
        }
        if (mbereg("[｡-ﾟ]", $this->field["GADDRESS1"])) {
            $this->setWarning("MSG901","保護者( 住所 )半角カナは入力できません");
            return false;
        }
        if (mbereg("[｡-ﾟ]", $this->field["GADDRESS2"])) {
            $this->setWarning("MSG901","保護者( 方書 )半角カナは入力できません");
            return false;
        }
        //重複チェック
        $cnt = $db->getOne(knjl011qQuery::getExistdataCnt($this));
        if ($cnt > 0 && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        //存在チェック
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }
        //受験番号範囲に登録されているか
        $cnt = $db->getOne(knjl011qQuery::getExistNoCnt($this));
        if ($cnt == 0 && $this->cmd == "add") {
            $this->setWarning("MSG901", "入力した( 受験番号 )が受験番号範囲に含まれていません。受験番号範囲設定を確認して下さい。");
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl011qQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011qQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
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
        $cnt = $db->getOne(knjl011qQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl011qQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>