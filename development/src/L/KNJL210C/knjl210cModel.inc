<?php

require_once('for_php7.php');

class knjl210cModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $receptno;          //受付番号
    var $applicantdiv;      //入試制度
    var $checkrecept;
    var $field = array();           //フィールドデータ

    function knjl210cModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->receptno);
            unset($this->preTestdiv);
            unset($this->year);
            unset($this->checkrecept);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->receptno     = VARS::post("PRE_RECEPTNO");
        $this->preTestdiv   = VARS::post("PRE_TESTDIV");

        if (strlen($this->receptno) > 0 && strlen($this->receptno) < 3) {
            $this->receptno = sprintf("%03d",$this->receptno);
            $this->receptno = $this->L104nmsp1[$this->preTestdiv].$this->receptno;
        }
        
        //フィールド
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "PRE_EXAM_TYPE"    =>  VARS::post("PRE_EXAM_TYPE"),    //受験型
                             "PRE_RECEPTDIV"    =>  VARS::post("PRE_RECEPTDIV"),    //申し込み方法
                             "PRE_RECEPTDATE"   =>  VARS::post("PRE_RECEPTDATE"),   //受付日付
                             "NAME"             =>  VARS::post("NAME"),             //氏名(受付者)
                             "NAME_KANA"        =>  VARS::post("NAME_KANA"),        //氏名かな(受付者)
                             "SEX"              =>  VARS::post("SEX"),              //性別
                             "GNAME"            =>  VARS::post("GNAME"),            //氏名(保護者)
                             "GKANA"            =>  VARS::post("GKANA"),            //氏名かな(保護者)
                             "ZIPCD"            =>  VARS::post("ZIPCD"),            //郵便番号
                             "ADDRESS1"         =>  VARS::post("ADDRESS1"),         //住所
                             "ADDRESS2"         =>  VARS::post("ADDRESS2"),         //方書
                             "TELNO"            =>  VARS::post("TELNO"),            //電話番号
                             "FS_CD"            =>  VARS::post("FINSCHOOLCD"),      //出身学校コード
                             "FS_NAME"          =>  VARS::post("FS_NAME"),          //出身学校名
                             "PS_CD"            =>  VARS::post("PS_CD"),            //塾コード
                             "PS_CONTACT"       =>  VARS::post("PS_CONTACT"),       //塾への連絡希望フラグ
                             "BUS_USE"          =>  VARS::post("BUS_USE"),          //学園バス利用フラグ
                             "STATIONDIV"       =>  VARS::post("STATIONDIV"),       //乗降地
                             "BUS_USER_COUNT"   =>  VARS::post("BUS_USER_COUNT"),   //ご利用人数
                             "REMARK"           =>  VARS::post("REMARK"),           //備考
                             "RECOM_EXAMNO"     =>  VARS::post("RECOM_EXAMNO")      //重複受験番号
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (!($this->receptno)) {
            $this->setWarning("MSG301", "( 受付番号 )");
            return false;
        }
        if (!($this->preTestdiv)) {
            $this->setWarning("MSG301", "( プレテスト区分 )");
            return false;
        }
        if (!($this->field["PRE_RECEPTDATE"])) {
            $this->setWarning("MSG301", "( 受付日付 )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 受付者 氏名 )");
            return false;
        }
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 受付者 氏名かな )");
            return false;
        }
        if (!($this->field["GNAME"])) {
            $this->setWarning("MSG301", "( 保護者 氏名 )");
            return false;
        }
        if (!($this->field["GKANA"])) {
            $this->setWarning("MSG301", "( 保護者 氏名かな )");
            return false;
        }
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }
        //学園バスを利用する時、利用人数は必須
        if (strlen($this->field["BUS_USE"]) && !strlen($this->field["BUS_USER_COUNT"])) {
            $this->setWarning("MSG301", "( 利用人数 )\\n学園バスを利用する時、利用人数は必須です。");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->receptno != $this->checkrecept) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //存在チェック
        $row = knjl210cQuery::Existdata($this);
        if (isset($row["PRE_RECEPTNO"]) && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        if (!isset($row["PRE_RECEPTNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }
        $row = knjl210cQuery::ExistdataNoAndName($this);
        if (strlen($this->field["RECOM_EXAMNO"]) > 0 && !isset($row["PRE_RECEPTNO"])) {
            $this->setWarning("MSG901", "受付番号：{$this->field["RECOM_EXAMNO"]}\\n　　氏名：{$this->field["NAME"]}\\nの生徒が存在しません。");
            return false;
        }
        //文字数チェック
        if (strlen($this->receptno) > 5) {
            $this->setWarning("MSG915","受付番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","受付者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","受付者( 氏名かな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( 氏名かな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 75) {
            $this->setWarning("MSG915","住所は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 75) {
            $this->setWarning("MSG915","方書は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","電話番号は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身学校コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["PS_CD"]) > 7) {
            $this->setWarning("MSG915","塾コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 45) {
            $this->setWarning("MSG915","備考は45バイトまでです。");
            return false;
        }
        //頭２桁チェック
        if (substr($this->receptno, 0, 2) != $this->L104nmsp1[$this->preTestdiv]) {
            $this->setWarning("MSG901", $this->L104nmsp1[$this->preTestdiv]."で始まる受付番号を入力して下さい。");
            return false;
        }
        $checkPreTestDiv = $this->preTestdiv == "1" ? "2" : "1";
        if (strlen($this->field["RECOM_EXAMNO"]) > 0 && substr($this->field["RECOM_EXAMNO"], 0, 2) != $this->L104nmsp1[$checkPreTestDiv]) {
            $this->setWarning("MSG901", $this->L104nmsp1[$checkPreTestDiv]."で始まる重複受験番号を入力して下さい。");
            return false;
        }
        //受験型チェック
        if ($this->L105nmsp1[$this->field["PRE_EXAM_TYPE"]] != $this->preTestdiv) {
            $setMessage = "";
            foreach ($this->L105nmsp1 as $nmcd1 => $nmsp1) {
                if ($this->preTestdiv == $nmsp1) {
                    $setMessage .= $sep.$this->L105nm1[$nmcd1];
                    $sep = "または";
                }
            }
            $this->setWarning("MSG901", "受験型：{$setMessage}を選択して下さい");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl210cQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl210cQuery::getInsertUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        if ($this->receptno == "") {
            $this->setWarning("MSG304", "( 受付番号 )");
            return false;
        }
        $row = knjl210cQuery::Existdata($this);
        if (!isset($row["PRE_RECEPTNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->receptno != $this->checkrecept) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl210cQuery::getDeleteQuery($this);
        $this->receptno = "";
        $this->setMessage("MSG201");
    }
 
    //追加処理
    function &getMaxExamno()
    {
        //最後の受付番号＋１
        $this->receptno = knjl210cQuery::getMaxExamnoQuery($this);
        if (strlen($this->receptno) < 3) {
            $this->receptno = sprintf("%03d",$this->receptno);
            $this->receptno = $this->L104nmsp1[$this->preTestdiv].$this->receptno;
        }
        return;
    }

}
?>